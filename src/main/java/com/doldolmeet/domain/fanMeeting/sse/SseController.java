package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRoomOrderRepository;
import com.doldolmeet.domain.openvidu.service.OpenviduService;
import com.doldolmeet.domain.users.idol.repository.IdolRepository;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.Connection;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.doldolmeet.exception.ErrorCode.*;


@CrossOrigin(origins = "https://youngeui-in-jungle.store/")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
    public final SseService sseService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository;
    private final OpenviduService openviduService;
    private final IdolRepository idolRepository;

    private OpenVidu openvidu = new OpenVidu("https://youngeui-in-jungle.store/", "MY_SECRET");


    @GetMapping(path = "/fanMeetings/{fanMeetingId}/sse/{username}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter createEmitter(@PathVariable Long fanMeetingId, @PathVariable String username) {
        // Add the emitter to a list of subscribers or handle it in another way
        log.info("SseController.subscribe() 호출됨");
        log.info("fanMeetingId: " + fanMeetingId);
        log.info("username: " + username);

        return sseService.createEmitter(fanMeetingId, username);
    }

    @Async
    @PostMapping("/my_webhook")
    public String webhook(@RequestBody String eventMessage) throws OpenViduJavaClientException, OpenViduHttpException{

        log.info("Webhook received!");
        log.info("EVENTMESSAGE:" + eventMessage);

        if (eventMessage.contains("sessionCreated") || eventMessage.contains("sessionDestroyed") || eventMessage.contains("webrtcConnectionCreated") || eventMessage.contains("webrtcConnectionDestroyed") || eventMessage.contains("streamCreated") || eventMessage.contains("streamDestroyed") || eventMessage.contains("recordingStatusChanged")) {
            return eventMessage;
        }

        if (eventMessage.contains("ADMIN") || eventMessage.contains("IDOL")) {
            return eventMessage;
        }

        String username = parseUsername(eventMessage);
        Long fanMeetingId = parseFanMeetingId(eventMessage);
        String sessionId = parseSessionId(eventMessage);

        // 참가자가 대기방에 들어왔을 때
        if (eventMessage.contains("participantJoined") && eventMessage.contains("waitingRoom")) {
            // 현재 방의 fanMeetingRoomOrder
            Optional<FanMeetingRoomOrder> currFanMeetingRoomOrderOpt = fanMeetingRoomOrderRepository.findByFanMeetingIdAndCurrentRoom(fanMeetingId, sessionId);
            // 없으면 예외
            if (currFanMeetingRoomOrderOpt.isEmpty()) {
                throw new CustomException(NOT_FOUND_FANMEETING_ROOM_ORDER);
            }
            FanMeetingRoomOrder currRoomOrder = currFanMeetingRoomOrderOpt.get();

            // 다음 방의 fanMeetingRoomOrder
            Optional<FanMeetingRoomOrder> nextFanMeetingRoomOrderOpt = fanMeetingRoomOrderRepository.findByFanMeetingIdAndCurrentRoom(fanMeetingId, currRoomOrder.getNextRoom());
            // 없으면 예외
            if (nextFanMeetingRoomOrderOpt.isEmpty()) {
                throw new CustomException(NOT_FOUND_FANMEETING_ROOM_ORDER);
            }

            FanMeetingRoomOrder nextRoomOrder = nextFanMeetingRoomOrderOpt.get();

            // 다음 방이 idolRoom이면,
            if (nextRoomOrder.getType().equals("idolRoom")) {
                // 다음 방의 커넥션 리스트 얻어서,
                List<Connection> connections = openviduService.getConnections(nextRoomOrder.getCurrentRoom());

                // 다음 방 커넥션이 2개면 해당 팬에게 아이돌방 들어가라고 에미터 쏴주기
                if (connections.size() == 2) {
                    Map<String, String> params = new HashMap<>();
                    params.put("nextRoomId", currRoomOrder.getNextRoom());
                    params.put("currRoomType", currRoomOrder.getType());
                    params.put("idolNickName", currRoomOrder.getNickname());
                    params.put("roomThumbnail", currRoomOrder.getRoomThumbnail());
                    params.put("motionType", nextRoomOrder.getMotionType());
                    params.put("gameType", nextRoomOrder.getGameType());

                    try {
                        log.info("해당 아이돌 방 커넥션 2개라서 팬 들여보냄.");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new CustomException(SLEEP_FAILED);
                        }

                        SseService.emitters.get(fanMeetingId).get(username).send(SseEmitter.event().name("moveToIdolRoom").data(params));
                        return eventMessage;
                        // 쏘고 나면, 클라이언트에서 이 이벤트를 받아 처리한다.(화면 전환 + 해당 세션에 입장)
                        // 입장시, joined 이벤트 발생 -> 웹훅 -> 대기방에 추가됨.

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // 다음 방 커넥션이 1개면 관리자만 들어와 있는 경우라고 설정
                else if (connections.size() == 1) {
                    Map<String, String> params = new HashMap<>();
                    params.put("nextRoomId", currRoomOrder.getNextRoom());
                    params.put("currRoomType", currRoomOrder.getType());
                    params.put("idolNickName", currRoomOrder.getNickname());
                    params.put("roomThumbnail", currRoomOrder.getRoomThumbnail());
                    params.put("motionType", nextRoomOrder.getMotionType());
                    params.put("gameType", nextRoomOrder.getGameType());

                    try {
                        log.info("해당 아이돌 방 커넥션 1개인데 일단 들여보냄");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new CustomException(SLEEP_FAILED);
                        }

                        SseService.emitters.get(fanMeetingId).get(username).send(SseEmitter.event().name("moveToIdolRoom").data(params));
                        return eventMessage;
                        // 쏘고 나면, 클라이언트에서 이 이벤트를 받아 처리한다.(화면 전환 + 해당 세션에 입장)
                        // 입장시, joined 이벤트 발생 -> 웹훅 -> 대기방에 추가됨.

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

//                    try {
//                        SseService.emitters.get(fanMeetingId).get(username).send(SseEmitter.event().name("adminOnly").data("adminOnly"));
//                        sseService.addwaiter(username, fanMeetingId, sessionId);
//                        return eventMessage;
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                }

                // 3개면 진행중임
                else if (connections.size() == 3) {
                    try {
                        log.info("해당 아이돌 방 커넥션 3개임.");
                        SseService.emitters.get(fanMeetingId).get(username).send(SseEmitter.event().name("full").data("full"));
                        sseService.addwaiter(username, fanMeetingId, sessionId);
                        return eventMessage;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // 0개거나 4개 이상이면 예외사항
                else {
                    log.info("해당 아이돌 방 커넥션 0개거나 4개 이상임.");
                    throw new CustomException(INVALID_IDOLROOM_STATE);
                }
            }

            // 다음 방도 waitRoom이면 그냥 웨이팅룸에 넣기.
            else if (nextRoomOrder.getType().equals("waitRoom")) {
                sseService.addwaiter(username, fanMeetingId, sessionId);
                return eventMessage;
            }

            // waitRoom에 들어갔는데 다음 방이 끝일리 없음
            else {
                throw new CustomException(INVALID_FANMEETING_END);
            }
        }

        // 참가자가 대기방에서 나갔을 때
        else if (eventMessage.contains("participantLeft") && eventMessage.contains("waitingRoom")) {
            sseService.removeWaiter(username, fanMeetingId, sessionId);
        }

        // 참가자가 아이돌방에 들어왔을 때
        else if (eventMessage.contains("participantJoined") && eventMessage.contains("idolRoom")) {
            log.info("timer 시작");
            //countdown 시작
            // 킥될 때는 바로 대기방B에 들어가는게 좋을듯(그래야 아이돌방A에서 나가는 이벤트가 바로 발생해서, 대기방A에 들어오는 팬과 레이스컨디션 되지 않을듯, 근데 초대 보내졌는데 늦게 클릭하다가 대기방)
            waitAndKick(eventMessage);
        }

        // 참가자가 아이돌방에서 나갔을 때
        else if (eventMessage.contains("participantLeft") & eventMessage.contains("idolRoom")) {
            // 자기 대기방에 있는 팬 중 가장 우선순위 높은 팬에게 쏨
            // 해당 방을 nextRoomId로 가지는 RoomOrder 조회
            Optional<FanMeetingRoomOrder> prevFanMeetingRoomOrderOpt = fanMeetingRoomOrderRepository.findByFanMeetingIdAndNextRoom(fanMeetingId, sessionId);
            // 없으면 예외
            if (prevFanMeetingRoomOrderOpt.isEmpty()) {
                throw new CustomException(NOT_FOUND_FANMEETING_ROOM_ORDER);
            }
            FanMeetingRoomOrder prevRoomOrder = prevFanMeetingRoomOrderOpt.get();


            if (SseService.waitingRooms.get(fanMeetingId).get(prevRoomOrder.getCurrentRoom()) == null) {
                throw new CustomException(WAITROOM_NOT_FOUND);
            }

            SortedSet<UserNameAndOrderNumber> waitingRoom = SseService.waitingRooms.get(fanMeetingId).get(prevRoomOrder.getCurrentRoom());
            if (waitingRoom.isEmpty()) {
                throw new CustomException(WAITROOM_FAN_NOT_FOUND);
            }

            String newUsername = waitingRoom.first().getUsername();

            Optional<FanMeetingRoomOrder> nextFanMeetingRoomOrderOpt = fanMeetingRoomOrderRepository.findByFanMeetingIdAndCurrentRoom(fanMeetingId, prevRoomOrder.getNextRoom());

            if (nextFanMeetingRoomOrderOpt.isEmpty()) {
                throw new CustomException(NOT_FOUND_FANMEETING_ROOM_ORDER);
            }
            FanMeetingRoomOrder nextRoomOrder = nextFanMeetingRoomOrderOpt.get();

            try {
                Map<String, String> params = new HashMap<>();
                params.put("nextRoomId", prevRoomOrder.getNextRoom());
                params.put("currRoomType", prevRoomOrder.getType());
                params.put("idolNickName", prevRoomOrder.getNickname());
                params.put("roomThumbnail", prevRoomOrder.getRoomThumbnail());
                params.put("motionType", nextRoomOrder.getMotionType());
                params.put("gameType", nextRoomOrder.getGameType());

                SseService.emitters.get(fanMeetingId).get(newUsername).send(SseEmitter.event().name("moveToIdolRoom").data(params));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return eventMessage;
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

    private String parseSessionId(String eventMessage) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(eventMessage);
            String sessionId = jsonNode.get("sessionId").asText();
            log.info("------SessionId: " + sessionId);

            return sessionId;
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

    public void waitAndKick(String body) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        // Submit tasks to the thread pool
//        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//        System.out.println(openvidu);
//        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        executorService.execute(new MyTask(body, openviduService, objectMapper, fanMeetingRoomOrderRepository, openvidu, idolRepository));
        // Shutdown the thread pool when done
        executorService.shutdown();
    }
}
