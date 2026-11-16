package com.back.domain.post.controller;

import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
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
    public List<Post> getItems() {
        return postService.findAll();
    }

    // GET /api/v1/posts/{id} : 글 단건 (주소의 {id} 부분이 @PathVariable 로 들어온다)
    @GetMapping("/{id}")
    public Post getItem(@PathVariable int id) {
        return postService.getPost(id);
    }
}
