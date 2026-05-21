package com.example.dbmatzip.domain.member.service;

import com.example.dbmatzip.domain.member.dto.UserProfileResponse;
import com.example.dbmatzip.domain.member.dto.UserProfileUpdateRequest;
import com.example.dbmatzip.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(Long userId) {
        return UserProfileResponse.from(
                userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateProfile(request.name(), request.phone(), request.nickname(), request.age());
        return UserProfileResponse.from(user);
    }
}
