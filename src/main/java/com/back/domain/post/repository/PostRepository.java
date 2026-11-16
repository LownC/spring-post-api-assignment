package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository<엔티티 타입, ID 타입> 을 상속받기만 하면
 * save, findById, findAll, delete, count 같은 기본 메서드를 스프링 데이터 JPA가 자동으로 구현해준다.
 */
public interface PostRepository extends JpaRepository<Post, Integer> {
    /**
     * 쿼리 메서드 : 메서드 이름만 규칙에 맞게 지으면 스프링 데이터 JPA가 SQL을 만들어준다.
     * findAll + OrderByIdAsc → SELECT * FROM post ORDER BY id ASC
     * findAll() 은 순서를 보장하지 않으므로, 항상 같은 순서로 응답하기 위해 정렬 기준을 명시한다.
     */
    List<Post> findAllByOrderByIdAsc();
}
