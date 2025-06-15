package com.tebutebu.apiserver.domain;

import com.tebutebu.apiserver.domain.common.TimeStampedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "team_badge",
        uniqueConstraints = @UniqueConstraint(columnNames = {"giver_team_id", "receiver_team_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBadgeStat extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giver_team_id", nullable = false)
    private Team giverTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_team_id", nullable = false)
    private Team receiverTeam;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int acquiredCount;

    public void incrementAcquiredCount() {
        this.acquiredCount++;
    }

}

