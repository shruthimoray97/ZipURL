package com.zipurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL length cannot exceed 2048 characters")
    private String originalUrl;

    @Size(max = 32, message = "Custom short code cannot exceed 32 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Custom short code can only contain alphanumeric characters, hyphens, and underscores")
    private String customCode;

    public CreateUrlRequest() {
    }

    public CreateUrlRequest(String originalUrl, String customCode) {
        this.originalUrl = originalUrl;
        this.customCode = customCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomCode() {
        return customCode;
    }

    public void setCustomCode(String customCode) {
        this.customCode = customCode;
    }
}
