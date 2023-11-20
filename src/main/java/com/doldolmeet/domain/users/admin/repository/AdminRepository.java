package com.doldolmeet.domain.users.admin.repository;

import com.doldolmeet.domain.users.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserCommonsUsername(String username);
}
