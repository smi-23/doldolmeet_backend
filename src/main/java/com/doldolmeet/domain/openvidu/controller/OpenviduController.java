package com.doldolmeet.domain.openvidu.controller;

import java.util.HashMap;
import java.util.Map;

import com.doldolmeet.domain.openvidu.service.OpenviduService;
import com.doldolmeet.utils.Message;
import jakarta.annotation.PostConstruct;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.openvidu.java.client.Connection;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class OpenviduController {
    private final OpenviduService openviduService;

    /**
     * @param params The Session properties
     * @return The Session ID
     */

    // 화상통화방을 만드는 API
    @PostMapping("/api/sessions")
    public ResponseEntity<String> initializeSession(@RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.initializeSession(params);
    }

    /**
     * @param sessionId The Session in which to create the Connection
     * @param params    The Connection properties
     * @return The Token associated to the Connection
     */

    // 화상통화방<->유저간 커넥션 생성 API
    @PostMapping("/api/sessions/{sessionId}/connections")
    public ResponseEntity<String> createConnection(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.createConnection(sessionId, params);
    }


    // 화상통화방 들어가는 API(팬, 아이돌 로직 분기처리됨)
    @GetMapping("fanMeetings/{fanMeetingId}/session")
    public ResponseEntity<Message> enterFanMeeting(@PathVariable Long fanMeetingId, HttpServletRequest request) throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.enterFanMeeting(fanMeetingId, request);
    }

    // 아이돌 입장에서 다음에 들어올 팬 데이터 가져오는 API
    @GetMapping("fanMeetings/{fanMeetingId}/nextFan")
    public ResponseEntity<Message> getNextFan(@PathVariable Long fanMeetingId, HttpServletRequest request) throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.getNextFan(fanMeetingId, request);
    }

    // 아이돌 입장에서 자기 방에 있는 팬 다음 방으로 가라는 API
    @GetMapping("fanMeetings/{fanMeetingId}/nextWaitRoom")
    public ResponseEntity<Message> getNextFanRoom(@PathVariable Long fanMeetingId, HttpServletRequest request) throws OpenViduJavaClientException, OpenViduHttpException {
        return openviduService.getNextWaitRoomId(fanMeetingId, request);
    }

    // AWS 로드밸런서 헬스체크용 API
    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }
}