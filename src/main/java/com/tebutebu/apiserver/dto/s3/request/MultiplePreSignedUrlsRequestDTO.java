package com.tebutebu.apiserver.dto.s3.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplePreSignedUrlsRequestDTO {

    @NotEmpty(message = ValidationMessages.FILE_LIST_MUST_NOT_BE_EMPTY)
    @Size(min = 1, max = 10, message = ValidationMessages.REQUEST_COUNT_EXCEEDED)
    private List<@Valid SinglePreSignedUrlRequestDTO> files;

}
