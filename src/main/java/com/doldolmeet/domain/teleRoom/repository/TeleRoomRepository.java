package com.doldolmeet.domain.teleRoom.repository;

import com.doldolmeet.domain.teleRoom.entity.TeleRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeleRoomRepository extends JpaRepository<TeleRoom, Long> {
    TeleRoom findByRoomId(String roomId);
}
