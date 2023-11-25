package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeetingRoomOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FanMeetingRoomOrderRepository extends JpaRepository<FanMeetingRoomOrder, Long> {
    @Query("select f from FanMeetingRoomOrder f join fetch f.fanMeeting where f.fanMeeting.id = :fanMeetingId")
    List<FanMeetingRoomOrder> findByFanMeetingId(@Param("fanMeetingId") Long fanMeetingId);
}
