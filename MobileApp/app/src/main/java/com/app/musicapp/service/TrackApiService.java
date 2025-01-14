package com.app.musicapp.service;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TrackApiService {
    @GET("/api/tracks/search")
    Call<Track> searchTrack(
            @Query("artist") String artist,
            @Query("track") String track
    );
    @POST("/api/tracks/similar")
    Call<List<Track>> getSimilarTracks(
            @Query("artist") String artist,
            @Query("track") String track
    );

    @GET("/api/tracks/download")
    Call<FilePathResponse> downloadTrack(
            @Query("artist") String artist,
            @Query("track") String track,
            @Query("duration") int duration
    );
}