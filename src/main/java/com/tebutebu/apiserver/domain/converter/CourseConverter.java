package com.tebutebu.apiserver.domain.converter;

import com.tebutebu.apiserver.domain.enums.Course;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CourseConverter extends BaseEnumConverter<Course> {
    public CourseConverter() {
        super(Course.class);
    }
}
