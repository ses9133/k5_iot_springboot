package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.D_Post.request.PostCreateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.request.PostUpdateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostDetailResponseDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostListResponseDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostWithCommentCountResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.D_Post;
import com.example.k5_iot_springboot.repository.D_PostRepository;
import com.example.k5_iot_springboot.service.D_PostService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

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

    // 6) 특정 작성자의 모든 게시글
    @Override
    public ResponseDto<List<PostListResponseDto>> getPostsByAuthor(String author) {
        List<D_Post> posts = postRepository.findByAuthorOrderByIdDesc(author);
        List<PostListResponseDto> result = posts.stream()
                .map(PostListResponseDto::from)
                .toList();
        return ResponseDto.setSuccess("SUCCESS", result);
    }

    // 7) 제목 키워드 으로 검색
    @Override
    public ResponseDto<List<PostListResponseDto>> searchPostsByTitle(String keyword) {
        List<D_Post> posts = postRepository.findByTitleContainingIgnoreCaseOrderByIdDesc(keyword);
        List<PostListResponseDto> result = posts.stream()
                .map(PostListResponseDto::from)
                .toList();
        return ResponseDto.setSuccess("SUCCESS", result);
    }

    // 8) 댓글 개수 순 탑5 조회
    @Override
    public ResponseDto<List<PostWithCommentCountResponseDto>> getTop5PostsByComments() {
        // var: 지역 변수 타입 추론(Java 10 버전 이상)
        // 장점 - 반환 타입의 길이가 길 경우, 간결하게 작성됨
        // 단점 - 타입을 숨겨버려 가독성이 저하될 수도 있음
        var rows = postRepository.findTopPostsByCommentCount_Native(5);

        List<PostWithCommentCountResponseDto> result = rows.stream()
                .map(PostWithCommentCountResponseDto::from) // 오버로딩 사용함
                .toList();
        return ResponseDto.setSuccess("SUCCESS", result);
    }

    // 9) 특정 댓글이 포함된 게시글들 조회
    @Override
    public ResponseDto<List<PostListResponseDto>> searchPostsByCommentKeyword(String keyword) {
        // 1) 입력값 정제/검증
        String clean = (keyword == null) ? "" : keyword.trim();
        if(clean.isEmpty()) {

            throw new IllegalArgumentException("검색키워드는 비워둘 수 없습니다.");
        }
        if(clean.length() > 100) {
            throw new IllegalArgumentException("검색 키워드는 100 자 이하여야합니다.");
        }

        var rows = postRepository.findByCommentKeyword(clean);
        List<PostListResponseDto> result = rows.stream()
                .map(PostListResponseDto::from)
                .toList();

        return ResponseDto.setSuccess("SUCCESS", result);
    }

    // 10) 특정 작성자의 게시글 중, 댓글 수가 minCount 이상인 게시글 조회
    @Override
    public ResponseDto<List<PostWithCommentCountResponseDto>> getAuthorPostsWithMinComment(String author, int minCount) {
        // 입력값 검증
        String clean = requireNonBlank(author, "author").trim();
        if(minCount < 0) {
            throw new IllegalArgumentException("minCount 는 0 이상이어야합니다.");
        }
        // 리포지토리 호출
        var rows = postRepository.findAuthorPostsWithMinCount(clean, minCount);

        // 매핑
        List<PostWithCommentCountResponseDto> result = rows.stream()
                .map(PostWithCommentCountResponseDto::from)
                .toList();

//        List<PostWithCommentCountResponseDto> result = rows.stream()
//                .map(r -> new PostWithCommentCountResponseDto(
//                        r.getPostId(),
//                        r.getTitle(),
//                        r.getAuthor(),
//                        r.getCommentCount() == null ? 0 : r.getCommentCount()
//                ))
//                .toList();

        return ResponseDto.setSuccess("SUCCESS", result);
    }

    // ======== 내부 유틸 메서드 ========= //
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