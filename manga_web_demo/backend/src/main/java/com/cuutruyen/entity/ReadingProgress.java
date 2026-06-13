package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ReadingProgress")
@IdClass(ReadingProgressId.class)
@Data
public class ReadingProgress {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "series_id")
    private Integer seriesId;

    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "page_number")
    private Integer pageNumber = 1;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "series_id", insertable = false, updatable = false)
    private Series series;
}

