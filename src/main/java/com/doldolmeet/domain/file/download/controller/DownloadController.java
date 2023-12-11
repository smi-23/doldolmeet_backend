package com.doldolmeet.domain.file.download.controller;

import com.doldolmeet.domain.file.download.service.DownloadService;
import com.doldolmeet.s3.service.AwsS3Service;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@ApiResponse
public class DownloadController {
    private final DownloadService downloadService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFileByUrl(@RequestParam String fileUrl) {
        try {
            return downloadService.downloadFileByUrl(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error downloading file from URL: " + fileUrl).getBytes());
        }
    }

    @GetMapping("/download/basic")
    public ResponseEntity<byte[]> basicDownloadFileByUrl(@RequestParam String fileUrl) {
        try {
            return downloadService.basicDownloadFileByUrl(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error downloading file from URL: " + fileUrl).getBytes());
        }
    }

    @GetMapping("/download/parallel")
    public ResponseEntity<byte[]> parallelDownloadFileByUrl(@RequestParam String fileUrl) {
        try {
            return downloadService.parallelDownloadFileByUrl(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error downloading file from URL: " + fileUrl).getBytes());
        }
    }
}
