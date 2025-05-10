package com.tebutebu.apiserver.dto.snapshot.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingItemDTO {

    @JsonProperty("project_id")
    private Long projectId;

    private Integer rank;

    @JsonProperty("gived_pumati_count")
    private Long givedPumatiCount;

}
