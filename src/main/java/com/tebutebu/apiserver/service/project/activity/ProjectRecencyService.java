package com.tebutebu.apiserver.service.project.activity;

import java.time.LocalDateTime;

public interface ProjectRecencyService {
    boolean isNewProjectAvailable(LocalDateTime after);
}
