package app.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class User {

    @Getter
    private final String id;

    @Getter
    private final String username;

    @Setter
    @Getter
    private Status status;

    @Setter
    @Getter
    private LocalDateTime lastTimeOnline;

    public User(String id, String username, Status status) {
        this.id = id;
        this.username = username;
        this.status = status;
        this.lastTimeOnline = null;
    }
}
