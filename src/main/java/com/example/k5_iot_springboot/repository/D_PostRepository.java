package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.dto.D_Post.response.PostWithCommentCountResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.D_Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*
    Post 와 Comment 의 관계가 1:N 의 관계

    D_Post post = postRepo.findById(id).get();
    post.getComments.forEach(...); // 댓글 접근

    == 코드 풀이 ==
    1) 첫 번째 쿼리: select * from posts where id = ?
    2) 두 번째 쿼리: LAZY 설정코드를 여러번 실행할 때마다 초기화를 위한 SELECT 문이 별도로 실행됨

    ## 상황1) 단일 POST만 조회하는 경우
    -- 1번째 쿼리
    select * from posts where id = ?
    -- 2번째 쿼리: 이후 post.getComments() 처음 호출시,
    댓글 컬렉션 초기화용으로 딱 1번 실행됨
    select * from comments where post_id = ?

    ## 상황2) Post 를 N개 먼저 가져온 뒤 각 Post 마다 getComments() 호출하는 경우
    -- 1번째 쿼리
    select * from posts limit 20;
    -- 2번째 쿼리
    select * from comments where post_id = ? (총 20번 실행)

    1번째 쿼리 1번 + 2번재 쿼리 N번이 실행됨
    => 1 + N  문제 발생
 */

@Repository
public interface D_PostRepository extends JpaRepository<D_Post, Long> {

    // 게시글 조회 + 댓글까지 즉시 로딩

    // 댓글까지 즉시 로딩 (상황1)
    @Query("""
        select distinct p 
        from D_Post p
            left join fetch p.comments c
        where p.id = :id
""")
    Optional<D_Post> findByIdWithComments(@Param("id") Long id);

    // 상황2
    // 전체 조회(댓글 제외)
    @Query("""
        select p
        from D_Post p
        order by p.id desc
""")
    List<D_Post> findAllOrderByIdDesc();

    // ================= 필터링 & 검색 ========================= //
    // 1) 쿼리 메서드 사용 (Query Method)
    // : Spring Data JPA 가 메서드 명을 파싱하여 JPQL 을 자동 생성
    // ex1) findByAuthorOrderByIdDesc -> where author = ? + order by id desc
    // ex2) findByTitleLikeIgnoreCaseOrderByIdDesc -> where lower(title) like lower(?) + order by id desc
    List<D_Post> findByAuthorOrderByIdDesc(String author);

    List<D_Post> findByTitleContainingIgnoreCaseOrderByIdDesc(String keyword);

    // +) 8) 댓글이 가장 많은 상위 N개
    //      : 쿼리 메서드 만으로는 집계/정렬 불가 -> JPQL 또는 Native Query 사용

    // === 2) JPQL(@Query) ===
    // : 그룹핑/집계/조인/서브쿼리 등 쿼리메서드로 표현이 어려운 경우 명시적으로 JPQL 을 작성
    // - 엔티티명/필드명을 기준으로 작성(테이블명X) (DB 에 독립적임)
    // - 특정 작성자의 모든 게시글(최신글 우선)
    @Query("""
        SELECT P
        FROM D_Post P
        WHERE P.author = :author
        ORDER BY P.id DESC
""")
    List<D_Post> findByAuthorOrderByIdDesc_Jpql(@Param("author") String author);
    // :author => {author} (동적으로 파라미터 받는 부분은 : 표시)

    // === 3) Native SQL ====
    // : DB 벤더 특화 기능 (극한 성능이 필요한 경우)
    // - 복잡한 통계/랭킹/리포트에 SQL 가독성이 JPQL 보다 뛰어남
    // 엔티티명이 아닌 "테이블명" 기준으로 작성("DB 기준"으로 작성)

    // +) 매핑 전략
    // [1] 엔티티 반환 - 엔티티 필드와 동일한 별칭으로 모든 컬럼 선택후 반환
    //      >> 일부 컬럼만 선택할 경우 매핑 실패 또는 지연 로딩 문제가 발생

    // [2] 인터페이스 프로젝션 (권장)
    //      >> 결과 컬럼 별칭 <-> 인터페이스의 getter 이름 매칭으로 타입 세이프 (캐스팅 불필요함)

