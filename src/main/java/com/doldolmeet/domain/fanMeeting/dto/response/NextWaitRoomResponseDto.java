package com.doldolmeet.domain.fanMeeting.dto.response;

import com.doldolmeet.domain.fanMeeting.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWaitRoomResponseDto {
    private String roomId;
    private RoomType roomType;
}
