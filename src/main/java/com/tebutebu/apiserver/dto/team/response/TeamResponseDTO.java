package com.tebutebu.apiserver.dto.team.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TeamResponseDTO {

    private Long id;

    private int term;

    private int number;

    private LocalDateTime createdAt, modifiedAt;

}
