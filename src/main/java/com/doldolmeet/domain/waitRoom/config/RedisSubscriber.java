package com.doldolmeet.domain.waitRoom.config;


import com.doldolmeet.domain.waitRoom.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            ChatMessageDto roomMessage = mapper.readValue(publishMessage, ChatMessageDto.class);
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //        try {
//            // 기존 로직에서 메시지를 직접 읽어오지 않고 Redis에서 읽어오도록 수정
//            String roomId = new String(message.getChannel());
//            List<ChatMessage> roomMessages = redisTemplate.opsForList().range(roomId, 0, -1);
//
//            // 메시지 변환 및 전송
//            for (ChatMessage roomMessage : roomMessages) {
//                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
}
