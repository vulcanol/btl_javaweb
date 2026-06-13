package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Authors")
@Data
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
