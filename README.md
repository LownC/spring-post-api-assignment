# spring-post-api-assignment

Spring Boot + Spring Data JPA + H2 로 만든 **게시글 / 댓글 조회·삭제 REST API** 과제 프로젝트입니다.
커밋 하나하나가 학습 단계가 되도록 기능 단위로 나눠서 구현했습니다.
코드를 처음부터 따라 읽고 싶다면 [docs/LEARNING_GUIDE.md](docs/LEARNING_GUIDE.md) 부터 보세요.

---

## 1. 프로젝트 소개

- 게시글(Post) 과 댓글(PostComment) 을 **조회**하고 **삭제**하는 API 6개를 제공합니다.
- 서버를 켜면 개발용 샘플 데이터(글 3개, 댓글 5개)가 자동으로 만들어집니다.
- 조회 성공 응답은 DTO(`PostDto`, `PostCommentDto`) 를 그대로, 삭제와 실패 응답은 공통 형식 `RsData<T>` 로 응답합니다.
- 글/댓글 작성·수정 API 는 과제 범위가 아니라서 만들지 않았습니다.

## 2. 사용 기술

| 구분 | 버전 / 내용 |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 (Spring Web MVC, Spring Data JPA, Validation) |
| Build | Gradle 9.8.2 (Wrapper, Kotlin DSL `build.gradle.kts`) |
| DB | H2 2.3 (dev: 파일 DB, test: 메모리 DB) |
| 기타 | Lombok, JUnit 5, MockMvc, AssertJ |

## 3. 실행 방법

Gradle 을 따로 설치할 필요가 없습니다. 프로젝트에 포함된 Gradle Wrapper(`gradlew`)를 사용합니다.

```powershell
# Windows (PowerShell)
.\gradlew.bat bootRun
```

```bash
# macOS / Linux / Git Bash
./gradlew bootRun
```

- 서버 주소 : <http://localhost:8080>
- IntelliJ 에서는 `src/main/java/com/back/BackApplication.java` 의 `main` 옆 ▶ 버튼으로 실행해도 됩니다.
- 실행 프로필 기본값은 `dev` 입니다. (`application.yaml` 의 `spring.profiles.active`)

## 4. H2 DB 설정

| 프로필 | 설정 파일 | JDBC URL | 특징 |
|---|---|---|---|
| dev | `application-dev.yaml` | `jdbc:h2:./db_dev;MODE=MySQL` | 프로젝트 폴더의 `db_dev.mv.db` 파일에 저장. 서버를 재시작해도 데이터 유지 |
| test | `application-test.yaml` | `jdbc:h2:mem:db_test;MODE=MySQL` | 메모리 DB. 테스트가 끝나면 사라짐. 개발 DB 에 영향 없음 |

**H2 콘솔** (dev 프로필에서만 활성화)

1. 서버 실행 후 <http://localhost:8080/h2-console> 접속
2. JDBC URL : `jdbc:h2:./db_dev;MODE=MySQL`, User Name : `sa`, Password : (비워둠)
3. `SELECT * FROM POST;`, `SELECT * FROM POST_COMMENT;` 로 데이터 확인

> 데이터를 처음 상태로 되돌리고 싶다면 서버를 끈 뒤 `db_dev.mv.db` 파일을 지우고 다시 실행하세요.
> (`.gitignore` 에 등록되어 있어서 Git 에는 올라가지 않습니다.)

## 5. 샘플 데이터

`global/initData/BaseInitData.java` 가 서버 시작 직후 한 번 실행됩니다.

| 글 id | 제목 | 내용 | 댓글 (id: 내용) |
|---|---|---|---|
| 1 | 제목 1 | 내용 1 | 1: 댓글 1-1, 2: 댓글 1-2, 3: 댓글 1-3 |
| 2 | 제목 2 | 내용 2 | 4: 댓글 2-1, 5: 댓글 2-2 |
| 3 | 제목 3 | 내용 3 | (없음) |

- `dev`, `test` 프로필에서만 동작합니다. (`@Profile({"dev", "test"})`)
- 글이 이미 1개 이상 있으면 아무것도 하지 않습니다. → 재시작해도 데이터가 **중복 생성되지 않고**, 기존 데이터를 **지우지도 않습니다**.
- 삭제 API 로 데이터를 지운 뒤 다시 보고 싶다면 위의 "처음 상태로 되돌리기" 를 하세요.

