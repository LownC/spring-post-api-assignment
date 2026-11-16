package com.back.domain.post.service;

import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 비즈니스 로직 담당.
 * 컨트롤러는 HTTP를, 리포지터리는 DB를 담당하고, 서비스는 그 사이에서 "무엇을 할지"를 결정한다.
 */
@Service
@RequiredArgsConstructor // final 필드를 받는 생성자를 롬복이 만들어준다. → 스프링이 생성자 주입으로 PostRepository 를 넣어준다.
@Transactional(readOnly = true) // 클래스의 모든 메서드는 기본적으로 "읽기 전용 트랜잭션" 안에서 실행된다.
public class PostService {
    private final PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    @Transactional // 데이터를 바꾸는 메서드는 readOnly 가 아닌 일반 트랜잭션으로 덮어쓴다.
    public Post write(String title, String content) {
        Post post = new Post(title, content);

        return postRepository.save(post);
    }
}
