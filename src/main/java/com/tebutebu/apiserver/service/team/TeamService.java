package com.tebutebu.apiserver.service.team;

import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.ai.badge.request.BadgeImageModificationRequestDTO;
import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgeStatPageResponseDTO;
import com.tebutebu.apiserver.dto.ai.report.request.BadgeStatDTO;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CountCursorMetaDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface TeamService {

    @Transactional(readOnly = true)
    TeamResponseDTO get(Long id);

    @Transactional(readOnly = true)
    TeamResponseDTO getByTermAndNumber(Integer term, Integer number);

    @Transactional(readOnly = true)
    List<TeamListResponseDTO> getAllTeams();

    @Transactional(readOnly = true)
    List<BadgeStatDTO> getReceivedBadgeStats(Long teamId);

    Long register(TeamCreateRequestDTO dto);

    @Transactional(readOnly = true)
    CursorPageResponseDTO<TeamBadgeStatPageResponseDTO, CountCursorMetaDTO> getReceivedBadgesPage(ContextCountCursorPageRequestDTO req);

    void increaseOrCreateBadge(Long giverTeamId, Long receiverTeamId);

    void requestUpdateBadgeImage(Long teamId, BadgeImageModificationRequestDTO badgeImageModificationRequestDTO);

    void updateBadgeImageUrl(Long teamId, String badgeImageUrl);

    void resetAiBadgeProgress(Long teamId);

    Long incrementGivedPumati(Long teamId);

    void incrementGivedPumatiBy(Long teamId, long amount);

    Long incrementReceivedPumati(Long teamId);

    void incrementReceivedPumatiBy(Long teamId, long amount);

    void resetAllPumatiCounts();

    Team dtoToEntity(TeamCreateRequestDTO dto);

    default TeamResponseDTO entityToDTO(Team team, Long projectId, Integer rank) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .term(team.getTerm())
                .number(team.getNumber())
                .projectId(projectId)
                .rank(rank)
                .givedPumatiCount(team.getGivedPumatiCount())
                .receivedPumatiCount(team.getReceivedPumatiCount())
                .badgeImageUrl(team.getBadgeImageUrl())
                .createdAt(team.getCreatedAt())
                .modifiedAt(team.getModifiedAt())
                .build();
    }

}
