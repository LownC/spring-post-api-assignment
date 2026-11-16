package com.back.domain.postComment.entity;

import com.back.domain.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
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
     * </ul>
     * 예전에는 엔티티를 직접 JSON 으로 응답해서 순환참조를 막으려고 @JsonIgnore 를 붙였었다.
     * 이제는 모든 API 가 DTO 로만 응답하므로 엔티티가 JSON 변환(Jackson)에 대해 알 필요가 없다.
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public PostComment(Post post, String content) {
        this.post = post;
        this.content = content;
    }
}
