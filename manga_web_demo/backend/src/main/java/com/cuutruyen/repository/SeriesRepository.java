package com.cuutruyen.repository;

import com.cuutruyen.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Integer> {
    // Basic CRUD provided by JpaRepository
    List<Series> findByApprovalStatus(Series.ApprovalStatus status);
    Page<Series> findByApprovalStatus(Series.ApprovalStatus status, org.springframework.data.domain.Pageable pageable);
    List<Series> findByTranslationGroup_GroupId(Integer groupId);
    List<Series> findByTranslationGroup_GroupIdAndApprovalStatus(Integer groupId, Series.ApprovalStatus status);
    boolean existsByTitle(String title);

    // Search methods
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM Series s LEFT JOIN s.genres g WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.genreName) LIKE LOWER(CONCAT('%', :query, '%'))) AND s.approvalStatus = :status")
    Page<Series> searchByTitleOrGenre(String query, Series.ApprovalStatus status, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Series s WHERE s.approvalStatus = :status ORDER BY s.totalViews DESC")
    List<Series> findTopByViews(Series.ApprovalStatus status, org.springframework.data.domain.Pageable pageable);
}
