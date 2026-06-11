package com.gitquest.backend.controller;

import com.gitquest.backend.dto.terminal.SessionCreateRequest;
import com.gitquest.backend.dto.terminal.SessionCreateResponse;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.GraphData;
import com.gitquest.backend.service.TerminalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionCreateResponse> createSession(
            @RequestBody(required = false) SessionCreateRequest request
    ) {
        String missionId = request != null ? request.missionId() : null;
        return ResponseEntity.ok(terminalService.createSession(missionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        terminalService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{sessionId}/graph")
    public ResponseEntity<GraphData> getGraph(@PathVariable String sessionId) {
        return ResponseEntity.ok(terminalService.getGraph(sessionId));
    }

    // GET /api/terminal/sessions/{sessionId}/check?missionId=... → ミッション完了判定
    @GetMapping("/sessions/{sessionId}/check")
    public ResponseEntity<MissionCheckResponse> checkMission(
            @PathVariable String sessionId,
            @RequestParam String missionId
    ) {
        return ResponseEntity.ok(new MissionCheckResponse(
                terminalService.checkMission(sessionId, missionId)));
    }

    public record MissionCheckResponse(boolean completed) {}
}
