package com.tebutebu.apiserver.pagination.dto.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CursorPageResponseDTO<T> {

    private List<T> data;

    private CursorMetaDTO meta;

}
