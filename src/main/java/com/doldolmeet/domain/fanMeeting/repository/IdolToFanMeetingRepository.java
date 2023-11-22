package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.IdolToFanMeeting;
import com.doldolmeet.domain.users.idol.entity.Idol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdolToFanMeetingRepository extends JpaRepository<IdolToFanMeeting, Long> {
    Optional<IdolToFanMeeting> findByIdolAndFanMeeting(Idol idol, FanMeeting fanMeeting);
}
