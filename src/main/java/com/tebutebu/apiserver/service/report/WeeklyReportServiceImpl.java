package com.tebutebu.apiserver.service.report;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.ai.report.request.BadgeStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.DailyPumatiStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.TeamInfoDTO;
import com.tebutebu.apiserver.dto.ai.report.request.WeeklyReportImageRequestDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import com.tebutebu.apiserver.repository.TeamBadgeStatRepository;
import com.tebutebu.apiserver.service.ai.report.AiWeeklyReportImageRequestService;
import com.tebutebu.apiserver.service.mail.MailService;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Log4j2
@Service
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final ProjectRepository projectRepository;

    private final MemberRepository memberRepository;

    private final TeamBadgeStatRepository teamBadgeStatRepository;

    private final MailService mailService;

    private final AiWeeklyReportImageRequestService aiWeeklyReportImageRequestService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    @Override
    public void sendWeeklyReportsToConsentingMembers() {
        List<Project> projects = projectRepository.findAll();

        for (Project project : projects) {
            Team team = project.getTeam();
            if (team == null) continue;

            WeeklyReportImageRequestDTO imageRequestDTO = generateReportImageRequest(project, team);
            String imageUrl = aiWeeklyReportImageRequestService.requestGenerateWeeklyReportImage(imageRequestDTO);

            List<Member> consentingMembers = memberRepository.findAllByTeamIdWithTeam(team.getId()).stream()
                    .filter(Member::hasEmailConsent)
                    .toList();

            for (Member member : consentingMembers) {
                try {
                    String subject = "[주간 리포트] %d기 %d팀 - %s".formatted(team.getTerm(), team.getNumber(), project.getTitle());
                    String content = generateReportContent(project, team, member, imageUrl);
                    mailService.sendMail(member.getEmail(), subject, content);
                    log.info("메일 발송 성공: {}", member.getEmail());
                } catch (Exception e) {
                    log.error("메일 발송 실패: {}", member.getEmail(), e);
                }
            }
        }
    }

    private WeeklyReportImageRequestDTO generateReportImageRequest(Project project, Team team) {
        List<Object[]> badgeStatsRaw = teamBadgeStatRepository.findReceivedBadgeStatsWithTermByReceiverTeamId(team.getId());
        List<BadgeStatDTO> badgeStats = badgeStatsRaw.stream()
                .map(stat -> new BadgeStatDTO((Integer) stat[0], (Integer) stat[1], (Integer) stat[2]))
                .toList();

        TeamInfoDTO teamInfo = new TeamInfoDTO(
                team.getTerm(),
                team.getNumber(),
                team.getReceivedPumatiCount(),
                team.getGivedPumatiCount(),
                team.getTotalReceivedBadgeCount()
        );

        List<DailyPumatiStatDTO> dailyPumatiStats = generateDailyStats(project.getId());

        return WeeklyReportImageRequestDTO.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .team(teamInfo)
                .badgeStats(badgeStats)
                .dailyPumatiStats(dailyPumatiStats)
                .build();
    }

    private List<DailyPumatiStatDTO> generateDailyStats(Long projectId) {
        List<ProjectRankingSnapshotResponseDTO> snapshots = projectRankingSnapshotService.getSnapshotsForLast7Days();

        long prevGivedPumatiCount = 0, prevReceivedPumatiCount = 0;
        List<DailyPumatiStatDTO> dailyStats = new ArrayList<>();

        for (int i = 0; i < snapshots.size(); i++) {
            ProjectRankingSnapshotResponseDTO snapshot = snapshots.get(i);
            if (snapshot == null) {
                LocalDate fallbackDate = LocalDate.now().minusDays(6 - i);
                String fallbackDayStr = fallbackDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                dailyStats.add(new DailyPumatiStatDTO(fallbackDayStr, 0, 0));
                continue;
            }

            RankingItemDTO item = snapshot.getData().stream()
                    .filter(dto -> dto.getProjectId().equals(projectId))
                    .findFirst()
                    .orElse(null);

            long currentGivedPumatiCount = item != null ? item.getGivedPumatiCount() : prevGivedPumatiCount;
            long currentReceivedPumatiCount = item != null ? item.getReceivedPumatiCount() : prevReceivedPumatiCount;

            long givedDiff = currentGivedPumatiCount - prevGivedPumatiCount;
            long receivedDiff = currentReceivedPumatiCount - prevReceivedPumatiCount;

            prevGivedPumatiCount = currentGivedPumatiCount;
            prevReceivedPumatiCount = currentReceivedPumatiCount;

            LocalDate actualDate = LocalDate.now().minusDays(6 - i);
            String dayStr = actualDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

            dailyStats.add(new DailyPumatiStatDTO(dayStr, givedDiff, receivedDiff));
        }

        return dailyStats;
    }

    private String generateReportContent(Project project, Team team, Member member, String imageUrl) {
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

            📊 주간 활동 그래프:
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
                badgeDetails.toString(),
                imageUrl != null ? imageUrl : "(이미지 생성 실패)"
        );
    }

}
