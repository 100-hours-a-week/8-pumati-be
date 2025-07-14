package com.tebutebu.apiserver.domain.converter;

import com.tebutebu.apiserver.domain.enums.MemberRole;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MemberRoleConverter extends BaseEnumConverter<MemberRole> {
    public MemberRoleConverter() {
        super(MemberRole.class);
    }
}
