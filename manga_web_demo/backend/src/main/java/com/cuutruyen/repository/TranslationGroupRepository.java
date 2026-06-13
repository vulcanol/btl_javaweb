package com.cuutruyen.repository;

import com.cuutruyen.entity.TranslationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationGroupRepository extends JpaRepository<TranslationGroup, Integer> {
    List<TranslationGroup> findByStatus(TranslationGroup.Status status);
    List<TranslationGroup> findByLeader_UserId(Integer userId);
    boolean existsByName(String name);
}
