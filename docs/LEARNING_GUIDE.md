# 학습 가이드 : 커밋 순서대로 읽는 Spring Boot REST API

이 문서는 `spring-post-api-assignment` 를 **커밋 1번부터 12번까지 순서대로** 읽으면서 공부하기 위한 안내서입니다.

## 이 문서 사용법

1. 터미널(또는 IntelliJ Git 탭)에서 커밋 목록을 봅니다.
   ```bash
   git log --oneline --reverse
   ```
2. 커밋 하나를 골라 "무엇이 바뀌었는지" 봅니다.
   ```bash
   git show 1d8f877          # 바뀐 내용(diff) 보기
   git show --stat 1d8f877   # 바뀐 파일 목록만 보기
   ```
3. 그 시점의 코드로 직접 이동해서 실행해볼 수도 있습니다. (다 보고 나면 꼭 `main` 으로 돌아오세요)
   ```bash
   git switch --detach 1d8f877   # 해당 커밋 시점으로 이동 (읽기 전용 상태)
   git switch main               # 최신 상태로 복귀
   ```
4. 아래 각 커밋 섹션을 읽고, "직접 확인할 수 있는 실습"을 해본 뒤, 이해도 확인 문제를 풉니다.
   정답은 문서 맨 아래 [정답 모음](#정답-모음)에 있습니다.

> 처음에는 **01 → 02 → 04 → 05** 를 먼저 읽어 "요청이 들어와서 JSON 이 나가기까지"의 흐름을 잡고,
> 그다음 나머지를 읽는 것을 추천합니다.

---

## 전체 그림 : 요청 하나가 처리되는 흐름

```text
브라우저 / curl
   │  GET /api/v1/posts/1
   ▼
DispatcherServlet (스프링이 모든 요청을 받는 입구)
   │  URL 을 보고 알맞은 컨트롤러 메서드를 찾는다
   ▼
PostController.getItem(1)            ← HTTP 담당
   │
   ▼
PostService.getPost(1)               ← 비즈니스 로직, 트랜잭션
   │
   ▼
PostRepository.findById(1)           ← DB 담당 (Spring Data JPA)
   │  SELECT * FROM post WHERE id = 1
   ▼
H2 DB  →  Post 엔티티 객체
   │
   ▼
new PostDto(post)                    ← 외부에 보여줄 필드만 담기
   │
   ▼
Jackson 이 PostDto 를 JSON 문자열로 변환
   │
   ▼
{"id":1,"createDate":"...","modifyDate":"...","title":"제목 1","content":"내용 1"}
```

---

## 커밋 01 — `6e2c753` chore: initialize Spring Boot project

### 1. 이번 커밋에서 구현한 기능
아무 기능도 없는 "빈 스프링 부트 프로젝트"를 만들었습니다. 서버가 켜지고, DB(H2)에 연결되는 것까지만 확인합니다.
[start.spring.io](https://start.spring.io) 에서 Spring Web, Spring Data JPA, Validation, H2, Lombok 을 선택해 만든 뒤 설정 파일을 정리했습니다.

### 2. 새로 등장한 Java/Spring 개념
- **Gradle / Gradle Wrapper** : 라이브러리를 내려받고, 컴파일하고, 테스트하는 빌드 도구. `gradlew.bat` 은 Gradle 이 설치되어 있지 않아도 정해진 버전을 자동으로 받아서 실행해줍니다.
- **build.gradle.kts** : 어떤 라이브러리를 쓸지(`dependencies`), 어떤 Java 버전을 쓸지(`toolchain`) 적는 파일.
- **@SpringBootApplication** : "여기서부터 스프링 부트 애플리케이션이 시작된다"는 표시. 이 클래스가 있는 패키지(`com.back`)와 그 하위 패키지를 전부 스캔합니다.
- **프로필(profile)** : 같은 코드라도 환경(dev, test, prod)에 따라 설정을 바꿔 쓰는 기능. `application-dev.yaml` 은 dev 프로필일 때만 적용됩니다.
- **H2** : 자바로 만든 작은 DB. 설치 없이 파일 또는 메모리에 데이터를 저장할 수 있어서 학습용으로 좋습니다.

### 3. 코드 실행 흐름
```text
gradlew bootRun → BackApplication.main() → SpringApplication.run()
 → application.yaml 읽기 (active: dev) → application-dev.yaml 읽기
 → H2 파일 DB(./db_dev) 연결 → 내장 톰캣 서버 8080 포트로 시작
```

### 4. 중요 코드 설명
| 파일 | 핵심 |
|---|---|
| `build.gradle.kts` | `spring-boot-starter-webmvc`(웹), `spring-boot-starter-data-jpa`(JPA), `h2`(DB), `lombok` |
| `application.yaml` | `profiles.active: dev`, `jpa.show-sql: true`(실행되는 SQL 출력), `ddl-auto: update`(테이블 자동 생성) |
| `application-dev.yaml` | 파일 DB `jdbc:h2:./db_dev`, H2 콘솔 켜기 |
| `application-test.yaml` | 메모리 DB `jdbc:h2:mem:db_test` |

### 5. 왜 이런 구조로 만들었는가?
- **개발 DB 와 테스트 DB 를 분리**했습니다. 테스트가 개발 중인 데이터를 지우거나 바꾸면 안 되기 때문입니다.
- dev 는 **파일 DB** 라서 서버를 껐다 켜도 데이터가 남습니다. 그래서 커밋 03 에서 "중복 생성 방지"가 필요해집니다.
- 대안 : MySQL 같은 실제 DB 를 써도 되지만, 설치·계정 설정이 필요해서 학습 초반에는 H2 가 편합니다. `MODE=MySQL` 로 MySQL 문법에 가깝게 맞춰두었습니다.

### 6. 직접 확인할 수 있는 실습
- `.\gradlew.bat bootRun` 실행 → 콘솔에 `Started BackApplication` 이 나오면 성공.
- <http://localhost:8080/h2-console> 접속 → JDBC URL 에 `jdbc:h2:./db_dev;MODE=MySQL` 입력 → Connect.
- 프로젝트 폴더에 `db_dev.mv.db` 파일이 생겼는지 확인.

### 7. 이해도 확인 문제
- **Q1-1.** `application.yaml` 과 `application-dev.yaml` 에 같은 설정이 있으면 어느 쪽 값이 쓰일까요?
- **Q1-2.** 테스트용 DB 를 메모리 DB 로 따로 둔 이유는 무엇일까요?
- **Q1-3.** Gradle 이 PC 에 설치되어 있지 않은데도 `gradlew.bat` 이 동작하는 이유는?

### 8. 다음 커밋과의 연결
서버는 켜지지만 DB 에 테이블이 하나도 없습니다. 다음 커밋에서 **엔티티 클래스**를 만들면 JPA 가 그걸 보고 테이블을 만들어줍니다.

---

## 커밋 02 — `1d8f877` feat: add post and comment entities

### 1. 이번 커밋에서 구현한 기능
게시글(`Post`)과 댓글(`PostComment`) **엔티티**와, 이들을 DB 에 저장/조회하는 **리포지터리**를 만들었습니다.
서버를 켜면 `post`, `post_comment` 테이블과 외래키가 자동으로 생깁니다.

### 2. 새로 등장한 Java/Spring 개념
- **@Entity** : "이 클래스는 DB 테이블과 연결된다"는 표시. `Post` 클래스 → `post` 테이블.
- **@Id, @GeneratedValue(strategy = IDENTITY)** : 기본키(PK). 값은 DB 가 1, 2, 3… 순서로 자동으로 매깁니다.
- **@MappedSuperclass** : 공통 필드(id, createDate, modifyDate)를 부모 클래스(`BaseEntity`)에 모아두고 상속받게 하는 기능.
- **JPA Auditing (@CreatedDate, @LastModifiedDate, @EnableJpaAuditing)** : 저장/수정 시각을 자동으로 채워줍니다.
- **@OneToMany / @ManyToOne** : 1:N 관계. 글 하나(1)에 댓글 여러 개(N).
- **mappedBy** : 연관관계의 주인이 아닌 쪽에 붙입니다. "외래키는 저쪽(PostComment.post)이 관리한다"는 뜻.
- **cascade, orphanRemoval** : 부모(글)의 저장/삭제를 자식(댓글)에게 전파하는 옵션. ([개념 정리 6](#6-jpa-연관관계와-cascade))
- **FetchType.LAZY** : 댓글을 가져올 때 글은 실제로 쓸 때까지 DB 에서 가져오지 않습니다(지연 로딩).
- **JpaRepository** : 인터페이스만 선언하면 `save`, `findById`, `findAll`, `delete`, `count` 를 자동으로 구현해줍니다. ([개념 정리 3](#3-jpa-repository가-db를-다루는-방법))
- **Lombok** : `@Getter`(getter 자동 생성), `@NoArgsConstructor`(기본 생성자 자동 생성).

### 3. 코드 실행 흐름
```text
서버 시작 → Hibernate(JPA 구현체)가 @Entity 클래스들을 스캔
 → ddl-auto: update 설정에 따라
   CREATE TABLE post (...)
   CREATE TABLE post_comment (..., post_id integer not null)
   ALTER TABLE post_comment ADD CONSTRAINT ... FOREIGN KEY (post_id) REFERENCES post
 → PostRepository, PostCommentRepository 구현체(프록시)를 스프링이 자동 생성
```

### 4. 중요 코드 설명
```java
// Post.java
@OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
@OrderBy("id ASC")
private final List<PostComment> comments = new ArrayList<>();

public PostComment addComment(String content) {
    PostComment postComment = new PostComment(this, content); // 댓글 쪽에 글을 연결
    comments.add(postComment);                                // 글 쪽 리스트에도 추가
    return postComment;
}
```
- 양방향 관계에서는 **양쪽을 모두** 맞춰줘야 객체 상태와 DB 상태가 어긋나지 않습니다. 그래서 `addComment` 안에서 한 번에 처리합니다.

```java
// PostComment.java
@ManyToOne(fetch = LAZY)
@JoinColumn(name = "post_id", nullable = false)
@JsonIgnore   // ← 이 커밋에서만 존재. 커밋 07 에서 제거됨
private Post post;
```
- `@JsonIgnore` : 엔티티를 그대로 JSON 으로 만들면 `Post → comments → PostComment → post → comments → …` 가 끝없이 반복됩니다(**순환참조**). 이를 끊기 위한 임시 조치입니다.

### 5. 왜 이런 구조로 만들었는가?
- 외래키(`post_id`)는 **댓글 테이블**에 있습니다. 그래서 DB 기준으로 실제 관계를 관리하는 쪽(주인)은 `PostComment.post` 입니다.
- `Post.comments` 는 "글에서 댓글을 쉽게 다루기 위한" 편의용 반대편입니다. (`mappedBy`)
- 대안 : 단방향(`PostComment → Post` 만)으로 만들 수도 있습니다. 하지만 그러면 "글을 지울 때 댓글도 지우기"를 직접 코딩해야 해서, 강의와 같이 양방향 + cascade 를 택했습니다.

### 6. 직접 확인할 수 있는 실습
- 서버 실행 후 콘솔에서 `create table post`, `create table post_comment`, `foreign key (post_id)` 로그 찾기.
- H2 콘솔에서 `SHOW COLUMNS FROM POST_COMMENT;` 실행.

### 7. 이해도 확인 문제
- **Q2-1.** `post_id` 칼럼은 `post` 테이블과 `post_comment` 테이블 중 어디에 생길까요? 그래서 연관관계의 주인은 누구일까요?
- **Q2-2.** `fetch = LAZY` 가 의미하는 것은?
- **Q2-3.** 엔티티를 그대로 JSON 으로 응답하면 왜 무한 루프가 생길 수 있나요?

### 8. 다음 커밋과의 연결
테이블은 생겼지만 비어 있습니다. API 를 만들어도 확인할 데이터가 없으니, 다음 커밋에서 **샘플 데이터**를 자동으로 넣습니다.

---

## 커밋 03 — `8ac8384` feat: initialize sample post and comment data

### 1. 이번 커밋에서 구현한 기능
서버가 켜질 때 글 3개, 댓글 5개를 자동으로 넣습니다. 이미 데이터가 있으면 아무것도 하지 않습니다.
글을 저장하는 `PostService` 도 처음 등장합니다.

### 2. 새로 등장한 Java/Spring 개념
- **@Service** : 비즈니스 로직 담당 클래스라는 표시. 스프링이 객체(빈)로 만들어 관리합니다.
- **@RequiredArgsConstructor + final 필드** : 생성자 주입. 스프링이 `PostRepository` 를 알아서 넣어줍니다(의존성 주입, DI).
- **@Transactional** : 메서드 안의 DB 작업을 하나의 묶음(트랜잭션)으로 처리합니다. ([개념 정리 5](#5-transactional이-필요한-이유))
- **@Transactional(readOnly = true)** : 읽기 전용 트랜잭션. 클래스에 붙이면 모든 메서드의 기본값이 됩니다.
- **@Configuration + @Bean + ApplicationRunner** : 스프링 부트가 다 켜진 직후 실행할 코드를 등록하는 방법.
- **@Profile({"dev", "test"})** : 지정한 프로필에서만 이 설정 클래스가 동작합니다.
- **self 주입(@Lazy)** : 같은 클래스 안에서 `this.method()` 로 부르면 `@Transactional` 이 무시됩니다. 스프링이 만든 프록시(`self`)를 통해 불러야 트랜잭션이 걸립니다.

### 3. 코드 실행 흐름
```text
서버 시작 완료 → ApplicationRunner 실행 → self.makeSampleData()  (트랜잭션 시작)
 → postService.count() > 0 ?  ── 예 → "건너뜁니다" 로그 후 종료
                              └─ 아니오 ↓
 → postService.write("제목 1", ...) × 3   (INSERT post)
 → post1.addComment(...) × 3, post2.addComment(...) × 2
 → 트랜잭션 커밋 시점에 cascade=PERSIST 로 댓글 5개 INSERT
```

### 4. 중요 코드 설명
```java
@Transactional
public void makeSampleData() {
    if (postService.count() > 0) return;   // 중복 생성 방지 + 기존 데이터 보존
    Post post1 = postService.write("제목 1", "내용 1");
    ...
    post1.addComment("댓글 1-1");           // repository.save 를 부르지 않았는데도 저장된다!
}
```
- 댓글은 `save` 를 호출하지 않았는데도 저장됩니다. `post1` 은 트랜잭션 안에서 **영속 상태**이고, `cascade = PERSIST` 덕분에 새로 추가된 댓글도 함께 저장되기 때문입니다.

### 5. 왜 이런 구조로 만들었는가?
- "데이터가 있으면 건너뛰기"는 가장 단순한 중복 방지입니다. `deleteAll()` 후 다시 넣는 방식은 개발 중에 만든 데이터를 날려버리므로 쓰지 않았습니다.
- 운영 환경에서 실수로 샘플 데이터가 들어가지 않도록 `@Profile` 로 막았습니다.
- 대안 : `data.sql` 파일로 SQL 을 직접 넣는 방법도 있지만, 자바 코드로 하면 연관관계 설정(`addComment`)을 그대로 재사용할 수 있습니다.

### 6. 직접 확인할 수 있는 실습
- 서버를 처음 켜면 `[BaseInitData] 샘플 데이터를 생성했습니다.`, 두 번째부터는 `기존 데이터가 있어 ... 건너뜁니다.` 로그 확인.
- H2 콘솔에서 `SELECT * FROM POST_COMMENT;` → `post_id` 가 1, 1, 1, 2, 2 인지 확인.
- 서버를 끄고 `db_dev.mv.db` 를 지운 뒤 다시 켜보기 → 다시 생성됨.

### 7. 이해도 확인 문제
- **Q3-1.** `makeSampleData()` 를 같은 클래스 안에서 `this.makeSampleData()` 로 호출하면 어떤 문제가 생길까요?
- **Q3-2.** 댓글은 `save()` 를 호출하지 않았는데 어떻게 DB 에 저장되었나요?
- **Q3-3.** 운영(prod) 프로필로 실행하면 샘플 데이터는 어떻게 될까요?

### 8. 다음 커밋과의 연결
드디어 DB 에 데이터가 있습니다. 다음 커밋에서 이 데이터를 **HTTP 로 조회하는 API** 를 만듭니다.

---

## 커밋 04 — `c62033c` feat: implement post read APIs

### 1. 이번 커밋에서 구현한 기능
- `GET /api/v1/posts` : 글 목록 (id 오름차순)
- `GET /api/v1/posts/{id}` : 글 하나
- 없는 글이면 404 응답

이 시점에는 **엔티티(Post)를 그대로** JSON 으로 응답합니다. (일부러 남겨둔 문제)

### 2. 새로 등장한 Java/Spring 개념
- **@RestController** = `@Controller` + `@ResponseBody`. 리턴값을 JSON 으로 바꿔서 응답합니다.
- **@RequestMapping("/api/v1/posts")** : 클래스 안 모든 주소의 공통 앞부분.
- **@GetMapping** : HTTP GET 요청을 처리하는 메서드.
- **@PathVariable** : URL 의 `{id}` 부분 값을 메서드 매개변수로 받습니다.
- **쿼리 메서드** : `findAllByOrderByIdAsc()` 처럼 이름만 규칙대로 지으면 SQL 이 자동 생성됩니다.
- **Optional / orElseThrow** : 값이 없을 수도 있음을 나타내는 상자. 없으면 예외를 던집니다.
- **ResponseStatusException** : 던지면 스프링이 지정한 HTTP 상태코드(404)로 응답합니다.
- **Jackson** : 자바 객체 ↔ JSON 변환 라이브러리. getter 를 보고 JSON 필드를 만듭니다.

### 3. 코드 실행 흐름
```text
GET /api/v1/posts/1
 → PostController.getItem(1)
 → PostService.getPost(1)  → postRepository.findById(1)  → SELECT ... WHERE id=1
 → Post 엔티티 → Jackson 이 getter(getId, getTitle, getComments ...)로 JSON 생성
```

### 4. 중요 코드 설명
```java
public Post getPost(int id) {
    return postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "%d번 글은 존재하지 않습니다.".formatted(id)));
}
```
- `findById(id).get()` 으로 쓰면 값이 없을 때 `NoSuchElementException` → **500 서버 오류**가 됩니다. 사용자의 잘못된 요청인데 서버 잘못처럼 보이므로 404 로 바꿨습니다.

### 5. 왜 이런 구조로 만들었는가?
- `findAll()` 은 순서를 보장하지 않습니다. 같은 요청에 항상 같은 순서로 응답하도록 정렬을 명시했습니다.
- 주소에 `/v1` 을 넣는 이유 : 나중에 응답 형식이 크게 바뀌면 `/v2` 를 새로 만들고, 기존 앱(프론트)은 `/v1` 을 계속 쓸 수 있습니다.

### 6. 직접 확인할 수 있는 실습
- 이 커밋으로 이동(`git switch --detach c62033c`) → 서버 실행 → <http://localhost:8080/api/v1/posts> 접속.
- 응답에 `comments` 배열까지 통째로 들어있는 것을 확인. **이게 다음 커밋에서 고칠 문제입니다.**
- <http://localhost:8080/api/v1/posts/999> → 404 확인.

### 7. 이해도 확인 문제
- **Q4-1.** `@RestController` 와 `@Controller` 의 차이는?
- **Q4-2.** `findById(id).get()` 대신 `orElseThrow` 를 쓴 이유는?
- **Q4-3.** `GET /api/v1/posts/7` 요청에서 `7` 은 어떤 어노테이션으로 받나요?

### 8. 다음 커밋과의 연결
응답에 `comments` 같은 내부 구조가 그대로 노출됩니다. 엔티티에 필드가 하나 추가되기만 해도 API 응답이 바뀌어 버립니다. 다음 커밋에서 **DTO** 로 응답 형식을 고정합니다.

---

## 커밋 05 — `ff26b41` refactor: introduce PostDto for API responses

### 1. 이번 커밋에서 구현한 기능
`PostDto` (record) 를 만들고, 글 조회 API 가 엔티티 대신 DTO 를 응답하도록 바꿨습니다. 응답에서 `comments` 가 사라집니다.

### 2. 새로 등장한 Java/Spring 개념
- **DTO (Data Transfer Object)** : 계층 사이, 또는 외부로 데이터를 전달하기 위한 객체. ([개념 정리 1](#1-entity와-dto의-차이))
- **record** : 필드·생성자·getter·equals/hashCode/toString 을 자동으로 만들어주는 불변 클래스. ([개념 정리 8](#8-java-record))
- **Stream API** : `list.stream().map(...).toList()` 로 리스트의 각 요소를 변환.
- **메서드 참조** : `post -> new PostDto(post)` 를 `PostDto::new` 로 짧게 쓰는 문법.

### 3. 코드 실행 흐름
```text
PostService.findAll() → List<Post>
 → .stream().map(PostDto::new).toList() → List<PostDto>
 → Jackson → [{"id":1,"createDate":...,"title":"제목 1","content":"내용 1"}, ...]
```

### 4. 중요 코드 설명
```java
public record PostDto(int id, LocalDateTime createDate, LocalDateTime modifyDate,
                      String title, String content) {
    public PostDto(Post post) {           // 엔티티 → DTO 변환 생성자
        this(post.getId(), post.getCreateDate(), post.getModifyDate(),
             post.getTitle(), post.getContent());
    }
}
```

### 5. 왜 이런 구조로 만들었는가?
- **API 응답 = 프론트엔드와의 약속**입니다. 엔티티는 DB 구조에 맞춰 자주 바뀌지만, DTO 를 두면 약속(응답 형식)을 따로 지킬 수 있습니다.
- 강의 8~9강처럼 일반 클래스 + `@Getter` 로 만들 수도 있지만, 응답 전용 데이터는 바뀔 일이 없으므로 **record** 가 더 간결하고 안전합니다.

### 6. 직접 확인할 수 있는 실습
- <http://localhost:8080/api/v1/posts> 응답에 `comments` 가 없는지 확인.
- `PostDto` 에서 `content` 를 지우고 실행해보기 → 응답에서 `content` 가 사라짐 (엔티티는 그대로).

### 7. 이해도 확인 문제
- **Q5-1.** 엔티티에 `password` 같은 민감한 필드가 추가되었다고 가정하면, DTO 가 왜 필요한지 설명해보세요.
- **Q5-2.** `.map(PostDto::new)` 를 람다식으로 풀어 쓰면?
- **Q5-3.** record 의 필드를 나중에 바꿀(set) 수 있나요?

### 8. 다음 커밋과의 연결
글은 조회되는데 **댓글을 볼 방법**이 없습니다. 다음 커밋에서 댓글 조회 API 를 만듭니다.

---

## 커밋 06 — `3388875` feat: implement comment read APIs

### 1. 이번 커밋에서 구현한 기능
- `GET /api/v1/posts/{postId}/comments` : 특정 글의 댓글 목록
- `GET /api/v1/posts/{postId}/comments/{id}` : 특정 글의 댓글 하나
- 없는 글 → 404, 없는 댓글 → 404, **다른 글의 댓글 → 404**

### 2. 새로 등장한 Java/Spring 개념
- **중첩 경로(Nested Resource)** : 댓글은 글에 속하므로 `/posts/{postId}/comments` 처럼 표현합니다.
- **@PathVariable 여러 개** : `postId`, `id` 를 동시에 받습니다.
- **조건이 여러 개인 쿼리 메서드** : `findByPostIdAndId(postId, id)` → `WHERE post_id = ? AND id = ?`
- **서비스가 다른 서비스를 사용** : `PostCommentService` 가 `PostService.getPost()` 로 글 존재 여부를 확인합니다.

### 3. 코드 실행 흐름
```text
GET /api/v1/posts/1/comments/4
 → PostCommentController.getItem(1, 4)
 → PostCommentService.getComment(1, 4)
    → postService.getPost(1)                       (글 있음)
    → postCommentRepository.findByPostIdAndId(1, 4) → 결과 없음 (4번 댓글은 2번 글 소속)
    → 404
```

### 4. 중요 코드 설명
```java
Optional<PostComment> findByPostIdAndId(int postId, int id);
```
- `findById(id)` 만 쓰면 `/posts/1/comments/4` 요청에 2번 글의 댓글이 조회되어 버립니다. **글 번호까지 조건에 넣는 것**이 핵심입니다.

### 5. 왜 이런 구조로 만들었는가?
- 강의는 `post.getComments()` 에서 stream 으로 찾지만, 과제 구조에 맞춰 `PostCommentService` 와 리포지터리 쿼리를 사용했습니다. DB 가 필요한 행만 골라 오므로 댓글이 많아져도 효율적입니다.
- 글이 없을 때 빈 배열(`[]`)이 아니라 404 를 주는 이유 : "댓글이 0개인 글"과 "존재하지 않는 글"은 다른 상황이기 때문입니다.

### 6. 직접 확인할 수 있는 실습
- <http://localhost:8080/api/v1/posts/1/comments/4> → 404, <http://localhost:8080/api/v1/posts/2/comments/4> → 200 비교.
- <http://localhost:8080/api/v1/posts/3/comments> → `[]` (글은 있지만 댓글 없음).
- 콘솔 SQL 로그에서 `where ... post_id=? and ... id=?` 확인.

### 7. 이해도 확인 문제
- **Q6-1.** `findById(id)` 만 썼다면 `/posts/1/comments/4` 요청에서 어떤 버그가 생길까요?
- **Q6-2.** 댓글이 없는 글(3번)과 없는 글(999번)의 응답은 각각 어떻게 다를까요?
- **Q6-3.** `findByPostIdOrderByIdAsc` 이름에서 `PostId` 는 어떤 칼럼 조건이 되나요?

### 8. 다음 커밋과의 연결
댓글 API 도 아직 **엔티티를 직접** 응답하고 있고, 순환참조를 막으려고 엔티티에 `@JsonIgnore` 가 붙어 있습니다. 다음 커밋에서 `PostCommentDto` 로 정리합니다.

---

## 커밋 07 — `8403194` refactor: introduce PostCommentDto for API responses

### 1. 이번 커밋에서 구현한 기능
`PostCommentDto` 를 만들어 댓글 API 응답을 DTO 로 바꿨고, 더 이상 필요 없어진 `PostComment.post` 의 `@JsonIgnore` 를 제거했습니다.

### 2. 새로 등장한 Java/Spring 개념
- **엔티티를 외부에 노출하지 않는 원칙** : 모든 API 가 DTO 로만 응답하게 되었습니다.
- **연관 객체 대신 식별자(postId)만 담기** : 객체 그래프를 끊어서 순환참조를 구조적으로 막습니다.

### 3. 코드 실행 흐름
```text
List<PostComment> → .map(PostCommentDto::new) → List<PostCommentDto>
PostCommentDto(postComment) 안에서 postComment.getPost().getId() 로 postId 만 꺼냄
```

### 4. 중요 코드 설명
```java
public record PostCommentDto(int id, LocalDateTime createDate, LocalDateTime modifyDate,
                             int postId, String content) { ... }
```
- 강의 코드에는 `postId` 가 없지만, 응답만 보고도 어느 글의 댓글인지 알 수 있도록 추가했습니다.

### 5. 왜 이런 구조로 만들었는가?
- `@JsonIgnore` 는 "엔티티가 JSON 변환 방법을 알고 있어야 하는" 구조입니다. DB 모델(엔티티)과 화면 모델(JSON)이 섞이는 것이므로, DTO 가 생긴 뒤에는 제거하는 것이 깔끔합니다.

### 6. 직접 확인할 수 있는 실습
- <http://localhost:8080/api/v1/posts/1/comments> → `postId` 필드 확인.
- `git show 8403194 -- src/main/java/com/back/domain/postComment/entity/PostComment.java` 로 `@JsonIgnore` 가 빠진 것 확인.

### 7. 이해도 확인 문제
- **Q7-1.** `@JsonIgnore` 를 지웠는데도 순환참조가 생기지 않는 이유는?
- **Q7-2.** `PostCommentDto` 에 `Post post` 필드를 그대로 넣으면 어떤 문제가 다시 생길 수 있을까요?
- **Q7-3.** 이 커밋 이후 엔티티를 직접 JSON 으로 응답하는 API 가 남아있나요?

### 8. 다음 커밋과의 연결
조회는 완성됐습니다. 이제 삭제 API 를 만들 차례인데, 삭제 결과는 "무엇을 돌려줘야 할까요?" 그리고 404 응답도 지금은 스프링 기본 형식(`timestamp`, `status`, `error`, `path`)이라 우리 API 와 모양이 다릅니다. 다음 커밋에서 **공통 응답 형식 `RsData<T>`** 를 만듭니다.

---

## 커밋 08 — `7f96057` feat: introduce generic RsData response

### 1. 이번 커밋에서 구현한 기능
- 공통 응답 `RsData<T>(resultCode, msg, data)`
- `ServiceException` : 서비스에서 "처리할 수 없음"을 알리는 예외
- `GlobalExceptionHandler` : 모든 예외를 `RsData` JSON + 알맞은 HTTP 상태코드로 변환
- 404 응답이 `{"resultCode":"404-1","msg":"999번 글은 존재하지 않습니다.","data":null}` 형태로 바뀜

### 2. 새로 등장한 Java/Spring 개념
- **제네릭 `<T>`** : 클래스를 만들 때 타입을 비워두고, 쓸 때 정하는 문법. ([개념 정리 9](#9-제네릭-t))
- **`RsData<Void>`** : "data 에 담을 것이 없다"를 타입으로 표현. ([개념 정리 10](#10-rsdatavoid의-의미))
- **@RestControllerAdvice + @ExceptionHandler** : 컨트롤러에서 던져진 예외를 한 곳에서 받아 응답을 만듭니다.
- **ResponseEntity** : 응답 본문뿐 아니라 HTTP 상태코드까지 직접 정할 수 있는 응답 객체.
- **@JsonIgnore (record 필드)** : `statusCode` 는 HTTP 상태 설정용이라 JSON 에는 넣지 않습니다.
- **커스텀 예외 (extends RuntimeException)**

### 3. 코드 실행 흐름
```text
GET /api/v1/posts/999
 → PostService.getPost(999) → throw new ServiceException("404-1", "999번 글은 ...")
 → 컨트롤러 밖으로 예외 전파
 → GlobalExceptionHandler.handle(ServiceException)
 → RsData<Void>("404-1", msg)  →  ResponseEntity.status(404).body(rsData)
 → {"resultCode":"404-1","msg":"999번 글은 존재하지 않습니다.","data":null}
```

### 4. 중요 코드 설명
```java
public record RsData<T>(String resultCode, @JsonIgnore int statusCode, String msg, T data) {
    public RsData(String resultCode, String msg) { this(resultCode, msg, null); }
    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }
}
```
- `resultCode` 는 `"HTTP상태코드-세부번호"` 규칙입니다. `"404-2"` → HTTP 404, 세부 2번(댓글 없음).
- `statusCode` 를 resultCode 에서 계산하므로 **본문의 코드와 실제 HTTP 상태가 어긋날 수 없습니다.**

### 5. 왜 이런 구조로 만들었는가?
- **조회 성공에는 RsData 를 쓰지 않습니다.** 조회 결과는 데이터 그 자체가 목적이라 `{"resultCode":"200-1","msg":"조회 성공","data":{...}}` 처럼 감싸면 오버스펙입니다. (강의 14강)
- **삭제/실패에는 RsData 를 씁니다.** "무엇이 어떻게 됐는지"를 사람이 읽을 메시지(msg)와 기계가 읽을 코드(resultCode)로 함께 알려줘야 하기 때문입니다.
- 예외 처리를 컨트롤러마다 `try-catch` 로 하지 않고 한 곳에 모았습니다. 새 API 가 늘어도 에러 응답 형식이 자동으로 통일됩니다.
- 강의는 19강 시점에 없는 id 를 `.get()` 으로 처리해서 500 이 납니다(48강에서 개선). 이 과제는 "없는 ID 는 500 이 아니어야 한다"는 기준이 있어 미리 적용했습니다.

### 6. 직접 확인할 수 있는 실습
- <http://localhost:8080/api/v1/posts/999>, <http://localhost:8080/api/v1/posts/1/comments/4>, <http://localhost:8080/api/v1/posts/abc> 를 호출하고 resultCode 비교.
- 브라우저 개발자도구(F12) → Network 탭에서 실제 HTTP 상태코드가 resultCode 앞자리와 같은지 확인.
- `RsDataTest` 를 IntelliJ 에서 실행.

### 7. 이해도 확인 문제
- **Q8-1.** `"404-2"` 라는 resultCode 에서 HTTP 상태코드는 어떻게 결정되나요?
- **Q8-2.** `RsData<Void>` 의 `data` 에는 어떤 값을 넣을 수 있나요?
- **Q8-3.** 글 목록 조회 성공 응답에 RsData 를 쓰지 않은 이유는?

### 8. 다음 커밋과의 연결
응답 형식이 준비됐으니, 이제 **삭제 API** 를 만들고 그 결과를 `RsData<Void>` 로 돌려줍니다. 먼저 댓글 삭제부터 합니다.

---

## 커밋 09 — `a6fb622` feat: implement comment deletion API

### 1. 이번 커밋에서 구현한 기능
`GET /api/v1/posts/{postId}/comments/{id}/delete` : 해당 글에 속한 댓글을 삭제하고 `{"resultCode":"200-1","msg":"2번 댓글이 삭제되었습니다.","data":null}` 을 응답합니다.

### 2. 새로 등장한 Java/Spring 개념
- **@Transactional (쓰기)** : 클래스 기본값은 `readOnly = true` 이므로 삭제 메서드에는 일반 `@Transactional` 을 다시 붙였습니다.
- **orphanRemoval** : 글의 `comments` 리스트에서 빠진 댓글을 JPA 가 DELETE 합니다.
- **변경 감지(더티 체킹)** : 트랜잭션이 끝날 때 JPA 가 "처음 불러왔을 때와 달라진 것"을 찾아 SQL 로 반영합니다. `repository.delete()` 를 직접 부르지 않아도 됩니다.

### 3. 코드 실행 흐름
```text
GET /api/v1/posts/1/comments/2/delete
 → PostCommentController.delete(1, 2)
 → PostCommentService.delete(1, 2)     ── 트랜잭션 시작
     → postService.getPost(1)           (글 확인)
     → getComment(1, 2)                 (이 글의 댓글인지 확인, 아니면 404)
     → post.deleteComment(comment)      (리스트에서 제거)
   ── 트랜잭션 커밋 → orphanRemoval → DELETE FROM post_comment WHERE id = 2
 → RsData<Void>("200-1", "2번 댓글이 삭제되었습니다.")
```

### 4. 중요 코드 설명
```java
@Transactional
public void delete(int postId, int id) {
    Post post = postService.getPost(postId);
    PostComment postComment = getComment(postId, id);  // 소속 확인이 먼저!
    post.deleteComment(postComment);                   // DELETE SQL 은 커밋 때 실행
}
```

### 5. 왜 이런 구조로 만들었는가?
- **소속 확인을 먼저** 합니다. `/posts/1/comments/4/delete` 요청으로 2번 글의 댓글이 지워지면 안 되기 때문입니다.
- `postCommentRepository.delete(comment)` 로 바로 지울 수도 있지만, 그러면 메모리 속 `post.comments` 리스트에는 지워진 댓글이 남아 객체 상태와 DB 가 어긋날 수 있습니다. 강의처럼 **부모를 통해 제거**하는 방식을 택했습니다.
- **삭제를 GET 으로** 만든 것은 과제 요구사항입니다. ([개념 정리 4](#4-http-get과-delete의-차이))

### 6. 직접 확인할 수 있는 실습
- `@Transactional` 을 지우고(=readOnly 상태로) 실행해보기 → 200 응답은 오는데 다시 조회하면 댓글이 **그대로** 있습니다. 트랜잭션이 왜 필요한지 체감할 수 있습니다. (실습 후 원래대로 되돌리기)
- <http://localhost:8080/api/v1/posts/1/comments/4/delete> → 404, 4번 댓글이 남아있는지 확인.
- 콘솔에서 `delete from post_comment where id=?` 로그 확인.

### 7. 이해도 확인 문제
- **Q9-1.** `post.deleteComment()` 는 리스트에서 빼기만 하는데, 왜 DB 에서도 삭제되나요?
- **Q9-2.** 삭제 메서드에 `@Transactional` 이 없으면(readOnly 트랜잭션이면) 어떻게 될까요?
- **Q9-3.** 왜 삭제 전에 "이 글의 댓글인지"를 먼저 확인해야 하나요?

### 8. 다음 커밋과의 연결
댓글은 지울 수 있게 됐습니다. 마지막 API 는 **글 삭제**인데, 글을 지우면 그 글의 댓글은 어떻게 해야 할까요?

---

## 커밋 10 — `f9101a2` feat: implement post deletion API

### 1. 이번 커밋에서 구현한 기능
`GET /api/v1/posts/{id}/delete` : 글을 삭제하고, 그 글의 댓글도 함께 삭제합니다.

### 2. 새로 등장한 Java/Spring 개념
- **cascade = REMOVE** : 부모를 삭제하면 자식도 삭제합니다.
- **외래키 제약조건** : 댓글이 `post_id` 로 글을 가리키고 있으면 글을 먼저 지울 수 없습니다. 그래서 JPA 가 댓글을 먼저 지웁니다.

### 3. 코드 실행 흐름
```text
GET /api/v1/posts/2/delete
 → PostService.delete(2)  ── 트랜잭션
     → getPost(2)
     → postRepository.delete(post)
        → cascade REMOVE : DELETE FROM post_comment WHERE id=4
                           DELETE FROM post_comment WHERE id=5
        → DELETE FROM post WHERE id=2
 → RsData<Void>("200-1", "2번 글이 삭제되었습니다.")
```

### 4. 중요 코드 설명
```java
@Transactional
public void delete(int id) {
    Post post = getPost(id);
    postRepository.delete(post);   // 댓글 삭제는 cascade 가 알아서
}
```

### 5. 왜 이런 구조로 만들었는가?
- 댓글 없이 글만 남는(또는 글 없이 댓글만 남는) "고아 데이터"를 막기 위해 cascade 를 썼습니다.
- 대안 : DB 의 `ON DELETE CASCADE` 외래키 옵션을 쓸 수도 있습니다. 하지만 JPA 메모리 상태와 어긋날 수 있어 JPA cascade 로 통일했습니다.
- 주의 : cascade REMOVE 는 댓글을 하나씩 DELETE 합니다. 댓글이 수만 개라면 느려질 수 있어, 그때는 벌크 삭제 쿼리를 따로 작성합니다(이 과제 범위 밖).

### 6. 직접 확인할 수 있는 실습
- <http://localhost:8080/api/v1/posts/2/delete> 호출 후 콘솔에서 DELETE 순서(post_comment 먼저, post 나중) 확인.
- H2 콘솔 `SELECT * FROM POST_COMMENT WHERE POST_ID = 2;` → 0건.
- `Post.java` 에서 `REMOVE` 를 빼고 실행해보기 → 외래키 위반 오류. (실습 후 되돌리기)

### 7. 이해도 확인 문제
- **Q10-1.** `cascade = REMOVE` 가 없으면 댓글이 있는 글을 삭제할 때 무슨 일이 생기나요?
- **Q10-2.** 삭제 로그에서 post_comment DELETE 가 post DELETE 보다 먼저 나오는 이유는?
- **Q10-3.** 이미 삭제된 글을 다시 삭제 요청하면 어떤 응답이 오나요?

### 8. 다음 커밋과의 연결
API 6개가 모두 완성됐습니다. 지금까지는 브라우저로 하나씩 눌러보며 확인했는데, 코드를 고칠 때마다 이걸 반복할 수는 없습니다. 다음 커밋에서 **자동화된 테스트**를 작성합니다.

---

## 커밋 11 — `1056ce6` test: add API integration and regression tests

### 1. 이번 커밋에서 구현한 기능
MockMvc 로 API 6개를 자동 검증하는 테스트 21개를 작성했습니다. 정상, 404, 400, 다른 글의 댓글 접근, 삭제 후 재조회, 글 삭제 시 댓글 삭제, 샘플 데이터 중복 방지, RsData 제네릭까지 확인합니다.

### 2. 새로 등장한 Java/Spring 개념
- **@SpringBootTest** : 실제 애플리케이션처럼 스프링 전체를 띄워서 테스트.
- **@ActiveProfiles("test")** : 메모리 DB 설정으로 실행 → 개발 DB 안전.
- **@AutoConfigureMockMvc + MockMvc** : 서버를 띄우지 않고 HTTP 요청/응답을 흉내 냅니다.
- **@Transactional (테스트)** : 테스트가 끝나면 **롤백**. 삭제 테스트를 해도 다음 테스트에 영향 없음.
- **jsonPath** : `$.resultCode`, `$[0].id` 처럼 JSON 안의 값을 꺼내 검증.
- **AssertJ `assertThat`** : 값 검증 문법.
- **EntityManager.flush() / clear()** : 아직 DB 에 보내지 않은 변경을 강제로 보내고(flush), 메모리 캐시를 비워(clear) DB 를 직접 다시 조회하게 만듭니다.

### 3. 코드 실행 흐름
```text
gradlew test → 테스트용 스프링 컨텍스트 시작(test 프로필, 메모리 DB)
 → BaseInitData 가 샘플 데이터 생성
 → 각 @Test 메서드 : 트랜잭션 시작 → mvc.perform(get(...)) → andExpect(...) 검증 → 롤백
```

### 4. 중요 코드 설명
```java
mvc.perform(get("/api/v1/posts/1/comments/2/delete"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultCode").value("200-1"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.statusCode").doesNotExist());   // @JsonIgnore 확인

em.flush();
em.clear();
assertThat(postCommentRepository.findById(2)).isEmpty();      // DB 에서 정말 지워졌는지
```

### 5. 왜 이런 구조로 만들었는가?
- **컨트롤러 통합 테스트**를 택했습니다. 요청부터 DB 까지 실제 흐름 그대로 검증하므로, 초보 단계에서 "API 가 약속대로 동작하는가"를 확인하는 데 가장 직관적입니다.
- 모킹(Mockito)은 쓰지 않았습니다. 롤백 덕분에 실제 DB 를 써도 테스트끼리 간섭하지 않기 때문입니다(강의 34강).
- 응답 JSON 의 필드를 꼼꼼히 검사합니다. 필드 이름 하나만 바뀌어도 프론트엔드가 깨지기 때문입니다(강의 39강).

### 6. 직접 확인할 수 있는 실습
- `.\gradlew.bat clean test` 실행 → `build/reports/tests/test/index.html` 을 브라우저로 열어 결과 보기.
- `PostCommentService.delete` 에서 `getComment(postId, id)` 줄을 지우고 테스트 실행 → 어떤 테스트가 실패하는지 확인. (실습 후 되돌리기)
- IntelliJ 에서 테스트 메서드 옆 ▶ 로 하나씩 실행.

### 7. 이해도 확인 문제
- **Q11-1.** 테스트 클래스에 `@Transactional` 을 붙이면 삭제 테스트 후 데이터는 어떻게 되나요?
- **Q11-2.** `em.flush()` 와 `em.clear()` 를 하지 않고 바로 `findById(2)` 를 하면 어떤 문제가 생길 수 있나요?
- **Q11-3.** 테스트가 개발용 `db_dev` 데이터를 바꾸지 않는 이유는?

### 8. 다음 커밋과의 연결
코드와 테스트가 완성됐습니다. 마지막으로 이 프로젝트를 다른 사람(그리고 미래의 나)이 이해할 수 있도록 문서를 씁니다.

---

## 커밋 12 — docs: add project guide and learning roadmap

### 1. 이번 커밋에서 구현한 기능
`README.md`(실행 방법, API 목록, 응답 예시, 제약 사항)와 이 학습 가이드를 추가했습니다.

### 2. 새로 등장한 개념
- **README** : 저장소에 들어온 사람이 가장 먼저 보는 문서. "무엇이고, 어떻게 실행하고, 어떻게 쓰는지"를 담습니다.
- **커밋 메시지 규칙(Conventional Commits)** : `feat:`(기능), `refactor:`(동작 변화 없는 구조 개선), `test:`, `docs:`, `chore:`(설정·잡무).

### 3. 흐름
```text
README (전체 요약) → LEARNING_GUIDE (커밋별 상세) → git show <해시> (실제 코드 변화)
```

### 4~6. 실습
- `git log --oneline --reverse` 의 메시지와 README 의 커밋 표를 비교해보세요.
- 이 가이드의 "직접 확인할 수 있는 실습"을 커밋 01 부터 하나씩 해보세요.

### 7. 이해도 확인 문제
- **Q12-1.** `feat:` 와 `refactor:` 커밋의 차이는?
- **Q12-2.** 커밋 05 가 `feat` 가 아니라 `refactor` 인 이유는?
- **Q12-3.** 하나의 큰 커밋 대신 기능별로 커밋을 나누면 좋은 점은?

### 8. 다음 단계 (스스로 해볼 과제)
강의 21강 이후 내용으로 이어서 연습해볼 수 있습니다.
1. 삭제를 `@DeleteMapping` 으로 바꾸고 테스트도 수정하기 (21강)
2. 글 작성 `POST /api/v1/posts` + `@RequestBody` + `@Valid` (22~24강)
3. 글/댓글 수정 API (31~32강)

---

# 핵심 개념 정리

## 1. Entity와 DTO의 차이

| | Entity | DTO |
|---|---|---|
| 목적 | DB 테이블과 1:1 대응 | 외부(API 응답)로 데이터 전달 |
| 예시 | `Post`, `PostComment` | `PostDto`, `PostCommentDto` |
| 바뀌는 이유 | DB 구조 변경 | API 약속(응답 형식) 변경 |
| 특징 | JPA 가 관리(영속성), 연관관계·지연로딩 있음 | 순수한 값, 불변(record) |

엔티티를 그대로 응답하면 ① 내부 필드가 노출되고 ② 순환참조·지연로딩 문제가 JSON 변환에 끼어들고 ③ DB 구조를 바꾸면 API 까지 바뀝니다.
DTO 는 "보여줄 것만 골라 담은 상자"입니다.

## 2. Controller와 Service의 차이

| | Controller | Service |
|---|---|---|
| 관심사 | HTTP (URL, 메서드, 상태코드, JSON) | 업무 규칙 ("이 글의 댓글인가?", "어떻게 삭제하나?") |
| 아는 것 | 요청 파라미터, DTO, RsData | 엔티티, 리포지터리, 트랜잭션 |
| 이 프로젝트 | `PostController`, `PostCommentController` | `PostService`, `PostCommentService` |

컨트롤러를 얇게 유지하면, 같은 로직을 다른 곳(예: 샘플 데이터 생성, 테스트, 나중에 생길 다른 API)에서 재사용하기 쉽습니다.
`BaseInitData` 가 컨트롤러가 아니라 `PostService.write()` 를 쓰는 것이 그 예입니다.

## 3. JPA Repository가 DB를 다루는 방법

```java
public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    Optional<PostComment> findByPostIdAndId(int postId, int id);
}
```
1. 우리는 **인터페이스만** 만듭니다. 구현 클래스는 없습니다.
2. 스프링 데이터 JPA 가 실행 시점에 구현체(프록시)를 자동으로 만듭니다.
3. 기본 메서드(`save`, `findById`, `findAll`, `delete`, `count`)는 `JpaRepository` 가 제공합니다.
4. 추가 메서드는 **이름 규칙**으로 SQL 을 만듭니다.
   - `findBy` + `PostId` + `And` + `Id` → `WHERE post_id = ? AND id = ?`
   - `OrderByIdAsc` → `ORDER BY id ASC`
5. 실제 SQL 은 Hibernate(JPA 구현체)가 만들어 JDBC 로 DB 에 보냅니다. `show-sql: true` 로 콘솔에서 볼 수 있습니다.

**영속성 컨텍스트** : JPA 가 트랜잭션 동안 불러온 엔티티를 기억하는 메모리 공간입니다. 같은 id 를 두 번 조회하면 두 번째는 DB 에 가지 않고 여기서 꺼내고, 트랜잭션이 끝날 때 바뀐 내용을 모아 SQL 로 보냅니다(변경 감지).

## 4. HTTP GET과 DELETE의 차이

| | GET | DELETE |
|---|---|---|
| 의미 | 조회 (서버 상태를 바꾸지 않음) | 삭제 |
| 안전(safe) | O — 몇 번 불러도 데이터가 바뀌지 않아야 함 | X |
| 브라우저 주소창 | 바로 호출 가능 | 불가 (fetch, curl, Postman 필요) |
| 캐시·미리 불러오기·크롤러 | 마음대로 호출될 수 있음 | 호출되지 않음 |

이 과제는 **브라우저로 바로 확인하기 위해** 삭제를 GET 으로 만들었습니다(강의 11강과 동일).
실무에서 GET 으로 삭제하면, 검색엔진 크롤러가 링크를 따라가거나 브라우저가 링크를 미리 불러오는 것만으로 데이터가 지워질 수 있습니다.
올바른 형태는 `DELETE /api/v1/posts/{id}` 입니다(강의 21강).

## 5. @Transactional이 필요한 이유

트랜잭션은 "모두 성공하거나, 모두 없던 일로 하거나" 묶음입니다.

1. **원자성** : 글 삭제는 "댓글 2개 DELETE + 글 1개 DELETE" 입니다. 중간에 실패하면 댓글만 지워진 이상한 상태가 남으면 안 됩니다.
2. **변경 감지가 동작하는 범위** : `post.deleteComment()` 처럼 객체만 바꾸고 SQL 을 직접 부르지 않는 코드는, 트랜잭션이 커밋될 때 JPA 가 변경을 찾아 SQL 로 바꿔줍니다. 쓰기 트랜잭션이 없으면 아무 일도 일어나지 않습니다.
3. **readOnly = true** : 조회 전용 메서드에 붙이면 변경 감지를 생략해 조금 더 가볍고, "이 메서드는 데이터를 바꾸지 않는다"는 의도가 드러납니다.

주의 : `@Transactional` 은 **스프링 프록시를 거쳐 호출될 때만** 동작합니다. 같은 클래스 안에서 `this.메서드()` 로 부르면 무시됩니다(`BaseInitData` 의 `self` 가 그 해결책).

## 6. JPA 연관관계와 Cascade

```text
post (1) ───< post_comment (N)
              └ post_id (FK)
```
- **@ManyToOne (PostComment.post)** : 외래키를 가진 쪽 = **연관관계의 주인**. 이 필드 값이 `post_id` 칼럼에 저장됩니다.
- **@OneToMany(mappedBy = "post") (Post.comments)** : 주인이 아닌 쪽. 읽기 편의용이며, 이 리스트만 바꿔서는 `post_id` 가 바뀌지 않습니다.
- **cascade = PERSIST** : 글을 저장할 때 리스트에 새로 들어온 댓글도 저장. (샘플 데이터에서 `addComment` 만으로 저장된 이유)
- **cascade = REMOVE** : 글을 삭제하면 댓글도 삭제. (글 삭제 API)
- **orphanRemoval = true** : 리스트에서 빠진 댓글(부모 잃은 고아)을 삭제. (댓글 삭제 API)

## 7. JSON 직렬화

**직렬화(serialization)** = 자바 객체 → JSON 문자열. 반대는 역직렬화.
스프링 부트는 **Jackson** 라이브러리로 이 일을 자동으로 합니다.

- 일반 클래스 : getter(`getTitle()`)를 보고 `"title"` 필드를 만듭니다. getter 가 없으면 필드도 안 나옵니다(강의 8강).
- record : 컴포넌트(`title()`)를 보고 선언 순서대로 필드를 만듭니다.
- `@JsonIgnore` : 해당 필드를 JSON 에서 뺍니다. (`RsData.statusCode`)
- `LocalDateTime` → `"2026-11-16T14:49:42.763385"` (ISO-8601 문자열)
- `null` 값도 기본적으로 출력됩니다. (`"data": null`)

## 8. Java record

```java
public record PostDto(int id, String title) { }
```
은 아래를 자동으로 만들어줍니다.
- `private final int id; private final String title;`
- 모든 필드를 받는 생성자 `PostDto(int id, String title)`
- 조회 메서드 `id()`, `title()` (get 접두사 없음)
- `equals()`, `hashCode()`, `toString()`

특징 : **불변**입니다(setter 없음). 값을 담아 전달만 하는 DTO 에 딱 맞습니다.
추가 생성자는 반드시 `this(...)` 로 기본 생성자를 호출해야 합니다(`PostDto(Post post)` 참고).
엔티티에는 쓰지 않습니다. JPA 는 기본 생성자와 값 변경이 필요하기 때문입니다.

## 9. 제네릭 `<T>`

```java
public record RsData<T>(String resultCode, String msg, T data) { }

RsData<PostDto> a = new RsData<>("200-1", "성공", postDto);
PostDto dto = a.data();          // 형변환 없이 PostDto 로 꺼냄

RsData<Object> b = new RsData<>("200-1", "성공", postDto);
PostDto dto2 = (PostDto) b.data();  // 직접 형변환 필요, 틀리면 실행 중 오류
```
- `T` 는 "나중에 정할 타입" 자리입니다. 쓰는 쪽에서 `<PostDto>` 처럼 채웁니다.
- 장점 : **컴파일 시점에 타입을 검사**하고, 형변환이 필요 없습니다.
- 강의 16~18강의 흐름 : `RsData`(PostCommentDto 전용) → `ForPostRsData` 따로 만들기(중복) → `Object` 로 바꾸기(형변환 필요) → **제네릭**(재사용 + 타입 안전).
- `<>` (다이아몬드) : 오른쪽 타입은 왼쪽에서 추론하므로 생략할 수 있습니다.

## 10. `RsData<Void>`의 의미

- `Void` 는 "값이 없음"을 뜻하는 자바 타입입니다. `Void` 객체는 만들 수 없어서 **넣을 수 있는 값은 `null` 뿐**입니다.
- 그래서 `RsData<Void>` 는 "이 응답에는 data 가 없다"를 **타입으로 약속**합니다.
- 그냥 `RsData` 로 쓰면(로 타입, raw type) `RsData<Object>` 처럼 동작해서 아무거나 넣을 수 있게 되고, 컴파일러 경고도 납니다. 의도를 분명히 하려면 `RsData<Void>` 를 씁니다(강의 18~19강).
- 이 프로젝트의 삭제 API 와 에러 응답이 모두 `RsData<Void>` 입니다.

---

# 정답 모음

### 커밋 01
- **Q1-1.** `application-dev.yaml`. 프로필 전용 파일이 공통 파일보다 우선합니다.
- **Q1-2.** 테스트가 데이터를 지우거나 바꿔도 개발 중인 데이터에 영향을 주지 않게 하려고. 또한 테스트마다 항상 같은 초기 상태에서 시작할 수 있습니다.
- **Q1-3.** `gradlew.bat` 이 `gradle-wrapper.properties` 에 적힌 버전(9.8.2)의 Gradle 을 자동으로 내려받아 실행하기 때문입니다.

### 커밋 02
- **Q2-1.** `post_comment` 테이블. 외래키를 가진 `PostComment.post` 가 연관관계의 주인입니다.
- **Q2-2.** 댓글을 조회할 때 연결된 글은 바로 가져오지 않고, `getPost()` 로 실제 값을 사용하는 순간 조회합니다.
- **Q2-3.** Post 는 comments 를, 각 PostComment 는 다시 post 를 가지고 있어서 Jackson 이 getter 를 따라가다 보면 끝없이 반복되기 때문입니다.

### 커밋 03
- **Q3-1.** 스프링 프록시를 거치지 않으므로 `@Transactional` 이 적용되지 않습니다. 트랜잭션 없이 실행되어 cascade 로 댓글이 저장되지 않는 등 의도와 다르게 동작합니다.
- **Q3-2.** 글(post1)이 트랜잭션 안에서 영속 상태이고 `cascade = PERSIST` 가 걸려 있어서, 커밋 시점에 리스트에 추가된 댓글도 함께 INSERT 되었습니다.
- **Q3-3.** `@Profile({"dev", "test"})` 때문에 `BaseInitData` 빈 자체가 만들어지지 않아 샘플 데이터가 생성되지 않습니다.

### 커밋 04
- **Q4-1.** `@RestController` 는 `@Controller` + `@ResponseBody` 로, 리턴값을 뷰(HTML) 이름이 아니라 응답 본문(JSON)으로 사용합니다.
- **Q4-2.** 값이 없을 때 `.get()` 은 `NoSuchElementException` → 500 오류가 됩니다. `orElseThrow` 로 404 같은 의미 있는 응답을 줄 수 있습니다.
- **Q4-3.** `@PathVariable`.

### 커밋 05
- **Q5-1.** 엔티티를 그대로 응답하면 `password` 도 JSON 에 노출됩니다. DTO 에 필드를 넣지 않으면 엔티티가 바뀌어도 응답에는 나가지 않습니다.
- **Q5-2.** `.map(post -> new PostDto(post))`
- **Q5-3.** 없습니다. record 의 필드는 `final` 이고 setter 가 없습니다. 바꾸려면 새 객체를 만들어야 합니다.

### 커밋 06
- **Q6-1.** 4번 댓글(2번 글 소속)이 1번 글의 댓글인 것처럼 조회됩니다. 삭제에 쓰였다면 다른 글의 댓글을 지울 수 있는 버그가 됩니다.
- **Q6-2.** 3번 글 → 200 + `[]`, 999번 글 → 404 (`404-1`).
- **Q6-3.** `post_comment.post_id` 칼럼 (`post` 연관관계의 `id`).

### 커밋 07
- **Q7-1.** 더 이상 엔티티를 JSON 으로 변환하지 않기 때문입니다. DTO 에는 Post 객체가 아니라 숫자 `postId` 만 들어있습니다.
- **Q7-2.** Post 엔티티가 다시 JSON 으로 변환되면서 comments → post → … 순환참조와 지연로딩 문제가 다시 생길 수 있습니다.
- **Q7-3.** 없습니다. 모든 조회 API 가 DTO 를 응답합니다.

### 커밋 08
- **Q8-1.** `"404-2".split("-", 2)[0]` → `"404"` → 정수 404 가 `statusCode` 가 되고, `GlobalExceptionHandler` 가 `ResponseEntity.status(404)` 로 응답합니다.
- **Q8-2.** `null` 만 넣을 수 있습니다.
- **Q8-3.** 조회 결과는 데이터 자체가 목적이라 resultCode/msg 로 감싸는 것은 불필요한 오버스펙입니다. 실패한 경우에만 RsData 로 이유를 알려줍니다.

### 커밋 09
- **Q9-1.** `orphanRemoval = true` 때문에, 트랜잭션 커밋 시 JPA 가 "리스트에서 빠진 자식"을 찾아 DELETE 합니다.
- **Q9-2.** 변경 감지가 일어나지 않아 DELETE SQL 이 실행되지 않습니다. 응답은 200 인데 실제로는 삭제되지 않는 버그가 됩니다.
- **Q9-3.** 확인하지 않으면 `/posts/1/comments/4/delete` 처럼 다른 글의 댓글을 지울 수 있기 때문입니다.

### 커밋 10
- **Q10-1.** 댓글의 `post_id` 가 글을 가리키고 있어서 외래키 제약조건 위반 오류가 발생하고 글이 삭제되지 않습니다.
- **Q10-2.** 외래키 때문에 자식(댓글)을 먼저 지워야 부모(글)를 지울 수 있고, cascade REMOVE 가 그 순서로 처리하기 때문입니다.
- **Q10-3.** 404 + `{"resultCode":"404-1","msg":"N번 글은 존재하지 않습니다.","data":null}`.

### 커밋 11
- **Q11-1.** 테스트가 끝나면 롤백되어 원래 샘플 데이터(글 3, 댓글 5)로 돌아갑니다.
- **Q11-2.** 삭제 SQL 이 아직 DB 에 보내지지 않았거나, 영속성 컨텍스트에 남아있는 객체를 돌려받아 "DB 에서 정말 지워졌는지" 를 제대로 확인하지 못할 수 있습니다.
- **Q11-3.** `@ActiveProfiles("test")` 로 `application-test.yaml` 의 메모리 DB(`jdbc:h2:mem:db_test`)를 사용하기 때문입니다.

### 커밋 12
- **Q12-1.** `feat` 는 새로운 기능(동작 변화)을 추가, `refactor` 는 겉으로 보이는 기능은 그대로 두고 내부 구조만 개선합니다.
- **Q12-2.** 글 조회 API 라는 기능은 이미 있었고, 응답을 DTO 로 바꿔 구조를 개선한 것이기 때문입니다. (엄밀히는 `comments` 필드가 빠지는 응답 변화가 있지만, 핵심 목적은 구조 개선입니다.)
- **Q12-3.** 변경 이유를 커밋 단위로 추적할 수 있고, 문제가 생긴 커밋만 되돌리거나(revert) 리뷰하기 쉬우며, 이 가이드처럼 단계별 학습 자료가 됩니다.
