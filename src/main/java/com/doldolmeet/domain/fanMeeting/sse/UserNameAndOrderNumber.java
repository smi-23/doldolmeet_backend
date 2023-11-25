package com.doldolmeet.domain.fanMeeting.sse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserNameAndOrderNumber {
    private String username;
    private Long orderNumber;
}
