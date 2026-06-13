package com.cuutruyen.controller;

import com.cuutruyen.entity.*;
import com.cuutruyen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReadingProgressRepository readingProgressRepository;

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Favorite> favorites = favoriteRepository.findByUserId(user.getUserId());
        // Return series data from favorites
        return ResponseEntity.ok(favorites.stream().map(Favorite::getSeries).toList());
    }

    @PostMapping("/favorites/{seriesId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Integer seriesId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndSeriesId(user.getUserId(), seriesId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return ResponseEntity.ok(Map.of("message", "Unfollowed", "following", false));
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(user.getUserId());
            favorite.setSeriesId(seriesId);
            // added_at will be set automatically by entity default
            favoriteRepository.save(favorite);
            return ResponseEntity.ok(Map.of("message", "Followed", "following", true));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(readingProgressRepository.findByUserIdOrderByUpdatedAtDesc(user.getUserId()));
    }}
