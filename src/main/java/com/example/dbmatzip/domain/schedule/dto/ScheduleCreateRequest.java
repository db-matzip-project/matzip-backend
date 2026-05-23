package com.example.dbmatzip.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.Size;

/** 로그인 사용자 기준으로 일정 생성 — JWT 에서 userId 를 가져옵니다. */
public record ScheduleCreateRequest(
        @Schema(description = "일정 제목", example = "주말 코스")
        @NotBlank String title,
        @Schema(description = "여행 날짜", example = "2026-05-24")
        LocalDate travelDate,
        @Schema(
                        description =
                                "초기 일정에 담을 식당 ID 목록(선택). 함께 보내면 schedule_restaurants가 visitOrder 1부터 생성됩니다.",
                        example = "[1,3,5]")
        @Size(max = 50) List<Long> restaurantIds) {}
