package com.project.findsimilartracks.dto;

public class DownloadTrack {
    private String trackTitle;
    private String artistName;
    private Integer durationMillis;
    private String downloadUrl;



    public DownloadTrack(String trackTitle, String artistName, Integer durationMillis, String downloadUrl) {
        this.trackTitle = trackTitle;
        this.artistName = artistName;
        this.durationMillis = durationMillis;
        this.downloadUrl = downloadUrl;
    }


    public String getTrackTitle() {
        return trackTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}