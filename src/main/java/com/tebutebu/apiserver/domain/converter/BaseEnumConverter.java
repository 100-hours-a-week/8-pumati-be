package com.tebutebu.apiserver.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public abstract class BaseEnumConverter<T extends Enum<T>> implements AttributeConverter<T, String> {

    private final Class<T> enumType;

    protected BaseEnumConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return (attribute != null) ? attribute.name() : null;
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}