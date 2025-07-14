package com.tebutebu.apiserver.dto.comment.response;

import com.tebutebu.apiserver.domain.enums.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthorDTO {

    private Long id;
    
    private String name;

    private String nickname;

    private Course course;

    private String profileImageUrl;

}
