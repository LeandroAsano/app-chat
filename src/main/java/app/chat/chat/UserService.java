package app.chat.chat;

import app.chat.model.User;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    // Using a concurrent map for thread safety
    @Getter
    private final Map<String, User> userMap = new ConcurrentHashMap<>();

    public void addUser(User user) {
        userMap.put(user.getId(), user);
    }

    public void removeUser(String userId) {
        userMap.remove(userId);
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }
}

