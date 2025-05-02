package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {

    Long registerOAuthUser(MemberOAuthSignupRequestDTO dto);

    Member dtoToEntity(MemberOAuthSignupRequestDTO dto, String email);

}
