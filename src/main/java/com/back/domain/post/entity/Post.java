package com.back.domain.post.entity;

import com.back.domain.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static lombok.AccessLevel.PROTECTED;

/**
 * 게시글 엔티티. DB의 post 테이블과 1:1로 대응된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = PROTECTED) // JPA가 객체를 만들 때 필요한 기본 생성자. 외부에서는 쓰지 못하게 막는다.
public class Post extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 이 글에 달린 댓글 목록 (1:N 관계의 "1" 쪽).
     * <ul>
     *   <li>mappedBy = "post" : 연관관계의 주인은 PostComment.post 이다. 외래키(post_id)는 post_comment 테이블에 있다.</li>
     *   <li>cascade = PERSIST : 글을 저장할 때 새로 추가된 댓글도 함께 저장한다.</li>
     *   <li>cascade = REMOVE : 글을 삭제하면 소속 댓글도 함께 삭제한다.</li>
     *   <li>orphanRemoval = true : 이 리스트에서 빠진 댓글(고아)은 DB에서도 삭제한다.</li>
     * </ul>
     */
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    private final List<PostComment> comments = new ArrayList<>();

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 댓글을 추가한다. 양방향 관계이므로 댓글 쪽(post 필드)과 글 쪽(comments 리스트)을 함께 맞춰준다.
     */
    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content);
        comments.add(postComment);

        return postComment;
    }

    /**
     * 댓글을 리스트에서 제거한다. orphanRemoval = true 이므로 트랜잭션이 끝날 때 DELETE 쿼리가 실행된다.
     */
    public boolean deleteComment(PostComment postComment) {
        return comments.remove(postComment);
    }
}
