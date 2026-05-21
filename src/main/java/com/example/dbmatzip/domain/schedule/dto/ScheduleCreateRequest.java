package com.example.dbmatzip.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/** 로그인 사용자 기준으로 일정 생성 — JWT 에서 userId 를 가져옵니다. */
public record ScheduleCreateRequest(@NotBlank String title, LocalDate travelDate) {}
