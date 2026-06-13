package com.cuutruyen.config;

import com.cuutruyen.entity.User;
import com.cuutruyen.entity.TranslationGroup;
import com.cuutruyen.entity.Series;
import com.cuutruyen.entity.Chapter;
import com.cuutruyen.entity.Page;
import com.cuutruyen.entity.Genre;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.repository.SeriesRepository;
import com.cuutruyen.repository.ChapterRepository;
import com.cuutruyen.repository.PageRepository;
import com.cuutruyen.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TranslationGroupRepository groupRepository;
    private final SeriesRepository seriesRepository;
    private final ChapterRepository chapterRepository;
    private final PageRepository pageRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;

    private static final long INITIAL_BALANCE = 100000L;


    @Override
    public void run(String... args) {
        log.info("Checking and initializing sample accounts...");
        
        createUserIfNotExist("admin", "admin@cuutruyen.com", "admin123", User.Role.admin);
        createUserIfNotExist("translator", "trans@cuutruyen.com", "trans123", User.Role.translator);
        createUserIfNotExist("uploader", "upload@cuutruyen.com", "upload123", User.Role.uploader);
        createUserIfNotExist("user", "user@cuutruyen.com", "user123", User.Role.user);

        // Create demo translation groups
        createDemoGroups();

        // Create demo mangas and chapters
        createDemoMangasAndChapters();

        // Assign existing mangas to groups if they don't have one
        assignMangasToDemoGroups();
        
        log.info("Initialization complete.");
    }

    private void createUserIfNotExist(String username, String email, String password, User.Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setDisplayName(username.toUpperCase());
            userRepository.save(user);
            log.info("Created user: {}", username);
        }
    }

    private void createDemoGroups() {
        User uploader = userRepository.findByUsername("uploader").orElse(null);
        User translator = userRepository.findByUsername("translator").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (uploader != null) {
            createGroupIfNotExist("Cứu Truyện Team", uploader, TranslationGroup.Status.ACTIVE);
        }
        if (translator != null) {
            createGroupIfNotExist("Manga World VN", translator, TranslationGroup.Status.ACTIVE);
        }
        if (admin != null) {
            createGroupIfNotExist("Admin Scans", admin, TranslationGroup.Status.ACTIVE);
        }
    }

    private void createGroupIfNotExist(String name, User leader, TranslationGroup.Status status) {
        if (!groupRepository.existsByName(name)) {
            TranslationGroup group = new TranslationGroup();
            group.setName(name);
            group.setLeader(leader);
            group.setStatus(status);
            group.setBalance(0L);
            groupRepository.save(group);
            log.info("Created demo group: {}", name);
        }
    }

    private Genre getOrCreateGenre(String name) {
        return genreRepository.findByGenreName(name)
                .orElseGet(() -> {
                    Genre g = new Genre();
                    g.setGenreName(name);
                    return genreRepository.save(g);
                });
    }

    private void createDemoPage(Chapter chapter, int pageNum, String imageUrl) {
        Page page = new Page();
        page.setChapter(chapter);
        page.setPageNumber((short) pageNum);
        page.setImageUrl(imageUrl);
        pageRepository.save(page);
    }

    private void createDemoMangasAndChapters() {
        // Nếu đã có truyện mẫu bằng link wikipedia cũ, hãy xóa đi để nạp lại ảnh local
        boolean hasOldDemo = seriesRepository.findAll().stream()
                .anyMatch(s -> s.getCoverUrl() != null && (s.getCoverUrl().contains("wikipedia") || s.getCoverUrl().contains("unsplash")));
        
        if (hasOldDemo) {
            log.info("Cleaning up old demo data with remote links...");
            pageRepository.deleteAll();
            chapterRepository.deleteAll();
            seriesRepository.deleteAll();
        }

        if (seriesRepository.count() > 0) {
            return;
        }

        log.info("Initializing demo mangas, chapters, and pages with local image files...");

        TranslationGroup defaultGroup = groupRepository.findAll().stream().findFirst().orElse(null);
        User adminUser = userRepository.findByUsername("admin").orElse(null);

        Genre action = getOrCreateGenre("Action");
        Genre adventure = getOrCreateGenre("Adventure");
        Genre fantasy = getOrCreateGenre("Fantasy");
        Genre comedy = getOrCreateGenre("Comedy");

        // 1. One Piece
        Series op = new Series();
        op.setTitle("One Piece");
        op.setAlternativeTitle("Đảo Hải Tặc");
        op.setDescription("Cuộc hành trình tìm kiếm kho báu vĩ đại nhất One Piece của Monkey D. Luffy và băng hải tặc Mũ Rơm.");
        op.setSeriesType(Series.SeriesType.manga);
        op.setStatus(Series.SeriesStatus.ongoing);
        op.setAgeRating(Series.AgeRating.all);
        op.setCoverUrl("/uploads/covers/one_piece.png");
        op.setApprovalStatus(Series.ApprovalStatus.approved);
        op.setTranslationGroup(defaultGroup);
        op.setUploadedBy(adminUser);
        op.setGenres(java.util.Set.of(action, adventure, fantasy));
        op.setCreatedAt(LocalDateTime.now());
        op.setUpdatedAt(LocalDateTime.now());
        op.setTotalViews(15420L);
        op.setAverageRating(new BigDecimal("4.8"));
        op.setTotalRatings(120);
        seriesRepository.save(op);

        Chapter opCh1 = new Chapter();
        opCh1.setSeries(op);
        opCh1.setChapterNumber(new BigDecimal("1.0"));
        opCh1.setTitle("Bình Minh Của Cuộc Phiêu Lưu");
        opCh1.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(opCh1);

        createDemoPage(opCh1, 1, "/uploads/chapters/manga_page_1.png");
        createDemoPage(opCh1, 2, "/uploads/chapters/manga_page_2.png");
        createDemoPage(opCh1, 3, "/uploads/chapters/manga_page_1.png");

        Chapter opCh2 = new Chapter();
        opCh2.setSeries(op);
        opCh2.setChapterNumber(new BigDecimal("2.0"));
        opCh2.setTitle("Cậu Bé Luffy Tóc Đỏ");
        opCh2.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(opCh2);
        createDemoPage(opCh2, 1, "/uploads/chapters/manga_page_2.png");
        createDemoPage(opCh2, 2, "/uploads/chapters/manga_page_1.png");

        // 2. Solo Leveling
        Series sl = new Series();
        sl.setTitle("Solo Leveling");
        sl.setAlternativeTitle("Tôi Thăng Cấp Một Mình");
        sl.setDescription("Trong một thế giới nơi các thợ săn phải chiến đấu với quái vật, thợ săn yếu nhất Sung Jin-Woo bất ngờ nhận được một sức mạnh độc nhất vô nhị.");
        sl.setSeriesType(Series.SeriesType.manhwa);
        sl.setStatus(Series.SeriesStatus.completed);
        sl.setAgeRating(Series.AgeRating.teen);
        sl.setCoverUrl("/uploads/covers/solo_leveling.png");
        sl.setApprovalStatus(Series.ApprovalStatus.approved);
        sl.setTranslationGroup(defaultGroup);
        sl.setUploadedBy(adminUser);
        sl.setGenres(java.util.Set.of(action, fantasy));
        sl.setCreatedAt(LocalDateTime.now());
        sl.setUpdatedAt(LocalDateTime.now());
        sl.setTotalViews(98200L);
        sl.setAverageRating(new BigDecimal("4.9"));
        sl.setTotalRatings(340);
        seriesRepository.save(sl);

        Chapter slCh1 = new Chapter();
        slCh1.setSeries(sl);
        slCh1.setChapterNumber(new BigDecimal("1.0"));
        slCh1.setTitle("Hầm Ngục Kép");
        slCh1.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(slCh1);
        createDemoPage(slCh1, 1, "/uploads/chapters/manga_page_1.png");
        createDemoPage(slCh1, 2, "/uploads/chapters/manga_page_2.png");

        // 3. Doraemon
        Series dr = new Series();
        dr.setTitle("Doraemon");
        dr.setAlternativeTitle("Chú Mèo Máy Đến Từ Tương Lai");
        dr.setDescription("Câu chuyện về chú mèo máy Doraemon quay ngược thời gian để giúp đỡ cậu bé Nobita hậu đậu nhưng nhân hậu.");
        dr.setSeriesType(Series.SeriesType.manga);
        dr.setStatus(Series.SeriesStatus.ongoing);
        dr.setAgeRating(Series.AgeRating.all);
        dr.setCoverUrl("/uploads/covers/doraemon.png");
        dr.setApprovalStatus(Series.ApprovalStatus.approved);
        dr.setTranslationGroup(defaultGroup);
        dr.setUploadedBy(adminUser);
        dr.setGenres(java.util.Set.of(comedy, fantasy));
        dr.setCreatedAt(LocalDateTime.now());
        dr.setUpdatedAt(LocalDateTime.now());
        dr.setTotalViews(5430L);
        dr.setAverageRating(new BigDecimal("4.7"));
        dr.setTotalRatings(80);
        seriesRepository.save(dr);

        Chapter drCh1 = new Chapter();
        drCh1.setSeries(dr);
        drCh1.setChapterNumber(new BigDecimal("1.0"));
        drCh1.setTitle("Người Bạn Đến Từ Tương Lai");
        drCh1.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(drCh1);
        createDemoPage(drCh1, 1, "/uploads/chapters/manga_page_2.png");
        createDemoPage(drCh1, 2, "/uploads/chapters/manga_page_1.png");

        log.info("Demo mangas initialized successfully.");
    }

    private void assignMangasToDemoGroups() {
        List<Series> mangas = seriesRepository.findAll();
        List<TranslationGroup> groups = groupRepository.findAll();
        if (groups.isEmpty()) return;

        TranslationGroup defaultGroup = groups.get(0); // Cứu Truyện Team

        for (Series s : mangas) {
            boolean updated = false;
            if (s.getTranslationGroup() == null) {
                s.setTranslationGroup(defaultGroup);
                updated = true;
            }
            if (s.getUploadedBy() == null) {
                s.setUploadedBy(defaultGroup.getLeader());
                updated = true;
            }
            if (s.getApprovalStatus() == null) {
                s.setApprovalStatus(Series.ApprovalStatus.approved);
                updated = true;
            }
            if (s.getCoverUrl() != null && !s.getCoverUrl().startsWith("/") && !s.getCoverUrl().startsWith("http")) {
                s.setCoverUrl("/uploads/covers/" + s.getCoverUrl());
                updated = true;
            }
            if (updated) {
                seriesRepository.save(s);
            }
        }
        if (!mangas.isEmpty()) {
            log.info("Assigned {} existing mangas to demo groups.", mangas.size());
        }
    }
}
