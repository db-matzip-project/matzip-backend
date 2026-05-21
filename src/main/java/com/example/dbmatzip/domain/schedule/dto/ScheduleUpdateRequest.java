package com.example.dbmatzip.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ScheduleUpdateRequest(@NotBlank String title, LocalDate travelDate) {}
