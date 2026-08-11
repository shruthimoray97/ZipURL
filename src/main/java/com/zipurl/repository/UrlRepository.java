package com.zipurl.repository;

import com.zipurl.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<Url> findAllByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM Url u")
    long sumTotalClicks();
}
