package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.users.fan.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FanToFanMeetingRepository extends JpaRepository<FanToFanMeeting, Long> {
    Optional<FanToFanMeeting> findByFanAndFanMeeting(Fan fan, FanMeeting fanMeeting);
}
