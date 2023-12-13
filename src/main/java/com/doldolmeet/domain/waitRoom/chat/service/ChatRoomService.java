package com.doldolmeet.domain.waitRoom.chat.service;

import com.doldolmeet.domain.waitRoom.chat.dto.ChatRoomDto;
import com.doldolmeet.domain.waitRoom.config.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private final RedisTemplate redisTemplate;

    private HashOperations<String, String, ChatRoomDto> opsHashChatRoom;
    private Map<String, ChannelTopic> topics = new HashMap<>();
    private static final String CHAT_ROOMS = "CHAT_ROOM";

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    public List<ChatRoomDto> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoomDto findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis에 hash구조로 저장한다.
    public ChatRoomDto createChatRoom(String name) {
        ChatRoomDto room = ChatRoomDto.create(name);
        // roomId값을 FanToChatRoom에 저장

        opsHashChatRoom.put(CHAT_ROOMS, room.getRoomId(), room);
        return room;
    }

    // 채팅방 입장 : Redis Topic을 만들고 pub/sub 통신을 하기 위해서 리스너를 설정한다.
    public boolean enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);

        // 방이 없으면 생성
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(redisSubscriber);
            redisMessageListener.addMessageListener(messageListenerAdapter, topic);
            topics.put(roomId, topic); // (방번호, 방번호에 해당하는 토픽)

            return true; // 처음 입장할 때만 true 반환
        }
        return false; // 이미 채팅방이 존재할 경우 false 반환
    }
}
