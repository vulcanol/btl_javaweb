package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Pages")
@Data
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Integer pageId;

    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "page_number", nullable = false)
    private Short pageNumber;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
}