## 6. API 목록

| # | Method | URL | 설명 | 성공 응답 |
|---|---|---|---|---|
| 1 | GET | `/api/v1/posts` | 글 목록 (id 오름차순) | `PostDto[]` |
| 2 | GET | `/api/v1/posts/{id}` | 글 단건 | `PostDto` |
| 3 | GET | `/api/v1/posts/{id}/delete` | 글 삭제 (소속 댓글도 삭제) | `RsData<Void>` |
| 4 | GET | `/api/v1/posts/{postId}/comments` | 글의 댓글 목록 (id 오름차순) | `PostCommentDto[]` |
| 5 | GET | `/api/v1/posts/{postId}/comments/{id}` | 글의 댓글 단건 | `PostCommentDto` |
| 6 | GET | `/api/v1/posts/{postId}/comments/{id}/delete` | 글의 댓글 삭제 | `RsData<Void>` |

> ⚠️ **삭제를 GET 으로 만든 것은 실습용 설계입니다.**
> 브라우저 주소창만으로 바로 결과를 확인할 수 있게 하려고 과제 요구사항대로 GET 을 사용했습니다.
> 실제 서비스에서는 `DELETE /api/v1/posts/{id}` 처럼 DELETE 메서드를 써야 합니다.
> GET 은 "읽기만 하는 안전한 요청"으로 취급되어 브라우저 미리 불러오기, 검색엔진 크롤러, 캐시 등이 마음대로 호출할 수 있기 때문입니다.

### 에러 응답 규칙

| 상황 | HTTP | resultCode | msg 예시 |
|---|---|---|---|
| 없는 글 | 404 | `404-1` | `999번 글은 존재하지 않습니다.` |
| 없는 댓글 / 다른 글의 댓글 | 404 | `404-2` | `1번 글에 4번 댓글은 존재하지 않습니다.` |
| id 자리에 숫자가 아닌 값 | 400 | `400-1` | `'id' 값이 올바르지 않습니다.` |

`resultCode` 의 앞 3자리는 항상 실제 HTTP 상태코드와 같습니다.

## 7. API 호출 예시

브라우저 주소창에 그대로 붙여넣거나 curl 로 호출합니다.

```bash
curl http://localhost:8080/api/v1/posts
curl http://localhost:8080/api/v1/posts/1
curl http://localhost:8080/api/v1/posts/1/comments
curl http://localhost:8080/api/v1/posts/1/comments/2
curl http://localhost:8080/api/v1/posts/1/comments/2/delete
curl http://localhost:8080/api/v1/posts/2/delete
```

