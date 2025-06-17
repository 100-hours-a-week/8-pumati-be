package com.tebutebu.apiserver.repository.paging.badge;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.QTeamBadgeStat;
import com.tebutebu.apiserver.domain.TeamBadgeStat;
import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgeStatPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.factory.CursorPageFactory;
import com.tebutebu.apiserver.pagination.factory.CursorPageSpec;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TeamBadgeStatPagingRepositoryImpl implements TeamBadgeStatPagingRepository {

    private final CursorPageFactory cursorPageFactory;

    private final ProjectRepository projectRepository;

    private final QTeamBadgeStat qTeamBadgeStat = QTeamBadgeStat.teamBadgeStat;

    @Override
    public CursorPage<TeamBadgeStatPageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(qTeamBadgeStat.receiverTeam.id.eq(req.getContextId()));

        Integer cursorCount = req.getCursorCount();
        Long cursorId = req.getCursorId();

        if (cursorCount != null && cursorId != null) {
            where.and(
                    qTeamBadgeStat.acquiredCount.lt(cursorCount)
                            .or(qTeamBadgeStat.acquiredCount.eq(cursorCount).and(qTeamBadgeStat.id.lt(cursorId)))
            );
        }

        OrderSpecifier<?>[] orderBy = new OrderSpecifier[]{
                qTeamBadgeStat.acquiredCount.desc(),
                qTeamBadgeStat.id.desc()
        };

        CursorPageSpec<TeamBadgeStat> spec = CursorPageSpec.<TeamBadgeStat>builder()
                .entityPath(qTeamBadgeStat)
                .where(where)
                .orderBy(orderBy)
                .idExpr(qTeamBadgeStat.id)
                .countExpr(qTeamBadgeStat.acquiredCount)
                .cursorId(cursorId)
                .cursorCount(cursorCount)
                .pageSize(req.getPageSize())
                .build();

        CursorPage<TeamBadgeStat> page = cursorPageFactory.createForCount(spec);

        List<TeamBadgeStatPageResponseDTO> responseList = page.items().stream()
                .map(badge -> TeamBadgeStatPageResponseDTO.builder()
                        .id(badge.getId())
                        .projectId(projectRepository.findProjectIdByTeamId(badge.getGiverTeam().getId()).orElse(null))
                        .giverTeamId(badge.getGiverTeam().getId())
                        .giverTeamTerm(badge.getGiverTeam().getTerm())
                        .giverTeamNumber(badge.getGiverTeam().getNumber())
                        .badgeImageUrl(badge.getGiverTeam().getBadgeImageUrl())
                        .acquiredCount(badge.getAcquiredCount())
                        .createdAt(badge.getCreatedAt())
                        .modifiedAt(badge.getModifiedAt())
                        .build())
                .toList();

        return CursorPage.<TeamBadgeStatPageResponseDTO>builder()
                .items(responseList)
                .nextCursorId(page.nextCursorId())
                .nextCursorCount(page.nextCursorCount())
                .hasNext(page.hasNext())
                .build();
    }

}
