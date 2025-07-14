package com.tebutebu.apiserver.service.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.ai.report.request.BadgeStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.DailyPumatiStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.TeamInfoDTO;
import com.tebutebu.apiserver.dto.ai.report.request.WeeklyReportImageRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import com.tebutebu.apiserver.service.ai.report.AiWeeklyReportImageRequestService;
import com.tebutebu.apiserver.service.mail.MailService;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.project.ProjectService;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import com.tebutebu.apiserver.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final MemberService memberService;

    private final TeamService teamService;

    private final MailService mailService;

    private final AiWeeklyReportImageRequestService aiWeeklyReportImageRequestService;

    private final ProjectService projectService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    @Value("${report.weekly.project-page-size}")
    private int projectPageSize;

    @Override
    public void sendWeeklyReportsToConsentingMembers() {
        CursorTimePageRequestDTO cursorTimePageRequestDTO = CursorTimePageRequestDTO.builder()
                .cursorId(null)
                .cursorTime(null)
                .pageSize(projectPageSize)
                .build();

        boolean hasNext;

        do {
            CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> page =
                    projectService.getLatestPage(cursorTimePageRequestDTO);

            List<ProjectPageResponseDTO> projectPageResponseDtoList = page.getData();

            for (ProjectPageResponseDTO projectDTO : projectPageResponseDtoList) {
                Long teamId = projectDTO.getTeamId();
                if (teamId == null) {
                    continue;
                }

                List<MemberResponseDTO> members = memberService.getMembersByTeamId(teamId);
                List<MemberResponseDTO> consentingMembers = members.stream()
                        .filter(member -> Boolean.TRUE.equals(member.getHasEmailConsent()))
                        .toList();

                if (consentingMembers.isEmpty()) {
                    continue;
                }

                WeeklyReportImageRequestDTO imageRequestDTO = generateReportImageRequest(projectDTO);
                String imageUrl;
                try {
                    String json = aiWeeklyReportImageRequestService.requestGenerateWeeklyReportImage(imageRequestDTO);
                    imageUrl = extractImageUrlFromJson(json);
                } catch (Exception e) {
                    log.warn("AI 리포트 이미지 생성 실패: {}", imageRequestDTO, e);
                    imageUrl = null;
                }

                for (MemberResponseDTO member : consentingMembers) {
                    try {
                        String subject = "[주간 리포트] %d기 %d팀 - %s"
                                .formatted(projectDTO.getTerm(), projectDTO.getTeamNumber(), projectDTO.getTitle());
                        String content = generateReportContent(projectDTO, member, imageUrl);
                        mailService.sendMail(member.getEmail(), subject, content);
                        log.info("메일 발송 성공: {}", member.getEmail());
                    } catch (Exception e) {
                        log.error("메일 발송 실패: {}", member.getEmail(), e);
                    }
                }
            }

            TimeCursorMetaDTO meta = page.getMeta();
            hasNext = meta.isHasNext();

            cursorTimePageRequestDTO = CursorTimePageRequestDTO.builder()
                    .cursorId(meta.getNextCursorId())
                    .cursorTime(meta.getNextCursorTime())
                    .pageSize(projectPageSize)
                    .build();

        } while (hasNext);
    }

    private WeeklyReportImageRequestDTO generateReportImageRequest(ProjectPageResponseDTO projectDTO) {
        List<BadgeStatDTO> badgeStats = teamService.getReceivedBadgeStats(projectDTO.getTeamId());

        int totalBadgeCount = badgeStats.stream()
                .mapToInt(BadgeStatDTO::badgeCount)
                .sum();

        TeamInfoDTO teamInfo = new TeamInfoDTO(
                projectDTO.getTerm(),
                projectDTO.getTeamNumber(),
                projectDTO.getReceivedPumatiCount(),
                projectDTO.getGivedPumatiCount(),
                totalBadgeCount
        );

        List<DailyPumatiStatDTO> dailyPumatiStats = generateDailyStats(projectDTO.getId());

        return WeeklyReportImageRequestDTO.builder()
                .projectId(projectDTO.getId())
                .projectTitle(projectDTO.getTitle())
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

            long currentGivedPumatiCount = (item != null && item.getGivedPumatiCount() != null)
                    ? item.getGivedPumatiCount()
                    : prevGivedPumatiCount;

            long currentReceivedPumatiCount = (item != null && item.getReceivedPumatiCount() != null)
                    ? item.getReceivedPumatiCount()
                    : prevReceivedPumatiCount;

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

    private String generateReportContent(ProjectPageResponseDTO projectDTO, MemberResponseDTO member, String imageUrl) {
        List<BadgeStatDTO> badgeStats = teamService.getReceivedBadgeStats(projectDTO.getTeamId());

        StringBuilder badgeDetails = new StringBuilder();
        for (BadgeStatDTO stat : badgeStats) {
            badgeDetails.append("- ")
                    .append(stat.giverTeamNumber())
                    .append("팀으로부터 받은 뱃지: ")
                    .append(stat.badgeCount())
                    .append("개\n");
        }

        String pumatiRank = getLatestPumatiRank(projectDTO.getId());

        return """
            안녕하세요, %s님!

            [%d기 %d팀 - %s] 프로젝트의 주간 활동 리포트를 보내드립니다.

            - 받은 품앗이 수: %d개
            - 준 품앗이 수: %d개
            - 품앗이 등수: %s위

            [받은 팀별 뱃지 수]
            %s

            📊 주간 활동 그래프:
            %s

            항상 응원합니다!
            """.formatted(
                member.getNickname(),
                projectDTO.getTerm(),
                projectDTO.getTeamNumber(),
                projectDTO.getTitle(),
                projectDTO.getReceivedPumatiCount(),
                projectDTO.getGivedPumatiCount(),
                pumatiRank,
                badgeDetails.toString(),
                imageUrl != null ? imageUrl : "(이미지 생성 실패)"
        );
    }

    private String getLatestPumatiRank(Long projectId) {
        ProjectRankingSnapshotResponseDTO latestSnapshot = projectRankingSnapshotService.getLatestSnapshot();
        if (latestSnapshot == null || latestSnapshot.getData() == null) return "N/A";

        return latestSnapshot.getData().stream()
                .filter(dto -> dto.getProjectId().equals(projectId))
                .map(RankingItemDTO::getRank)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .findFirst()
                .orElse("N/A");
    }

    private String extractImageUrlFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<>() {});
            return map.getOrDefault("reportImageUrl", null);
        } catch (Exception e) {
            log.warn("이미지 URL 파싱 실패: {}", json, e);
            return null;
        }
    }

}
