package com.cuutruyen.controller;

import com.cuutruyen.entity.Report;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.ReportRepository;
import com.cuutruyen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String entityType = (String) payload.get("entityType");
        if (entityType == null || !java.util.List.of("series", "manga", "comment", "user", "chapter").contains(entityType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Loại báo cáo không hợp lệ"));
        }

        Object entityIdObj = payload.get("entityId");
        if (entityIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "ID thực thể không được để trống"));
        }

        Report report = new Report();
        report.setUser(user);
        report.setEntityType(entityType.toLowerCase());
        try {
            report.setEntityId(Integer.valueOf(entityIdObj.toString()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "ID thực thể phải là số nguyên"));
        }
        report.setReason((String) payload.get("reason"));
        report.setStatus("PENDING");

        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Báo cáo đã được gửi thành công"));
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(reportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase()));
        }
        return ResponseEntity.ok(reportRepository.findAllByOrderByCreatedAtDesc());
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        String status = payload.getOrDefault("status", "RESOLVED");
        report.setStatus(status.toUpperCase());
        reportRepository.save(report);
        
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái báo cáo thành công", "report", report));
    }
}
