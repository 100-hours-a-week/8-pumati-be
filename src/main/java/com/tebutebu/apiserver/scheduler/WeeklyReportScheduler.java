package com.tebutebu.apiserver.scheduler;

import com.tebutebu.apiserver.service.report.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final WeeklyReportService weeklyReportService;

    @Scheduled(
            cron = "${scheduler.weekly-report.send-cron}",
            zone = "${scheduler.weekly-report.send-zone}"
    )
    public void sendWeeklyReports() {
        log.info("Sending weekly project reports to consenting members");
        weeklyReportService.sendWeeklyReportsToConsentingMembers();
    }

}
