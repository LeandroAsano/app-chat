package app.chat.chat;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String userId;
    private String content;
    private String sender;
    private MessageType type;
}
