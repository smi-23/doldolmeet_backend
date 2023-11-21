package com.doldolmeet.domain.video.controller;

import com.doldolmeet.domain.video.dto.VideoDto;
import com.doldolmeet.domain.video.service.VideoService;
import com.doldolmeet.s3.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    /**
     * 비디오 전체 리스트 조회
     * @return 성공 시 200 Success와 함께 비디오 리스트 반환
     */
    @GetMapping("/")
    public ResponseEntity<List<VideoDto>> getAllVideos() {
        List<VideoDto> videoList = videoService.getAllVideos();
        return ResponseEntity.ok(videoList);
    }
}
