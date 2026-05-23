package com.example.dbmatzip.domain.preference.repository;

import com.example.dbmatzip.domain.preference.entity.UserPreference;
import com.example.dbmatzip.domain.preference.entity.UserPreferenceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreferenceId> {

    List<UserPreference> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    long countByUser_Id(Long userId);
}
