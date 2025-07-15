package com.tebutebu.apiserver.service.mail.template;

import com.tebutebu.apiserver.dto.mail.template.WeeklyReportTemplateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class WeeklyReportTemplateServiceImpl implements WeeklyReportTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String renderWeeklyReport(WeeklyReportTemplateDTO dto) {
        Context context = new Context();
        context.setVariable("nickname", dto.getNickname());
        context.setVariable("term", dto.getTerm());
        context.setVariable("teamNumber", dto.getTeamNumber());
        context.setVariable("projectTitle", dto.getProjectTitle());
        context.setVariable("receivedPumatiCount", dto.getReceivedPumatiCount());
        context.setVariable("givedPumatiCount", dto.getGivedPumatiCount());
        context.setVariable("pumatiRank", dto.getPumatiRank());
        context.setVariable("badgeStats", dto.getBadgeStats());
        context.setVariable("imageUrl", dto.getReportImageUrl());
        return templateEngine.process("report/weekly-report", context);
    }

}
