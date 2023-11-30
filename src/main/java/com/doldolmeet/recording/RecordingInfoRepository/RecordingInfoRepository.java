package com.doldolmeet.recording.RecordingInfoRepository;

import com.doldolmeet.domain.memo.entity.Memo;
import com.doldolmeet.recording.entity.RecordingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordingInfoRepository extends JpaRepository<RecordingInfo, Long> {

    // FanMeetingId, FanId, IdolId로 조회
    Optional<RecordingInfo> findByFanMeetingIdAndFanIdAndIdolId(Long fanMeetingId, Long fanId, Long idolId);
}
