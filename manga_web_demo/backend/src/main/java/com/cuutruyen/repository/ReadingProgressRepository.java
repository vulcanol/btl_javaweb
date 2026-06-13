package com.cuutruyen.repository;

import com.cuutruyen.entity.ReadingProgress;
import com.cuutruyen.entity.ReadingProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, ReadingProgressId> {
    List<ReadingProgress> findByUserIdOrderByUpdatedAtDesc(Integer userId);
    List<ReadingProgress> findBySeriesId(Integer seriesId);
}
