package com.doldolmeet.domain.video.service;

import com.doldolmeet.domain.video.dto.VideoDto;
import com.doldolmeet.domain.video.entity.Video;
import com.doldolmeet.domain.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public List<VideoDto> getAllVideos() {
        List<Video> videos = videoRepository.findAll();
        return videos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private VideoDto convertToDto(Video video) {
        return VideoDto.builder()
                .videoId(video.getId())
                .videoName(video.getVideoName())
                .videoUrl(video.getVideoUrl())
                .fanMeetingId(video.getFanMeeting().getId())
                .build();
    }
}