package com.tebutebu.apiserver.dto.team.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TeamListResponseDTO {

    private Integer term;

    private List<Integer> teamNumbers;

}
