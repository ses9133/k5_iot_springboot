package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.D_Post.request.PostCreateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.request.PostUpdateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostDetailResponseDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostListResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.D_Post;
import com.example.k5_iot_springboot.repository.D_PostRepository;
import com.example.k5_iot_springboot.service.D_PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 읽기 전용 (클래스 기본) - 변경 메서드만 @Transactional
public class D_PostServiceImpl implements D_PostService {

    private final D_PostRepository postRepository;

    // 1) 게시글 생성
    @Override
    @Transactional // 쓰기 트랜잭션
    public ResponseDto<PostDetailResponseDto> createPost(PostCreateRequestDto dto) {
        // 생성된 DTO 자체가 null 인지 즉시 방어 (null 인 경우 NPE 발생)
        Objects.requireNonNull(dto, "PostCreateRequestDto must not be null");

        String title = dto.title().trim();
        String content = dto.content().trim();
        String author = dto.author().trim();

        D_Post post = D_Post.create(title, content, author);
        D_Post saved = postRepository.save(post);
        return ResponseDto.setSuccess("SUCCESS", PostDetailResponseDto.from(saved));
    }

    // 2) 단건 조회
    @Override
    public ResponseDto<PostDetailResponseDto> getPostById(Long id) {
        Long pid = requirePositiveId(id);
        D_Post post = postRepository.findByIdWithComments(pid)
                .orElseThrow(() -> new EntityNotFoundException("해당 id 의 게시글을 찾을 수 없습니다."));
        return ResponseDto.setSuccess("SUCCESS", PostDetailResponseDto.from(post));
    }

    // 3) 전체 조회
    @Override
    public ResponseDto<List<PostListResponseDto>> getAllPosts() {
        List<D_Post> posts = postRepository.findAllOrderByIdDesc(); // 최신순
        List<PostListResponseDto> result = posts.stream()
                .map(PostListResponseDto::from)
                .map(dto -> dto.summarize(5))
                .toList();
        return ResponseDto.setSuccess("SUCCESS",result);
    }

    // 4) 게시글 수정
    @Override
    @Transactional
    public ResponseDto<PostDetailResponseDto> updatePost(Long id, PostUpdateRequestDto dto) {
        Objects.requireNonNull(dto, "PostUpdateRequestDto must not be null");
        Long pid = requirePositiveId(id);
        D_Post post = postRepository.findByIdWithComments(pid)
                .orElseThrow(() -> new EntityNotFoundException("해당 id 의 게시글을 찾을 수 없습니다."));
        post.changeTitle(dto.title().trim());
        post.changeContent(dto.content().trim());

        // save 생략되어있음. Dirty Checking 으로 저장 (영속성 컨텍스트에 담긴 엔티티의 상태 변화를 자동 감지)

        return ResponseDto.setSuccess("SUCCESS", PostDetailResponseDto.from(post));
    }

    // 5) 게시글 삭제
    @Override
    @Transactional
    public ResponseDto<Void> deletePost(Long id) {
        D_Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 id의 게시글을 찾을 수 없습니다."));
        postRepository.delete(post);

        //orphanRemoval, Cascade 설정으로 댓글을 자동 정리됨

        return ResponseDto.setSuccess("SUCCESS", null);
    }

    // ===================================================================== //

    // 6)
    @Override
    public ResponseDto<List<PostListResponseDto>> getPostsByAuthor(String author) {
        return null;
    }

    // == 내부 유틸 메서드 ===//
    private Long requirePositiveId(Long id) {
        if(id == null || id <= 0) throw new IllegalArgumentException("id는 반드시 양수여야합니다.");
        return id;
    }

    private String requireNonBlank(String s, String fieldName) {
        if(!StringUtils.hasText(s)) throw new IllegalArgumentException(fieldName + " 비워질 수 없습니다.");
        // StringUtils.hasText(s)
        // : 스프링프레임워크 제공 메서드
        // : 문자열이 "의미있는 글자"를 가지고 있는지를 확인

        // hasText(s)
        // : null 이면 false, s.length() == 0 이면 false, s가 공백 문자만 있으면 false
        // 그 외 텍스트가 있으면 true

        return s;
    }
}