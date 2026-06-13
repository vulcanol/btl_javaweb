package com.cuutruyen.repository;

import com.cuutruyen.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByChapterChapterIdAndParentCommentIsNull(Integer chapterId);
    List<Comment> findBySeriesSeriesId(Integer seriesId);
}
