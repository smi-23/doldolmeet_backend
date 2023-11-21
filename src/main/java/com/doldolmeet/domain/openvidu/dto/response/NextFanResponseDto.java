package com.doldolmeet.domain.openvidu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextFanResponseDto {
    private String username;
    private String waitRoomId;
    private String connectionId;
}
