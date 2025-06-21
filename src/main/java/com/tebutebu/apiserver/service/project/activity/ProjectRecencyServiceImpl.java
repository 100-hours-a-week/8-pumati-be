package com.tebutebu.apiserver.service.project.activity;

import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectRecencyServiceImpl implements ProjectRecencyService {

    private final ProjectRepository projectRepository;

    @Override
    public boolean isNewProjectAvailable(LocalDateTime after) {
        LocalDateTime latest = projectRepository.findLatestCreatedAt()
                .orElse(LocalDateTime.MIN);
        return latest.isAfter(after);
    }

}