PowerShell 에서는 `curl.exe` 또는 `Invoke-RestMethod` 를 사용하세요.

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/posts
```

## 8. 응답 JSON 예시

**GET /api/v1/posts**  (200)

```json
[
  {
    "id": 1,
    "createDate": "2026-11-16T14:49:42.763385",
    "modifyDate": "2026-11-16T14:49:42.763385",
    "title": "제목 1",
    "content": "내용 1"
  },
  { "id": 2, "...": "..." },
  { "id": 3, "...": "..." }
]
```

**GET /api/v1/posts/1/comments/2**  (200)

```json
{
  "id": 2,
  "createDate": "2026-11-16T14:49:42.815504",
  "modifyDate": "2026-11-16T14:49:42.815504",
  "postId": 1,
  "content": "댓글 1-2"
}
```

**GET /api/v1/posts/1/comments/2/delete**  (200)

```json
{
  "resultCode": "200-1",
  "msg": "2번 댓글이 삭제되었습니다.",
  "data": null
}
```

**GET /api/v1/posts/999**  (404)

```json
{
  "resultCode": "404-1",
  "msg": "999번 글은 존재하지 않습니다.",
  "data": null
}
```

## 9. 디렉터리 구조

```text
spring-post-api-assignment
├── build.gradle.kts                  # 의존성, Java 버전
├── settings.gradle.kts
├── gradlew, gradlew.bat, gradle/     # Gradle Wrapper
├── docs/
│   └── LEARNING_GUIDE.md             # 커밋별 학습 가이드
└── src
    ├── main
    │   ├── java/com/back
    │   │   ├── BackApplication.java              # 시작점 (@EnableJpaAuditing)
    │   │   ├── domain
    │   │   │   ├── post
    │   │   │   │   ├── controller/PostController.java
    │   │   │   │   ├── dto/PostDto.java
    │   │   │   │   ├── entity/Post.java
    │   │   │   │   ├── repository/PostRepository.java
    │   │   │   │   └── service/PostService.java
    │   │   │   └── postComment
    │   │   │       ├── controller/PostCommentController.java
    │   │   │       ├── dto/PostCommentDto.java
    │   │   │       ├── entity/PostComment.java
    │   │   │       ├── repository/PostCommentRepository.java
    │   │   │       └── service/PostCommentService.java
    │   │   └── global
    │   │       ├── exception/GlobalExceptionHandler.java   # 예외 → RsData 응답
    │   │       ├── exception/ServiceException.java
    │   │       ├── initData/BaseInitData.java              # 샘플 데이터
    │   │       ├── jpa/entity/BaseEntity.java              # id, createDate, modifyDate
    │   │       └── rsData/RsData.java                      # 공통 응답
    │   └── resources
    │       ├── application.yaml
    │       ├── application-dev.yaml
    │       └── application-test.yaml
    └── test/java/com/back
        ├── BackApplicationTests.java
        ├── domain/post/controller/PostControllerTest.java
        ├── domain/postComment/controller/PostCommentControllerTest.java
        └── global
            ├── initData/BaseInitDataTest.java
            └── rsData/RsDataTest.java
