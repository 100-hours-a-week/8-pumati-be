package com.tebutebu.apiserver.domain.converter;

import com.tebutebu.apiserver.domain.MemberState;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MemberStateConverter extends BaseEnumConverter<MemberState> {
    public MemberStateConverter() {
        super(MemberState.class);
    }
}
