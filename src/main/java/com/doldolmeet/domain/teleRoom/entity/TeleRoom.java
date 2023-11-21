package com.doldolmeet.domain.teleRoom.entity;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeleRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_meeting_id")
    private FanMeeting fanMeeting;

    @Column
    private String roomId;

}
