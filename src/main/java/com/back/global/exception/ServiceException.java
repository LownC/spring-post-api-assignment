package com.back.global.exception;

import com.back.global.rsData.RsData;
import lombok.Getter;

/**
 * 서비스 로직에서 "요청을 처리할 수 없다"는 것을 알리는 예외.
 * resultCode 와 msg 를 가지고 있어서, GlobalExceptionHandler 가 그대로 RsData 응답으로 바꿔준다.
 * <p>
 * 예) throw new ServiceException("404-1", "999번 글은 존재하지 않습니다.");
 */
@Getter
public class ServiceException extends RuntimeException {
    private final String resultCode;
    private final String msg;

    public ServiceException(String resultCode, String msg) {
        super(resultCode + " : " + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public RsData<Void> toRsData() {
        return new RsData<>(resultCode, msg);
    }
}
