package com.tebutebu.apiserver.dto.team.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class TeamCreateRequestDTO {

    @NotNull(message = "기수는 필수 입력 값입니다.")
    @Min(value = 1, message = "기수는 1 이상의 숫자여야 합니다.")
    private Integer term;

    @NotNull(message = "팀(조) 번호는 필수 입력 값입니다.")
    @Min(value = 1, message = "팀 번호는 1 이상의 숫자여야 합니다.")
    private Integer number;

}
