package com.back.domain.postComment.entity;

import com.back.domain.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

/**
 * 댓글 엔티티. DB의 post_comment 테이블과 대응된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostComment extends BaseEntity {
    /**
     * 이 댓글이 소속된 글 (N:1 관계의 "N" 쪽).
     * <ul>
     *   <li>연관관계의 주인 : 외래키(post_id)를 실제로 관리하는 쪽이다.</li>
     *   <li>fetch = LAZY : 댓글을 조회할 때 글은 실제로 필요할 때까지 조회하지 않는다.</li>
     *   <li>@JsonIgnore : 엔티티를 그대로 JSON으로 바꿀 때 Post → comments → post → comments ... 무한 반복(순환참조)을 막는다.</li>
     * </ul>
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public PostComment(Post post, String content) {
        this.post = post;
        this.content = content;
    }
}
