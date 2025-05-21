package com.tebutebu.apiserver.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(
        name = "oauth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_oauth_member", columnNames = "member_id"),
                @UniqueConstraint(name = "uk_oauth_provider_providerId", columnNames = {"provider", "provider_id"})
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"member"})
public class OAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotBlank(message = "제공자는 필수 입력 값입니다.")
    @Column(nullable = false, length = 20)
    private String provider;

    @NotBlank(message = "제공자 ID는 필수 입력 값입니다.")
    @Column(name = "provider_id", nullable = false, length = 50)
    private String providerId;

}
