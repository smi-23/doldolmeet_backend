package com.doldolmeet.domain.capture.service;

import com.doldolmeet.domain.capture.dto.CaptureDto;
import com.doldolmeet.domain.capture.entity.Capture;
import com.doldolmeet.domain.capture.repository.CaptureRepository;
import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.s3.service.AwsS3Service;
import com.doldolmeet.security.jwt.JwtUtil;
import com.doldolmeet.utils.Message;
import com.doldolmeet.utils.UserUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaptureService {
    private final AwsS3Service awsS3Service;
    private final FanMeetingRepository fanMeetingRepository;
    private final CaptureRepository captureRepository;
    private final JwtUtil jwtUtil;
    private final UserUtils userUtils;
    private Claims claims;


    // Capture Upload to S3
    @Transactional
    public ResponseEntity<Message> uploadCapture(Long fanMeetingId, MultipartFile file, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        FanMeeting fanMeeting = fanMeetingRepository.findById(fanMeetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FanMeeting not found with id: " + fanMeetingId));

        String captureUrl = awsS3Service.uploadFile(file);

        Capture capture = Capture.builder()
                .captureName(file.getOriginalFilename())
                .captureUrl(captureUrl)
                .fanMeeting(fanMeeting)
                .fan(fan)
                .build();

        captureRepository.save(capture);
        return new ResponseEntity<>(new Message("캡쳐 저장(s3에) 완료!!", captureUrl), HttpStatus.OK);
    }

    // fan이 찍은 모든 capture 조회
    @Transactional
    public ResponseEntity<Message> getAllCapture(HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        List<Capture> captures = captureRepository.findByFan(fan);

        List<CaptureDto> captureDtos = captures.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new Message("내가 작성한 모든 캡쳐!!", captureDtos), HttpStatus.OK);
    }

    // fan이 fanMeeting에서 찍은 모든 capture 조회
    @Transactional
    public ResponseEntity<Message> getMyCapture(Long fanMeetingId, HttpServletRequest request) {
        claims = jwtUtil.getClaims(request);
        Fan fan = userUtils.getFan(claims.getSubject());

        FanMeeting fanMeeting = fanMeetingRepository.findById(fanMeetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FanMeeting not found with id: " + fanMeetingId));

        List<Capture> captures = captureRepository.findByFanIdAndFanMeetingId(fan.getId(), fanMeetingId);

        List<CaptureDto> captureDtos = captures.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new Message("내가 " + fanMeeting.getFanMeetingName() + "에서 작성한 모든 캡쳐!!", captureDtos), HttpStatus.OK);
    }

    // CaptureService 내부에 추가
    private CaptureDto mapToDto(Capture capture) {
        return CaptureDto.builder()
                .captureId(capture.getId())
                .captureName(capture.getCaptureName())
                .fanMeetingId(capture.getFanMeeting().getId())
                .captureUrl(capture.getCaptureUrl())
                .fanId(capture.getFan().getId())
                .build();
    }

}
