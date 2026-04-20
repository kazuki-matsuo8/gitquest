package com.gitquest.backend.repository;

import com.gitquest.backend.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<Mission, UUID> {
    List<Mission> findByLevelOrderByOrderIndexAsc(int level);
    List<Mission> findAllByOrderByLevelAscOrderIndexAsc();
}
