package com.zipurl.service;

import com.zipurl.dto.CreateUrlRequest;
import com.zipurl.dto.StatsResponse;
import com.zipurl.dto.UrlResponse;
import com.zipurl.entity.Url;
import com.zipurl.exception.DuplicateShortCodeException;
import com.zipurl.exception.InvalidUrlException;
import com.zipurl.exception.ResourceNotFoundException;
import com.zipurl.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, String baseUrl) {
        String normalizedUrl = normalizeAndValidateUrl(request.getOriginalUrl());
        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().trim().isEmpty()) {
            shortCode = request.getCustomCode().trim();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new DuplicateShortCodeException("Custom short code '" + shortCode + "' is already in use.");
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        Url url = new Url(normalizedUrl, shortCode, LocalDateTime.now());
        Url saved = urlRepository.save(url);
        return UrlResponse.fromEntity(saved, baseUrl);
    }

    @Transactional
    public String getOriginalUrlAndIncrementCount(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code '" + shortCode + "' not found."));

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlByShortCode(String shortCode, String baseUrl) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code '" + shortCode + "' not found."));

        return UrlResponse.fromEntity(url, baseUrl);
    }

    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls(String baseUrl) {
        return urlRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(url -> UrlResponse.fromEntity(url, baseUrl))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUrl(Long id) {
        if (!urlRepository.existsById(id)) {
            throw new ResourceNotFoundException("URL with ID " + id + " not found.");
        }
        urlRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long totalUrls = urlRepository.count();
        long totalClicks = urlRepository.sumTotalClicks();
        long recentUrls = urlRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(1));
        return new StatsResponse(totalUrls, totalClicks, recentUrls);
    }

    private String normalizeAndValidateUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty.");
        }

        String cleaned = rawUrl.trim();
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "https://" + cleaned;
        }

        try {
            URI uri = new URI(cleaned);
            if (uri.getHost() == null || uri.getHost().trim().isEmpty() || !uri.getHost().contains(".")) {
                throw new InvalidUrlException("Invalid URL domain format: " + rawUrl);
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL structure: " + rawUrl);
        }
    }

    private String generateUniqueShortCode() {
        String code;
        int attempts = 0;
        do {
            code = generateRandomBase62(DEFAULT_CODE_LENGTH);
            attempts++;
            if (attempts > 10) {
                // Expand code length if high collision rate
                code = generateRandomBase62(DEFAULT_CODE_LENGTH + 1);
            }
        } while (urlRepository.existsByShortCode(code));

        return code;
    }

    private String generateRandomBase62(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
