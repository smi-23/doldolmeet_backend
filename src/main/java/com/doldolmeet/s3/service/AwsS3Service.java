package com.doldolmeet.s3.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private  String bucket;

    private final AmazonS3 amazonS3;

    // 20MB를 바이트 단위로 설정
    private static final int SOME_THRESHOLD_SIZE = 20 * 1024 * 1024;

    // 캡쳐나 비디오등등 파일을 업로드할 때 공통으로 쓸 함수 현재 인자로 multipartfile이 들어가는 이유는 메소드들을 사용하기 위해서
    public String uploadFile(MultipartFile file) {
        String fileName = createFileName(file.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        return fileName;
    }

    public List<String> uploadMultipartFile(List<MultipartFile> multipartFile) {
        List<String> fileNameList = new ArrayList<>();

        // forEach 구문을 통해 multipartFile로 넘어온 파일들 하나씩 fileNameList에 추가
        multipartFile.forEach(file -> {
            String fileName = createFileName(file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try(InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch(IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
            }
            fileNameList.add(fileName);
        });

        return fileNameList;
    }

    /**
     * AWS S3 버킷에서 파일을 다운로드하는 메서드.
     * download 속도는 느리지만 transfer start 속도는 빠름
     * @param fileName 다운로드할 파일의 고유한 파일명
     * @return ResponseEntity<Resource> 객체로 다운로드된 파일과 관련된 정보를 포함하는 응답
     */
    public ResponseEntity<Resource> downloadFile(String fileName) {
        // AWS S3에서 파일 객체 가져오기
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, fileName));
        // S3ObjectInputStream을 이용하여 Resource 생성
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        Resource resource = new InputStreamResource(s3ObjectInputStream);
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(s3Object.getObjectMetadata().getContentLength());
        headers.setContentDispositionFormData("attachment", fileName);
        // ResponseEntity를 사용하여 파일 다운로드 응답 생성
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // 용량에 따라 다른방식으로 처리하는 download api / download 속도는 빠르지만 transfer start 속도는 느림
    public ResponseEntity<byte[]> fastDownloadFile(String fileName) throws IOException {
        // AWS S3에서 파일 객체 가져오기
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, fileName));

        // S3ObjectInputStream을 이용하여 버퍼링된 InputStream 생성
        try (InputStream inputStream = new BufferedInputStream(s3Object.getObjectContent())) {
            byte[] bytes = IOUtils.toByteArray(inputStream);

            // 파일 크기 체크
            if (bytes.length > SOME_THRESHOLD_SIZE) {
                // 파일이 일정 크기 이상이면 스트리밍 방식으로 처리
                return ResponseEntity
                        .status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "\"")
                        .body(bytes);
            }

            // 파일이 일정 크기 미만이면 전체 파일을 응답
            String storedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentLength(bytes.length);
            httpHeaders.setContentDispositionFormData("attachment", storedFileName);

            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        }
    }

    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    ///////////////////
    // help function//
    /////////////////
    private String createFileName(String fileName) {
        // 먼저 파일 업로드 시, 파일명을 난수화하기 위해 random으로 돌립니다.
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) {
        // file 형식이 잘못된 경우를 확인하기 위해 만들어진 로직이며, 파일 타입과 상관없이 업로드할 수 있게 하기 위해 .의 존재 유무만 판단하였습니다.
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }
}
