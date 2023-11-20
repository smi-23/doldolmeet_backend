package com.doldolmeet.domain.guest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)[a-z\\d]{4,10}", message = "회원을 찾을 수 없습니다.")
    private String username;

    // 알파벳 대소문자(a~z, A~Z), 숫자(0~9)
    @NotBlank
    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,15}", message = "회원을 찾을 수 없습니다.")
    private String password;
}
