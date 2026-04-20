package com.gitquest.backend.service;

import com.gitquest.backend.dto.mission.MissionResponse;
import com.gitquest.backend.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    // 全ミッションをレベルごとにグルーピングして返す
    @Transactional(readOnly = true)
    public Map<Integer, List<MissionResponse>> getAllGroupedByLevel() {
        return missionRepository.findAllByOrderByLevelAscOrderIndexAsc()
                .stream()
                .map(MissionResponse::from)
                .collect(Collectors.groupingBy(MissionResponse::level));
    }

    // 指定 ID のミッション詳細（ステップ付き）を返す
    @Transactional(readOnly = true)
    public MissionResponse getById(UUID id) {
        return missionRepository.findById(id)
                .map(MissionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("ミッションが見つかりません"));
    }
}
