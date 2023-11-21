package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.users.fan.entity.Fan;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FanToFanMeetingRepository extends JpaRepository<FanToFanMeeting, Long> {
    // 팬미팅 대기 순서
    @Query("SELECT ftm.orderNumber FROM FanToFanMeeting ftm WHERE ftm.fan = :fan and ftm.fanMeeting = :fanMeeting")
    Optional<Integer> waitingOrder(@Param("fan") Fan fan, @Param("fanMeeting") FanMeeting fanMeeting);
}


