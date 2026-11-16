package com.back.domain.postComment.controller;

import com.back.domain.postComment.dto.PostCommentDto;
import com.back.domain.postComment.service.PostCommentService;
import com.back.global.rsData.RsData;
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

    /**
     * GET /api/v1/posts/{postId}/comments/{id}/delete : 댓글 삭제
     * <p>
     * 원래 REST 에서는 삭제에 DELETE 메서드를 써야 한다.
     * 이번 과제는 브라우저 주소창으로 바로 확인할 수 있도록 GET 으로 구현한다. (실습용 설계)
     * <p>
     * 응답할 데이터가 없으므로 RsData<Void> 를 사용한다. (data 는 항상 null)
     */
    @GetMapping("/{id}/delete")
    public RsData<Void> delete(
            @PathVariable int postId,
            @PathVariable int id
    ) {
        postCommentService.delete(postId, id);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id)
        );
    }
}
