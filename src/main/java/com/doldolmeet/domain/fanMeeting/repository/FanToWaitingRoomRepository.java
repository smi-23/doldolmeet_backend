package com.doldolmeet.domain.fanMeeting.repository;

import com.doldolmeet.domain.fanMeeting.entity.FanMeeting;
import com.doldolmeet.domain.fanMeeting.entity.FanToFanMeeting;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.idol.entity.IdolConnection;
import com.doldolmeet.domain.users.idol.entity.Idol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FanToWaitingRoomRepository extends JpaRepository<IdolConnection, Long> {
    // Fan중에서 orderNumber가 가장 작은 Fan을 찾는다.
    // SELECT fan_id FROM fan_to_fan_meeting where fan_id in (SELECT fan_id FROM idol_connection where id = 1) order by order_number asc limit 1;

    @Query("SELECT ftm FROM FanToFanMeeting ftm WHERE ftm in (SELECT subftm.fan FROM IdolConnection subftm WHERE subftm.idol =:idol) order by ftm.orderNumber asc limit 1")
    Optional<FanToFanMeeting> findFirstByWaitingRoomSession(@Param("idol") Idol idol);


}
