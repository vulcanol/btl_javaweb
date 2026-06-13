package com.cuutruyen.repository;

import com.cuutruyen.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByStatusOrderByCreatedAtDesc(String status);
    List<Report> findAllByOrderByCreatedAtDesc();
}
