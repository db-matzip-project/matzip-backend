package com.example.dbmatzip.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SignupRequest(
        @Schema(description = "로그인 ID", example = "matzip_user01")
        @NotBlank @Size(max = 50) String loginId,
        @Schema(description = "로그인 비밀번호", example = "Password123!")
        @NotBlank @Size(min = 8, max = 80) String password,
        @Schema(description = "이름", example = "홍길동")
        @NotBlank @Size(max = 100) String name,
        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[0-9\\-+\\s]{9,30}$", message = "전화번호 형식을 확인하세요") String phone,
        @Schema(description = "닉네임", example = "길동이")
        @Size(max = 50) String nickname,
        @Schema(description = "나이", example = "24")
        @Min(1) @Max(120) Integer age,
        @Schema(description = "초기 취향 ID 목록(선택)", example = "[1,3,5]")
        @Size(max = 50) List<Long> preferenceIds) {}
