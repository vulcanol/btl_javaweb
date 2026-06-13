package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Favorites")
@IdClass(FavoriteId.class)
@Data
public class Favorite {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "series_id")
    private Integer seriesId;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "series_id", insertable = false, updatable = false)
    private Series series;
}

