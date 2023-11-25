package com.doldolmeet.domain.memo.controller;

import com.doldolmeet.domain.memo.dto.MemoRequestDto;
import com.doldolmeet.domain.memo.dto.MemoResponseDto;
import com.doldolmeet.domain.memo.service.MemoService;
import com.doldolmeet.utils.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// MemoController.java

@RestController
@RequiredArgsConstructor
public class MemoController {
    private final MemoService memoService;

    // 메모 생성
    @PostMapping("/memos")
    public ResponseEntity<Message> createMemo(@RequestBody @Valid MemoRequestDto requestDto, HttpServletRequest request) {
        return memoService.createMemo(requestDto, request);
    }

    // 선택한 메모 조회
    @GetMapping("/memos/{id}")
    public ResponseEntity<?> getMemo(@PathVariable Long id) {
        return memoService.getMemo(id);
    }

    // 메모 수정
    @PutMapping("/memos/{id}")
    public ResponseEntity<Message> updateMemo(@PathVariable Long id, @RequestBody @Valid MemoRequestDto requestDto) {
        return memoService.updateMemo(id, requestDto);
    }

    // 메모 삭제
    @DeleteMapping("/memos/{id}")
    public ResponseEntity<Message> deleteMemo(@PathVariable Long id) {
        return memoService.deleteMemo(id);
    }
}

