package com.cuutruyen.service;

import com.cuutruyen.entity.Comment;
import com.cuutruyen.repository.CommentRepository;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.repository.ChapterRepository;
import com.cuutruyen.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final SeriesRepository seriesRepository;

    public List<Comment> getCommentsByChapter(Integer chapterId) {
        return commentRepository.findByChapterChapterIdAndParentCommentIsNull(chapterId);
    }

    public Comment addComment(Integer userId, Integer seriesId, Integer chapterId, String content) {
        Comment comment = new Comment();
        comment.setUser(userRepository.findById(userId).orElseThrow());
        comment.setSeries(seriesRepository.findById(seriesId).orElseThrow());
        if (chapterId != null) {
            comment.setChapter(chapterRepository.findById(chapterId).orElse(null));
        }
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }
}
