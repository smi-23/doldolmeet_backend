package com.doldolmeet.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NOT_ADMIN(HttpStatus.BAD_REQUEST, "관리자 회원이 아닙니다."),
    NOT_USER(HttpStatus.BAD_REQUEST, "가입된 아이디가 아닙니다."),
    ALREADY_JOIN_USER(HttpStatus.BAD_REQUEST, "이미 가입한 회원입니다. 로그인해주세요"),
    DUPLICATE_IDENTIFIER(HttpStatus.BAD_REQUEST, "중복된 이메일 입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "중복된 닉네임 입니다."),
    DUPLICATE_GITHUB_ID(HttpStatus.BAD_REQUEST, "중복된 깃허브 아이디 입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "잘못된 아이디 입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호 입니다."),
    PLZ_INPUT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요"),
    PLZ_INPUT_CONTENT(HttpStatus.BAD_REQUEST, "내용을 입력해주세요"),
    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "이메일을 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    MOGAKKO_IS_FULL(HttpStatus.BAD_REQUEST, "모각코 인원이 마감되었습니다."),
    ALREADY_OUT_MEMBER(HttpStatus.BAD_REQUEST, "이미 방에서 나간 유저 입니다."),
    ALREADY_ENTER_MEMBER(HttpStatus.BAD_REQUEST, "이미 입장한 유저 입니다."),
    NOT_MOGAKKO_MEMBER(HttpStatus.BAD_REQUEST, "방에 있는 멤버가 아닙니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MOGAKKO_NOT_FOUND(HttpStatus.NOT_FOUND, "모각코 방이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없습니다."),
    CANNOT_FOUND_FRIEND(HttpStatus.NOT_FOUND, "친구를 찾을 수 없습니다."),
    NOT_SUPPORTED_SOCIALTYPE(HttpStatus.NOT_FOUND, "지원하지 않는 소셜로그인 입니다."),
    NOTIFICATION_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 발송에 실패했습니다"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메세지가 존재하지 않습니다."),
    INVALID_FRIEND_CODE(HttpStatus.BAD_REQUEST, "친구 코드를 정확하게 입력해주세요."),
    CANNOT_REQUEST(HttpStatus.BAD_REQUEST, "자신에게 요청을 보낼 수 없습니다."),
    PLZ_INPUT(HttpStatus.BAD_REQUEST, "입력해주세요."),
    PLZ_INPUT_REASON_OF_REPORT(HttpStatus.BAD_REQUEST, "신고 이유를 입력해주세요."),
    GITHUB_TOKEN_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GitHub 에서 엑세스 토큰을 가져오지 못했습니다."),
    FAILED_TO_GET_USERINFO(HttpStatus.BAD_REQUEST, "GitHub로 부터 정보를 받아오지 못했습니다."),
    USER_MISMATCH_ERROR(HttpStatus.BAD_REQUEST, "사용자와 해당 메세지의 발신자 혹은 수신자가 일치하지 않습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 팀을 찾을 수 없습니다."),
    IDOL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 아이돌을 찾을 수 없습니다."),
    FANMEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 팬미팅을  찾을 수 없습니다."),
    //인증
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    //serverError
    INTERNAL_SERER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");

    private final HttpStatus httpStatus;
    private final String data;
}
