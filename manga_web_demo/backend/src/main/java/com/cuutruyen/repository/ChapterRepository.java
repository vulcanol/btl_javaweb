package com.cuutruyen.repository;

import com.cuutruyen.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberDesc(Integer seriesId);
    List<Chapter> findAllBySeriesSeriesId(Integer seriesId);
}
