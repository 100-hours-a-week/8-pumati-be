package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.fortune.request.FortuneGenerateRequestDTO;

public interface FortuneService {

    DevLuckDTO getnerateDevLuck(FortuneGenerateRequestDTO request);

}
