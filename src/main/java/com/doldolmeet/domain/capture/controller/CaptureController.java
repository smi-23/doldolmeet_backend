package com.doldolmeet.domain.capture.controller;

import com.doldolmeet.domain.capture.service.CaptureService;
import com.doldolmeet.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/captures")
public class CaptureController {
    private final CaptureService captureService;

    @PostMapping("/upload/{fanId}/{fanMeetingId}")
    public ResponseEntity<Message> uploadCapture(@PathVariable Long fanId, @PathVariable Long fanMeetingId, @RequestPart MultipartFile file) {
        return captureService.uploadCapture(fanId, fanMeetingId, file);
    }
}
