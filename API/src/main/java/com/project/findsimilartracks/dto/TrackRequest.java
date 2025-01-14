package com.project.findsimilartracks.dto;
import jakarta.validation.constraints.NotNull;

public class TrackRequest {

    @NotNull
    private String artist;

    @NotNull
    private String track;

    // Getters and Setters
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }
}