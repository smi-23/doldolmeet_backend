package com.doldolmeet.s3.controller;

import com.doldolmeet.s3.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    /**
     * 최적화 시켜야함 지금 코드 개판임
     * 팬미팅에서 얻은 녹취록이나, 영상이나, 사진을 업로드
     * Amazon S3에 파일 업로드
     * @return 성공 시 200 Success와 함께 업로드 된 파일의 파일명 리스트 반환
     */
    @PostMapping("/file/video/{fanMeetingId}")
    public ResponseEntity<List<String>> uploadFileVideo(@PathVariable Long fanMeetingId, @RequestPart List<MultipartFile> multipartFile) {
        // Upload logic
        List<String> uploadedFiles = awsS3Service.uploadFileVideo(multipartFile, fanMeetingId);

        // Return success response
        return ResponseEntity.ok(uploadedFiles);
    }

    // 팬미팅 만들기 전 이미지 업로드
    @PostMapping("/file")
    public ResponseEntity<List<String>> uploadFile(@RequestPart List<MultipartFile> multipartFile) {
        // Upload logic
        List<String> uploadedFiles = awsS3Service.uploadMultipartFile(multipartFile);

        // Return success response
        return ResponseEntity.ok(uploadedFiles);
    }

    /**
     * Amazon S3에서 파일 다운로드
     * @param fileName 다운로드할 파일의 파일명
     * @return 성공 시 200 Success와 함께 파일 다운로드 응답 반환
     * ex) http://localhost:8080/s3/file/download?fileName=c11111.mp4
     */
    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        // Download logic
        return awsS3Service.downloadFile(fileName);
    }

    /**
     * Amazon S3에 업로드 된 파일을 삭제
     * @return 성공 시 200 Success
     */
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestParam String fileName) {
        // Delete logic
        awsS3Service.deleteFile(fileName);

        // Return success response with no content
        return ResponseEntity.noContent().build();
    }
}
