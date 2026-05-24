package com.example.dbmatzip.domain.member.dto;

public record UserProfileResponse(
        Long id, String loginId, String name, String phone, String nickname, Integer age) {

    public static UserProfileResponse from(com.example.dbmatzip.domain.member.entity.User user) {
        String safeNickname = user.getNickname() == null || user.getNickname().isBlank() ? user.getName() : user.getNickname();
        Integer safeAge = user.getAge() == null ? 0 : user.getAge();
        return new UserProfileResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getPhone(),
                safeNickname,
                safeAge);
    }
}
