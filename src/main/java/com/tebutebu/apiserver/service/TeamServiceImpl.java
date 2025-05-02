package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.repository.TeamRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    public TeamResponseDTO get(Long id) {
        Optional<Team> result = teamRepository.findById(id);
        Team team = result.orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        return entityToDTO(team);
    }

    @Override
    public TeamResponseDTO getByTermAndNumber(Integer term, Integer number) {
        if (term == null && number == null) {
            return null;
        }
        Optional<Team> result = teamRepository.findByTermAndNumber(term, number);
        Team team = result.orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        return entityToDTO(team);
    }

    @Override
    public Long register(TeamCreateRequestDTO dto) {

        if (teamRepository.existsByTermAndNumber(dto.getTerm(), dto.getNumber())) {
            throw new CustomValidationException("teamAlreadyExists");
        }

        Team team = teamRepository.save(dtoToEntity(dto));
        return team.getId();
    }

    @Override
    public Team dtoToEntity(TeamCreateRequestDTO dto) {
        return Team.builder()
                .term(dto.getTerm())
                .number(dto.getNumber())
                .build();
    }

}
