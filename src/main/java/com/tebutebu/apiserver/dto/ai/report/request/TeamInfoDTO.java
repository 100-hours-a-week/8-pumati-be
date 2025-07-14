package com.tebutebu.apiserver.dto.ai.report.request;

public record TeamInfoDTO(
        int term,
        int number,
        long receivedPumatiCount,
        long givedPumatiCount,
        long totalBadgeCount
) {}
