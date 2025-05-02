package com.tebutebu.apiserver.domain.converter;

import com.tebutebu.apiserver.domain.Course;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CourseConverter implements AttributeConverter<Course, String> {

    @Override
    public String convertToDatabaseColumn(Course course) {
        if (course == null) {
            return null;
        }
        return course.name();
    }

    @Override
    public Course convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Course.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

