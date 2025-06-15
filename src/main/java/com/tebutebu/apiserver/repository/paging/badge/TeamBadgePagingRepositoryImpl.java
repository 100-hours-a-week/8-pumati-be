package com.tebutebu.apiserver.repository.paging.badge;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.QTeamBadge;
import com.tebutebu.apiserver.domain.TeamBadge;
import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgePageResponseDTO;
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
public class TeamBadgePagingRepositoryImpl implements TeamBadgePagingRepository {

    private final CursorPageFactory cursorPageFactory;

    private final ProjectRepository projectRepository;

    private final QTeamBadge qTeamBadge = QTeamBadge.teamBadge;

    @Override
    public CursorPage<TeamBadgePageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(qTeamBadge.giverTeam.id.eq(req.getContextId()));

        Integer cursorCount = req.getCursorCount();
        Long cursorId = req.getCursorId();

        if (cursorCount != null && cursorId != null) {
            where.and(
                    qTeamBadge.acquiredCount.lt(cursorCount)
                            .or(qTeamBadge.acquiredCount.eq(cursorCount).and(qTeamBadge.id.lt(cursorId)))
            );
        }

        OrderSpecifier<?>[] orderBy = new OrderSpecifier[]{
                qTeamBadge.acquiredCount.desc(),
                qTeamBadge.id.desc()
        };

        CursorPageSpec<TeamBadge> spec = CursorPageSpec.<TeamBadge>builder()
                .entityPath(qTeamBadge)
                .where(where)
                .orderBy(orderBy)
                .idExpr(qTeamBadge.id)
                .countExpr(qTeamBadge.acquiredCount)
                .cursorId(cursorId)
                .cursorCount(cursorCount)
                .pageSize(req.getPageSize())
                .build();

        CursorPage<TeamBadge> page = cursorPageFactory.createForCount(spec);

        List<TeamBadgePageResponseDTO> responseList = page.items().stream()
                .map(badge -> TeamBadgePageResponseDTO.builder()
                        .id(badge.getId())
                        .projectId(projectRepository.findProjectIdByTeamId(badge.getReceiverTeam().getId()).orElse(null))
                        .giverTeamId(badge.getGiverTeam().getId())
                        .receiverTeamId(badge.getReceiverTeam().getId())
                        .term(badge.getReceiverTeam().getTerm())
                        .teamNumber(badge.getReceiverTeam().getNumber())
                        .badgeImageUrl(badge.getReceiverTeam().getBadgeImageUrl())
                        .acquiredCount(badge.getAcquiredCount())
                        .createdAt(badge.getCreatedAt())
                        .modifiedAt(badge.getModifiedAt())
                        .build())
                .toList();

        return CursorPage.<TeamBadgePageResponseDTO>builder()
                .items(responseList)
                .nextCursorId(page.nextCursorId())
                .nextCursorCount(page.nextCursorCount())
                .hasNext(page.hasNext())
                .build();
    }

}
