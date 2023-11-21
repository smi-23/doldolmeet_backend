package com.doldolmeet.domain.waitRoom.repository;

import com.doldolmeet.domain.waitRoom.entity.WaitRoomFan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WaitRoomFanRepository extends JpaRepository<WaitRoomFan, Long> {
    @Query("select w from WaitRoomFan w join fetch w.waitRoom where w.fan.id = :fanId and w.waitRoom.fanMeeting.id = :fanMeetingId")
    Optional<WaitRoomFan> findByFanIdAndWaitRoomId(Long fanId, Long fanMeetingId);
}
