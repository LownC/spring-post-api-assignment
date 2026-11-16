package com.back.domain.postComment.dto;

import com.back.domain.postComment.entity.PostComment;

import java.time.LocalDateTime;

/**
 * API 응답 전용 댓글 데이터.
 * Post 객체 전체 대신 postId(숫자)만 담는다. → 순환참조가 구조적으로 생길 수 없다.
 */
public record PostCommentDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int postId,
        String content
) {
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreateDate(),
                postComment.getModifyDate(),
                postComment.getPost().getId(),
                postComment.getContent()
        );
    }
}
