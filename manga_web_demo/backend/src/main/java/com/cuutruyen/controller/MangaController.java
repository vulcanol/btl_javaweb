package com.cuutruyen.controller;

import com.cuutruyen.entity.Series;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.service.MangaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.cuutruyen.repository.GenreRepository;
import com.cuutruyen.entity.Genre;

@RestController
@RequestMapping("/api/manga")
@RequiredArgsConstructor
public class MangaController {
    private final MangaService mangaService;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getAllGenres() {
        return ResponseEntity.ok(genreRepository.findAll());
    }

    @PostMapping("/genres")
    public ResponseEntity<Genre> createGenre(@RequestBody java.util.Map<String, String> payload) {
        String name = payload.get("genreName");
        if (name == null || name.trim().isEmpty()) throw new RuntimeException("Genre name cannot be empty");
        Genre genre = new Genre();
        genre.setGenreName(name.trim());
        return ResponseEntity.ok(genreRepository.save(genre));
    }

    @PutMapping("/genres/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable Integer id, @RequestBody java.util.Map<String, String> payload) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new RuntimeException("Genre not found"));
        String name = payload.get("genreName");
        if (name != null && !name.trim().isEmpty()) {
            genre.setGenreName(name.trim());
        }
        return ResponseEntity.ok(genreRepository.save(genre));
    }

    @DeleteMapping("/genres/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        genreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Series>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mangaService.getLatestManga(limit));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Series>> getTopRated(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mangaService.getTopRatedManga(limit));
    }

    @GetMapping("/top-views")
    public ResponseEntity<List<Series>> getTopViews(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mangaService.getTopViewsManga(limit));
    }

    @GetMapping
    public ResponseEntity<Page<Series>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mangaService.getAllManga(page, size));
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<Series>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mangaService.getAllMangaForAdmin(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Series>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mangaService.searchManga(q, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Series> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(mangaService.getMangaById(id));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Series>> getByGroup(@PathVariable Integer groupId) {
        return ResponseEntity.ok(mangaService.getMangasByGroup(groupId));
    }

    @GetMapping("/group/{groupId}/pending")
    public ResponseEntity<List<Series>> getPendingByGroup(@PathVariable Integer groupId) {
        return ResponseEntity.ok(mangaService.getPendingMangasByGroup(groupId));
    }

    @PostMapping
    public ResponseEntity<Series> create(
            @RequestParam("title") String title,
            @RequestParam("alternativeTitle") String alternativeTitle,
            @RequestParam("description") String description,
            @RequestParam("seriesType") String seriesType,
            @RequestParam(value = "genres", required = false) List<String> genres,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam(value = "banner", required = false) MultipartFile bannerFile,
            Authentication auth) {
        User uploader = null;
        if (auth != null) {
            uploader = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return ResponseEntity
                .ok(mangaService.createSeriesWithCover(title, alternativeTitle, description, seriesType, coverFile, bannerFile, uploader, genres));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable Integer id, @RequestParam boolean isFollowing, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(mangaService.toggleFavorite(id, isFollowing));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateManga(@PathVariable Integer id, @RequestParam int rating, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(mangaService.rateManga(id, rating));
    }

    // Lấy danh sách truyện chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<List<Series>> getPendingManga() {
        return ResponseEntity.ok(mangaService.getMangaByApprovalStatus(Series.ApprovalStatus.pending));
    }

    // Duyệt truyện
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveManga(@PathVariable Integer id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        Series series = mangaService.getMangaById(id);
        
        boolean isAdmin = user.getRole() == User.Role.admin;
        boolean isLeader = series.getTranslationGroup() != null && series.getTranslationGroup().getLeader().getUserId().equals(user.getUserId());
        boolean isGroupUploader = user.getRole() == User.Role.uploader && user.getGroupId() != null && 
                                 series.getTranslationGroup() != null && user.getGroupId().equals(series.getTranslationGroup().getGroupId());

        if (!isAdmin && !isLeader && !isGroupUploader) {
            return ResponseEntity.status(403).body("Bạn không có quyền duyệt truyện này!");
        }

        series.setApprovalStatus(Series.ApprovalStatus.approved);
        return ResponseEntity.ok(mangaService.saveSeries(series));
    }

    // Từ chối truyện
    @PutMapping("/{id}/reject-manga")
    public ResponseEntity<?> rejectManga(@PathVariable Integer id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        Series series = mangaService.getMangaById(id);
        
        boolean isAdmin = user.getRole() == User.Role.admin;
        boolean isLeader = series.getTranslationGroup() != null && series.getTranslationGroup().getLeader().getUserId().equals(user.getUserId());
        boolean isGroupUploader = user.getRole() == User.Role.uploader && user.getGroupId() != null && 
                                 series.getTranslationGroup() != null && user.getGroupId().equals(series.getTranslationGroup().getGroupId());

        if (!isAdmin && !isLeader && !isGroupUploader) {
            return ResponseEntity.status(403).body("Bạn không có quyền từ chối truyện này!");
        }

        series.setApprovalStatus(Series.ApprovalStatus.rejected);
        return ResponseEntity.ok(mangaService.saveSeries(series));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManga(@PathVariable Integer id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        
        Series series = mangaService.getMangaById(id);
        if (series == null) return ResponseEntity.notFound().build();
        
        boolean isAdmin = user.getRole() == User.Role.admin;
        boolean isUploader = series.getUploadedBy() != null && series.getUploadedBy().getUserId().equals(user.getUserId());
        
        if (!isAdmin && !isUploader) {
            return ResponseEntity.status(403).build();
        }
        
        mangaService.deleteManga(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Series> updateManga(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("alternativeTitle") String alternativeTitle,
            @RequestParam("description") String description,
            @RequestParam("seriesType") String seriesType,
            @RequestParam("status") String status,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam(value = "banner", required = false) MultipartFile bannerFile,
            Authentication auth) {
        User user = null;
        if (auth != null) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(mangaService.updateSeries(id, title, alternativeTitle, description, seriesType, status, coverFile, bannerFile, user));
    }
}
