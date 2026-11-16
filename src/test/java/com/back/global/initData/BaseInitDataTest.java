package com.back.global.initData;

import com.back.domain.post.repository.PostRepository;
import com.back.domain.postComment.repository.PostCommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BaseInitDataTest {
    @Autowired
    private BaseInitData baseInitData;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Test
    @DisplayName("애플리케이션 시작 시 샘플 데이터(글 3개, 댓글 5개)가 관계에 맞게 생성된다")
    void sampleDataCreated() {
        assertThat(postRepository.count()).isEqualTo(3);
        assertThat(postCommentRepository.count()).isEqualTo(5);

        assertThat(postCommentRepository.findByPostIdOrderByIdAsc(1)).hasSize(3);
        assertThat(postCommentRepository.findByPostIdOrderByIdAsc(2)).hasSize(2);
        assertThat(postCommentRepository.findByPostIdOrderByIdAsc(3)).isEmpty();
    }

    @Test
    @DisplayName("샘플 데이터 생성을 다시 실행해도 중복으로 쌓이지 않는다")
    void sampleDataNotDuplicated() {
        baseInitData.makeSampleData();
        baseInitData.makeSampleData();

        assertThat(postRepository.count()).isEqualTo(3);
        assertThat(postCommentRepository.count()).isEqualTo(5);
    }
}
