package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ratings")
@IdClass(RatingId.class)
@Data
public class Rating {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "series_id")
    private Integer seriesId;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "rated_at", updatable = false)
    private LocalDateTime ratedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "series_id", insertable = false, updatable = false)
    private Series series;
}

