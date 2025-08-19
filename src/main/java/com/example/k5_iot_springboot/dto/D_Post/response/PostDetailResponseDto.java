package com.example.k5_iot_springboot.dto.D_Post.response;

import com.example.k5_iot_springboot.dto.D_Comment.response.CommentResponseDto;
import com.example.k5_iot_springboot.entity.D_Comment;
import com.example.k5_iot_springboot.entity.D_Post;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
// : JSON 변환시 null 인 필드는 응답에서 제외된다
//      >> 불필요한 null 값을 응답에 포함시키지 않는다
public record PostDetailResponseDto (
        Long id,
        String title,
        String content,
        String author,
        List<CommentResponseDto> comments
) {

    // D_Post 엔티티 -> PostDetailResponseDto 로 변환해줌
    public static PostDetailResponseDto from(D_Post post) {
        if(post == null) return null; // NPE 방지

        // 댓글 엔티티 가져오기
        List<D_Comment> comments = post.getComments() != null ? post.getComments() : Collections.emptyList();

        List<CommentResponseDto> commentDtos = comments.stream()
                .filter(Objects::nonNull)
                // .filter(comment -> comment != null)
                .map(CommentResponseDto::from)
                // .map(comment -> CommentResponseDto.from(comment))
                .toList();

        return new PostDetailResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                commentDtos);
    }
}
