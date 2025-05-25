package com.dreamscometrue.courseplatform.controller;

import com.dreamscometrue.courseplatform.model.Enrollment;
import com.dreamscometrue.courseplatform.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<Enrollment> enrollStudent(@RequestParam Long studentId, @RequestParam Long courseId) {
        Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/by-student/{id}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(id));
    }
}