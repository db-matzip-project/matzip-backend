package com.example.dbmatzip.domain.schedule.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ScheduleSummaryResponse(
        Long id, String title, LocalDate travelDate, Instant createdAt, Long itemCount) {}
