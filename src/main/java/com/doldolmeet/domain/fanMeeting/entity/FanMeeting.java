package com.doldolmeet.domain.fanMeeting.entity;

import com.doldolmeet.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FanMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // 그룹에 대한 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 팬미팅 시작 시간 및 끝 시간
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // 팬미팅 이름
    @Column(nullable = false)
    private String fanMeetingName;

    // 팬미팅 이미지
    @Column
    private String fanMeetingImgUrl;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String roomId;

    @OneToMany(mappedBy = "fanMeeting", cascade = CascadeType.ALL)
    private List<FanToFanMeeting> fanToFanMeetings = new ArrayList<>();

}
