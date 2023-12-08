//package com.doldolmeet.domain.waitRoom.mainWaitRoom.controller;
//
//
//import com.doldolmeet.domain.fanMeeting.dto.request.FanMeetingRequestDto;
//import com.doldolmeet.utils.Message;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import com.doldolmeet.domain.fanMeeting.sse.SseService;
//
//@RequiredArgsConstructor
//@Controller
//public class mainWaitRoomController {
//    SseService sseService;
//
//
//    @GetMapping("/{fanMeetingId}/getNumberOfPeopleAhead")
//    public ResponseEntity<Message> getNumberOfPeopleAhead( FanMeetingRequestDto requestDto, HttpServletRequest request) {
//        SseService.getNumberOfPeopleAhead(requestDto, request);
//
//        ;
//    }
//
//}
