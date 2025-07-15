package com.tebutebu.apiserver.service.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.ai.report.request.BadgeStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.DailyPumatiStatDTO;
import com.tebutebu.apiserver.dto.ai.report.request.TeamInfoDTO;
import com.tebutebu.apiserver.dto.ai.report.request.WeeklyReportImageRequestDTO;
import com.tebutebu.apiserver.dto.mail.template.WeeklyReportTemplateDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import com.tebutebu.apiserver.service.ai.report.AiWeeklyReportImageRequestService;
import com.tebutebu.apiserver.service.mail.MailService;
import com.tebutebu.apiserver.service.mail.template.WeeklyReportTemplateService;
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

    private final WeeklyReportTemplateService weeklyReportTemplateService;

    @Value("${report.weekly.project-page-size}")
    private int projectPageSize;

    @Override
    public void sendWeeklyReportsToConsentingMembers() {
        CursorTimePageRequestDTO cursorTimePageRequestDTO = CursorTimePageRequestDTO.builder()
                .cursorId(null)
                .cursorTime(null)
                .pageSize(projectPageSize)
                .build();

        List<ProjectRankingSnapshotResponseDTO> snapshots = projectRankingSnapshotService.getSnapshotsForLast7Days();

        boolean hasNext;

        do {
            CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> page =
                    projectService.getLatestPage(cursorTimePageRequestDTO);

            List<ProjectPageResponseDTO> projectPageResponseDtoList = page.getData();

            for (ProjectPageResponseDTO projectDTO : projectPageResponseDtoList) {
                Long teamId = projectDTO.getTeamId();
                if (teamId == null) continue;

                List<MemberResponseDTO> members = memberService.getMembersByTeamId(teamId);
                List<MemberResponseDTO> consentingMembers = members.stream()
                        .filter(member -> Boolean.TRUE.equals(member.getHasEmailConsent()))
                        .toList();

                if (consentingMembers.isEmpty()) continue;

                WeeklyReportImageRequestDTO imageRequestDTO = generateReportImageRequest(projectDTO, snapshots);
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
                        String content = weeklyReportTemplateService.renderWeeklyReport(
                                WeeklyReportTemplateDTO.builder()
                                        .nickname(member.getNickname())
                                        .term(projectDTO.getTerm())
                                        .teamNumber(projectDTO.getTeamNumber())
                                        .projectTitle(projectDTO.getTitle())
                                        .receivedPumatiCount(projectDTO.getReceivedPumatiCount())
                                        .givedPumatiCount(projectDTO.getGivedPumatiCount())
                                        .badgeStats(teamService.getReceivedBadgeStats(projectDTO.getTeamId()))
                                        .pumatiRank(getLatestPumatiRank(projectDTO.getId()))
                                        .reportImageUrl(imageUrl)
                                        .build()
                        );
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

    private WeeklyReportImageRequestDTO generateReportImageRequest(
            ProjectPageResponseDTO projectDTO,
            List<ProjectRankingSnapshotResponseDTO> snapshots
    ) {
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

        List<DailyPumatiStatDTO> dailyPumatiStats = generateDailyStats(projectDTO.getId(), snapshots);

        return WeeklyReportImageRequestDTO.builder()
                .projectId(projectDTO.getId())
                .projectTitle(projectDTO.getTitle())
                .team(teamInfo)
                .badgeStats(badgeStats)
                .dailyPumatiStats(dailyPumatiStats)
                .build();
    }

    private List<DailyPumatiStatDTO> generateDailyStats(
            Long projectId,
            List<ProjectRankingSnapshotResponseDTO> snapshots
    ) {
        long prevGivedPumatiCount = 0, prevReceivedPumatiCount = 0;
        List<DailyPumatiStatDTO> dailyStats = new ArrayList<>();

        for (int i = 0; i < snapshots.size(); i++) {
            ProjectRankingSnapshotResponseDTO snapshot = snapshots.get(i);

            DailyPumatiStatDTO statDTO = createDailyStat(
                    snapshot, projectId, i, prevGivedPumatiCount, prevReceivedPumatiCount
            );

            dailyStats.add(statDTO);

            prevGivedPumatiCount += statDTO.givedPumatiCount();
            prevReceivedPumatiCount += statDTO.receivedPumatiCount();
        }

        return dailyStats;
    }

    private DailyPumatiStatDTO createDailyStat(
            ProjectRankingSnapshotResponseDTO snapshot,
            Long projectId,
            int index,
            long prevGivedPumatiCount,
            long prevReceivedPumatiCount
    ) {
        String dayStr = getDayOfWeekLabel(index);

        if (snapshot == null) {
            return new DailyPumatiStatDTO(dayStr, 0, 0);
        }

        RankingItemDTO item = snapshot.getData().stream()
                .filter(dto -> dto.getProjectId().equals(projectId))
                .findFirst()
                .orElse(null);

        long currentGived = (item != null && item.getGivedPumatiCount() != null)
                ? item.getGivedPumatiCount()
                : prevGivedPumatiCount;

        long currentReceived = (item != null && item.getReceivedPumatiCount() != null)
                ? item.getReceivedPumatiCount()
                : prevReceivedPumatiCount;

        long givedDiff = currentGived - prevGivedPumatiCount;
        long receivedDiff = currentReceived - prevReceivedPumatiCount;

        return new DailyPumatiStatDTO(dayStr, givedDiff, receivedDiff);
    }

    private String getDayOfWeekLabel(int index) {
        LocalDate startOfLastWeek = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .minusWeeks(1);

        return startOfLastWeek
                .plusDays(index)
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();
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
