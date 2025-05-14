package com.tebutebu.apiserver.domain;

import com.tebutebu.apiserver.domain.common.TimeStampedEntity;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"team"})
public class Member extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Email(message = "올바른 이메일 주소 형식을 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean isSocial = true;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 가능합니다.")
    @Pattern(regexp = "^\\S+$", message = "이름에 공백 불가")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
    @Pattern(regexp = "^\\S+$", message = "닉네임에 공백 불가")
    @Column(nullable = false)
    private String nickname;

    private Course course;

    @Size(max = 512, message = "이미지 URL은 최대 512자까지 가능합니다.")
    @Column(length = 512)
    private String profileImageUrl;

    @Builder.Default
    private MemberRole role = MemberRole.USER;

    @Builder.Default
    private MemberState state = MemberState.ACTIVE;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<AttendanceDaily> attendancesDaily;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<AttendanceWeekly> attendancesWeekly;

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

}
