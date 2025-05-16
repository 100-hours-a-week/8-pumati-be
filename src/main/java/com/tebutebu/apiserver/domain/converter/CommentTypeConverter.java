package com.tebutebu.apiserver.domain.converter;

import com.tebutebu.apiserver.domain.CommentType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CommentTypeConverter extends BaseEnumConverter<CommentType> {
    public CommentTypeConverter() {
        super(CommentType.class);
    }
}
