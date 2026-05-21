package com.example.dbmatzip.domain.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 50) String loginId,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[0-9\\-+\\s]{9,30}$", message = "전화번호 형식을 확인하세요") String phone,
        @Size(max = 50) String nickname,
        @Min(1) @Max(120) Integer age) {}
