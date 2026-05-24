package com.example.dbmatzip.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ScheduleUpdateRequest(
        @Schema(description = "일정 제목(부분 수정 가능)", example = "주말 코스")
        @Size(min = 1, max = 200) String title,
        @Schema(description = "여행 날짜(부분 수정 가능)", example = "2026-05-24")
        LocalDate travelDate) {}
