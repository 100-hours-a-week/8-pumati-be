package com.tebutebu.apiserver.service.ai.fortune;

import com.tebutebu.apiserver.dto.ai.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.ai.fortune.request.AiFortuneGenerateRequestDTO;

public interface AiFortuneService {

    DevLuckDTO generateDevLuck(AiFortuneGenerateRequestDTO request);

}
