package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface TeamService {

    TeamResponseDTO get(Long id);

    TeamResponseDTO getByTermAndNumber(Integer term, Integer number);

    List<TeamListResponseDTO> getAllTeams();

    Long register(TeamCreateRequestDTO dto);

    Team dtoToEntity(TeamCreateRequestDTO dto);

    default TeamResponseDTO entityToDTO(Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .term(team.getTerm())
                .number(team.getNumber())
                .createdAt(team.getCreatedAt())
                .modifiedAt(team.getModifiedAt())
                .build();
    }

}
