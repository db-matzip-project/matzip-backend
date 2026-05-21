package com.example.dbmatzip.domain.preference.controller;

import com.example.dbmatzip.domain.preference.dto.PreferenceOptionResponse;
import com.example.dbmatzip.domain.preference.dto.ReplaceUserPreferencesRequest;
import com.example.dbmatzip.domain.preference.dto.UserPreferenceItemResponse;
import com.example.dbmatzip.domain.preference.service.UserPreferenceApplicationService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final UserPreferenceApplicationService userPreferenceApplicationService;

    /** 온보딩용 전체 취향 태그 목록 */
    @GetMapping
    public List<PreferenceOptionResponse> listAll() {
        return userPreferenceApplicationService.listAllPreferences();
    }

    @GetMapping("/me")
    public List<UserPreferenceItemResponse> mine(@AuthenticationPrincipal MemberPrincipal principal) {
        return userPreferenceApplicationService.getMine(principal.getId());
    }

    @PutMapping("/me")
    public List<UserPreferenceItemResponse> replaceMine(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody ReplaceUserPreferencesRequest request) {
        return userPreferenceApplicationService.replaceMine(principal.getId(), request);
    }
}
