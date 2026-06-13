package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Series")
@Data
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "series_id")
    private Integer seriesId;

    @Column(nullable = false)
    private String title;

    @Column(name = "alternative_title")
    private String alternativeTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "series_type", nullable = false)
    private SeriesType seriesType;

    @Enumerated(EnumType.STRING)
    private SeriesStatus status = SeriesStatus.ongoing;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating")
    private AgeRating ageRating = AgeRating.all;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "total_views")
    private Long totalViews = 0L;

    @Column(name = "total_favorites")
    private Integer totalFavorites = 0;

    @Column(name = "average_rating")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.approved;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private TranslationGroup translationGroup;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
        name = "SeriesAuthors",
        joinColumns = @JoinColumn(name = "series_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "SeriesGenres",
        joinColumns = @JoinColumn(name = "series_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres;

    public enum SeriesType {
        manga, manhwa, manhua, comic
    }

    public enum SeriesStatus {
        ongoing, completed, hiatus, cancelled
    }

    public enum ApprovalStatus {
        pending, approved, rejected
    }

    public enum AgeRating {
        all, teen, mature
    }
}
