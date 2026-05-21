package com.example.dbmatzip.domain.member.dto;

public record AuthTokenResponse(String tokenType, String accessToken, long expiresInMs, UserProfileResponse user) {

    public static AuthTokenResponse of(String accessToken, long expiresInMs, UserProfileResponse user) {
        return new AuthTokenResponse("Bearer", accessToken, expiresInMs, user);
    }
}
