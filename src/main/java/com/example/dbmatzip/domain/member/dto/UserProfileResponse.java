package com.example.dbmatzip.domain.member.dto;

public record UserProfileResponse(
        Long id, String loginId, String name, String phone, String nickname, Integer age) {

    public static UserProfileResponse from(com.example.dbmatzip.domain.member.entity.User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getPhone(),
                user.getNickname(),
                user.getAge());
    }
}
