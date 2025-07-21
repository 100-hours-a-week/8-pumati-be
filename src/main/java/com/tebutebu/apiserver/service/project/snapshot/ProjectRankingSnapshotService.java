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

    @Transactional(readOnly = true)
    ProjectRankingSnapshotResponseDTO getLatestSnapshot();

    List<ProjectRankingSnapshotResponseDTO> getSnapshotsForLast7Days();

    default ProjectRankingSnapshotResponseDTO entityToDTO(ProjectRankingSnapshot snapshot) {
        try {
            String raw = snapshot.getRankingData();

            if (raw.startsWith("\"") && raw.endsWith("\"")) {
                raw = new ObjectMapper().readValue(raw, String.class);
            }

            Map<String, List<RankingItemDTO>> wrapper = new ObjectMapper().readValue(
                    raw,
                    new TypeReference<>() {}
            );

            return ProjectRankingSnapshotResponseDTO.builder()
                    .id(snapshot.getId())
                    .data(wrapper.getOrDefault("projects", List.of()))
                    .requestedAt(snapshot.getRequestedAt())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ProjectRankingSnapshot JSON", e);
        }
    }

}
