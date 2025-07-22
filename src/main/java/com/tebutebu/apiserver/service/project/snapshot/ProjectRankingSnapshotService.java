package com.tebutebu.apiserver.service.project.snapshot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
public interface ProjectRankingSnapshotService {

    Long register();

    ProjectRankingSnapshotResponseDTO getLatestSnapshot();

    List<ProjectRankingSnapshotResponseDTO> getSnapshotsForLast7Days();

    default ProjectRankingSnapshotResponseDTO entityToDTO(ProjectRankingSnapshot snapshot) {
        List<RankingItemDTO> items;
        try {
            Map<String, List<RankingItemDTO>> wrapper = new ObjectMapper()
                    .readValue(
                            snapshot.getRankingData(),
                            new TypeReference<>() {
                            }
                    );
            items = wrapper.get("projects");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return ProjectRankingSnapshotResponseDTO.builder()
                .id(snapshot.getId())
                .data(items)
                .requestedAt(snapshot.getRequestedAt())
                .build();
    }

}
