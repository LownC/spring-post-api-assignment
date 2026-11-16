package com.back.global.rsData;

import com.back.domain.post.dto.PostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RsData 단위 테스트. 스프링 없이 순수 자바로 실행된다(빠르다).
 */
class RsDataTest {
    @Test
    @DisplayName("resultCode 앞부분이 statusCode 가 된다")
    void statusCodeFromResultCode() {
        assertThat(new RsData<Void>("200-1", "성공").statusCode()).isEqualTo(200);
        assertThat(new RsData<Void>("404-2", "없음").statusCode()).isEqualTo(404);
        assertThat(new RsData<Void>("400-1", "잘못된 요청").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("RsData<Void> 는 data 가 항상 null 이다")
    void voidData() {
        RsData<Void> rsData = new RsData<>("200-1", "1번 글이 삭제되었습니다.");

        assertThat(rsData.resultCode()).isEqualTo("200-1");
        assertThat(rsData.msg()).isEqualTo("1번 글이 삭제되었습니다.");
        assertThat(rsData.data()).isNull();
    }

    @Test
    @DisplayName("제네릭 덕분에 data 를 형변환 없이 원래 타입(PostDto)으로 꺼낼 수 있다")
    void genericData() {
        PostDto postDto = new PostDto(1, LocalDateTime.now(), LocalDateTime.now(), "제목", "내용");
        RsData<PostDto> rsData = new RsData<>("200-1", "성공", postDto);

        PostDto data = rsData.data(); // (PostDto) 캐스팅이 필요 없다.

        assertThat(data.title()).isEqualTo("제목");
        assertThat(data).isSameAs(postDto);
    }
}
