package com.doldolmeet.domain.capture.controller;

import com.doldolmeet.domain.capture.service.CaptureService;
import com.doldolmeet.utils.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/captures")
public class CaptureController {
    private final CaptureService captureService;

    // Capture Upload to S3
    @PostMapping("/upload/{fanMeetingId}")
    public ResponseEntity<Message> uploadCapture(@PathVariable Long fanMeetingId, @RequestPart MultipartFile file, HttpServletRequest request) {
        return captureService.uploadCapture(fanMeetingId, file, request);
    }

    // fan이 찍은 모든 capture 조회
    @GetMapping("/")
    public ResponseEntity<Message> getAllCapture(HttpServletRequest request) {
        return captureService.getAllCapture(request);
    }

    // fan이 fanMeeting에서 찍은 모든 capture 조회
    @GetMapping("/{fanMeetingId}")
    public ResponseEntity<Message> getMyCaptrue(@PathVariable Long fanMeetingId, HttpServletRequest request) {
        return captureService.getMyCapture(fanMeetingId, request);
    }
}
