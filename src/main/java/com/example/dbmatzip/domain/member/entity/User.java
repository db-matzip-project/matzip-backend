package com.example.dbmatzip.domain.member.entity;

import com.example.dbmatzip.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원. 로그인 아이디·비밀번호(해시)·실명·전화번호 필수.
 * nickname·age 는 선택(표시명·추천 OLAP 연령대 매칭용).
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 50)
    private String nickname;

    @Column
    private Integer age;

    public User(
            String loginId,
            String passwordHash,
            String name,
            String phone,
            String nickname,
            Integer age) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.nickname = nickname != null && !nickname.isBlank() ? nickname : name;
        this.age = age;
    }

    public void updateProfile(String name, String phone, String nickname, Integer age) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (nickname != null) {
            this.nickname = nickname.isBlank() ? this.name : nickname;
        }
        if (age != null) {
            this.age = age;
        }
    }

    public void changePasswordHash(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }
}
