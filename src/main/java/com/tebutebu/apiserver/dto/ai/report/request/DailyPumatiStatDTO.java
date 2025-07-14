package com.tebutebu.apiserver.dto.ai.report.request;

public record DailyPumatiStatDTO(
    String day,
    long givedPumatiCount,
    long receivedPumatiCount
) {}
