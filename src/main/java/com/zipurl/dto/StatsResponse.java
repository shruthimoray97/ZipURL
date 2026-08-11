package com.zipurl.dto;

public class StatsResponse {

    private long totalUrls;
    private long totalClicks;
    private long recentUrls;

    public StatsResponse() {
    }

    public StatsResponse(long totalUrls, long totalClicks, long recentUrls) {
        this.totalUrls = totalUrls;
        this.totalClicks = totalClicks;
        this.recentUrls = recentUrls;
    }

    public long getTotalUrls() {
        return totalUrls;
    }

    public void setTotalUrls(long totalUrls) {
        this.totalUrls = totalUrls;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public long getRecentUrls() {
        return recentUrls;
    }

    public void setRecentUrls(long recentUrls) {
        this.recentUrls = recentUrls;
    }
}
