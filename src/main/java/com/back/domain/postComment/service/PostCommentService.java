package com.back.domain.postComment.service;

import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.postComment.entity.PostComment;
import com.back.domain.postComment.repository.PostCommentRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 댓글이 없거나, 있더라도 다른 글의 댓글이면 404 로 응답한다. (resultCode "404-2")
     */
    public PostComment getComment(int postId, int id) {
        postService.getPost(postId);

        return postCommentRepository.findByPostIdAndId(postId, id)
                .orElseThrow(() -> new ServiceException(
                        "404-2",
                        "%d번 글에 %d번 댓글은 존재하지 않습니다.".formatted(postId, id)
                ));
    }

    /**
     * postId 번 글에 소속된 id 번 댓글을 삭제한다.
     * <ol>
     *   <li>getComment 로 "이 글의 댓글이 맞는지" 먼저 확인한다. 아니면 404 → 다른 글의 댓글은 지워지지 않는다.</li>
     *   <li>post.deleteComment 로 글의 댓글 목록에서 뺀다.</li>
     *   <li>orphanRemoval = true 이므로, 트랜잭션이 커밋될 때 JPA가 DELETE 쿼리를 실행한다.</li>
     * </ol>
     * {@code @Transactional} 이 없으면(=readOnly) 변경 감지가 동작하지 않아 DELETE 가 실행되지 않는다.
     */
    @Transactional
    public void delete(int postId, int id) {
        Post post = postService.getPost(postId);
        PostComment postComment = getComment(postId, id);

        post.deleteComment(postComment);
    }
}
