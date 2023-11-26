package com.doldolmeet.domain.fanMeeting.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
    public final SseService sseService;

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
    public String webhook(@RequestBody String eventMessage) throws OpenViduJavaClientException, OpenViduHttpException {

        System.out.println("Webhook received!");
        System.out.println(eventMessage);

        // 참가자가 대기방에 들어왔을 때
        if (eventMessage.contains("participantJoined") & eventMessage.contains("waitingRoom")) {
            sseService.addwaiter(eventMessage);
        }

        // 참가자가 대기방에서 나갔을 때
        if (eventMessage.contains("participantLeft") & eventMessage.contains("waitingRoom")) {
            sseService.removeWaiter(eventMessage);
        }

        // 참가자가 아이돌방에 들어왔을 때
        if (eventMessage.contains("participantJoined") & eventMessage.contains("idolRoom")) {
            System.out.println("timer 시작");
            //countdown 시작
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("10초뒤에 시작");
//					RoomList.get("idolRoom").forceDisconnect();
                }
            }, 10, TimeUnit.SECONDS);
        }

//        // 참가자가 아이돌방에서 나갔을 때
//        if (eventMessage.contains("participantLeft") & eventMessage.contains("idolRoom")) {
//            callNextFan();
//        }

        return eventMessage;
    }

//    private void callNextFan () {
//        try {
//            Long nextfan = Collections.min(SseService.waitingRoom);
//            System.out.println("nextfan : " + nextfan);
//            SseService.emitters.get(nextfan).send("karina");
//        } catch (Exception e) {
//            System.out.println("waitingRoom is empty");
//        }
//    }
}
