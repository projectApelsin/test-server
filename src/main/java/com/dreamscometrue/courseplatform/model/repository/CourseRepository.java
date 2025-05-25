package com.dreamscometrue.courseplatform.model.repository;

import com.dreamscometrue.courseplatform.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
}