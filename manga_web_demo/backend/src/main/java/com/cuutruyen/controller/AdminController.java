package com.cuutruyen.controller;

import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.repository.SeriesRepository;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.entity.Series;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final SeriesRepository seriesRepository;
    private final TranslationGroupRepository translationGroupRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalManga", seriesRepository.count());
        
        // Sum total views across all series
        Long totalViews = seriesRepository.findAll().stream()
                .mapToLong(Series::getTotalViews)
                .sum();
        stats.put("totalViews", totalViews);
        
        stats.put("totalGroups", translationGroupRepository.count());
        
        return ResponseEntity.ok(stats);
    }
}
