package com.example.dbmatzip.domain.preference.entity;

import com.example.dbmatzip.domain.member.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_preferences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {

    @EmbeddedId
    private UserPreferenceId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("preferenceId")
    @JoinColumn(name = "preference_id")
    private Preference preference;

    @Column(nullable = false)
    private Integer weight;

    @Builder
    private UserPreference(User user, Preference preference, Integer weight) {
        this.id = new UserPreferenceId(user.getId(), preference.getId());
        this.user = user;
        this.preference = preference;
        this.weight = weight;
    }
}
