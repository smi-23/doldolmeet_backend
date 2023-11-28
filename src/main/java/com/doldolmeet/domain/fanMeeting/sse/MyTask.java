package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRoomOrderRepository;
import com.doldolmeet.domain.openvidu.service.OpenviduService;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.recording.MyRecordingController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.doldolmeet.exception.ErrorCode.NOT_FOUND_FANMEETING_ROOM_ORDER;

@Slf4j
public class MyTask implements Runnable {
    private String body;
    private OpenviduService openviduService;

    private ObjectMapper objectMapper;
    private FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository;
    public MyTask(String body, OpenviduService openviduService, ObjectMapper objectMapper, FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository) {
        this.body = body;
        this.openviduService = openviduService;
        this.objectMapper = objectMapper;
        this.fanMeetingRoomOrderRepository = fanMeetingRoomOrderRepository;
    }

    @Override
    public void run() {
        long timeLimit = 11000;
        long endNotice = 4000;

        try {
            Thread.sleep(timeLimit - endNotice);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            System.out.println("jsonNode : " + jsonNode);

            String sessionId = jsonNode.get("sessionId").asText();
            String connectionId = jsonNode.get("connectionId").asText();
            String username = parseUsername(body);
            Long fanMeetingId = parseFanMeetingId(body);
            SseEmitter emitter = SseService.emitters.get(fanMeetingId).get(username);
            // 종료 알림을 보내고
            emitter.send(SseEmitter.event().name("endNotice").data(new HashMap<>()));
            Thread.sleep(endNotice);

            log.info("-------종료되는 connectionId : " + connectionId);
            Session session = openviduService.getSession(sessionId);
//            MyRecordingController.stopRecording();
            session.forceDisconnect(connectionId);
            Optional<FanMeetingRoomOrder> currFanMeetingRoomOrderOpt = fanMeetingRoomOrderRepository.findByFanMeetingIdAndCurrentRoom(fanMeetingId, sessionId);
            // 없으면 예외
            if (currFanMeetingRoomOrderOpt.isEmpty()) {
                throw new CustomException(NOT_FOUND_FANMEETING_ROOM_ORDER);
            }
            FanMeetingRoomOrder currRoomOrder = currFanMeetingRoomOrderOpt.get();

            Map<String, String> params = new HashMap<>();
            params.put("nextRoomId", currRoomOrder.getNextRoom());
            params.put("currRoomType", currRoomOrder.getType());
            emitter.send(SseEmitter.event().name("moveToWaitRoom").data(params));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (OpenViduJavaClientException e) {
            throw new RuntimeException(e);
        } catch (OpenViduHttpException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Task " + " is running on thread " + Thread.currentThread().getName());

    }

    private Long parseFanMeetingId(String eventMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            Long fanMeetingId = jsonNode.get("fanMeetingId").asLong();
            System.out.println("--------- Fan Meeting ID: " + fanMeetingId);

            return fanMeetingId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseUsername(String eventMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            String username = jsonNode.get("userName").asText();
            System.out.println("--------- User Name: " + username);

            return username;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
