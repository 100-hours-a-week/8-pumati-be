package com.tebutebu.apiserver.domain;

import com.tebutebu.apiserver.domain.common.TimeStampedEntity;
import com.tebutebu.apiserver.domain.enums.Course;
import com.tebutebu.apiserver.domain.enums.MemberRole;
import com.tebutebu.apiserver.domain.enums.MemberState;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"team", "comments", "attendancesDaily", "attendancesWeekly", "subscriptions"})
public class Member extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean isSocial = true;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    private Course course;

    @Column(length = 512)
    private String profileImageUrl;

    @Builder.Default
    private MemberRole role = MemberRole.USER;

    @Builder.Default
    private MemberState state = MemberState.ACTIVE;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Comment> comments;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<AttendanceDaily> attendancesDaily;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<AttendanceWeekly> attendancesWeekly;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    private LocalDateTime emailOptInAt;

    private LocalDateTime emailOptOutAt;

    public void changeCourse(Course course) {
        this.course = course;
    }

    public void changeTeam(Team team) {
        this.team = team;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void changeRole(MemberRole role) {
        this.role = role;
    }

    public void changeState(MemberState state) {
        this.state = state;
    }

    public void agreeToReceiveEmail() {
        this.emailOptInAt = LocalDateTime.now();
        this.emailOptOutAt = null;
    }

    public void declineToReceiveEmail() {
        this.emailOptOutAt = LocalDateTime.now();
    }

    public boolean hasEmailConsent() {
        return this.emailOptInAt != null && this.emailOptOutAt == null;
    }

}
