package com.zipurl.controller;

import com.zipurl.exception.ResourceNotFoundException;
import com.zipurl.service.UrlService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Set;

@Controller
public class RedirectController {

    private static final Set<String> RESERVED_PATHS = Set.of(
            "api", "css", "js", "images", "favicon.ico", "404", "index.html", "static"
    );

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public Object redirectToOriginal(@PathVariable String shortCode) {
        if (RESERVED_PATHS.contains(shortCode.toLowerCase())) {
            return "forward:/" + shortCode;
        }

        try {
            String originalUrl = urlService.getOriginalUrlAndIncrementCount(shortCode);
            RedirectView redirectView = new RedirectView(originalUrl);
            redirectView.setStatusCode(org.springframework.http.HttpStatus.FOUND); // 302 Redirect
            return redirectView;
        } catch (ResourceNotFoundException e) {
            return "redirect:/#404-" + shortCode;
        }
    }
}
