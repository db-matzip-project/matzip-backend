package com.example.dbmatzip.domain.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 30) String phone,
        @Size(max = 50) String nickname,
        @Min(1) @Max(120) Integer age) {}
