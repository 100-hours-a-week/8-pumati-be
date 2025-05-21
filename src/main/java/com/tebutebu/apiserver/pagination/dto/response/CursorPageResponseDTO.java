package com.tebutebu.apiserver.pagination.dto.response;

import com.tebutebu.apiserver.pagination.dto.response.meta.CursorMetaDTO;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CursorPageResponseDTO<T, M extends CursorMetaDTO> {

    private List<T> data;

    private M meta;

}
