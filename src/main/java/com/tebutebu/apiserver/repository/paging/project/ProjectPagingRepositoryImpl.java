package com.tebutebu.apiserver.repository.paging.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.dto.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProjectPagingRepositoryImpl implements ProjectPagingRepository {

    private final ProjectRankingSnapshotRepository snapshotRepository;

    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper;

    @Override
    public CursorPage<Project> findByRankingCursor(CursorPageRequestDTO req) {
        ProjectRankingSnapshot snapshot = snapshotRepository.findById(req.getContextId())
                .orElseThrow(() -> new NoSuchElementException("snapshotNotFound"));

        List<RankingItemDTO> dtoList = parseSnapshotJson(snapshot);

        int start = calculateStartIndex(dtoList, req.getCursorId());
        int end = Math.min(start + req.getPageSize(), dtoList.size());

        List<Long> projectIds = dtoList.subList(start, end).stream()
                .map(RankingItemDTO::getProjectId)
                .collect(Collectors.toList());

        List<Project> projects = projectRepository.findAllById(projectIds).stream()
                .sorted(Comparator.comparingInt(p -> projectIds.indexOf(p.getId())))
                .collect(Collectors.toList());

        boolean hasNext = end < dtoList.size();
        Long nextCursorId = hasNext ? dtoList.get(end - 1).getProjectId() : null;

        return CursorPage.<Project>builder()
                .items(projects)
                .nextCursorId(nextCursorId)
                .nextCursorTime(null)
                .hasNext(hasNext)
                .build();
    }

    private List<RankingItemDTO> parseSnapshotJson(ProjectRankingSnapshot snapshot) {
        try {
            Map<String, List<RankingItemDTO>> map = objectMapper.readValue(
                    snapshot.getRankingData(),
                    new TypeReference<>() {}
            );
            return map.get("projects");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int calculateStartIndex(List<RankingItemDTO> all, Long afterId) {
        if (afterId == null) {
            return 0;
        }
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getProjectId().equals(afterId)) {
                return i + 1;
            }
        }
        return 0;
    }

}
