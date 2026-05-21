package com.example.dbmatzip.domain.preference.repository;

import com.example.dbmatzip.domain.preference.entity.Preference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    Optional<Preference> findByCode(String code);
}
