package com.dreamscometrue.courseplatform.model.repository;

import com.dreamscometrue.courseplatform.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}