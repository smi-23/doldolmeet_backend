package com.doldolmeet.domain.waitRoom.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDto implements Serializable {

    public enum MessageType {
        ENTER, TALK
    }

    private MessageType type;
    private String roomId;
    @NotNull
    @NotBlank
    private String sender;
    private String message;
    private String profileImg;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.s")
    private LocalDateTime timestamp; // 새로운 필드 추가

    // 인스턴스를 생성할 때 timestamp를 현재 시간으로 설정하는 생성자를 추가합니다.
    public ChatMessageDto() {
        this.timestamp = LocalDateTime.now();
    }
}
