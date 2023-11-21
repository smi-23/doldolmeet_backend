package com.doldolmeet.domain.fanMeeting.service;

import com.doldolmeet.domain.commons.UserCommons;
import com.doldolmeet.domain.fanMeeting.dto.request.FanMeetingRequestDto;
import com.doldolmeet.domain.fanMeeting.dto.response.FanMeetingResponseDto;
import com.doldolmeet.domain.fanMeeting.dto.response.FanToFanMeetingResponseDto;
import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanMeetingApplyStatus;
import com.doldolmeet.domain.fanMeeting.entity.FanMeetingSearchOption;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.repository.FanToFanMeetingRepository;
import com.doldolmeet.domain.fanMeeting.repository.FanToWaitingRoomRepository;
import com.doldolmeet.domain.team.entity.Team;
import com.doldolmeet.domain.team.repository.TeamRepository;
import com.doldolmeet.domain.users.admin.entity.Admin;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.idol.entity.Idol;
import com.doldolmeet.domain.users.idol.entity.IdolConnection;
import com.doldolmeet.domain.users.idol.repository.IdolConnectionRepository;
import com.doldolmeet.exception.CustomException;
import com.doldolmeet.exception.ErrorCode;
import com.doldolmeet.security.jwt.JwtUtil;
import com.doldolmeet.utils.Message;
import com.doldolmeet.utils.UserUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.doldolmeet.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class FanMeetingService {
    private final FanMeetingRepository fanMeetingRepository;
    private final TeamRepository teamRepository;
    private final FanToFanMeetingRepository fanToFanMeetingRepository;
    private final FanToWaitingRoomRepository fanToWaitingRoomRepository;
    private final IdolConnectionRepository idolConnectionRepository;
    private final JwtUtil jwtUtil;
    private final UserUtils userUtils;
    private Claims claims;

    public ResponseEntity<Message> createFanMeeting(FanMeetingRequestDto requestDto, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        userUtils.checkIfAdmin(claims);

        Optional<Team> team = teamRepository.findByTeamName(requestDto.getTeamName());

        if (!team.isPresent()) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        FanMeeting fanMeeting = FanMeeting.builder()
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .capacity(requestDto.getCapacity())
                .fanMeetingName(requestDto.getFanMeetingName())
                .team(team.get())
                .roomId(UUID.randomUUID().toString())
                .build();

        fanMeetingRepository.save(fanMeeting);
        return new ResponseEntity<>(new Message("팬미팅 생성 완료", null), HttpStatus.OK);
    }

    public ResponseEntity<Message> getFanMeetings(String option) {
        List<FanMeetingResponseDto> result = new ArrayList<>();
        List<FanMeeting> fanMeetings;

        if (option.equals(FanMeetingSearchOption.OPENED.value())) {
            fanMeetings = fanMeetingRepository.findFanMeetingsByEndTimeAfter(LocalDateTime.now());
        }
        else if (option.equals(FanMeetingSearchOption.CLOSED.value())) {
            fanMeetings = fanMeetingRepository.findFanMeetingsByEndTimeBefore(LocalDateTime.now());
        }

        else {
            fanMeetings = fanMeetingRepository.findAll();
        }

        for (FanMeeting fanMeeting : fanMeetings) {
            FanMeetingResponseDto responseDto = FanMeetingResponseDto.builder()
                    .id(fanMeeting.getId())
                    .imgUrl(fanMeeting.getFanMeetingImgUrl())
                    .title(fanMeeting.getFanMeetingName())
                    .startTime(fanMeeting.getStartTime())
                    .build();

            result.add(responseDto);
        }

        return new ResponseEntity<>(new Message("팬미팅 조회 성공", result), HttpStatus.OK);
    }

    public ResponseEntity<Message> applyFanMeeting(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        Optional<FanMeeting> fanMeeting = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeeting.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanToFanMeeting fanToFanMeeting = FanToFanMeeting.builder()
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .fan(fan)
                .fanMeeting(fanMeeting.get())
                .build();

        fan.getFanToFanMeetings().add(fanToFanMeeting);
        fanMeeting.get().getFanToFanMeetings().add(fanToFanMeeting);

        fanToFanMeetingRepository.save(fanToFanMeeting);

        FanToFanMeetingResponseDto responseDto = FanToFanMeetingResponseDto.builder()
                .id(fanToFanMeeting.getId())
                .fanMeetingId(fanMeetingId)
                .fanId(fan.getId())
                .fanMeetingApplyStatus(FanMeetingApplyStatus.APPROVED)
                .build();

        return new ResponseEntity<>(new Message("팬미팅 신청 성공", responseDto), HttpStatus.OK);
    }

    public ResponseEntity<Message> getMyLatestFanMeeting(HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        LocalDateTime currentTime = LocalDateTime.now();
        Optional<FanMeeting> fanMeetingOpt = fanMeetingRepository.findEarliestFanMeetingByFan(fan, currentTime);

        if (!fanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        FanMeeting fanMeeting = fanMeetingOpt.get();

        FanMeetingResponseDto responseDto = FanMeetingResponseDto.builder()
                .id(fanMeeting.getId())
                .imgUrl(fanMeeting.getFanMeetingImgUrl())
                .title(fanMeeting.getFanMeetingName())
                .startTime(fanMeeting.getStartTime())
                .build();

        return new ResponseEntity<>(new Message("나의 예정된 팬미팅 중 가장 최신 팬미팅 받기 성공", responseDto), HttpStatus.OK);
    }

    public Integer getMyOrder(HttpServletRequest request, Long fanMeetingId) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());
        System.out.println("fan = " + fan.getId());

        Optional<FanMeeting> fanMeeting = fanMeetingRepository.findById(fanMeetingId);

        if (!fanMeeting.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        Optional<Integer> fanToFanMeetingOpt = fanToFanMeetingRepository.waitingOrder(fan, fanMeeting.get());

        if (!fanToFanMeetingOpt.isPresent()) {
            throw new CustomException(FANMEETING_NOT_FOUND);
        }

        return fanToFanMeetingOpt.get();
    }

    // Idol에 대한 fanId를 받아서 db에 저장
    @Transactional
    public void saveConnection(String idolName, String fanName) {

        // sessionId 를 waitingRoomSession으로 가지는 Idol

        Idol idol = userUtils.getIdol(idolName);
        Fan fan = userUtils.getFan(fanName);

        IdolConnection idolConnection = IdolConnection.builder()
                .idol(idol)
                .fan(fan)
                .build();
        fanToWaitingRoomRepository.save(idolConnection);
    }

    //idol을 기다리는 fan중 orderNumber가 가장 작은(우선순위가 가장 높은) fan을 받아서 return
    public String getNextFan(String IdolName){
        Idol idol = userUtils.getIdol(IdolName);
        Optional<FanToFanMeeting> fanToFanMeeting = fanToWaitingRoomRepository.findFirstByWaitingRoomSession(idol);

        if(!fanToFanMeeting.isPresent()){
            throw new CustomException(ErrorCode.FAN_NOT_FOUND);
        }

        return fanToFanMeeting.get().getFan().getUserCommons().getUsername();
    };

    public void deleteEnteredFan(String idolName){
        Idol idol = userUtils.getIdol(idolName);
        Optional<FanToFanMeeting> fanToFanMeeting = fanToWaitingRoomRepository.findFirstByWaitingRoomSession(idol);

        if(!fanToFanMeeting.isPresent()){
            throw new CustomException(ErrorCode.FAN_NOT_FOUND);
        }

        // idolConnection 을 삭제 할거야
        Fan fan = fanToFanMeeting.get().getFan();

        Optional<IdolConnection> idolConnection = idolConnectionRepository.findIdolConnectionBy(idol.getId(), fan.getId());
        idolConnectionRepository.delete(idolConnection.get());

    }
}
