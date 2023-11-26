package com.doldolmeet.domain.capture.service;

import com.doldolmeet.domain.capture.entity.Capture;
import com.doldolmeet.domain.capture.repository.CaptureRepository;
import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.repository.FanMeetingRepository;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.fan.repository.FanRepository;
import com.doldolmeet.s3.service.AwsS3Service;
import com.doldolmeet.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CaptureService {
    private final AwsS3Service awsS3Service;
    private final FanMeetingRepository fanMeetingRepository;
    private final FanRepository fanRepository;
    private final CaptureRepository captureRepository;

    @Transactional
    public ResponseEntity<Message> uploadCapture(Long fanId, Long fanMeetingId, MultipartFile file) {
        Fan fan = fanRepository.findById(fanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fan not found with id: " + fanId));

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
}
