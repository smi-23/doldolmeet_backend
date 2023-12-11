package com.doldolmeet.domain.file.download.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private final RestTemplate restTemplate;

    private static final int SOME_THRESHOLD_SIZE = 20 * 1024 * 1024;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Adjust the number of threads as needed

    // 기본 다운로드
    public ResponseEntity<byte[]> basicDownloadFileByUrl(String fileUrl) throws IOException {
        byte[] fileContent = restTemplate.getForObject(fileUrl, byte[].class);

        // ResponseEntity를 생성하여 반환
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=\"" + fileUrl + "\"")
                .body(fileContent);
    }

    // 병렬 다운로드
    public ResponseEntity<byte[]> parallelDownloadFileByUrl(String fileUrl) throws IOException {
        byte[] fileContent = downloadFileContentInParallel(fileUrl);

        // ResponseEntity를 생성하여 반환
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=\"" + fileUrl + "\"")
                .body(fileContent);
    }

    public ResponseEntity<byte[]> downloadFileByUrl(String fileUrl) throws IOException {
        byte[] fileContent = restTemplate.getForObject(fileUrl, byte[].class);

        // 파일 크기 체크 등 추가 로직이 필요하다면 여기에 추가

        // ResponseEntity를 생성하여 반환
        // 파일이 일정 크기 초과이면 부분 응답 전송
        if (fileContent.length > SOME_THRESHOLD_SIZE) {
            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileUrl, "UTF-8").replaceAll("\\+", "%20") + "\"")
                    .body(fileContent);
        }
        // 파일이 일정 크기 미만이면 전체 파일을 응답
        String storedFileName = URLEncoder.encode(fileUrl, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(fileContent.length);
        httpHeaders.setContentDispositionFormData("attachment", storedFileName);

        return new ResponseEntity<>(fileContent, httpHeaders, HttpStatus.OK);
    }

    ///////////////////////////
    //////helper function//////
    ///////////////////////////

    private byte[] downloadFileContentInParallel(String fileUrl) throws IOException {
        CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(fileUrl, byte[].class), executorService);

        try {
            return future.get(); // Blocking until the download is complete
        } catch (Exception e) {
            throw new IOException("Error downloading file", e);
        }
    }
}
