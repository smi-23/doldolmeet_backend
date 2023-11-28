package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanToFanMeetingRepository;
import com.doldolmeet.exception.CustomException;
import io.openvidu.java.client.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.doldolmeet.exception.ErrorCode.NOT_FOUND_FANTOFANMEETING;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {
    private final FanToFanMeetingRepository fanToFanMeetingRepository;

    // 팬미팅ID, 유저이름, 에미터
    public static Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 팬미팅ID, 대기방Id, 대기자들 리스트(username)
//    public static Map<Long, Map<String, List<String>>> waitingRooms = new ConcurrentHashMap<>();
    public static Map<Long, Map<String, SortedSet<UserNameAndOrderNumber>>> waitingRooms = new ConcurrentHashMap<>();

    public static Map<Long, Map<String, Session>> Rooms = new ConcurrentHashMap<>();

    //Emitter 추가
    public SseEmitter createEmitter(Long fanMeetingId, String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        if (emitters.get(fanMeetingId) == null) {
            emitters.put(fanMeetingId, new ConcurrentHashMap<>());
        }

        emitters.get(fanMeetingId).put(username, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            emitters.get(fanMeetingId).remove(username);    // 만료되면 리스트에서 삭제
        });

        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitter.complete();
        });

        log.info("SseService.addEmitter() called");
        return emitter;
    }

    //waitingRoom에 waiter 추가
    @Transactional
    public void addwaiter(String username, Long fanMeetingId, String sessionId) {
        log.info("SseService.addWaiter() called");

        // fanMeetingId(PK값), 대기방Id(세션ID) 구해서 넣기
        if (waitingRooms.get(fanMeetingId) == null) {
            waitingRooms.put(fanMeetingId, new ConcurrentHashMap<>());
        }

        if (waitingRooms.get(fanMeetingId).get(sessionId) == null) {
            Comparator comparator = new OrderNumberComparator();
            SortedSet<UserNameAndOrderNumber> sortedSet = new TreeSet(comparator);
            waitingRooms.get(fanMeetingId).put(sessionId, sortedSet);
        }

        Optional<FanToFanMeeting> ftfm = fanToFanMeetingRepository.findByFanMeetingIdAndFanUsername(fanMeetingId, username);

        if (!ftfm.isPresent()) {
            throw new CustomException(NOT_FOUND_FANTOFANMEETING);
        }



        waitingRooms.get(fanMeetingId).get(sessionId).add(new UserNameAndOrderNumber(username, ftfm.get().getOrderNumber()));
    }

    //waitingRoom에 waiter 제거
    public void removeWaiter(String username, Long fanMeetingId, String sessionId) {
        log.info("SseService.removeWaitingRoom() called");

        SortedSet sortedSet = waitingRooms.get(fanMeetingId).get(sessionId);

        // sortedSet에서 해당 user의 UserNameAndOrderNumber를 찾아서 제거
        if (sortedSet != null) {
            Iterator iterator = sortedSet.iterator();
            while (iterator.hasNext()) {
                UserNameAndOrderNumber userNameAndOrderNumber = (UserNameAndOrderNumber) iterator.next();
                if (userNameAndOrderNumber.getUsername().equals(username)) {
                    sortedSet.remove(userNameAndOrderNumber);
                    break;
                }
            }
        }
    }



    public void sendEvents() {
//        for (SseEmitter emitter : emitters) {
//            try {
//                System.out.println("SseService.sendEvents() called");
//
//                emitter.send(System.currentTimeMillis());
//                System.out.println("SseService.sendEvents() called1111");
//            } catch (IOException e) {
//                emitter.complete();
//                System.out.println("SseService.sendEvents() IOException");
//                emitters.remove(emitter);
//            }
//            System.out.println("SseService.sendEvents() called");
//        }
    }

    @Scheduled(initialDelay = 3000, fixedRate = 3000)
    public void printemitter() {
        log.info("emitter : " + emitters);
        log.info("waitingRoom : " + waitingRooms);
    }
}
