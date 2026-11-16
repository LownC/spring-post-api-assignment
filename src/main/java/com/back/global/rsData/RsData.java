package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 조회가 아닌 요청(삭제 등)이나 실패한 요청에 대한 공통 응답 형식.
 * <pre>
 * {
 *   "resultCode": "200-1",   // "HTTP상태코드-세부코드"
 *   "msg": "1번 글이 삭제되었습니다.",
 *   "data": null             // 추가로 돌려줄 데이터가 있으면 여기에 담는다.
 * }
 * </pre>
 *
 * @param <T> data 필드의 타입. 담을 데이터가 없으면 {@code RsData<Void>} 로 쓴다.
 *            (Void 는 "값이 없음"을 뜻하는 타입이라 data 에는 null 만 들어갈 수 있다.)
 */
public record RsData<T>(
        String resultCode,
        @JsonIgnore int statusCode, // HTTP 응답코드 설정용. JSON 에는 포함하지 않는다.
        String msg,
        T data
) {
    /**
     * data 없이 resultCode, msg 만 담는 생성자.
     */
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    /**
     * resultCode 의 앞부분("200-1" → 200)을 HTTP 상태코드(statusCode)로 사용한다.
     * 이렇게 하면 응답 본문의 resultCode 와 실제 HTTP 상태코드가 항상 일치한다.
     */
    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }
}
