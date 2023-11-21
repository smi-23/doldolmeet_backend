package com.doldolmeet.domain.users.idol.repository;

import com.doldolmeet.domain.users.idol.entity.IdolConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IdolConnectionRepository extends JpaRepository<IdolConnection, Long>{
    @Query("SELECT ic FROM IdolConnection ic WHERE ic.idol.id = :idol_id AND ic.fan.id = :fan_id")
    Optional<IdolConnection> findIdolConnectionBy(Long idol_id, Long fan_id);
}



