package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRoomOrderRepository;
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
    private final FanMeetingRoomOrderRepository fanMeetingRoomOrderRepository;
    private final FanMeetingRepository fanMeetingRepository;

    // 팬미팅ID, 유저이름, 에미터
    public static Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

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

//        emitter.onCompletion(() -> {
//            log.info("onCompletion callback");
//            emitters.get(fanMeetingId).remove(username);    // 만료되면 리스트에서 삭제
//        });
//
//        emitter.onTimeout(() -> {
//            log.info("onTimeout callback");
//            emitter.complete();
//        });
//
//        emitter.onError(throwable -> {
//            log.info("onError callback");
//            log.info(throwable.getMessage());
//            emitter.complete();
//        }

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

        SortedSet<UserNameAndOrderNumber> watingFans = waitingRooms.get(fanMeetingId).get(sessionId);

        // sortedSet에서 해당 user의 UserNameAndOrderNumber를 찾아서 제거
        if (watingFans != null) {
            for (UserNameAndOrderNumber userNameAndOrderNumber : watingFans) {
                if (userNameAndOrderNumber.getUsername().equals(username)) {
                    watingFans.remove(userNameAndOrderNumber);
                    break;
                }
            }
        }
    }



    //mainWaitingRoom에서 내 앞에 대기하는 인원 수 알려주기
    @Scheduled(fixedRate = 1000)
    public void noticeNumberOfPeopleAhead() {
        // fanMeetingId를 알아야함.
        // main 대기방이 뭔지 알아야함.
        List<FanMeeting> fanMeetings = fanMeetingRepository.findAll();
        for (FanMeeting fanMeeting : fanMeetings) {
//            System.out.println("@@@@@@@@@ 1 @@@@@@@@@");
            Long fanMeetingId = fanMeeting.getId();
//            System.out.println("@@@@@@@@@ 2 @@@@@@@@@");
            String mainWaitingRoomId = fanMeetingRoomOrderRepository.getMainWaitingRoomfindByFanMeetingId(fanMeetingId).get().getCurrentRoom();
//            System.out.println("@@@@@@@@@ 3 @@@@@@@@@");
            if (waitingRooms.get(fanMeetingId) == null) {
                continue;
            }

            if (waitingRooms.get(fanMeetingId).get(mainWaitingRoomId) == null) {
                continue;
            }

            SortedSet<UserNameAndOrderNumber> waitersInMainWaitingRoom = waitingRooms.get(fanMeetingId).get(mainWaitingRoomId);
//            System.out.println("@@@@@@@@@ 4 @@@@@@@@@");
            // mainwaitingroom에 있는 사람들에게 이벤트를 보내서, 자기가 몇번째 대기자인지 알려준다.
            waitersInMainWaitingRoom.forEach(waiter -> {
                try {
                    Long myOrder = waiter.getOrderNumber();
                    // myOrder 보다 작은 orderNumber를 가진 사람들의 수를 구한다.
                    Long numberOfPeopleAhead = waitersInMainWaitingRoom.stream().filter(userNameAndOrderNumber -> userNameAndOrderNumber.getOrderNumber() < myOrder).count();
                    emitters.get(fanMeetingId).get(waiter.getUsername()).send(SseEmitter.event().name("numberOfPeopleAhead").data(numberOfPeopleAhead));
                    System.out.println("waiter" + waiter.getUsername() + "numberOfPeopleAhead : " + numberOfPeopleAhead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }



    @Scheduled(initialDelay = 3000, fixedRate = 3000)
    public void printemitter() {
        log.info("emitter : " + emitters);
        log.info("waitingRoom : " + waitingRooms);
    }
}
