package app.chat.model;

import lombok.Getter;
import lombok.Setter;

public class User {

    @Getter
    private final String id;

    @Getter
    private final String username;

    @Setter
    @Getter
    private Status status;

    public User(String id, String username, Status status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }
}
