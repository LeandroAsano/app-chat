package app.chat.chat;

import app.chat.model.Status;
import app.chat.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@Slf4j
public class ChatController {

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        User user = new User(chatMessage.getUserId(), chatMessage.getSender(), Status.ONLINE);

        log.info("User: " + chatMessage.getSender() + " id: " + chatMessage.getUserId() + " added");
        userService.addUser(user);
        return chatMessage;
    }

    @MessageMapping("/chat.putUser")
    public void putUser(@Payload User user) {
        log.info("User Status Changed to " + user.getStatus());
        userService.changeStatus(user);
    }

    @MessageMapping("/chat.removeUser")
    public void removeUser(@Payload ChatMessage chatMessage) {
        log.info("User: " + chatMessage.getSender() + " id: " + chatMessage.getUserId() + " removed");
        userService.removeUser(chatMessage.getUserId());
    }
}
