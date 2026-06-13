package com.cuutruyen.repository;

import com.cuutruyen.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    java.util.Optional<Genre> findByGenreName(String genreName);
}
