package com.cuutruyen.repository;

import com.cuutruyen.entity.Rating;
import com.cuutruyen.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    java.util.List<Rating> findBySeriesId(Integer seriesId);
}
