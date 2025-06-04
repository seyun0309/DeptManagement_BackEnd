package sunjin.DeptManagement_BackEnd.global.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketTextHandler extends TextWebSocketHandler {

    Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();
    private final JwtProvider jwtProvider;

    public SocketTextHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromQuery(session);
        Long userId = jwtProvider.extractUserIdFromTokenIfValid(token);

        sessions.put(userId, session);
        sessionIdToUserId.put(session.getId(), userId);

        log.info("WebSocket 연결됨: userId={}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = sessionIdToUserId.remove(session.getId());
        if (userId != null) {
            sessions.remove(userId);
        }

        log.info("WebSocket 연결 해제됨: sessionId={}", session.getId());
    }

    public void sendToUser(Long userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    private String getTokenFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("token=")) {
            String token = query.substring(6);
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return token;
        }
        return null;
    }
}
