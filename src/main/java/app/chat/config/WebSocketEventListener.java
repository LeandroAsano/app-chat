package app.chat.config;

import app.chat.chat.ChatMessage;
import app.chat.chat.MessageType;
import app.chat.chat.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;
    private final UserService userService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent disconnectEvent) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(disconnectEvent.getMessage());
        String userid = Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId").toString();
        String username = Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username").toString();

        var chatMessage = ChatMessage.builder()
                .type(MessageType.LEAVE)
                .userId(userid)
                .sender(username)
                .build();

        messageTemplate.convertAndSend("/topic/public", chatMessage);
    }
}

