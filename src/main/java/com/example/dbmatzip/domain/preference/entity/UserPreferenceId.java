package com.example.dbmatzip.domain.preference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferenceId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "preference_id", nullable = false)
    private Long preferenceId;

    public UserPreferenceId(Long userId, Long preferenceId) {
        this.userId = userId;
        this.preferenceId = preferenceId;
    }
}
