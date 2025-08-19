package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.D_Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
