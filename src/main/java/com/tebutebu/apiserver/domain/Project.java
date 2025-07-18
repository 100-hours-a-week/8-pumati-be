package com.tebutebu.apiserver.domain;

import com.tebutebu.apiserver.domain.common.TimeStampedEntity;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"team", "images", "projectTags", "comments", "subscriptions"})
public class Project extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, length = 64)
    private String title;

    @Column(length = 150)
    private String introduction;

    @Column(name = "detailed_description", length = 1000)
    private String detailedDescription;

    @Column(name = "representative_image_url", length = 512)
    private String representativeImageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectImage> images = new ArrayList<>();

    @Column(name = "deployment_url", length = 512)
    private String deploymentUrl;

    @Column(name = "github_url", length = 512)
    private String githubUrl;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTag> projectTags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<TagResponseDTO> tagContents;

    @Builder.Default
    @OneToMany(mappedBy="project", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void changeDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public void changeRepresentativeImageUrl(String representativeImageUrl) {
        this.representativeImageUrl = representativeImageUrl;
    }

    public void changeDeploymentUrl(String deploymentUrl) {
        this.deploymentUrl = deploymentUrl;
    }

    public void changeGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void addTag(Tag tag) {
        ProjectTag pt = ProjectTag.builder()
                .project(this)
                .tag(tag)
                .build();
        this.projectTags.add(pt);
        tag.getProjectTags().add(pt);
    }

    public void replaceTags(List<Tag> tags) {
        this.projectTags.clear();
        if (tags != null) {
            tags.forEach(this::addTag);
        }
    }

    public void changeTagContents(List<String> contents) {
        this.tagContents = contents == null ? List.of() :
                contents.stream()
                        .map(TagResponseDTO::new)
                        .collect(Collectors.toList());
    }

}
