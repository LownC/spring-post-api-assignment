package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;

import java.time.LocalDateTime;

/**
 * API 응답 전용 게시글 데이터.
 * <p>
 * record : 값을 담기만 하는 불변 클래스를 짧게 선언하는 문법(Java 16+).
 * 필드, 생성자, getter(id(), title() ...), equals/hashCode/toString 이 자동으로 만들어진다.
 * <p>
 * 엔티티(Post)를 그대로 응답하지 않고 DTO 로 바꿔서 응답하는 이유
 * <ul>
 *   <li>외부에 보여줄 필드만 고를 수 있다. (comments 목록 같은 내부 연관관계는 노출하지 않는다.)</li>
 *   <li>엔티티 구조가 바뀌어도 API 응답 형식(프론트와의 약속)은 그대로 유지할 수 있다.</li>
 *   <li>순환참조, 지연로딩 같은 JPA 문제가 JSON 변환 과정에 끼어들지 않는다.</li>
 * </ul>
 */
public record PostDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String title,
        String content
) {
    /**
     * 엔티티 → DTO 변환용 생성자.
     */
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getTitle(),
                post.getContent()
        );
    }
}
