package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.users.fan.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FanMeetingRepository extends JpaRepository<FanMeeting, Long> {
    List<FanMeeting> findFanMeetingsByEndTimeAfter(LocalDateTime now);
    List<FanMeeting> findFanMeetingsByEndTimeBefore(LocalDateTime now);
    // 가장 이른 팬미팅 조회
    @Query("SELECT fm FROM FanMeeting fm WHERE fm.endTime > :currentTime ORDER BY fm.startTime ASC")
    Optional<FanMeeting> findEarliestFanMeeting(Instant currentTime);

    // Fan이 신청한 팬미팅 중에서 가장 이른 팬미팅 조회
    @Query("SELECT ftfm.fanMeeting FROM FanToFanMeeting ftfm " +
            "WHERE ftfm.fan = :fan " +
            "AND ftfm.fanMeeting.endTime > :currentTime " +
            "ORDER BY ftfm.fanMeeting.startTime ASC")
    Optional<FanMeeting> findEarliestFanMeetingByFan(@Param("fan") Fan fan, @Param("currentTime") LocalDateTime currentTime);
}
