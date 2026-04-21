package com.gitquest.backend.controller;

import com.gitquest.backend.dto.progress.ProgressResponse;
import com.gitquest.backend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    // GET /api/progress → 自分の進捗一覧
    @GetMapping
    public ResponseEntity<List<ProgressResponse>> getMyProgress() {
        return ResponseEntity.ok(progressService.getMyProgress());
    }

    // POST /api/progress/{missionId}/start → ミッション開始
    @PostMapping("/{missionId}/start")
    public ResponseEntity<ProgressResponse> start(@PathVariable UUID missionId) {
        return ResponseEntity.ok(progressService.start(missionId));
    }

    // POST /api/progress/{missionId}/complete → ミッション完了
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<ProgressResponse> complete(@PathVariable UUID missionId) {
        return ResponseEntity.ok(progressService.complete(missionId));
    }
}
