package com.example.dbmatzip.domain.preference.service;

import com.example.dbmatzip.domain.member.repository.UserRepository;
import com.example.dbmatzip.domain.preference.dto.PreferenceOptionResponse;
import com.example.dbmatzip.domain.preference.dto.ReplaceUserPreferencesRequest;
import com.example.dbmatzip.domain.preference.dto.UserPreferenceItemResponse;
import com.example.dbmatzip.domain.preference.entity.Preference;
import com.example.dbmatzip.domain.preference.entity.UserPreference;
import com.example.dbmatzip.domain.preference.repository.PreferenceRepository;
import com.example.dbmatzip.domain.preference.repository.UserPreferenceRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferenceApplicationService {

    private final PreferenceRepository preferenceRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    public List<PreferenceOptionResponse> listAllPreferences() {
        return preferenceRepository.findAll().stream()
                .sorted(Comparator.comparing(Preference::getCode))
                .map(p -> new PreferenceOptionResponse(p.getId(), p.getCode(), p.getDisplayName()))
                .toList();
    }

    public List<UserPreferenceItemResponse> getMine(Long userId) {
        return userPreferenceRepository.findByUser_Id(userId).stream()
                .map(up -> new UserPreferenceItemResponse(
                        up.getPreference().getId(),
                        up.getPreference().getCode(),
                        up.getPreference().getDisplayName()))
                .toList();
    }

    @Transactional
    public List<UserPreferenceItemResponse> replaceMine(Long userId, ReplaceUserPreferencesRequest request) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>(request.preferenceIds());
        if (ids.size() != request.preferenceIds().size()) {
            throw new IllegalArgumentException("preferenceIds 에 중복이 있습니다.");
        }
        List<Preference> loaded = preferenceRepository.findAllById(ids);
        if (loaded.size() != ids.size()) {
            throw new IllegalArgumentException("존재하지 않는 취향 ID 가 포함되어 있습니다.");
        }
        Map<Long, Preference> byId =
                loaded.stream().collect(Collectors.toMap(Preference::getId, p -> p));

        userPreferenceRepository.deleteByUser_Id(userId);

        var user = userRepository.getReferenceById(userId);
        for (Long prefId : ids) {
            Preference pref = byId.get(prefId);
            UserPreference row =
                    UserPreference.builder().user(user).preference(pref).build();
            userPreferenceRepository.save(row);
        }
        return getMine(userId);
    }
}
