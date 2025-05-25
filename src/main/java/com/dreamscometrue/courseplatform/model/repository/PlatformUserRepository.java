package com.dreamscometrue.courseplatform.model.repository;

import com.dreamscometrue.courseplatform.model.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {
    PlatformUser findByUsername(String username);
}