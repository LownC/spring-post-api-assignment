package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository<엔티티 타입, ID 타입> 을 상속받기만 하면
 * save, findById, findAll, delete, count 같은 기본 메서드를 스프링 데이터 JPA가 자동으로 구현해준다.
 */
public interface PostRepository extends JpaRepository<Post, Integer> {
}
