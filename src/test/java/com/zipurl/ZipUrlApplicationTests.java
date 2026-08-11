package com.zipurl;

import com.zipurl.dto.CreateUrlRequest;
import com.zipurl.dto.StatsResponse;
import com.zipurl.dto.UrlResponse;
import com.zipurl.exception.DuplicateShortCodeException;
import com.zipurl.exception.InvalidUrlException;
import com.zipurl.exception.ResourceNotFoundException;
import com.zipurl.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ZipUrlApplicationTests {

    @Autowired
    private UrlService urlService;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(urlService);
    }

    @Test
    @DisplayName("Should create short URL and generate valid short code")
    void testCreateShortUrlSuccess() {
        CreateUrlRequest request = new CreateUrlRequest("https://example.com/some/very/long/url", null);
        UrlResponse response = urlService.createShortUrl(request, "http://localhost:8080");

        assertNotNull(response.getId());
        assertNotNull(response.getShortCode());
        assertEquals(6, response.getShortCode().length());
        assertEquals("https://example.com/some/very/long/url", response.getOriginalUrl());
        assertEquals("http://localhost:8080/" + response.getShortCode(), response.getShortUrl());
        assertEquals(0, response.getClickCount());
    }

    @Test
    @DisplayName("Should support custom short codes")
    void testCustomShortCodeSuccess() {
        CreateUrlRequest request = new CreateUrlRequest("example.com", "custom-alias");
        UrlResponse response = urlService.createShortUrl(request, "http://localhost:8080");

        assertEquals("custom-alias", response.getShortCode());
        assertEquals("https://example.com", response.getOriginalUrl());
    }

    @Test
    @DisplayName("Should throw DuplicateShortCodeException when custom code already exists")
    void testDuplicateCustomShortCodeThrowsException() {
        CreateUrlRequest request1 = new CreateUrlRequest("https://example.com/1", "my-code");
        urlService.createShortUrl(request1, "http://localhost:8080");

        CreateUrlRequest request2 = new CreateUrlRequest("https://example.com/2", "my-code");
        assertThrows(DuplicateShortCodeException.class, () -> {
            urlService.createShortUrl(request2, "http://localhost:8080");
        });
    }

    @Test
    @DisplayName("Should throw InvalidUrlException for invalid domain format")
    void testInvalidUrlThrowsException() {
        CreateUrlRequest request = new CreateUrlRequest("not-a-valid-domain", null);
        assertThrows(InvalidUrlException.class, () -> {
            urlService.createShortUrl(request, "http://localhost:8080");
        });
    }

    @Test
    @DisplayName("Should increment click count when resolving original URL")
    void testRedirectIncrementsClickCount() {
        CreateUrlRequest request = new CreateUrlRequest("https://google.com", "google-link");
        urlService.createShortUrl(request, "http://localhost:8080");

        String resolvedUrl = urlService.getOriginalUrlAndIncrementCount("google-link");
        assertEquals("https://google.com", resolvedUrl);

        UrlResponse info = urlService.getUrlByShortCode("google-link", "http://localhost:8080");
        assertEquals(1, info.getClickCount());
    }

    @Test
    @DisplayName("Should compute global stats correctly")
    void testGlobalStats() {
        CreateUrlRequest req1 = new CreateUrlRequest("https://site1.com", "s1");
        CreateUrlRequest req2 = new CreateUrlRequest("https://site2.com", "s2");
        urlService.createShortUrl(req1, "http://localhost:8080");
        urlService.createShortUrl(req2, "http://localhost:8080");

        urlService.getOriginalUrlAndIncrementCount("s1");
        urlService.getOriginalUrlAndIncrementCount("s1");

        StatsResponse stats = urlService.getStats();
        assertTrue(stats.getTotalUrls() >= 2);
        assertTrue(stats.getTotalClicks() >= 2);
    }

    @Test
    @DisplayName("Should delete shortened URL successfully")
    void testDeleteUrl() {
        CreateUrlRequest request = new CreateUrlRequest("https://delete-me.com", "temp-link");
        UrlResponse created = urlService.createShortUrl(request, "http://localhost:8080");

        urlService.deleteUrl(created.getId());

        assertThrows(ResourceNotFoundException.class, () -> {
            urlService.getUrlByShortCode("temp-link", "http://localhost:8080");
        });
    }
}
