package com.tebutebu.apiserver.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"member"})
public class AttendanceWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "last_checked_date", nullable = false)
    private LocalDate lastCheckedDate;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int streak = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary", columnDefinition = "json", nullable = false)
    private Map<String, Boolean> summary;

    public void incrementStreak() {
        this.streak++;
    }

    public void resetStreak() {
        this.streak = 0;
    }

    public void updateLastCheckedDate(LocalDate date) {
        this.lastCheckedDate = date;
    }

    public void updateSummary(Map<String, Boolean> summary) {
        this.summary = summary;
    }

}
