package app.chat.chat;

import app.chat.model.Status;
import app.chat.model.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    // Using a concurrent map for thread safety
    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    @Autowired
    private SimpMessageSendingOperations messageTemplate;

    public void addUser(User user) {
        userMap.put(user.getId(), user);
        broadcastUserList();
    }

    public void changeStatus(User user) {
        userMap.get(user.getId()).setStatus(user.getStatus());
        broadcastUserList();
    }

    public void removeUser(String userId) {
        userMap.remove(userId);
        broadcastUserList();
    }

    public Map<String, User> getOnlineUsers() {
        return userMap;
    }

    public void broadcastUserList(){
        messageTemplate.convertAndSend("/topic/userList", getOnlineUsers());
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }
}

