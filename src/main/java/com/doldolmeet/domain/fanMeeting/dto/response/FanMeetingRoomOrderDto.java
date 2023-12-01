package com.doldolmeet.domain.fanMeeting.dto.response;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FanMeetingRoomOrderDto {
    private String currentRoom; // current sessionId
    private String type;
}
