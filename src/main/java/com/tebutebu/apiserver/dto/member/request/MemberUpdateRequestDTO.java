package com.tebutebu.apiserver.dto.member.request;

import com.tebutebu.apiserver.domain.Course;
import com.tebutebu.apiserver.domain.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateRequestDTO {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 가능합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    @Pattern(regexp = "^\\S+$", message = "닉네임은 공백을 포함할 수 없습니다.")
    private String nickname;

    private String profileImageUrl;

    @Positive(message = "기수는 양수여야 합니다.")
    private Integer term;

    @Positive(message = "팀(조) 번호는 양수여야 합니다.")
    private Integer teamNumber;

    private Course course;

    private MemberRole role;

}
