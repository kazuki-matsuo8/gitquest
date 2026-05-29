package com.gitquest.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitquest.backend.service.TerminalService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class TerminalWebSocketHandler extends AbstractWebSocketHandler {

    private final TerminalService terminalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TerminalWebSocketHandler(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);
        terminalService.attachWebSocket(sessionId, session);
    }

    // バイナリメッセージ = ターミナルへのキー入力
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        byte[] data = new byte[message.getPayload().remaining()];
        message.getPayload().get(data);
        terminalService.writeToSession(sessionId, data);
    }

    // テキストメッセージ = リサイズなどの制御コマンド (JSON)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String type = node.path("type").asText();
            if ("resize".equals(type)) {
                int cols = node.path("cols").asInt(80);
                int rows = node.path("rows").asInt(24);
                terminalService.resizeSession(sessionId, cols, rows);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);
        terminalService.deleteSession(sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractSessionId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
