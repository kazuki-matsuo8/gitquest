package com.gitquest.backend.controller;

import com.gitquest.backend.dto.mission.MissionResponse;
import com.gitquest.backend.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // GET /api/missions → レベルごとにグルーピングされたミッション一覧
    @GetMapping
    public ResponseEntity<Map<Integer, List<MissionResponse>>> getAll() {
        return ResponseEntity.ok(missionService.getAllGroupedByLevel());
    }

    // GET /api/missions/{id} → ミッション詳細（ステップ付き）
    @GetMapping("/{id}")
    public ResponseEntity<MissionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(missionService.getById(id));
    }
}
