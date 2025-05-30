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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@ToString(exclude = {"members", "memberBadges"})
public class Team extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @NotNull(message = "기수는 필수 입력 값입니다.")
    @Column(nullable = false)
    private int term;

    @NotNull(message = "팀(조) 번호는 필수 입력 값입니다.")
    @Column(nullable = false)
    private int number;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Member> members;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTeamBadge> memberBadges;

    @Builder.Default
    @Column(columnDefinition = "INT UNSIGNED")
    private Long givedPumatiCount = 0L;

    @Builder.Default
    @Column(columnDefinition = "INT UNSIGNED")
    private Long receivedPumatiCount = 0L;

    @Size(max = 512, message = "이미지 URL은 최대 512자까지 가능합니다.")
    @Column(length = 512)
    private String badgeImageUrl;

    public void increaseGivedPumati() {
        this.givedPumatiCount++;
    }

    public void increaseReceivedPumati() {
        this.receivedPumatiCount++;
    }

    public void changeBadgeImageUrl(String badgeImageUrl) {
        this.badgeImageUrl = badgeImageUrl;
    }

}
