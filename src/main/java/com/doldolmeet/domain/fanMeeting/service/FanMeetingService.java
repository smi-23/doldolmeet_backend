package com.doldolmeet.domain.fanMeeting.service;

import com.doldolmeet.domain.fanMeeting.dto.request.FanMeetingRequestDto;
import com.doldolmeet.domain.fanMeeting.dto.response.*;
import com.doldolmeet.domain.fanMeeting.entity.*;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.repository.FanToFanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.repository.IdolToFanMeetingRepository;
import com.doldolmeet.domain.team.entity.Team;
import com.doldolmeet.domain.team.repository.TeamRepository;
import com.doldolmeet.domain.teleRoom.entity.TeleRoomFan;
import com.doldolmeet.domain.teleRoom.repository.TeleRoomFanRepository;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.fan.repository.FanRepository;
import com.doldolmeet.domain.users.idol.entity.Idol;
import com.doldolmeet.domain.users.idol.repository.IdolRepository;
import com.doldolmeet.domain.waitRoom.chat.repository.ChatRoomRepository;
import com.doldolmeet.domain.waitRoom.entity.WaitRoom;
import com.doldolmeet.domain.waitRoom.entity.WaitRoomFan;
import com.doldolmeet.domain.waitRoom.repository.WaitRoomFanRepository;
import com.doldolmeet.domain.waitRoom.repository.WaitRoomRepository;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.security.jwt.JwtUtil;
import com.doldolmeet.utils.Message;
import com.doldolmeet.utils.UserUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.doldolmeet.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanMeetingService {
    private final FanMeetingRepository fanMeetingRepository;
    private final TeamRepository teamRepository;
    private final FanToFanMeetingRepository fanToFanMeetingRepository;
    private final IdolToFanMeetingRepository idolToFanMeetingRepository;
    private final FanRepository fanRepository;
    private final IdolRepository idolRepository;
    private final WaitRoomFanRepository waitRoomFanRepository;
    private final WaitRoomRepository waitRoomRepository;
    private final TeleRoomFanRepository teleRoomFanRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final JwtUtil jwtUtil;
    private final UserUtils userUtils;
    private Claims claims;

    @Transactional
    public ResponseEntity<Message> createFanMeeting(FanMeetingRequestDto requestDto, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        userUtils.checkIfAdmin(claims);

        Optional<Team> team = teamRepository.findByTeamName(requestDto.getTeamName());

        if (!team.isPresent()) {
            throw new CustomException(TEAM_NOT_FOUND);
        }

        String chatRoomId = chatRoomRepository.createChatRoom(requestDto.getFanMeetingName()).getRoomId();

        FanMeeting fanMeeting = FanMeeting.builder()
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .capacity(requestDto.getCapacity())
                .fanMeetingName(requestDto.getFanMeetingName())
                .fanMeetingImgUrl(requestDto.getFanMeetingImgUrl())
                .team(team.get())
                .waitRooms(new ArrayList<>())
                .fanToFanMeetings(new ArrayList<>())
                .teleRooms(new ArrayList<>())
                .nextOrder(1L)
                .chatRoomId(chatRoomId)
                .build();

        fanMeetingRepository.save(fanMeeting);
        Map<String, Long> result = new HashMap<>();
        result.put("id", fanMeeting.getId());

        return new ResponseEntity<>(new Message("팬미팅 생성 완료", result), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> getFanMeetings(String option) {
        List<FanMeetingResponseDto> result = new ArrayList<>();
        List<FanMeeting> fanMeetings;

        if (option.equals(FanMeetingSearchOption.OPENED.value())) {
            fanMeetings = fanMeetingRepository.findFanMeetingsByEndTimeAfter(LocalDateTime.now());
        } else if (option.equals(FanMeetingSearchOption.CLOSED.value())) {
            fanMeetings = fanMeetingRepository.findFanMeetingsByEndTimeBefore(LocalDateTime.now());
        } else {
            fanMeetings = fanMeetingRepository.findAll();
        }

        for (FanMeeting fanMeeting : fanMeetings) {
            FanMeetingResponseDto responseDto = FanMeetingResponseDto.builder()
                    .id(fanMeeting.getId())
                    .imgUrl(fanMeeting.getFanMeetingImgUrl())
                    .title(fanMeeting.getFanMeetingName())
                    .startTime(fanMeeting.getStartTime())
                    .chatRoomId(fanMeeting.getChatRoomId())
                    .build();

            result.add(responseDto);
        }

        return new ResponseEntity<>(new Message("팬미팅 조회 성공", result), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> applyFanMeeting(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        String chatRoomId = chatRoomRepository.createChatRoom(fanMeeting.getFanMeetingName()).getRoomId();

        FanToFanMeeting fanToFanMeeting = FanToFanMeeting.builder()
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .fan(fan)
                .fanMeeting(fanMeeting)
                .orderNumber(fanMeeting.getNextOrder())
                .chatRoomId(chatRoomId)
                .build();

        fanMeeting.setNextOrder(fanMeeting.getNextOrder() + 1L);
        fan.getFanToFanMeetings().add(fanToFanMeeting);
        fanMeeting.getFanToFanMeetings().add(fanToFanMeeting);

        fanToFanMeetingRepository.save(fanToFanMeeting);

        FanToFanMeetingResponseDto responseDto = FanToFanMeetingResponseDto.builder()
                .id(fanToFanMeeting.getId())
                .fanMeetingId(fanMeetingId)
                .fanId(fan.getId())
                .orderNumber(fanToFanMeeting.getOrderNumber())
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .chatRoomId(chatRoomId)
                .build();

        return new ResponseEntity<>(new Message("팬미팅 신청 성공", responseDto), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> idolApplyFanMeeting(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Idol idol = userUtils.getIdol(claims.getSubject());


        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        IdolToFanMeeting idolToFanMeeting = IdolToFanMeeting.builder()
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .idol(idol)
                .fanMeeting(fanMeeting)
                .build();

        fanMeeting.setNextOrder(fanMeeting.getNextOrder() + 1L);
        idol.getIdolToFanMeetings().add(idolToFanMeeting);
        fanMeeting.getIdolToFanMeetings().add(idolToFanMeeting);

        idolToFanMeetingRepository.save(idolToFanMeeting);

        IdolToFanMeetingResponseDto responseDto = IdolToFanMeetingResponseDto.builder()
                .id(idolToFanMeeting.getId())
                .fanMeetingId(fanMeetingId)
                .idolId(idol.getId())
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .build();

        return new ResponseEntity<>(new Message("팬미팅(아이돌) 신청 성공", responseDto), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> getMyTodayFanMeeting(HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Optional<Fan> fan = fanRepository.findByUserCommonsUsername(claims.getSubject());
        Optional<Idol> idol = idolRepository.findByUserCommonsUsername(claims.getSubject());

        Optional<FanMeeting> fanMeetingOpt;

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime midNightTime = currentTime.with(LocalTime.MIN);

        log.info("현재시간: " + currentTime);
        log.info("자정시간: " + midNightTime);

        if (fan.isPresent()) {
            fanMeetingOpt = fanMeetingRepository.findFanMeetingsByFan(fan.get(), midNightTime, currentTime);
        } else if (idol.isPresent()) {
            fanMeetingOpt = fanMeetingRepository.findFanMeetingsByIdol(idol.get(), midNightTime, currentTime);
        } else {
            throw new CustomException(USER_NOT_FOUND);
        }
        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }
        FanMeeting fanMeeting = fanMeetingOpt.get();

        FanMeetingResponseDto responseDto = FanMeetingResponseDto.builder()
                .id(fanMeeting.getId())
                .imgUrl(fanMeeting.getFanMeetingImgUrl())
                .title(fanMeeting.getFanMeetingName())
                .startTime(fanMeeting.getStartTime())
                .endTime(fanMeeting.getEndTime())
                .chatRoomId(fanMeeting.getChatRoomId())
                .build();

        return new ResponseEntity<>(new Message("나의 예정된 팬미팅 중 가장 최신 팬미팅 받기 성공", responseDto), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> canEnterFanMeeting(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        Optional<FanToFanMeeting> fanToFanMeetingOpt = fanToFanMeetingRepository.findByFanAndFanMeeting(fan, fanMeeting);

        if (!fanToFanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanToFanMeeting fanToFanMeeting = fanToFanMeetingOpt.get();

        if (!fanToFanMeeting.getFanMeetingApplyStatus().equals(FanMeetingApplyStatus.APPROVED)) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        return new ResponseEntity<>(new Message("팬미팅 입장 가능", null), HttpStatus.OK);
    }

    public ResponseEntity<Message> getMainWaitRoom(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);

        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        WaitRoom mainWaitRoom = fanMeeting.getWaitRooms().get(0);
        mainWaitRoom.getRoomId();

        MainWaitRoomResponseDto responseDto = MainWaitRoomResponseDto.builder()
                .roomId(mainWaitRoom.getRoomId())
                .build();

        return new ResponseEntity<>(new Message("팬미팅의 메인 대기방 데이터 조회 성공", responseDto), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Message> getNextFan(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Idol idol = userUtils.getIdol(claims.getSubject());

        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);
        Optional<WaitRoom> waitRoomOpt = waitRoomRepository.findByRoomId(idol.getWaitRoomId());

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        if (!waitRoomOpt.isPresent()) {
            throw new CustomException(WAITROOM_NOT_FOUND);
        }

        WaitRoom waitRoom = waitRoomOpt.get();
        Optional<WaitRoomFan> waitRoomFan = waitRoomFanRepository.findFirstByWaitRoomOrderByOrderAsc(waitRoom);

        if (!waitRoomFan.isPresent()) {
            throw new CustomException(WAITROOMFAN_NOT_FOUND);
        }

        Fan fan = waitRoomFan.get().getFan();

        NextFanResponseDto responseDto = NextFanResponseDto.builder()
                .username(fan.getUserCommons().getUsername())
                .connectionId(waitRoomFan.get().getConnectionId())
                .waitRoomId(waitRoom.getRoomId())
                .roomType(RoomType.WAITING_ROOM)
                .build();

        return new ResponseEntity<>(new Message("다음에 참여할 팬 조회 성공", responseDto), HttpStatus.OK);
    }

    public ResponseEntity<Message> getNextWaitRoomId(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Idol idol = userUtils.getIdol(claims.getSubject());

        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);
        Optional<WaitRoom> waitRoomOpt = waitRoomRepository.findByRoomId(idol.getWaitRoomId());

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        if (!waitRoomOpt.isPresent()) {
            throw new CustomException(WAITROOM_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();
        WaitRoom waitRoom = waitRoomOpt.get();

        int waitRoomIdx = fanMeeting.getWaitRooms().indexOf(waitRoom);

        NextWaitRoomResponseDto responseDto = new NextWaitRoomResponseDto();

        // 마지막 대기열이었을 경우,
        if (waitRoomIdx == fanMeeting.getWaitRooms().size() - 1) {
            responseDto.setRoomId("END");
            return new ResponseEntity<>(new Message("마지막 대기열입니다.", responseDto), HttpStatus.OK);
        }

        // 그 외엔 다음 대기열세션ID 반환
        WaitRoom nextWaitRoom = fanMeeting.getWaitRooms().get(waitRoomIdx + 1);

        responseDto.setRoomId(nextWaitRoom.getRoomId());
        responseDto.setRoomType(RoomType.WAITING_ROOM);

        return new ResponseEntity<>(new Message("다음 대기열ID 반환 성공", responseDto), HttpStatus.OK);
    }

    public ResponseEntity<Message> getCurrentRoomId(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        String role = (String) claims.get("auth");

        Fan fan = userUtils.getFan(claims.getSubject());
        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        Optional<WaitRoomFan> waitRoomFan = waitRoomFanRepository.findByFanIdAndWaitRoomId(fan.getId(), fanMeetingId);
        Optional<TeleRoomFan> teleRoomFan = teleRoomFanRepository.findByFanIdAndTeleRoomId(fan.getId(), fanMeetingId);

        if (waitRoomFan.isPresent()) {
            String roomId = waitRoomFan.get().getWaitRoom().getRoomId();
            CurrRoomInfoResponseDto responseDto = CurrRoomInfoResponseDto.builder()
                    .roomId(roomId)
                    .build();

            return new ResponseEntity<>(new Message("현재 위치한 방(대기방)의 세션ID 반환 성공", responseDto), HttpStatus.OK);
        } else if (teleRoomFan.isPresent()) {
            String roomId = teleRoomFan.get().getTeleRoom().getRoomId();
            CurrRoomInfoResponseDto responseDto = CurrRoomInfoResponseDto.builder()
                    .roomId(roomId)
                    .build();

            return new ResponseEntity<>(new Message("현재 위치한 방(화상방)의 세션ID 반환 성공", responseDto), HttpStatus.OK);
        } else {
            throw new CustomException(FAN_NOT_IN_ROOM);
        }
    }

    // 팬미팅 조회 함수
    public ResponseEntity<Message> getFanMeeting(Long fanMeetingId, HttpServletRequest request) {
        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        FanMeetingResponseDto responseDto = FanMeetingResponseDto.builder()
                .id(fanMeeting.getId())
                .imgUrl(fanMeeting.getFanMeetingImgUrl())
                .title(fanMeeting.getFanMeetingName())
                .startTime(fanMeeting.getStartTime())
                .endTime(fanMeeting.getEndTime())
                .chatRoomId(fanMeeting.getChatRoomId())
                .build();

        return new ResponseEntity<>(new Message("팬미팅 조회 성공", responseDto), HttpStatus.OK);
    }

}
