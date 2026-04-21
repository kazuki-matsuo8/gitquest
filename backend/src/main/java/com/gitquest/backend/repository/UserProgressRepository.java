package com.gitquest.backend.repository;

import com.gitquest.backend.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    List<UserProgress> findByUserId(UUID userId);
    Optional<UserProgress> findByUserIdAndMissionId(UUID userId, UUID missionId);
}
