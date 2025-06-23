package com.tebutebu.apiserver.dto.team.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
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

    @NotNull(message = ValidationMessages.TEAM_TERM_REQUIRED)
    @Min(value = 1, message = ValidationMessages.TERM_MUST_BE_POSITIVE)
    private Integer term;

    @NotNull(message = ValidationMessages.TEAM_NUMBER_REQUIRED)
    @Min(value = 1, message = ValidationMessages.TEAM_NUMBER_MUST_BE_POSITIVE)
    private Integer number;

}
