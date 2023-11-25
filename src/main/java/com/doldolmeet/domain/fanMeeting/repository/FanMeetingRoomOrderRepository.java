package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FanMeetingRoomOrderRepository extends JpaRepository<FanMeetingRoomOrder, Long> {
    List<FanMeetingRoomOrder> findByFanMeetingId(Long fanMeetingId);
}
