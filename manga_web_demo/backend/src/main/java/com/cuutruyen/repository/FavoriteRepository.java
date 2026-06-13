package com.cuutruyen.repository;

import com.cuutruyen.entity.Favorite;
import com.cuutruyen.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUserId(Integer userId);
    Optional<Favorite> findByUserIdAndSeriesId(Integer userId, Integer seriesId);
    List<Favorite> findBySeriesId(Integer seriesId);
}
