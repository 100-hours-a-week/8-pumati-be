package com.tebutebu.apiserver.service.project.image;

import com.tebutebu.apiserver.domain.ProjectImage;
import com.tebutebu.apiserver.dto.project.image.request.ProjectImageRequestDTO;
import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProjectImageService {

    @Transactional(readOnly = true)
    List<ProjectImageResponseDTO> getListByProjectId(Long projectId);

    Long register(Long projectId, ProjectImageRequestDTO dto);

    List<Long> updateProjectImages(Long projectId, List<ProjectImageRequestDTO> images);

    void delete(Long projectId, Integer sequence);

    ProjectImage dtoToEntity(ProjectImageRequestDTO dto);

    default ProjectImageResponseDTO entityToDTO(ProjectImage projectImage) {
        return ProjectImageResponseDTO.builder()
                .id(projectImage.getId())
                .projectId(projectImage.getProject().getId())
                .sequence(projectImage.getSequence())
                .url(projectImage.getUrl())
                .createdAt(projectImage.getCreatedAt())
                .modifiedAt(projectImage.getModifiedAt())
                .build();
    }

}
