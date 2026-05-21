package com.example.dbmatzip.domain.preference.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceUserPreferencesRequest(@NotNull List<Long> preferenceIds) {}
