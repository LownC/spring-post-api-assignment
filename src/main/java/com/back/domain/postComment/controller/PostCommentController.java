package com.back.domain.postComment.controller;

import com.back.domain.postComment.dto.PostCommentDto;
import com.back.domain.postComment.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 댓글 API 컨트롤러.
 * 댓글은 글에 속해 있으므로 주소도 /api/v1/posts/{postId}/comments 처럼 글 아래에 둔다.
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    // GET /api/v1/posts/{postId}/comments : postId 번 글의 댓글 목록
    @GetMapping
    public List<PostCommentDto> getItems(@PathVariable int postId) {
        return postCommentService.findAllByPostId(postId)
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    // GET /api/v1/posts/{postId}/comments/{id} : postId 번 글의 id 번 댓글
    @GetMapping("/{id}")
    public PostCommentDto getItem(
            @PathVariable int postId,
            @PathVariable int id
    ) {
        return new PostCommentDto(postCommentService.getComment(postId, id));
    }
}
