package com.example.dbmatzip.domain.member.controller;

import com.example.dbmatzip.domain.member.dto.UserProfileResponse;
import com.example.dbmatzip.domain.member.dto.UserProfileUpdateRequest;
import com.example.dbmatzip.domain.member.service.MemberProfileService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal MemberPrincipal principal) {
        return memberProfileService.getProfile(principal.getId());
    }

    @PatchMapping("/me")
    public UserProfileResponse patchMe(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return memberProfileService.updateProfile(principal.getId(), request);
    }
}
