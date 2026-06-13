package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Genres")
@Data
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Integer genreId;

    @Column(name = "genre_name", unique = true, nullable = false)
    private String genreName;
}
