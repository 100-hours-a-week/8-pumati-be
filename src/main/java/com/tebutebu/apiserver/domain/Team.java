package com.tebutebu.apiserver.domain;

import com.tebutebu.apiserver.domain.common.TimeStampedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(
        name = "team",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_team_term_number",
                columnNames = { "term", "number" }
        )
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"members", "givenBadges", "receivedBadges"})
public class Team extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false)
    private int term;

    @Column(nullable = false)
    private int number;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members;

    @OneToMany(mappedBy = "giverTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamBadgeStat> givenBadges;

    @OneToMany(mappedBy = "receiverTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamBadgeStat> receivedBadges;

    @Builder.Default
    @Column(columnDefinition = "INT UNSIGNED")
    private Long givedPumatiCount = 0L;

    @Builder.Default
    @Column(columnDefinition = "INT UNSIGNED")
    private Long receivedPumatiCount = 0L;

    @Column(length = 512)
    private String badgeImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAiBadgeInProgress = false;

    public void increaseGivedPumati() {
        this.givedPumatiCount++;
    }

    public void increaseGivedPumatiBy(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        this.givedPumatiCount += amount;
    }

    public void increaseReceivedPumati() {
        this.receivedPumatiCount++;
    }

    public void increaseReceivedPumatiBy(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        this.receivedPumatiCount += amount;
    }

    public void changeBadgeImageUrl(String badgeImageUrl) {
        this.badgeImageUrl = badgeImageUrl;
    }

    public void resetPumatiCounts() {
        this.givedPumatiCount = 0L;
        this.receivedPumatiCount = 0L;
    }

    public void setAiBadgeInProgress(boolean isAiBadgeInProgress) {
        this.isAiBadgeInProgress = isAiBadgeInProgress;
    }

}
