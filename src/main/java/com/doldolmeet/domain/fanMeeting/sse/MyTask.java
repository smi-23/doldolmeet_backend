package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRoomOrderRepository;
import com.doldolmeet.domain.openvidu.service.OpenviduService;
import com.doldolmeet.domain.users.idol.entity.Idol;
import com.doldolmeet.domain.users.idol.repository.IdolRepository;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.recording.controller.MyRecordingController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.openvidu.java.client.OpenVidu;


import static com.doldolmeet.exception.ErrorCode.IDOL_NOT_FOUND;
import static com.doldolmeet.exception.ErrorCode.NOT_FOUND_FANMEETING_ROOM_ORDER;

@Slf4j
public class MyTask implements Runnable {
    private String body;
    private OpenviduService openviduService;

    private ObjectMapper objectMapper;
    private FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository;
    private OpenVidu openvidu;
    private IdolRepository idolRepository;

    public MyTask(String body, OpenviduService openviduService, ObjectMapper objectMapper, FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository, OpenVidu openvidu, IdolRepository idolRepository){
        this.body = body;
        this.openviduService = openviduService;
        this.objectMapper = objectMapper;
        this.fanMeetingRoomOrderRepository = fanMeetingRoomOrderRepository;
        this.openvidu = openvidu;
        this.idolRepository = idolRepository;
    }

    @Override
    public void run() {
        long timeLimit = 60000;
        long gameStart = 40000;
        long gameEnd = 20000;
        long endNotice = 10000;

        try {
            // 게임 시작 전까지 자기
            Thread.sleep(timeLimit - gameStart); // 20초 대화

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            log.info("jsonNode : " + jsonNode);

            String sessionId = jsonNode.get("sessionId").asText();
            String connectionId = jsonNode.get("connectionId").asText();

            String gameType = parseGameType(body);
            String username = parseUsername(body);
            Long fanMeetingId = parseFanMeetingId(body);
            String idolName = parseIdolName(body);

            Idol idol = idolRepository.findByUserCommonsNickname(idolName).orElseThrow(() -> new CustomException(IDOL_NOT_FOUND));
            SseEmitter emitter = SseService.emitters.get(fanMeetingId).get(username);
            SseEmitter idolEmitter = SseService.emitters.get(fanMeetingId).get(idol.getUserCommons().getUsername());

            // 게임 시작
            log.info("게임 시작!!");
            emitter.send(SseEmitter.event().name("gameStart").data(new HashMap<>()));

            log.info("아이돌 에미터: " + idolEmitter.toString());
            idolEmitter.send(SseEmitter.event().name("idolGameStart").data(new HashMap<>()));

            // 게임 종료시까지 자기
            Thread.sleep(gameStart - gameEnd); // 20초 게임 진행

            // 게임 종료
            log.info("게임 종료!!");
            emitter.send(SseEmitter.event().name("gameEnd").data(new HashMap<>()));

            // 팬미팅 종료시까지 자기
            Thread.sleep(gameEnd - endNotice); // 10초 대화

            // 종료 알림을 보내기
            emitter.send(SseEmitter.event().name("endNotice").data(new HashMap<>()));
            Thread.sleep(endNotice); // 종료알림 보내고 10초 후 끝

            log.info("-------종료되는 connectionId : " + connectionId);
            Session session = openviduService.getSession(sessionId);

            // 연결 끊기기전 녹화 종료
//            String recordingId = MyRecordingController.recordingInfo.get(List.of(fanMeetingId, username, sessionId));
//            String recordingId = MyRecordingController.sessionRecordings.get();
            String recordingId = MyRecordingController.sessionIdRecordingsMap.get(sessionId).getId();
            this.openvidu.stopRecording(recordingId);
            MyRecordingController.sessionRecordings.remove(sessionId);
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

    private String parseIdolName(String eventMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            String idolName = jsonNode.get("idolName").asText();
            log.info("--------- idolName: " + idolName);

            return idolName;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String parseGameType(String eventMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            jsonNode = objectMapper.readTree(jsonNode.get("clientData").asText());
            String gameType = jsonNode.get("gameType").asText();
            log.info("--------- gameType: " + gameType);

            return gameType;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
