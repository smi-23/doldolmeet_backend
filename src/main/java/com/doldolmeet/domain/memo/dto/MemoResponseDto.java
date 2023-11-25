package com.doldolmeet.domain.memo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoResponseDto {
    private Long id;
    private String contents;

    public MemoResponseDto(Long id, String contents) {
        this.id = id;
        this.contents = contents;
    }
}
