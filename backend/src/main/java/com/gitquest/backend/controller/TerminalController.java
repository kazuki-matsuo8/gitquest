package com.gitquest.backend.controller;

import com.gitquest.backend.dto.terminal.SessionCreateResponse;
import com.gitquest.backend.dto.terminal.TerminalCommandRequest;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse;
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

    // POST /api/terminal/sessions → 新しいターミナルセッションを作成
    @PostMapping("/sessions")
    public ResponseEntity<SessionCreateResponse> createSession() {
        String sessionId = terminalService.createSession();
        return ResponseEntity.ok(new SessionCreateResponse(sessionId));
    }

    // DELETE /api/terminal/sessions/{sessionId} → セッションを破棄（作業ディレクトリ削除）
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        terminalService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/terminal/sessions/{sessionId}/exec → コマンド実行
    @PostMapping("/sessions/{sessionId}/exec")
    public ResponseEntity<TerminalCommandResponse> execute(
            @PathVariable String sessionId,
            @RequestBody TerminalCommandRequest request
    ) {
        return ResponseEntity.ok(terminalService.execute(sessionId, request.command()));
    }
}
