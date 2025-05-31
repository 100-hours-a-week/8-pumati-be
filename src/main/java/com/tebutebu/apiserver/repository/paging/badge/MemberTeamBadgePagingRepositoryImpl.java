package com.tebutebu.apiserver.repository.paging.badge;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.MemberTeamBadge;
import com.tebutebu.apiserver.domain.QMemberTeamBadge;
import com.tebutebu.apiserver.dto.ai.badge.response.MemberTeamBadgePageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.factory.CursorPageFactory;
import com.tebutebu.apiserver.pagination.factory.CursorPageSpec;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberTeamBadgePagingRepositoryImpl implements MemberTeamBadgePagingRepository {

    private final CursorPageFactory cursorPageFactory;

    private final QMemberTeamBadge qMemberTeamBadge = QMemberTeamBadge.memberTeamBadge;

    @Override
    public CursorPage<MemberTeamBadgePageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(qMemberTeamBadge.member.id.eq(req.getContextId()));

        Integer cursorCount = req.getCursorCount();
        Long cursorId = req.getCursorId();

        if (cursorCount != null && cursorId != null) {
            where.and(
                    qMemberTeamBadge.acquiredCount.lt(cursorCount)
                            .or(qMemberTeamBadge.acquiredCount.eq(cursorCount).and(qMemberTeamBadge.id.lt(cursorId)))
            );
        }

        OrderSpecifier<?>[] orderBy = new OrderSpecifier[]{
                qMemberTeamBadge.acquiredCount.desc(),
                qMemberTeamBadge.id.desc()
        };

        CursorPageSpec<MemberTeamBadge> spec = CursorPageSpec.<MemberTeamBadge>builder()
                .entityPath(qMemberTeamBadge)
                .where(where)
                .orderBy(orderBy)
                .idExpr(qMemberTeamBadge.id)
                .countExpr(qMemberTeamBadge.acquiredCount)
                .cursorId(cursorId)
                .cursorCount(cursorCount)
                .pageSize(req.getPageSize())
                .build();

        CursorPage<MemberTeamBadge> page = cursorPageFactory.createForCount(spec);

        List<MemberTeamBadgePageResponseDTO> memberTeamBadgePageResponseDtoList = page.items().stream()
                .map(badge -> MemberTeamBadgePageResponseDTO.builder()
                        .id(badge.getId())
                        .teamId(badge.getTeam().getId())
                        .term(badge.getTeam().getTerm())
                        .teamNumber(badge.getTeam().getNumber())
                        .badgeImageUrl(badge.getTeam().getBadgeImageUrl())
                        .acquiredCount(badge.getAcquiredCount())
                        .createdAt(badge.getCreatedAt())
                        .modifiedAt(badge.getModifiedAt())
                        .build())
                .toList();

        return CursorPage.<MemberTeamBadgePageResponseDTO>builder()
                .items(memberTeamBadgePageResponseDtoList)
                .nextCursorId(page.nextCursorId())
                .nextCursorCount(page.nextCursorCount())
                .hasNext(page.hasNext())
                .build();
    }

}
