package com.tebutebu.apiserver.service.mail.template;

import com.tebutebu.apiserver.dto.mail.template.WeeklyReportTemplateDTO;

public interface WeeklyReportTemplateService {

    String renderWeeklyReport(WeeklyReportTemplateDTO dto);

}
