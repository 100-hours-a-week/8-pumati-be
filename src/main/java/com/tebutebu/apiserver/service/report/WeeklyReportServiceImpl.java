package com.tebutebu.apiserver.service.report;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import com.tebutebu.apiserver.repository.TeamBadgeStatRepository;
import com.tebutebu.apiserver.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final ProjectRepository projectRepository;

    private final MemberRepository memberRepository;

    private final TeamBadgeStatRepository teamBadgeStatRepository;

    private final MailService mailService;

    @Override
    public void sendWeeklyReportsToConsentingMembers() {
        List<Project> projects = projectRepository.findAll();

        for (Project project : projects) {
            Team team = project.getTeam();
            if (team == null) {
                continue;
            }

            List<Member> consentingMembers = memberRepository.findAllByTeamIdWithTeam(team.getId()).stream()
                    .filter(Member::hasEmailConsent)
                    .toList();

            for (Member member : consentingMembers) {
                try {
                    String subject = "[주간 리포트] %d기 %d팀 - %s"
                            .formatted(team.getTerm(), team.getNumber(), project.getTitle());

                    String content = generateReportContent(project, team, member);
                    mailService.sendMail(member.getEmail(), subject, content);
                    log.info("메일 발송 성공: {}", member.getEmail());
                } catch (Exception e) {
                    log.error("메일 발송 실패: {}", member.getEmail(), e);
                }
            }
        }
    }

    private String generateReportContent(Project project, Team team, Member member) {
        List<Object[]> badgeStats = teamBadgeStatRepository.findReceivedBadgeStatsByReceiverTeamId(team.getId());
        StringBuilder badgeDetails = new StringBuilder();
        for (Object[] stat : badgeStats) {
            Integer giverTeamNumber = (Integer) stat[0];
            Integer count = (Integer) stat[1];
            badgeDetails.append("- ").append(giverTeamNumber).append("팀으로부터 받은 뱃지: ")
                    .append(count).append("개\n");
        }

        return """
            안녕하세요, %s님!

            [%d기 %d팀 - %s] 프로젝트의 주간 활동 리포트를 보내드립니다.

            - 받은 품앗이 수: %d개
            - 준 품앗이 수: %d개
            - 총 뱃지 수: %d개

            [받은 팀별 뱃지 수]
            %s

            항상 응원합니다!
            """.formatted(
                member.getNickname(),
                team.getTerm(),
                team.getNumber(),
                project.getTitle(),
                team.getReceivedPumatiCount(),
                team.getGivedPumatiCount(),
                team.getTotalReceivedBadgeCount(),
                badgeDetails.toString()
        );
    }

}
