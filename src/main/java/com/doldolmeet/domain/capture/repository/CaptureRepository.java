package com.doldolmeet.domain.capture.repository;

import com.doldolmeet.domain.capture.entity.Capture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptureRepository extends JpaRepository<Capture, Long> {
}
