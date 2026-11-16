package com.back.domain.post.controller;

import com.back.domain.post.dto.PostDto;
import com.back.domain.post.service.PostService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 게시글 API 컨트롤러.
 * {@code @RestController} = {@code @Controller} + {@code @ResponseBody}
 * → 메서드가 리턴한 객체를 HTML 이 아니라 JSON 으로 변환해서 응답한다.
 */
@RestController
@RequestMapping("/api/v1/posts") // 이 클래스의 모든 주소는 /api/v1/posts 로 시작한다.
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // GET /api/v1/posts : 글 목록
    @GetMapping
    public List<PostDto> getItems() {
        return postService.findAll()
                .stream()
                .map(PostDto::new) // post -> new PostDto(post) 와 같은 의미 (메서드 참조)
                .toList();
    }

    // GET /api/v1/posts/{id} : 글 단건 (주소의 {id} 부분이 @PathVariable 로 들어온다)
    @GetMapping("/{id}")
    public PostDto getItem(@PathVariable int id) {
        return new PostDto(postService.getPost(id));
    }

    /**
     * GET /api/v1/posts/{id}/delete : 글 삭제 (소속 댓글도 함께 삭제)
     * 실습 편의를 위해 GET 으로 구현했다. 실제 서비스라면 DELETE /api/v1/posts/{id} 가 맞다.
     */
    @GetMapping("/{id}/delete")
    public RsData<Void> delete(@PathVariable int id) {
        postService.delete(id);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(id)
        );
    }
}
