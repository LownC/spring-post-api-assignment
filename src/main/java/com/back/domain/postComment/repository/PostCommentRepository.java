package com.back.domain.postComment.repository;

import com.back.domain.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    /**
     * 특정 글(postId)의 댓글 목록을 id 오름차순으로 조회한다.
     * → SELECT * FROM post_comment WHERE post_id = ? ORDER BY id ASC
     */
    List<PostComment> findByPostIdOrderByIdAsc(int postId);

    /**
     * "postId 번 글에 소속된" id 번 댓글을 조회한다.
     * 조건에 post_id 를 함께 넣기 때문에, 다른 글의 댓글 번호를 넣으면 결과가 비어있다(Optional.empty).
     * → SELECT * FROM post_comment WHERE post_id = ? AND id = ?
     */
    Optional<PostComment> findByPostIdAndId(int postId, int id);
}
