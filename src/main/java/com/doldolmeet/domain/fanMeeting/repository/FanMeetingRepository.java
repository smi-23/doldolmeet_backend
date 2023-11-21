package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.users.fan.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface FanMeetingRepository extends JpaRepository<FanMeeting, Long> {
    List<FanMeeting> findFanMeetingsByEndTimeAfter(LocalDateTime now);
    List<FanMeeting> findFanMeetingsByEndTimeBefore(LocalDateTime now);

    // 가장 이른 팬미팅 조회
    @Query("SELECT ftfm.fanMeeting FROM FanToFanMeeting ftfm " +
            "WHERE ftfm.fan = :fan " +
            "AND ftfm.fanMeeting.startTime >= :midNightTime " +
            "AND ftfm.fanMeeting.endTime > :currentTime " +
            "ORDER BY ftfm.fanMeeting.endTime DESC LIMIT 1")
    Optional<FanMeeting> findTodayLatestFanMeetingByFan(@Param("fan") Fan fan, @Param("midNightTime") LocalDateTime midNightTime, @Param("currentTime") LocalDateTime currentTime);
}
