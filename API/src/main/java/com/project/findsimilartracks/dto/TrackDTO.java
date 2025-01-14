package com.project.findsimilartracks.dto;

import java.util.List;

public class TrackDTO {
    private String name;
    private String artist;
    private String album;
    private String albumUrl;
    private List<String> tags;
    private int playCount;
    private int listeners;
    private String url;
    private Double match; // Для хранения match
    private Integer duration; // Для хранения duration

    private String imageSmall;
    private String imageMedium;
    private String imageLarge;

    // Конструкторы, геттеры и сеттеры
    public TrackDTO() {}

    public TrackDTO(String name, String artist, String album, String albumUrl, List<String> tags, int playCount, int listeners, String url, Double match, Integer duration, String imageSmall, String imageMedium, String imageLarge) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.albumUrl = albumUrl;
        this.tags = tags;
        this.playCount = playCount;
        this.listeners = listeners;
        this.url = url;
        this.match = match;
        this.duration = duration;
        this.imageSmall = imageSmall;
        this.imageMedium = imageMedium;
        this.imageLarge = imageLarge;
    }

    // Геттеры и сеттеры


    public String getImageSmall() {
        return imageSmall;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(String imageMedium) {
        this.imageMedium = imageMedium;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public void setImageLarge(String imageLarge) {
        this.imageLarge = imageLarge;
    }

    public Double getMatch() {
        return match;
    }

    public void setMatch(Double match) {
        this.match = match;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}