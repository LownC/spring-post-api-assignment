package com.back.global.exception;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 모든 컨트롤러에서 발생한 예외를 한 곳에서 받아서 RsData 형식의 JSON 으로 응답한다.
 * {@code @RestControllerAdvice} = 모든 @RestController 에 공통으로 적용되는 예외 처리기.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 서비스에서 던진 ServiceException → resultCode 에 맞는 HTTP 상태코드 + RsData 본문.
     * 예) "404-1" → HTTP 404
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException e) {
        RsData<Void> rsData = e.toRsData();

        return ResponseEntity
                .status(rsData.statusCode())
                .body(rsData);
    }

    /**
     * /api/v1/posts/abc 처럼 숫자 자리에 숫자가 아닌 값이 들어온 경우 → 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentTypeMismatchException e) {
        RsData<Void> rsData = new RsData<>(
                "400-1",
                "'%s' 값이 올바르지 않습니다.".formatted(e.getName())
        );

        return ResponseEntity
                .status(rsData.statusCode())
                .body(rsData);
    }
}
