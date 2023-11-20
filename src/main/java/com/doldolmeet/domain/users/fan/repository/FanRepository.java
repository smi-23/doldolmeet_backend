package com.doldolmeet.domain.users.fan.repository;

import com.doldolmeet.domain.users.fan.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FanRepository extends JpaRepository<Fan, Long> {
    Optional<Fan> findByUserCommonsUsername(String username);
}
