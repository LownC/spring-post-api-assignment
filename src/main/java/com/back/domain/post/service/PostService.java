package com.back.domain.post.service;

import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Post> findAll() {
        return postRepository.findAllByOrderByIdAsc();
    }

    /**
     * 글 하나를 가져온다. 없으면 ServiceException("404-1") 을 던진다.
     * → GlobalExceptionHandler 가 HTTP 404 + RsData JSON 으로 바꿔서 응답한다.
     * (findById 는 Optional 을 돌려준다. 값이 없을 때 .get() 을 그냥 호출하면 NoSuchElementException → 500 에러가 된다.)
     */
    public Post getPost(int id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        "404-1",
                        "%d번 글은 존재하지 않습니다.".formatted(id)
                ));
    }

    /**
     * 글을 삭제한다.
     * Post.comments 에 cascade = REMOVE 가 걸려 있어서, JPA가 소속 댓글을 먼저 DELETE 한 뒤 글을 DELETE 한다.
     * (댓글을 먼저 지우지 않으면 post_comment.post_id 외래키 때문에 글을 지울 수 없다.)
     */
    @Transactional
    public void delete(int id) {
        Post post = getPost(id);

        postRepository.delete(post);
    }
}
