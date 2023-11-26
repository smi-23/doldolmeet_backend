package com.doldolmeet.domain.fanMeeting.scheduler;


import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.sse.SseService;
import com.doldolmeet.domain.fanMeeting.sse.UserNameAndOrderNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FanMeetingScheduler {
    private final FanMeetingRepository fanMeetingRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 예제를 위해 간단하게 맵에 팬미팅 시작시간과 특정 로직을 설정
//    private static final Runnable fanMeetingTask = (FanMeeting fanMeeting) -> {
//        System.out.println("FanMeeting started! Perform specific logic.");
//        // 여기에 팬미팅에 대한 특정 로직을 추가하세요.
//    };

    @Scheduled(fixedRate = 60 * 1000) // 매 분마다 실행
    @Transactional
    public void checkFanMeetingStartTime() throws IOException {
        // 오늘 열려있는 팬미팅들 조회

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime midNightTime = currentTime.with(LocalTime.MIN);
        LocalDateTime tomorrowMidNightTime = midNightTime.plusDays(1);

        List<FanMeeting> fanMeetings = fanMeetingRepository.findTodayFanMeetings(midNightTime, tomorrowMidNightTime);

        log.info("현재 시간: " + currentTime);

        for (FanMeeting fanMeeting : fanMeetings) {
            if (fanMeeting.getStartTime().isBefore(currentTime)) {
                log.info("FanMeeting start time: " + fanMeeting.getStartTime());

                    // 대기방 입장 로직
                    // 일단, 팬들은 대기방에 입장해 있음. -> SSE 생성되어 있음.
                    // 팬미팅 시작시간이 되면, 팬미팅에 참여하는 팬들을 대기방에서 팬미팅방으로 이동시킴.
                    // 팬미팅방으로 이동시키는 방법은, 팬미팅방에 입장하는 SSE를 생성해서, 팬들에게 SSE를 보내는 방법이 있음.
                    // 해당 팬미팅의 메인 대기방에 해당하는 세션ID 구해야 함.


                    Map<String, SortedSet<UserNameAndOrderNumber>> s = SseService.waitingRooms.get(fanMeeting.getId());
                    FanMeetingRoomOrder roomOrder = fanMeeting.getFanMeetingRoomOrders().get(0); // TODO: NullPointerException

                    String mainRoomId = roomOrder.getCurrentRoom();

                    UserNameAndOrderNumber a =  s.get(mainRoomId).first();
                    String username = a.getUsername();
                    SseEmitter emitter = SseService.emitters.get(fanMeeting.getId()).get(username);

                    Map<String, String> params = new HashMap<>();
                    params.put("nextRoomId", roomOrder.getNextRoom());
                    params.put("currRoomType", roomOrder.getType());

                    try {
                        emitter.send(SseEmitter.event().name("moveToFirstIdolWaitRoom").data(params));
                        // 쏘고 나면, 클라이언트에서 이 이벤트를 받아 처리한다.(화면 전환 + 해당 세션에 입장)
                        // 입장시, joined 이벤트 발생 -> 웹훅 -> 대기방에 추가됨.

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }

        log.info("팬미팅 스케쥴링 완료");
    }
}
