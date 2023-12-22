package app.chat.controller;

import app.chat.model.ChatMessage;
import app.chat.service.UserService;
import app.chat.model.Status;
import app.chat.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

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

    @MessageMapping("/chat.getUser")
    @SendTo("/topic/oldUser")
    public User getUser(@Payload String userId) {
        User user = userService.getUser(userId);

        if (user == null) {
            log.error("User with userId: " + userId + " does not exist");
            return null;
        }

        log.info("Existent User Found: " + user.getStatus());
        return user;
    }

    @MessageMapping("/chat.putUser")
    public void putUser(@Payload User user) {
        log.info("User Status Changed to " + user.getStatus());
        userService.changeStatus(user);
    }

    @MessageMapping("/chat.removeUser")
    public void removeUser(@Payload ChatMessage chatMessage) {
        log.info("User: " + chatMessage.getSender() + " id: " + chatMessage.getUserId() + " removed");
        userService.setOfflineUser(chatMessage.getUserId());
    }
}