```

계층별 역할

| 계층 | 하는 일 |
|---|---|
| Controller | HTTP 요청을 받고, 결과를 JSON 으로 응답 |
| Service | 비즈니스 로직, 트랜잭션 경계 |
| Repository | DB 접근 (Spring Data JPA) |
| Entity | DB 테이블과 대응되는 JPA 객체 |
| DTO | API 응답 전용 데이터 (record) |
| RsData | 삭제/실패 응답의 공통 형식 |

## 10. 커밋별 구현 내용

`git log --oneline --reverse` 로 순서대로 볼 수 있습니다. 각 커밋은 `git show <해시>` 또는 IntelliJ 의 Git 탭에서 확인하세요.

| # | 해시 | 커밋 메시지 | 내용 |
|---|---|---|---|
| 01 | `6e2c753` | chore: initialize Spring Boot project | Spring Boot 4.1.2 / Java 25 / Gradle, dev·test 프로필, H2 |
| 02 | `1d8f877` | feat: add post and comment entities | BaseEntity, Post 1:N PostComment, cascade, orphanRemoval, Repository |
| 03 | `8ac8384` | feat: initialize sample post and comment data | BaseInitData (글 3, 댓글 5, 중복 방지) |
| 04 | `c62033c` | feat: implement post read APIs | 글 목록·단건 API (엔티티 직접 응답) |
| 05 | `ff26b41` | refactor: introduce PostDto for API responses | PostDto record 로 응답 교체 |
| 06 | `3388875` | feat: implement comment read APIs | 댓글 목록·단건 API, 다른 글의 댓글 접근 차단 |
| 07 | `8403194` | refactor: introduce PostCommentDto for API responses | PostCommentDto, 엔티티의 @JsonIgnore 제거 |
| 08 | `7f96057` | feat: introduce generic RsData response | RsData&lt;T&gt;, ServiceException, GlobalExceptionHandler |
| 09 | `a6fb622` | feat: implement comment deletion API | 댓글 삭제 (orphanRemoval + @Transactional) |
| 10 | `f9101a2` | feat: implement post deletion API | 글 삭제 (cascade REMOVE 로 댓글도 삭제) |
| 11 | `1056ce6` | test: add API integration and regression tests | MockMvc 통합 테스트 21개 |
| 12 | (이 문서) | docs: add project guide and learning roadmap | README, 학습 가이드 |

## 11. 테스트 실행 방법

```powershell
.\gradlew.bat clean test     # 테스트만
.\gradlew.bat build          # 컴파일 + 테스트 + jar 생성
```

- 테스트는 `test` 프로필(메모리 DB)로 실행되므로 개발용 `db_dev` 데이터는 바뀌지 않습니다.
- 각 테스트는 `@Transactional` 로 끝나면 롤백되어 서로 영향을 주지 않습니다.
- 결과 리포트 : `build/reports/tests/test/index.html`
- IntelliJ 에서는 `src/test/java` 폴더 우클릭 → Run 'All Tests'

| 테스트 클래스 | 개수 | 검증 내용 |
|---|---|---|
| PostControllerTest | 6 | 글 목록/단건/404/400/삭제(+소속 댓글 삭제 확인) |
| PostCommentControllerTest | 9 | 댓글 목록/빈 목록/단건/404/다른 글의 댓글 접근/삭제 후 재조회/다른 글의 댓글 삭제 차단 |
| BaseInitDataTest | 2 | 샘플 데이터 개수·관계, 재실행 시 중복 방지 |
| RsDataTest | 3 | resultCode → statusCode, `RsData<Void>`, 제네릭 타입 |
| BackApplicationTests | 1 | 애플리케이션 컨텍스트 로딩 |

## 12. 알려진 제약 사항

- **삭제가 GET 입니다.** 과제 요구사항에 따른 실습용 설계입니다. (6번 항목 참고)
- 인증/권한이 없습니다. 누구나 모든 글과 댓글을 삭제할 수 있습니다.
- 글/댓글 작성·수정 API 가 없습니다. (과제 범위 밖)
- 목록 조회에 페이징이 없습니다. 데이터가 많아지면 한 번에 전부 응답합니다.
- `spring.jpa.hibernate.ddl-auto: update` 는 학습용 설정입니다. 운영 환경에서는 Flyway 같은 마이그레이션 도구를 씁니다.
- `spring.jpa.open-in-view` 가 기본값(true)이라 시작 시 경고 로그가 한 줄 출력됩니다. 동작에는 문제가 없습니다.
- H2 파일 DB 는 한 번에 하나의 프로세스만 열 수 있습니다. 서버가 켜져 있으면 다른 프로그램에서 `db_dev` 파일을 열 수 없습니다. (`Database may be already in use` 오류)

## 13. 강의 코드와 다르게 구현한 부분

참고 강의 : [slog.gg/p/14172](https://www.slog.gg/p/14172) / [jhs512/p-14172-2](https://github.com/jhs512/p-14172-2) (19강 시점)

| 항목 | 강의 | 이 프로젝트 | 이유 |
|---|---|---|---|
| 패키지 | `domain/post/post`, `domain/post/postComment` | `domain/post`, `domain/postComment` | 과제 명세의 디렉터리 구조를 따름 |
| 컨트롤러 이름 | `ApiV1PostController` | `PostController` | 과제 명세의 파일명을 따름 (버전은 URL `/api/v1` 로 표현) |
| `@Transactional` 위치 | 컨트롤러 액션 메서드 | 서비스 메서드 | 트랜잭션은 비즈니스 로직 단위라서 서비스에 두는 것이 일반적 |
| 댓글 서비스 | `PostService` 가 댓글까지 처리 | `PostCommentService` + `PostCommentRepository` 쿼리 | 과제 명세의 계층 구조. `post_id AND id` 조건으로 "그 글의 댓글"만 조회 |
| 없는 id 처리 | `Optional.get()` → 500 (19강 시점) | `ServiceException` → 404 + RsData | 과제 품질 기준 "없는 ID 는 500 이 아니어야 함" (강의 48강에서 다루는 방식과 같은 방향) |
| `PostCommentDto` | `postId` 없음 | `postId` 포함 | 응답만 보고 소속 글을 알 수 있게 |
| 샘플 데이터 | 모든 프로필 | `@Profile({"dev", "test"})` | 과제 요구사항 "개발·테스트 환경에서만" |
| 목록 순서 | `findAll()` | `findAllByOrderByIdAsc()`, `@OrderBy("id ASC")` | 과제 요구사항 "일관된 결과 순서" |
| 범위 | SpringDoc, devtools, AOP(ResponseAspect) 등 포함 | 포함하지 않음 | 과제 범위 밖 기능은 추가하지 않음 |
