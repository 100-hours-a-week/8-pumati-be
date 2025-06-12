package com.tebutebu.apiserver.fixture.s3;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PreSignedUrlRequestDTOFixture {

    public static SinglePreSignedUrlRequestDTO createSingleRequestDTO(String fileName, String contentType) {
        return SinglePreSignedUrlRequestDTO.builder()
                .fileName(fileName)
                .contentType(contentType)
                .build();
    }

    public static MultiplePreSignedUrlsRequestDTO createMultipleRequestDTO(int count) {
        List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, count)
                .mapToObj(i -> createSingleRequestDTO("file" + i + ".jpg", "image/jpeg"))
                .collect(Collectors.toList());

        return MultiplePreSignedUrlsRequestDTO.builder()
                .files(files)
                .build();
    }

}
