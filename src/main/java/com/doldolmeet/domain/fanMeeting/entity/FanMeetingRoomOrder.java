package com.doldolmeet.domain.fanMeeting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FanMeetingRoomOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column
    private String currentRoom; // current sessionId

    @Column
    private String nextRoom;  // next sessionId

    @Column
    private String nickname;

    @Column
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_meeting_id", nullable = false)
    private FanMeeting fanMeeting;
}
