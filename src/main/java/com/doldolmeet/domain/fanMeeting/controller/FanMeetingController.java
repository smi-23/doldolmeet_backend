package com.doldolmeet.domain.fanMeeting.controller;

import com.doldolmeet.domain.fanMeeting.dto.request.FanMeetingRequestDto;
import com.doldolmeet.domain.fanMeeting.service.FanMeetingService;
import com.doldolmeet.utils.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FanMeetingController {
    private final FanMeetingService fanMeetingService;

    // 팬미팅 생성 API
    @PostMapping("/fanMeetings")
    public ResponseEntity<Message> createFanMeeting(@RequestBody FanMeetingRequestDto requestDto, HttpServletRequest request) {
        return fanMeetingService.createFanMeeting(requestDto, request);
    }

    // option에 해당하는 모든 팬미팅 조회(로그인 안한 사람도 가능)
    @GetMapping("/fanMeetings")
    @ResponseBody
    public ResponseEntity<Message> getFanMeetings(@RequestParam String option) {
        return fanMeetingService.getFanMeetings(option);
    }

    // 해당 팬미팅 신청 API
    @PostMapping("/fanMeetings/{fanMeetingId}")
    public ResponseEntity<Message> applyFanMeeting(@PathVariable Long fanMeetingId, HttpServletRequest request) {
        return fanMeetingService.applyFanMeeting(fanMeetingId, request);
    }

    // 내가 신청한 팬미팅 중 예정된 가장 첫번째 팬미팅
    @GetMapping("/fanMeetings/latest")
    public ResponseEntity<Message> getMyLatestFanMeeting(HttpServletRequest request) {
        return fanMeetingService.getMyLatestFanMeeting(request);
    }



}
