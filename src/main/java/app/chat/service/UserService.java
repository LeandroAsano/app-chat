package app.chat.service;

import app.chat.model.Status;
import app.chat.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
@Slf4j
public class UserService {

    // Using a concurrent map for thread safety
    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    @Autowired
    private SimpMessageSendingOperations messageTemplate;

    public void addUser(User user) {
        userMap.put(user.getId(), user);
    }

    public void changeStatus(User user) {
        userMap.get(user.getId()).setStatus(user.getStatus());
    }

    public void setOfflineUser(String userId) {
        userMap.get(userId).setStatus(Status.OFFLINE);
        userMap.get(userId).setLastTimeOnline(LocalDateTime.now());
    }

    public void removeUser(String userId) {
        userMap.remove(userId);
    }

    public Map<String, User> getOnlineUsers() {
        return userMap;
    }

    public void broadcastUserList(){
        log.info("BROADCASTING LIST");
        messageTemplate.convertAndSend("/topic/userList", getOnlineUsers());
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }
}

