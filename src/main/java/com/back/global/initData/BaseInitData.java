package com.back.global.initData;

import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발/테스트용 샘플 데이터 생성기.
 * <ul>
 *   <li>{@code @Profile({"dev", "test"})} : dev, test 프로필에서만 빈으로 등록된다. 운영(prod) 환경에서는 실행되지 않는다.</li>
 *   <li>{@code ApplicationRunner} : 스프링 부트가 완전히 뜬 직후 한 번 실행된다.</li>
 * </ul>
 */
@Slf4j
@Profile({"dev", "test"})
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    /**
     * 자기 자신의 "프록시"를 주입받는다.
     * 같은 클래스 안에서 this.makeSampleData() 로 호출하면 @Transactional 이 적용되지 않기 때문에,
     * 스프링이 만든 프록시 객체(self)를 거쳐서 호출해야 트랜잭션이 시작된다.
     * {@code @Lazy} 는 "자기 자신을 만드는 도중에 자기 자신이 필요한" 순환 문제를 피하기 위해 붙인다.
     */
    @Autowired
    @Lazy
    private BaseInitData self;

    private final PostService postService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.makeSampleData();
    }

    @Transactional
    public void makeSampleData() {
        // 이미 글이 하나라도 있으면 아무것도 하지 않는다. → 서버를 여러 번 재시작해도 데이터가 중복으로 쌓이지 않는다.
        // 기존 데이터를 지우거나 초기화하지도 않는다.
        if (postService.count() > 0) {
            log.info("[BaseInitData] 기존 데이터가 있어 샘플 데이터 생성을 건너뜁니다. (글 {}개)", postService.count());
            return;
        }

        Post post1 = postService.write("제목 1", "내용 1");
        Post post2 = postService.write("제목 2", "내용 2");
        postService.write("제목 3", "내용 3");

        // 댓글은 Post 를 통해 추가한다. cascade = PERSIST 덕분에 트랜잭션이 끝날 때 자동으로 INSERT 된다.
        post1.addComment("댓글 1-1");
        post1.addComment("댓글 1-2");
        post1.addComment("댓글 1-3");
        post2.addComment("댓글 2-1");
        post2.addComment("댓글 2-2");

        log.info("[BaseInitData] 샘플 데이터를 생성했습니다. (글 3개, 댓글 5개)");
    }
}
