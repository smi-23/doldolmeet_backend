package com.doldolmeet.domain.fanMeeting.entity;

import com.doldolmeet.domain.users.idol.entity.Idol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class IdolToFanMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    // 팬
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_id", referencedColumnName = "id")
    private Idol idol;

    // 팬미팅
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_meeting_id", referencedColumnName = "id")
    private FanMeeting fanMeeting;

    // 신청 상태
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private FanMeetingApplyStatus fanMeetingApplyStatus;

    public void setUserAndFanMeeting(Idol idol, FanMeeting fanMeeting) {
        this.idol = idol;
        this.fanMeeting = fanMeeting;
    }
}
