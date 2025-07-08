package com.tebutebu.apiserver.repository.paging.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.domain.QProject;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.factory.CursorPageSpec;
import com.tebutebu.apiserver.pagination.factory.CursorPageFactory;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.CommentRepository;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import com.tebutebu.apiserver.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProjectPagingRepositoryImpl implements ProjectPagingRepository {

        private final ProjectRankingSnapshotRepository snapshotRepository;

        private final ProjectRepository projectRepository;

        private final CommentRepository commentRepository;

        private final SubscriptionRepository subscriptionRepository;

        private final CursorPageFactory cursorPageFactory;

        private final ObjectMapper objectMapper;

        private final RedisTemplate<String, ProjectRankingSnapshotResponseDTO> snapshotRedisTemplate;

        private final QProject qProject = QProject.project;

        @Value("${ranking.snapshot.cache.key-prefix}")
        private String snapshotCacheKeyPrefix;

        @Override
        public CursorPage<ProjectPageResponseDTO> findByRankingCursor(ContextCursorPageRequestDTO req) {
                Long snapshotId = req.getContextId();
                String cacheKey = snapshotCacheKeyPrefix + snapshotId;

                ProjectRankingSnapshotResponseDTO cachedSnapshot = snapshotRedisTemplate.opsForValue().get(cacheKey);

                List<RankingItemDTO> dtoList;
                if (cachedSnapshot != null) {
                        dtoList = cachedSnapshot.getData();
                } else {
                        ProjectRankingSnapshot snapshot = snapshotRepository.findById(snapshotId)
                                        .orElseThrow(() -> new NoSuchElementException("snapshotNotFound"));
                        dtoList = parseSnapshotJson(snapshot);
                }

                int start = calculateStartIndex(dtoList, req.getCursorId());
                int end = Math.min(start + req.getPageSize(), dtoList.size());

                List<Long> projectIds = dtoList.subList(start, end).stream()
                                .map(RankingItemDTO::getProjectId)
                                .collect(Collectors.toList());

                List<Project> projects = projectRepository.findAllById(projectIds).stream()
                                .sorted(Comparator.comparingInt(p -> projectIds.indexOf(p.getId())))
                                .toList();

                Map<Long, Long> commentCountMap = commentRepository.findCommentCountMap(projectIds);
                Set<Long> subscribedProjectIds = (req.getMemberId() != null)
                                ? new HashSet<>(subscriptionRepository
                                                .findSubscribedProjectIdsByMemberId(req.getMemberId()))
                                : Collections.emptySet();

                List<ProjectPageResponseDTO> projectPageResponseDtoList = projects.stream()
                                .map(proj -> toPageResponseDTO(proj, commentCountMap, subscribedProjectIds))
                                .collect(Collectors.toList());

                boolean hasNext = end < dtoList.size();
                Long nextCursorId = hasNext ? dtoList.get(end - 1).getProjectId() : null;

                return CursorPage.<ProjectPageResponseDTO>builder()
                                .items(projectPageResponseDtoList)
                                .nextCursorId(nextCursorId)
                                .nextCursorTime(null)
                                .hasNext(hasNext)
                                .build();
        }

        @Override
        public CursorPage<ProjectPageResponseDTO> findByLatestCursor(CursorTimePageRequestDTO req) {
                BooleanBuilder where = new BooleanBuilder();
                OrderSpecifier<?>[] orderBy = new OrderSpecifier<?>[] {
                                qProject.createdAt.desc(),
                                qProject.id.desc()
                };

                CursorPageSpec<Project> spec = CursorPageSpec.<Project>builder()
                                .entityPath(qProject)
                                .where(where)
                                .orderBy(orderBy)
                                .createdAtExpr(qProject.createdAt)
                                .idExpr(qProject.id)
                                .cursorId(req.getCursorId())
                                .cursorTime(req.getCursorTime())
                                .pageSize(req.getPageSize())
                                .build();

                CursorPage<Project> page = cursorPageFactory.create(spec);
                List<Long> projectIds = page.items().stream().map(Project::getId).toList();
                Map<Long, Long> commentCountMap = commentRepository.findCommentCountMap(projectIds);
                Set<Long> subscribedProjectIds = (req.getMemberId() != null)
                                ? new HashSet<>(subscriptionRepository
                                                .findSubscribedProjectIdsByMemberId(req.getMemberId()))
                                : Collections.emptySet();

                List<ProjectPageResponseDTO> projectPageResponseDtoList = page.items().stream()
                                .map(proj -> toPageResponseDTO(proj, commentCountMap, subscribedProjectIds))
                                .toList();

                return CursorPage.<ProjectPageResponseDTO>builder()
                                .items(projectPageResponseDtoList)
                                .nextCursorId(page.nextCursorId())
                                .nextCursorTime(page.nextCursorTime())
                                .hasNext(page.hasNext())
                                .build();
        }

        @Override
        public CursorPage<ProjectPageResponseDTO> findSubscribedProjectsByTerm(Long memberId, int term,
                        CursorTimePageRequestDTO req) {
                BooleanBuilder where = new BooleanBuilder();
                where.and(qProject.team.term.eq(term));
                where.and(qProject.subscriptions.any().member.id.eq(memberId));
                where.and(qProject.subscriptions.any().deletedAt.isNull());

                OrderSpecifier<?>[] orderBy = new OrderSpecifier<?>[] {
                                qProject.createdAt.desc(),
                                qProject.id.desc()
                };

                CursorPageSpec<Project> spec = CursorPageSpec.<Project>builder()
                                .entityPath(qProject)
                                .where(where)
                                .orderBy(orderBy)
                                .createdAtExpr(qProject.createdAt)
                                .idExpr(qProject.id)
                                .cursorId(req.getCursorId())
                                .cursorTime(req.getCursorTime())
                                .pageSize(req.getPageSize())
                                .build();

                CursorPage<Project> page = cursorPageFactory.create(spec);
                List<Long> projectIds = page.items().stream().map(Project::getId).toList();
                Map<Long, Long> commentCountMap = commentRepository.findCommentCountMap(projectIds);

                List<ProjectPageResponseDTO> pageResponseDtoList = page.items().stream()
                                .map(proj -> toPageResponseDTO(proj, commentCountMap, Set.of(proj.getId())))
                                .toList();

                return CursorPage.<ProjectPageResponseDTO>builder()
                                .items(pageResponseDtoList)
                                .nextCursorId(page.nextCursorId())
                                .nextCursorTime(page.nextCursorTime())
                                .hasNext(page.hasNext())
                                .build();
        }

        private List<RankingItemDTO> parseSnapshotJson(ProjectRankingSnapshot snapshot) {
                try {
                        Map<String, List<RankingItemDTO>> map = objectMapper.readValue(
                                        snapshot.getRankingData(),
                                        new TypeReference<>() {
                                        });
                        return map.get("projects");
                } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                }
        }

        private int calculateStartIndex(List<RankingItemDTO> all, Long afterId) {
                if (afterId == null)
                        return 0;
                for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).getProjectId().equals(afterId)) {
                                return i + 1;
                        }
                }
                return 0;
        }

        private ProjectPageResponseDTO toPageResponseDTO(Project project, Map<Long, Long> commentCountMap,
                        Set<Long> subscribedIds) {
                boolean isSubscribed = subscribedIds != null && subscribedIds.contains(project.getId());
                return ProjectPageResponseDTO.builder()
                                .id(project.getId())
                                .teamId(project.getTeam().getId())
                                .term(project.getTeam().getTerm())
                                .teamNumber(project.getTeam().getNumber())
                                .title(project.getTitle())
                                .introduction(project.getIntroduction())
                                .representativeImageUrl(project.getRepresentativeImageUrl())
                                .tags(project.getTagContents().stream()
                                                .map(t -> new TagResponseDTO(t.getContent()))
                                                .toList())
                                .commentCount(commentCountMap.getOrDefault(project.getId(), 0L))
                                .givedPumatiCount(project.getTeam().getGivedPumatiCount())
                                .receivedPumatiCount(project.getTeam().getReceivedPumatiCount())
                                .badgeImageUrl(project.getTeam().getBadgeImageUrl())
                                .isSubscribed(isSubscribed)
                                .createdAt(project.getCreatedAt())
                                .modifiedAt(project.getModifiedAt())
                                .build();
        }

}
