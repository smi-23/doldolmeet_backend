package com.doldolmeet.domain.fanMeeting.dto.response;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingApplyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdolToFanMeetingResponseDto {
    private Long id;
    private Long idolId;
    private Long fanMeetingId;
    private FanMeetingApplyStatus fanMeetingApplyStatus;
}