    // [3] 인터페이스 프로젝션을 사용하지 않으면, Object 객체로 반환받기 때문에
    //                                          >> 각 필드별로 형변환 과정이 필요
    // 6) 특정 작성자의 모든 게시글(id desc)
    @Query(value = """
        select *
        from posts
        where author = :author
        order by id desc
""", nativeQuery = true)
    List<D_Post> findByAuthorOrderByIdDesc_Native(@Param("author") String author);

    // 7) 제목 키워드 검색(JPQL , Native SQL 사용)
    /*
        (일반 SQL)
        SELECT * FROM posts
        WHERE
            title LIKE %keyword%
        ORDER BY
            id DESC;
     */

    // (JPQL 사용)
//    @Query("""
//        select P
//        from D_Post p
//        where
//            lower(P.title) like lower(concat('%', :keyword, '%'))
//        order by
//            P.id desc
//""")
//    List<D_Post> searchByTitleKeyword_Jpql(@Param("keyword") String keyword);

    // (Native SQL 사용)
    @Query(value = """
        select * 
        from posts
        where 
            title like concat('%', :keyword, '%')
        order by 
            id desc
""", nativeQuery = true)
    List<D_Post> searchByTitleKeyword_Native(@Param("keyword") String keyword);

//     8) 댓글이 가장 많은 상위 N개(JPQL , Native SQL 사용)
    // (JPQL 사용)
//    @Query("""
//        select P as post, count(C.id) as cnt
//        from
//            D_Post P
//            left join D_Comment C
//            ON C.post = P
//        group by P
//        order by cnt desc, P.id desc
//""")
//    List<Object[]> findTopPostsByCommentCount_Jpql(); // 출력값이 post, cnt 두개라서 Object 로 반환해야함
    // Object[]
    // : [0] - D_Post, [1] - Number 타입 (댓글 수)

    // (Native SQL 사용) - 인터페이스 프로젝션 사용
    public interface PostWithCommentCountProjection {

        Long getPostId(); // posts.id
        String getTitle();
        String getAuthor();
        Long getCommentCount(); // count(c.id)
    }

    @Query(value = """
        SELECT 
            p.id as postId,
            p.title as title,
            p.author as author,
            count(c.id) as commentCount
        FROM
            posts p 
            LEFT JOIN comments c
            ON c.post_id = p.id
        GROUP BY
            p.id, p.title, p.author
        ORDER BY 
            commentCount DESC, p.id DESC
        LIMIT :limit
""", nativeQuery = true)

    List<PostWithCommentCountProjection> findTopPostsByCommentCount_Native(@Param("limit") int limit);

    // 9) 특정 키워드를 포함한 댓글이 달린 게시글들 조회
    public interface PostListProjection {
        Long getId();
        String getTitle();
        String getAuthor();
    }

    @Query(value = """
        SELECT 
            P.id as id,
            P.title as title,
            P.author as author
        FROM
            posts P
            LEFT JOIN comments C
            ON C.post_id = P.id
        WHERE
            C.content LIKE concat('%', :keyword, '%')
        GROUP BY
            P.id, P.title, P.author
        ORDER BY
            P.id desc
""", nativeQuery = true)
    List<PostListProjection> findByCommentKeyword(@Param("keyword") String keyword);

    // 10) 특정 작성자의 게시글 중, 댓글 수가 minCount 이상인 게시글 조회
//    @Query(value = """
//    SELECT
//        P.id as id,
//        P.title as title,
//        P.author as author,
//        count(C.id) as cnt
//    FROM
//        posts P
//        LEFT JOIN comments C
//        ON C.post_id = P.id
//    WHERE
//        P.author = :author
//    GROUP BY
//        P.id
//    HAVING
//        COUNT(*) >= :minCount;
//""", nativeQuery = true)
//
//    List<PostWithCommentCountProjection> findAuthorPostsWithMinCount(
//            @Param("author") String author,
//            @Param("minCount") int minCount
//    );

    @Query(value = """
    SELECT 
        p.id        AS postId,
        p.title     AS title,
        p.author    AS author,
        COUNT(c.id) AS commentCount
    FROM 
        posts p
        LEFT JOIN comments c 
                ON c.post_id = p.id
    WHERE 
        p.author = :author
    GROUP BY 
        p.id, p.title, p.author
    HAVING 
        COUNT(c.id) >= :minCount
    ORDER BY 
        commentCount DESC, p.id DESC
    """, nativeQuery = true)
    List<PostWithCommentCountProjection> findAuthorPostsWithMinCount(
            @Param("author") String author,
            @Param("minCount") int minCount
    );












}
