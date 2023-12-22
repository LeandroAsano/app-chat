package app.chat.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class User implements Cloneable{

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

    @Override
    public User clone() throws CloneNotSupportedException {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should not happen since User implements Cloneable
            throw new InternalError(e);
        }
    }

    public boolean equalsValue(User user) {
        return (Objects.equals(this.id, user.id)
                && Objects.equals(this.username, user.username)
                && Objects.equals(this.status, user.status)
                && Objects.equals(this.lastTimeOnline, user.lastTimeOnline));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;  // References are the same
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;  // Different classes or null
        }

        User userObj = (User) obj;

        return (this.id.equals(userObj.id)
                && this.username.equals(userObj.username)
                && this.status.equals(userObj.status)
                && this.lastTimeOnline.equals(userObj.lastTimeOnline));
    }
}
