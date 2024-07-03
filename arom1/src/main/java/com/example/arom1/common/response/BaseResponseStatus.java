package com.example.arom1.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {

    /**
     * 성공 코드 2xx
     * 코드의 원활한 이해을 위해 code는 숫자가 아닌 아래 형태로 입력해주세요.
     */
    SUCCESS(true, HttpStatus.OK.value(), "요청에 성공하였습니다."),

    // 4xx : client error
    EXIST_EMAIL(false, HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다."),
    FAIL_LOGIN(false, HttpStatus.NO_CONTENT.value(), "로그인에 실패했습니다."),
    NON_EXIST_USER(false, HttpStatus.NO_CONTENT.value(), "존재하지 않는 회원입니다."),
    PASSWORD_ERROR(false, HttpStatus.NO_CONTENT.value(), "비밀번호가 틀렸습니다."),
    HTTP_METHOD_ERROR(false, HttpStatus.FORBIDDEN.value(), "http 메서드가 올바르지 않습니다."),

    // 5xx : server error
    DATABASE_INSERT_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스 입력에 실패했습니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

    /**
     * isSuccess : 요청의 성공 또는 실패
     * code : Http Status Code
     * message : 설명
     */
    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}