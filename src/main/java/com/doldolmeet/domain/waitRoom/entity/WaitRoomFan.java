package com.doldolmeet.domain.waitRoom.entity;

import com.doldolmeet.domain.users.fan.entity.Fan;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitRoomFan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // 다음 화상채팅방 인덱스
    @Column(nullable = false)
    private Long nextTeleRoomIdx;

    // 다음 대기열방 인덱스
    @Column(nullable = false)
    private Long nextWaitRoomIdx;

    // 커넥션Id
    @Column
    private String connectionId;

    @Column
    private Long orderNumber;

    @Column
    private String currRoomId;

    @Column
    private String connectionToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wait_room_id")
    private WaitRoom waitRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_id")
    private Fan fan;
}
