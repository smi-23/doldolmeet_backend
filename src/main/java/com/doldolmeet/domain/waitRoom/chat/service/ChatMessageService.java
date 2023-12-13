package com.doldolmeet.domain.waitRoom.chat.service;

import com.doldolmeet.domain.waitRoom.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final RedisTemplate redisTemplate;

    public List<ChatMessageDto> getChatMessages(String roomId) {
        return redisTemplate.opsForList().range(roomId, 0, -1);
    }

    public void saveChatMessage(String roomId, ChatMessageDto chatMessage) {
        redisTemplate.opsForList().rightPush(roomId, chatMessage);
    }
}
