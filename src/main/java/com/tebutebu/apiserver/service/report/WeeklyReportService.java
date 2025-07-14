package com.tebutebu.apiserver.service.report;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface WeeklyReportService {

    @Transactional(readOnly = true)
    void sendWeeklyReportsToConsentingMembers();

}
