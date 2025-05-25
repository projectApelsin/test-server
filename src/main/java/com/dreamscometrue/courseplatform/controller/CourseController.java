package com.dreamscometrue.courseplatform.controller;

import com.dreamscometrue.courseplatform.DTO.CourseDTO;
import com.dreamscometrue.courseplatform.model.Course;
import com.dreamscometrue.courseplatform.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody CourseDTO courseDTO) {
        Course savedCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(savedCourse);
    }

    @GetMapping("/by-instructor/{id}")
    public ResponseEntity<List<Course>> getCoursesByInstructor(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(id));
    }
}
