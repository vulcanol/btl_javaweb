package com.cuutruyen.controller;

import com.cuutruyen.entity.Comment;
import com.cuutruyen.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;

    @GetMapping("/chapter/{id}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getCommentsByChapter(id));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, Object> payload, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        
        Integer userId = user.getUserId();
        Integer seriesId = payload.get("seriesId") != null ? Integer.valueOf(payload.get("seriesId").toString()) : null;
        Integer chapterId = payload.get("chapterId") != null ? Integer.valueOf(payload.get("chapterId").toString()) : null;
        String content = (String) payload.get("content");
        
        return ResponseEntity.ok(commentService.addComment(userId, seriesId, chapterId, content));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Comment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteCommentAdmin(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
