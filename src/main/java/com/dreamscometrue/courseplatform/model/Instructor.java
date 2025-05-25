package com.dreamscometrue.courseplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "instructor")
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "expertise", nullable = false)
    private String expertise;

    @JsonIgnore
    @OneToMany(mappedBy = "instructor", orphanRemoval = true)
    private Set<Course> courses = new LinkedHashSet<>();

}