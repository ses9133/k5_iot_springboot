package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.D_Comment.request.CommentCreateRequestDto;
import com.example.k5_iot_springboot.dto.D_Comment.request.CommentUpdateRequestDto;
import com.example.k5_iot_springboot.dto.D_Comment.response.CommentResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.D_Comment;
import com.example.k5_iot_springboot.entity.D_Post;
import com.example.k5_iot_springboot.repository.D_CommentRepository;
import com.example.k5_iot_springboot.repository.D_PostRepository;
import com.example.k5_iot_springboot.service.D_CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class D_CommentServiceImpl implements D_CommentService {

    private final D_CommentRepository commentRepository;
    private final D_PostRepository postRepository;

    // 생성
    @Override
    @Transactional
    public ResponseDto<CommentResponseDto> createComment(Long postId, CommentCreateRequestDto dto) {
        D_Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 id 의 게시글을 찾을 수 없습니다."));
        D_Comment comment = D_Comment.create(dto.content(), dto.commenter());
        post.addComment(comment); // 연관관계 편으 ㅣ메서드
        D_Comment saved = commentRepository.save(comment);

        return ResponseDto.setSuccess("SUCCESS", CommentResponseDto.from(saved));
    }

    // 수정
    @Override
    @Transactional
    public ResponseDto<CommentResponseDto> updateComment(Long postId, Long commentId, CommentUpdateRequestDto dto) {
        D_Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 id 의 댓글을 찾을 수 없습니다."));

        if(!comment.getPost().getId().equals(postId)) {
            // 일치하지 않으면
            throw new IllegalArgumentException("해당 댓글이 게시글내에 속해있지않습니다..");
        }
        comment.changeContent(dto.content());

        return ResponseDto.setSuccess("SUCCESS", CommentResponseDto.from(comment));
    }

    @Override
    @Transactional
    public ResponseDto<Void> deleteComment(Long postId, Long commentId) {
        D_Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 id의 댓글을 찾을 수 없습니다."));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 댓글이 게시글 내에 속해있지 않습니다.");
        }

        // 고아 객체 제거: 컬렉션에서 삭제할 경우 실제 DB에서도 삭제
        D_Post post = comment.getPost();
        post.removeComment(comment);

        // 필요 시 명시 가능(중복 방지 - 주로 생략)
        // commentRepository.delete(comment);

        return ResponseDto.setSuccess("SUCCESS", null);
    }
}
