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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.openvidu.java.client.OpenVidu;


import static com.doldolmeet.exception.ErrorCode.NOT_FOUND_FANMEETING_ROOM_ORDER;

@Slf4j
public class MyTask implements Runnable {
    private String body;
    private OpenviduService openviduService;

    private ObjectMapper objectMapper;
    private FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository;
    private OpenVidu openvidu;
    public MyTask(String body, OpenviduService openviduService, ObjectMapper objectMapper, FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository, OpenVidu openvidu){
        this.body = body;
        this.openviduService = openviduService;
        this.objectMapper = objectMapper;
        this.fanMeetingRoomOrderRepository = fanMeetingRoomOrderRepository;
        this.openvidu = openvidu;
    }

    @Override
    public void run() {
        long timeLimit = 30000;
        long endNotice = 10000;

        try {
            Thread.sleep(timeLimit - endNotice);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            log.info("jsonNode : " + jsonNode);

            String sessionId = jsonNode.get("sessionId").asText();
            String connectionId = jsonNode.get("connectionId").asText();
            String username = parseUsername(body);
            Long fanMeetingId = parseFanMeetingId(body);
            SseEmitter emitter = SseService.emitters.get(fanMeetingId).get(username);
            // 종료 알림을 보내기
            emitter.send(SseEmitter.event().name("endNotice").data(new HashMap<>()));
            Thread.sleep(endNotice);

            log.info("-------종료되는 connectionId : " + connectionId);
            Session session = openviduService.getSession(sessionId);

            // 연결 끊기기전 녹화 종료
//            String recordingId = MyRecordingController.recordingInfo.get(List.of(fanMeetingId, username, sessionId));
//            String recordingId = MyRecordingController.sessionRecordings.get();
            String recordingId = MyRecordingController.sessionIdRecordingsMap.get(sessionId).getId();
            log.info("@@@@@@@@@@@@@@@@@@@@@recordingId : " + recordingId);
            this.openvidu.stopRecording(recordingId);
            log.info("@@@@@@@@@@@@@@@@@@@@@recordingIdremove : " + recordingId);
            MyRecordingController.sessionRecordings.remove(sessionId);
            log.info("@@@@@@@@@@@@@@@@@@@@@forceDisconnect : " + recordingId);
            // 연결 끊기
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
        log.info("Task " + " is running on thread " + Thread.currentThread().getName());

    }

    private Long parseFanMeetingId(String eventMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            Long fanMeetingId = jsonNode.get("fanMeetingId").asLong();
            log.info("--------- Fan Meeting ID: " + fanMeetingId);

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
            log.info("--------- User Name: " + username);

            return username;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
