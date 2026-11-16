package com.back.domain.post.controller;

import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.postComment.repository.PostCommentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 게시글 API 통합 테스트.
 * <ul>
 *   <li>@SpringBootTest : 실제 애플리케이션과 똑같이 스프링을 전부 띄운다.</li>
 *   <li>@ActiveProfiles("test") : application-test.yaml(메모리 DB)을 사용한다. 개발용 DB(db_dev)는 건드리지 않는다.</li>
 *   <li>@AutoConfigureMockMvc : 실제 서버를 띄우지 않고 HTTP 요청을 흉내 내는 MockMvc 를 준비한다.</li>
 *   <li>@Transactional : 각 테스트가 끝나면 롤백한다. → 삭제 테스트를 해도 다음 테스트에는 샘플 데이터가 그대로 있다.</li>
 * </ul>
 * 샘플 데이터 : 글 1~3, 댓글 1~3(1번 글), 댓글 4~5(2번 글)
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("글 목록 조회 : id 오름차순 PostDto 배열, 엔티티 내부 필드(comments)는 노출되지 않는다")
    void getItems() throws Exception {
        ResultActions resultActions = mvc.perform(get("/api/v1/posts"));

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[0].title").value("제목 1"))
                .andExpect(jsonPath("$[0].content").value("내용 1"))
                .andExpect(jsonPath("$[0].createDate").exists())
                .andExpect(jsonPath("$[0].modifyDate").exists())
                .andExpect(jsonPath("$[0].comments").doesNotExist());
    }

    @Test
    @DisplayName("글 단건 조회 : PostDto 의 모든 필드가 DB 값과 같다")
    void getItem() throws Exception {
        Post post = postRepository.findById(1).get();

        ResultActions resultActions = mvc.perform(get("/api/v1/posts/1"));

        resultActions
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.createDate").value(startsWith(post.getCreateDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.modifyDate").value(startsWith(post.getModifyDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.comments").doesNotExist());
    }

    @Test
    @DisplayName("없는 글 단건 조회 : 404 + RsData(404-1), 500 이 아니다")
    void getItem_notFound() throws Exception {
        mvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("999번 글은 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.statusCode").doesNotExist());
    }

    @Test
    @DisplayName("숫자가 아닌 id : 400 + RsData(400-1)")
    void getItem_badId() throws Exception {
        mvc.perform(get("/api/v1/posts/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("글 삭제 : 200 + RsData<Void>(200-1), 글과 소속 댓글이 DB 에서 사라진다")
    void delete() throws Exception {
        assertThat(postCommentRepository.findByPostIdOrderByIdAsc(1)).hasSize(3);

        ResultActions resultActions = mvc.perform(get("/api/v1/posts/1/delete"));

        resultActions
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.statusCode").doesNotExist());

        // 영속성 컨텍스트에 남은 변경을 DB 에 반영(flush)하고 비운(clear) 뒤, DB 에서 직접 다시 확인한다.
        em.flush();
        em.clear();

        assertThat(postRepository.findById(1)).isEmpty();
        assertThat(postCommentRepository.findByPostIdOrderByIdAsc(1)).isEmpty();
        assertThat(postCommentRepository.count()).isEqualTo(2); // 2번 글의 댓글 2개만 남는다.

        mvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("없는 글 삭제 : 404 + RsData(404-1), 다른 데이터는 그대로")
    void delete_notFound() throws Exception {
        mvc.perform(get("/api/v1/posts/999/delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));

        assertThat(postRepository.count()).isEqualTo(3);
        assertThat(postCommentRepository.count()).isEqualTo(5);
    }
}
