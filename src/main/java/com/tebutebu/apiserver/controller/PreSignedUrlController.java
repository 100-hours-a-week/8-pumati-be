package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;
import com.tebutebu.apiserver.service.s3.PreSignedUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class PreSignedUrlController {

    private final PreSignedUrlService preSignedUrlService;

    @PostMapping("/pre-signed-url")
    public ResponseEntity<?> createPreSignedUrl(
            @Valid @RequestBody SinglePreSignedUrlRequestDTO dto
    ) {
        SinglePreSignedUrlResponseDTO data = preSignedUrlService.generatePreSignedUrl(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "getPreSignedUrlSuccess",
                        "data", data
                ));
    }

    @PostMapping("/pre-signed-urls")
    public ResponseEntity<?> createPreSignedUrls(
            @Valid @RequestBody MultiplePreSignedUrlsRequestDTO dto
    ) {
        MultiplePreSignedUrlsResponseDTO data = preSignedUrlService.generatePreSignedUrls(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "getPreSignedUrlsSuccess",
                        "data", data
                ));
    }

}
