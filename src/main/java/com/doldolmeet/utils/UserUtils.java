package com.doldolmeet.utils;

import com.doldolmeet.domain.commons.Role;
import com.doldolmeet.domain.users.admin.entity.Admin;
import com.doldolmeet.domain.users.admin.repository.AdminRepository;
import com.doldolmeet.domain.users.fan.entity.Fan;
import com.doldolmeet.domain.users.fan.repository.FanRepository;
import com.doldolmeet.domain.users.idol.entity.Idol;
import com.doldolmeet.domain.users.idol.repository.IdolRepository;
import com.doldolmeet.exception.CustomException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.doldolmeet.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class UserUtils {
    private final AdminRepository adminRepository;
    private final FanRepository fanRepository;
    private final IdolRepository idolRepository;

    public void checkIfUserExist(Claims claims) {
        String username = claims.getSubject();

        Optional<Fan> fan = fanRepository.findByUserCommonsUsername(username);
        Optional<Admin> admin = adminRepository.findByUserCommonsUsername(username);
        Optional<Idol> idol = idolRepository.findByUserCommonsUsername(username);

        if (!fan.isPresent() && !admin.isPresent() && !idol.isPresent()) {
            throw new CustomException(NOT_USER);
        }
    }


    public void checkIfAdmin(Claims claims) {
        String role = (String)claims.get("auth");
        Optional<Admin> admin = adminRepository.findByUserCommonsUsername(claims.getSubject());

        if (!admin.isPresent()) {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    public Fan getFan(String username) {
        Optional<Fan> fan = fanRepository.findByUserCommonsUsername(username);

        if (!fan.isPresent()) {
            throw new CustomException(NOT_USER);
        }

        return fan.get();
    }

    public Idol getIdol(String username) {
        Optional<Idol> idol = idolRepository.findByUserCommonsUsername(username);

        if (!idol.isPresent()) {
            throw new CustomException(NOT_USER);
        }

        return idol.get();
    }
}
