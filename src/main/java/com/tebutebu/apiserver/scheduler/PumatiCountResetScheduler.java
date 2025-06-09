package com.tebutebu.apiserver.scheduler;

import com.tebutebu.apiserver.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class PumatiCountResetScheduler {

    private final TeamService teamService;

    @Scheduled(
            cron = "${scheduler.pumati-count.reset-cron}",
            zone = "${scheduler.pumati-count.reset-zone}"
    )
    public void resetWeeklyPumatiCounts() {
        log.info("Resetting all team pumati counts");
        teamService.resetAllPumatiCounts();
    }

}
