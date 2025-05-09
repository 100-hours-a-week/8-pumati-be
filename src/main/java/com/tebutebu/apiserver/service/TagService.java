package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Tag;
import com.tebutebu.apiserver.dto.tag.request.TagCreateRequestDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TagService {

    Long register(Long projectId, TagCreateRequestDTO dto);

    Tag dtoToEntity(TagCreateRequestDTO dto);

    default TagResponseDTO entityToDTO(Tag tag) {
        return TagResponseDTO.builder()
                .content(tag.getContent())
                .build();
    }

}
