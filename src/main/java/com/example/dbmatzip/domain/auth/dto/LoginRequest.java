package com.example.dbmatzip.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 50) String loginId,
        @NotBlank @Size(min = 6, max = 80) String password) {}
