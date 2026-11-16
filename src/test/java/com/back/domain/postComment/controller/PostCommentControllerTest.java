package com.back.domain.postComment.controller;

import com.back.domain.postComment.entity.PostComment;
import com.back.domain.postComment.repository.PostCommentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 댓글 API 통합 테스트.
 * 샘플 데이터 : 1번 글 - 댓글 1, 2, 3 / 2번 글 - 댓글 4, 5 / 3번 글 - 댓글 없음
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("댓글 목록 조회 : 1번 글의 댓글 3개, id 오름차순 PostCommentDto 배열")
    void getItems() throws Exception {
        mvc.perform(get("/api/v1/posts/1/comments"))
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[0].postId").value(1))
                .andExpect(jsonPath("$[0].content").value("댓글 1-1"))
                .andExpect(jsonPath("$[0].createDate").exists())
                .andExpect(jsonPath("$[0].modifyDate").exists())
                .andExpect(jsonPath("$[0].post").doesNotExist());
    }

    @Test
    @DisplayName("댓글 목록 조회 : 댓글이 없는 글은 빈 배열")
    void getItems_empty() throws Exception {
        mvc.perform(get("/api/v1/posts/3/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("없는 글의 댓글 목록 조회 : 404 + RsData(404-1)")
    void getItems_postNotFound() throws Exception {
        mvc.perform(get("/api/v1/posts/999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("999번 글은 존재하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 단건 조회 : PostCommentDto 의 모든 필드")
    void getItem() throws Exception {
        PostComment postComment = postCommentRepository.findById(2).get();

        mvc.perform(get("/api/v1/posts/1/comments/2"))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.content").value(postComment.getContent()))
                .andExpect(jsonPath("$.createDate").exists())
                .andExpect(jsonPath("$.modifyDate").exists())
                .andExpect(jsonPath("$.post").doesNotExist());
    }

    @Test
    @DisplayName("없는 댓글 단건 조회 : 404 + RsData(404-2)")
    void getItem_notFound() throws Exception {
        mvc.perform(get("/api/v1/posts/1/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-2"))
                .andExpect(jsonPath("$.msg").value("1번 글에 999번 댓글은 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("다른 글의 댓글 조회 : 4번 댓글은 2번 글 소속이므로 1번 글 주소로는 404")
    void getItem_otherPostsComment() throws Exception {
        mvc.perform(get("/api/v1/posts/1/comments/4"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-2"));

        mvc.perform(get("/api/v1/posts/2/comments/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(2));
    }

    @Test
    @DisplayName("댓글 삭제 : 200 + RsData<Void>(200-1), 재조회하면 404, DB 에서도 사라진다")
    void delete() throws Exception {
        mvc.perform(get("/api/v1/posts/1/comments/2/delete"))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("2번 댓글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.statusCode").doesNotExist());

        // 삭제 후 재조회
        mvc.perform(get("/api/v1/posts/1/comments/2"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/posts/1/comments"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3));

        // DB 직접 확인
        em.flush();
        em.clear();
        assertThat(postCommentRepository.findById(2)).isEmpty();
        assertThat(postCommentRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("다른 글의 댓글 삭제 시도 : 404, 해당 댓글은 지워지지 않는다")
    void delete_otherPostsComment() throws Exception {
        mvc.perform(get("/api/v1/posts/1/comments/4/delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-2"));

        em.flush();
        em.clear();
        assertThat(postCommentRepository.findById(4)).isPresent();
        assertThat(postCommentRepository.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("없는 글의 댓글 삭제 시도 : 404-1")
    void delete_postNotFound() throws Exception {
        mvc.perform(get("/api/v1/posts/999/comments/1/delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }
}
