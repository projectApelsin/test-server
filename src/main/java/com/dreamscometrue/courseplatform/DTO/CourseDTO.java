package com.dreamscometrue.courseplatform.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDTO {
    public String title;
    public int durationWeeks;
    public Long instructorId;
}
