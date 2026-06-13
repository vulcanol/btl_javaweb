package com.cuutruyen.service;

import com.cuutruyen.entity.Series;
import com.cuutruyen.entity.User;
import com.cuutruyen.entity.TranslationGroup;
import com.cuutruyen.repository.SeriesRepository;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.repository.ChapterRepository;
import com.cuutruyen.repository.PageRepository;
import com.cuutruyen.repository.CommentRepository;
import com.cuutruyen.repository.FavoriteRepository;
import com.cuutruyen.repository.RatingRepository;
import com.cuutruyen.repository.ReadingProgressRepository;
import com.cuutruyen.entity.Chapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.StandardCopyOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MangaService {
    private final SeriesRepository seriesRepository;
    private final TranslationGroupRepository translationGroupRepository;
    private final ChapterRepository chapterRepository;
    private final PageRepository pageRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final com.cuutruyen.repository.GenreRepository genreRepository;
    private static final String UPLOAD_DIR = "uploads";

    public List<Series> getLatestManga(int limit) {
        return seriesRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();
    }

    public List<Series> getTopRatedManga(int limit) {
        return seriesRepository.findAll(PageRequest.of(0, limit, Sort.by("averageRating").descending())).getContent();
    }

    public List<Series> getTopViewsManga(int limit) {
        return seriesRepository.findTopByViews(Series.ApprovalStatus.approved,
                PageRequest.of(0, limit));
    }

    public Page<Series> getAllManga(int page, int size) {
        return seriesRepository.findByApprovalStatus(Series.ApprovalStatus.approved,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<Series> getAllMangaForAdmin(int page, int size) {
        return seriesRepository.findAll(PageRequest.of(page, size, Sort.by("seriesId").ascending()));
    }

    public Page<Series> searchManga(String query, int page, int size) {
        return seriesRepository.searchByTitleOrGenre(
                query, Series.ApprovalStatus.approved, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Series getMangaById(Integer id) {
        return seriesRepository.findById(id).orElseThrow(() -> new RuntimeException("Manga not found"));
    }

    public Series createSeries(Series series) {
        series.setCreatedAt(LocalDateTime.now());
        series.setUpdatedAt(LocalDateTime.now());
        return seriesRepository.save(series);
    }

    public Series createSeriesWithCover(String title, String alternativeTitle, String description, String seriesType,
            MultipartFile coverFile, MultipartFile bannerFile, User uploader, List<String> genreNames) {
        Series series = new Series();
        series.setTitle(title);
        series.setAlternativeTitle(alternativeTitle);
        series.setDescription(description);
        if (seriesType != null) {
            try {
                series.setSeriesType(Series.SeriesType.valueOf(seriesType.trim().toLowerCase()));
            } catch (Exception e) {
                log.warn("Invalid seriesType during creation: {}. Defaulting to manga.", seriesType);
                series.setSeriesType(Series.SeriesType.manga);
            }
        } else {
            series.setSeriesType(Series.SeriesType.manga);
        }
        series.setUploadedBy(uploader);

        // Process Genres
        if (genreNames != null && !genreNames.isEmpty()) {
            java.util.Set<com.cuutruyen.entity.Genre> genres = new java.util.HashSet<>();
            for (String gName : genreNames) {
                com.cuutruyen.entity.Genre genre = genreRepository.findByGenreName(gName.trim())
                    .orElseGet(() -> {
                        com.cuutruyen.entity.Genre newGenre = new com.cuutruyen.entity.Genre();
                        newGenre.setGenreName(gName.trim());
                        return genreRepository.save(newGenre);
                    });
                genres.add(genre);
            }
            series.setGenres(genres);
        }

        // Find group of uploader
        if (uploader != null) {
            List<TranslationGroup> groups = translationGroupRepository.findByLeader_UserId(uploader.getUserId());
            if (!groups.isEmpty()) {
                series.setTranslationGroup(groups.get(0));
            } else if (uploader.getGroupId() != null) {
                translationGroupRepository.findById(uploader.getGroupId()).ifPresent(series::setTranslationGroup);
            }
        }

        // Tất cả truyện đăng lên đều phải qua bước duyệt (pending)
        series.setApprovalStatus(Series.ApprovalStatus.pending);

        series.setCreatedAt(LocalDateTime.now());
        series.setUpdatedAt(LocalDateTime.now());

        // Handle cover image upload
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String coverUrl = uploadCoverImage(coverFile);
                series.setCoverUrl(coverUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload cover image: " + e.getMessage(), e);
            }
        }

        // Handle banner image upload
        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                String bannerUrl = uploadCoverImage(bannerFile);
                series.setBannerUrl(bannerUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload banner image: " + e.getMessage(), e);
            }
        }

        return seriesRepository.save(series);
    }

    private String uploadCoverImage(MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        File uploadsDir = new File(UPLOAD_DIR, "covers");
        if (!uploadsDir.exists()) {
            log.info("Creating directory: {}", uploadsDir.getAbsolutePath());
            uploadsDir.mkdirs();
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            originalFileName = "cover.jpg";
        }
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // Save file
        Path filePath = Paths.get(UPLOAD_DIR, "covers", uniqueFileName).toAbsolutePath();
        log.info("Saving cover to: {}", filePath);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return relative path for database
        return "/uploads/covers/" + uniqueFileName;
    }

    public Series toggleFavorite(Integer id, boolean isFollowing) {
        Series series = getMangaById(id);
        if (series.getTotalFavorites() == null)
            series.setTotalFavorites(0);

        if (isFollowing) {
            series.setTotalFavorites(series.getTotalFavorites() + 1);
        } else {
            series.setTotalFavorites(Math.max(0, series.getTotalFavorites() - 1));
        }
        return seriesRepository.save(series);
    }

    public Series rateManga(Integer id, int rating) {
        Series series = getMangaById(id);
        if (series.getTotalRatings() == null)
            series.setTotalRatings(0);
        if (series.getAverageRating() == null)
            series.setAverageRating(java.math.BigDecimal.ZERO);

        int currentCount = series.getTotalRatings();
        java.math.BigDecimal currentAvg = series.getAverageRating();

        java.math.BigDecimal newTotal = currentAvg.multiply(new java.math.BigDecimal(currentCount))
                .add(new java.math.BigDecimal(rating));
        int newCount = currentCount + 1;
        java.math.BigDecimal newAvg = newTotal.divide(new java.math.BigDecimal(newCount), 1,
                java.math.RoundingMode.HALF_UP);

        series.setTotalRatings(newCount);
        series.setAverageRating(newAvg);
        return seriesRepository.save(series);
    }

    public Series saveSeries(Series series) {
        series.setUpdatedAt(LocalDateTime.now());
        return seriesRepository.save(series);
    }

    public Series updateSeries(Integer id, String title, String alternativeTitle, String description, String seriesType,
            String status, MultipartFile coverFile, MultipartFile bannerFile, User currentUser) {
        Series series = getMangaById(id);

        // Kiểm tra quyền: Admin hoặc Uploader gốc hoặc (Cùng nhóm dịch AND
        // (Leader/Member))
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().name().equalsIgnoreCase("admin");
        boolean isOriginalUploader = series.getUploadedBy() != null
                && series.getUploadedBy().getUserId().equals(currentUser.getUserId());
        boolean isLeader = series.getTranslationGroup() != null
                && series.getTranslationGroup().getLeader().getUserId().equals(currentUser.getUserId());
        boolean isMemberOfSameGroup = series.getTranslationGroup() != null && currentUser.getGroupId() != null &&
                currentUser.getGroupId().equals(series.getTranslationGroup().getGroupId());

        if (!isAdmin && !isOriginalUploader && !isLeader && !isMemberOfSameGroup) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa truyện này!");
        }

        if (!series.getTitle().equals(title) && seriesRepository.existsByTitle(title)) {
            throw new RuntimeException("Tên truyện '" + title + "' đã tồn tại trên hệ thống!");
        }

        series.setTitle(title);
        series.setAlternativeTitle(alternativeTitle);
        series.setDescription(description);
        if (seriesType != null && !seriesType.trim().isEmpty()) {
            try {
                series.setSeriesType(Series.SeriesType.valueOf(seriesType.trim().toLowerCase()));
            } catch (Exception e) {
                log.warn("Invalid seriesType provided: {}", seriesType);
            }
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                series.setStatus(Series.SeriesStatus.valueOf(status.trim().toLowerCase()));
            } catch (Exception e) {
                log.warn("Invalid status provided: {}", status);
            }
        }
        series.setUpdatedAt(LocalDateTime.now());

        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String coverUrl = uploadCoverImage(coverFile);
                series.setCoverUrl(coverUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update cover image: " + e.getMessage(), e);
            }
        }

        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                String bannerUrl = uploadCoverImage(bannerFile);
                series.setBannerUrl(bannerUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update banner image: " + e.getMessage(), e);
            }
        }

        return seriesRepository.save(series);
    }

    public List<Series> getMangaByApprovalStatus(Series.ApprovalStatus status) {
        return seriesRepository.findByApprovalStatus(status);
    }

    public List<Series> getMangasByGroup(Integer groupId) {
        return seriesRepository.findByTranslationGroup_GroupId(groupId);
    }

    public List<Series> getPendingMangasByGroup(Integer groupId) {
        return seriesRepository.findByTranslationGroup_GroupIdAndApprovalStatus(groupId, Series.ApprovalStatus.pending);
    }

    @Transactional
    public void deleteManga(Integer id) {
        // 1. Delete all pages of all chapters of this series
        List<Chapter> chapters = chapterRepository.findAllBySeriesSeriesId(id);
        for (var chapter : chapters) {
            // Delete pages
            var pages = pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId());
            pageRepository.deleteAll(pages);
        }

        // 2. Delete series-level related records
        commentRepository.deleteAll(commentRepository.findBySeriesSeriesId(id));
        favoriteRepository.deleteAll(favoriteRepository.findBySeriesId(id));
        ratingRepository.deleteAll(ratingRepository.findBySeriesId(id));
        readingProgressRepository.deleteAll(readingProgressRepository.findBySeriesId(id));

        // 3. Delete all chapters
        chapterRepository.deleteAll(chapters);

        // 4. Delete the series
        seriesRepository.deleteById(id);
    }
}
