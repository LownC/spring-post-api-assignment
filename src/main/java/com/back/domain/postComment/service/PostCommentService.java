package com.back.domain.postComment.service;

import com.back.domain.post.service.PostService;
import com.back.domain.postComment.entity.PostComment;
import com.back.domain.postComment.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 댓글 비즈니스 로직 담당.
 * 댓글은 항상 "어떤 글의 댓글인지"가 함께 주어진다. 그래서 모든 메서드가 postId 를 받는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentService {
    private final PostService postService;
    private final PostCommentRepository postCommentRepository;

    /**
     * postId 번 글의 댓글 목록.
     * 글이 존재하지 않으면 빈 목록이 아니라 404 로 응답한다. (postService.getPost 가 예외를 던진다.)
     */
    public List<PostComment> findAllByPostId(int postId) {
        postService.getPost(postId);

        return postCommentRepository.findByPostIdOrderByIdAsc(postId);
    }

    /**
     * postId 번 글에 소속된 id 번 댓글.
     * 댓글이 없거나, 있더라도 다른 글의 댓글이면 404 로 응답한다.
     */
    public PostComment getComment(int postId, int id) {
        postService.getPost(postId);

        return postCommentRepository.findByPostIdAndId(postId, id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "%d번 글에 %d번 댓글은 존재하지 않습니다.".formatted(postId, id)
                ));
    }
}
