package com.tebutebu.apiserver.service.ai.report;

import com.tebutebu.apiserver.dto.ai.report.request.WeeklyReportImageRequestDTO;

public interface AiWeeklyReportImageRequestService {

    String requestGenerateWeeklyReportImage(WeeklyReportImageRequestDTO request);

}
