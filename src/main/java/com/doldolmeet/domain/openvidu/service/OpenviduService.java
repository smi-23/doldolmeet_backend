package com.doldolmeet.domain.openvidu.service;

import com.doldolmeet.domain.commons.Role;
import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanToFanMeetingRepository;
import com.doldolmeet.domain.openvidu.dto.response.EnterResponseDto;
import com.doldolmeet.domain.teleRoom.entity.TeleRoom;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.teleRoom.repository.TeleRoomRepository;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.fan.repository.FanRepository;
import com.doldolmeet.domain.users.idol.entity.Idol;
import com.doldolmeet.domain.waitRoom.entity.WaitRoom;
import com.doldolmeet.domain.waitRoom.entity.WaitRoomFan;
import com.doldolmeet.domain.waitRoom.repository.WaitRoomFanRepository;
import com.doldolmeet.domain.waitRoom.repository.WaitRoomRepository;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.security.jwt.JwtUtil;
import com.doldolmeet.utils.Message;
import com.doldolmeet.utils.UserUtils;
import io.jsonwebtoken.Claims;
import io.openvidu.java.client.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.doldolmeet.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OpenviduService {
    private final JwtUtil jwtUtil;
    private final UserUtils userUtils;
    private final FanRepository fanRepository;
    private final FanMeetingRepository fanMeetingRepository;
    private final WaitRoomRepository waitRoomRepository;
    private final TeleRoomRepository teleRoomRepository;
    private final WaitRoomFanRepository waitRoomFanRepository;
    private final FanToFanMeetingRepository fanToFanMeetingRepository;

    private Claims claims;
    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;


    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    public ResponseEntity<String> initializeSession(Map<String, Object> params) throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

    public ResponseEntity<String> createConnection(String sessionId, Map<String, Object> params) throws OpenViduJavaClientException, OpenViduHttpException{
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> enterFanMeeting(Long fanMeetingId, HttpServletRequest request) throws OpenViduJavaClientException, OpenViduHttpException {
        claims = jwtUtil.getClaims(request);
        String role = (String)claims.get("auth");
        EnterResponseDto responseDto = new EnterResponseDto();

        Optional<FanMeeting> fanMeeting = fanMeetingRepository.findById(fanMeetingId);

        // 존재하지 않는 팬미팅.
        if (!fanMeeting.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        // 팬인 경우 재접속 판단.
        if (role.equals(Role.FAN.getKey())) {
            Fan fan = userUtils.getFan(claims.getSubject());
            WaitRoom waitRoom;
            Session waitRoomSession;

            // 일단 재접속 유무 검사.
            Optional<WaitRoomFan> waitRoomFanOpt = waitRoomFanRepository.findByFanIdAndWaitRoomId(fan.getId(), fanMeetingId);

            // 재접속이면,
            if (waitRoomFanOpt.isPresent()) {
                WaitRoomFan waitRoomFan = waitRoomFanOpt.get();
                Long idx = waitRoomFan.getNextWaitRoomIdx();
                waitRoom = fanMeeting.get().getWaitRooms().get(idx.intValue());

                // 다시 연결
//                waitRoom.getWaitRoomFans().add(waitRoomFan.get()); // waitRoom에 팬 추가.
                Session session = openvidu.getActiveSession(waitRoom.getRoomId());

                if (session == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                Map<String, Object> param = new HashMap<>();
                param.put("customSessionId", waitRoom.getRoomId());

                ConnectionProperties properties = ConnectionProperties.fromJson(param).build();
                Connection connection = session.createConnection(properties);
                waitRoomFan.setConnectionId(connection.getConnectionId());
                waitRoomFan.setCurrRoomId(waitRoom.getRoomId());
                waitRoomFan.setConnectionToken(connection.getToken());

                responseDto.setToken(connection.getToken());
                return new ResponseEntity<>(new Message("팬 재접속 성공", responseDto), HttpStatus.OK);
            }

            // 존재 안하면,
            else {
                Optional<FanToFanMeeting> fanToFanMeeting = fanToFanMeetingRepository.findByFanAndFanMeeting(fan, fanMeeting.get());

                if (!fanToFanMeeting.isPresent()) {
                    throw new CustomException(FAN_TO_FANMEETING_NOT_FOUND);
                }
                // 새로 만들기
                WaitRoomFan newWaitRoomFan = WaitRoomFan.builder()
                        .nextTeleRoomIdx(0L)
                        .nextWaitRoomIdx(0L)
                        .orderNumber(fanToFanMeeting.get().getOrderNumber())
                        .fan(userUtils.getFan(claims.getSubject()))
                        .build();

                waitRoom = fanMeeting.get().getWaitRooms().get(0);
                newWaitRoomFan.setWaitRoom(waitRoom);
                waitRoomSession = openvidu.getActiveSession(fanMeeting.get().getWaitRooms().get(0).getRoomId());
                waitRoom.getWaitRoomFans().add(newWaitRoomFan);
                ConnectionProperties properties = ConnectionProperties.fromJson(new HashMap<>()).build(); // params에 뭐?
                Connection connection = waitRoomSession.createConnection(properties);
                newWaitRoomFan.setConnectionId(connection.getConnectionId());
                newWaitRoomFan.setCurrRoomId(waitRoom.getRoomId());
                newWaitRoomFan.setConnectionToken(connection.getToken());

                if (waitRoomSession == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                responseDto.setToken(connection.getToken());
                waitRoomFanRepository.save(newWaitRoomFan);
                return new ResponseEntity<>(new Message("팬미팅 입장 성공", responseDto), HttpStatus.OK);
            }
        }

        // 아이돌이 입장버튼 누를시, 자기방 생성
        else if (role.equals(Role.IDOL.getKey())) {
            Map<String, String> result = new HashMap<>();
            Idol idol = userUtils.getIdol(claims.getSubject());

            // 아이돌이 가지고 있는 TeleRoomId에 해당하는 방이 존재하면 거기로 가기
            if (openvidu.getActiveSession(idol.getTeleRoomId()) != null) {
                Session session = openvidu.getActiveSession(idol.getTeleRoomId());

                if (session == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                ConnectionProperties properties = ConnectionProperties.fromJson(new HashMap<>()).build(); // params에 뭐?
                Connection connection = session.createConnection(properties);

                responseDto.setToken(connection.getToken());
                responseDto.setTeleRoomId(idol.getTeleRoomId());
                responseDto.setWaitRoomId(idol.getWaitRoomId());
                return new ResponseEntity<>(new Message("아이돌이 자기방 재입장 성공", responseDto), HttpStatus.OK);
            }

            // teleRoom, waitRoom 생성
            Map<String, Object> param1 = new HashMap<>(); // videoRoom용
            Map<String, Object> param2 = new HashMap<>(); // waitList용

            param1.put("customSessionId", idol.getTeleRoomId());
            param2.put("customSessionId", idol.getWaitRoomId());

            SessionProperties properties1 = SessionProperties.fromJson(param1).build();
            SessionProperties properties2 = SessionProperties.fromJson(param2).build();

            try {
                Session teleSession = openvidu.createSession(properties1);
                Session waitSession = openvidu.createSession(properties2);

                responseDto.setTeleRoomId(teleSession.getSessionId());
                responseDto.setWaitRoomId(waitSession.getSessionId());

                WaitRoom waitRoom = new WaitRoom();
                waitRoom.setRoomId(idol.getWaitRoomId());
                waitRoom.setFanMeeting(fanMeeting.get());

                TeleRoom teleRoom = new TeleRoom();
                teleRoom.setRoomId(idol.getTeleRoomId());
                teleRoom.setFanMeeting(fanMeeting.get());

                fanMeeting.get().getWaitRooms().add(waitRoom);
                fanMeeting.get().getTeleRooms().add(teleRoom);

                Session session = openvidu.getActiveSession(teleSession.getSessionId());

                if (session == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                ConnectionProperties properties = ConnectionProperties.fromJson(new HashMap<>()).build(); // params에 뭐?
                Connection connection = session.createConnection(properties);
                responseDto.setToken(connection.getToken());

                waitRoomRepository.save(waitRoom);
                teleRoomRepository.save(teleRoom);

                return new ResponseEntity<>(new Message("아이돌 방생성 및 입장 성공", responseDto), HttpStatus.OK);
            } catch (OpenViduHttpException e) {
                if (e.getStatus() == 409) {
                    return new ResponseEntity<>(new Message("이미 존재하는 방입니다.", null), HttpStatus.CONFLICT);
                } else {
                    return new ResponseEntity<>(new Message("openvidu 오류", null), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        else {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    // 현재 자신과 통화중인 팬을 다음 대기열로 옮기는 기능
    // 자신의 대기
    // 열에서 최우선순위 팬을 들여오는 기능


//    // openvidu connection 해제
//    public ResponseEntity<Message> disconnect(String connectionId) throws OpenViduJavaClientException, OpenViduHttpException {
//        Connection connection = openvidu.getActiveConnection(connectionId);
//        if (connection == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        connection.getSession().close();
//        return new ResponseEntity<>(new Message("Connection successfully closed", null), HttpStatus.OK);
//    }
}
