package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Tag;
import com.tebutebu.apiserver.dto.tag.request.TagCreateRequestDTO;
import com.tebutebu.apiserver.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public Long register(Long projectId, TagCreateRequestDTO dto) {
        return tagRepository.findByContentIgnoreCase(dto.getContent())
                .map(Tag::getId)
                .orElseGet(() -> tagRepository.save(dtoToEntity(dto)).getId());
    }

    @Override
    public Tag dtoToEntity(TagCreateRequestDTO dto) {
        return Tag.builder()
                .content(dto.getContent())
                .build();
    }

}
