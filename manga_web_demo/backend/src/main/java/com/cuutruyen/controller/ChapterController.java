package com.cuutruyen.controller;

import com.cuutruyen.entity.*;
import com.cuutruyen.entity.Page;
import com.cuutruyen.repository.*;
import com.cuutruyen.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final UserRepository userRepository;
    private final TranslationGroupRepository translationGroupRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Chapter> getChapter(@PathVariable Integer id) {
        return chapterService.getChapter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pages")
    public ResponseEntity<List<Page>> getChapterPages(@PathVariable Integer id, Authentication auth) {
        if (auth != null) {
            chapterService.trackReadingProgress(id, auth.getName());
        }
        return ResponseEntity.ok(chapterService.getChapterPages(id));
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<List<Chapter>> getChaptersBySeries(@PathVariable Integer seriesId) {
        return ResponseEntity.ok(chapterService.getChaptersBySeries(seriesId));
    }

    @PostMapping
    public ResponseEntity<Chapter> createChapter(@RequestBody Chapter chapter) {
        return ResponseEntity.ok(chapterService.createChapter(chapter));
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Void> uploadPages(
            @PathVariable Integer id,
            @RequestParam("files") MultipartFile[] files) {
        chapterService.uploadPages(id, files);
        return ResponseEntity.ok().build();
    }

}

