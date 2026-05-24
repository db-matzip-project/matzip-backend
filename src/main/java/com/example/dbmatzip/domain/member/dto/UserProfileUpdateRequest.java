package com.example.dbmatzip.domain.member.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(@Size(max = 100) String name, @Size(max = 30) String phone) {}
