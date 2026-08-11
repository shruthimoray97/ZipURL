package com.zipurl.controller;

import com.zipurl.dto.CreateUrlRequest;
import com.zipurl.dto.StatsResponse;
import com.zipurl.dto.UrlResponse;
import com.zipurl.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
public class UrlApiController {

    private final UrlService urlService;

    public UrlApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request,
                                                     HttpServletRequest servletRequest) {
        String baseUrl = extractBaseUrl(servletRequest);
        UrlResponse response = urlService.createShortUrl(request, baseUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UrlResponse>> getAllUrls(HttpServletRequest servletRequest) {
        String baseUrl = extractBaseUrl(servletRequest);
        List<UrlResponse> urls = urlService.getAllUrls(baseUrl);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = urlService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> getUrlInfo(@PathVariable String shortCode,
                                                  HttpServletRequest servletRequest) {
        String baseUrl = extractBaseUrl(servletRequest);
        UrlResponse response = urlService.getUrlByShortCode(shortCode, baseUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }

    private String extractBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        return url.toString();
    }
}
