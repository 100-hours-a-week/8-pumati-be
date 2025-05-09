package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectImage;
import com.tebutebu.apiserver.dto.project.image.request.ProjectImageRequestDTO;
import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import com.tebutebu.apiserver.repository.ProjectImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProjectImageServiceImpl implements ProjectImageService {

    private final ProjectImageRepository projectImageRepository;

    @Override
    public List<ProjectImageResponseDTO> getListByProjectId(Long projectId) {
        Optional<List<ProjectImage>> result = projectImageRepository.findByProjectIdOrderBySequenceAsc(projectId);
        if (result.isEmpty()) {
            throw new NoSuchElementException("projectImagesNotFound");
        }

        return result.get().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long register(Long projectId, ProjectImageRequestDTO dto) {
        Optional<List<ProjectImage>> result = projectImageRepository.findByProjectIdOrderBySequenceAsc(projectId);
        if (result.isEmpty()) {
            throw new NoSuchElementException("projectImagesNotFound");
        }

        ProjectImage projectImage = projectImageRepository.save(dtoToEntity(dto));
        return projectImage.getId();
    }

    @Override
    public List<Long> updateProjectImages(Long projectId, List<ProjectImageRequestDTO> images) {
        Optional<List<ProjectImage>> result = projectImageRepository.findByProjectIdOrderBySequenceAsc(projectId);
        if (result.isEmpty()) {
            throw new NoSuchElementException("projectImagesNotFound");
        }

        projectImageRepository.deleteAllByProjectId(projectId);

        return images.stream()
                .map(dto -> {
                    ProjectImage saved = projectImageRepository.save(dtoToEntity(dto));
                    return saved.getId();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long projectId, Integer sequence) {
        Optional<List<ProjectImage>> result = projectImageRepository.findByProjectIdOrderBySequenceAsc(projectId);
        if (result.isEmpty()) {
            throw new NoSuchElementException("projectImagesNotFound");
        }

        ProjectImage projectImage = projectImageRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new NoSuchElementException("projectImageNotFound"));

        projectImageRepository.delete(projectImage);
    }

    @Override
    public ProjectImage dtoToEntity(ProjectImageRequestDTO dto) {
        return ProjectImage.builder()
                .project(Project.builder().id(dto.getProjectId()).build())
                .url(dto.getUrl())
                .sequence(dto.getSequence())
                .build();
    }

}
