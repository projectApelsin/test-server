package com.dreamscometrue.courseplatform.model.repository;

import com.dreamscometrue.courseplatform.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}