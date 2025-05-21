package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.fortune.request.AiFortuneGenerateRequestDTO;

public interface AiFortuneService {

    DevLuckDTO generateDevLuck(AiFortuneGenerateRequestDTO request);

}
