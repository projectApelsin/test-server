package com.dreamscometrue.courseplatform.service;

import com.dreamscometrue.courseplatform.DTO.CourseDTO;
import com.dreamscometrue.courseplatform.model.Course;
import com.dreamscometrue.courseplatform.model.repository.CourseRepository;
import com.dreamscometrue.courseplatform.model.repository.InstructorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDurationWeeks(courseDTO.getDurationWeeks());
        course.setInstructor(instructorRepository.getOne(courseDTO.getInstructorId()));
        return courseRepository.save(course);
    }

    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }
}