package com.app.musicapp.track_player;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> trackName = new MutableLiveData<>();
    private MutableLiveData<String> trackArtist = new MutableLiveData<>();
    private MutableLiveData<String> trackUrl = new MutableLiveData<>();

    private MutableLiveData<String> trackPhoto = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    public MutableLiveData<String> getTrackName() {
        Log.d("PlayerViewModel", "Returning track name: " + trackName.getValue());
        return trackName;
    }

    public MutableLiveData<String> getTrackArtist() {
        Log.d("PlayerViewModel", "Returning artist name: " + trackArtist.getValue());
        return trackArtist;
    }

    public MutableLiveData<String> getTrackUrl() {
        Log.d("PlayerViewModel", "Returning track URL: " + trackUrl.getValue());
        return trackUrl;
    }

    public MutableLiveData<String> getTrackPhoto() {
        return trackPhoto;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setTrack(String url, String name, String artist, String photo) {
        Log.d("PlayerViewModel", "Setting track: URL=" + url + ", Name=" + name + ", Artist=" + artist);
        trackUrl.postValue(url);  // Используем postValue вместо setValue
        trackName.postValue(name);  // Используем postValue вместо setValue
        trackArtist.postValue(artist);  // Используем postValue вместо setValue
        trackPhoto.postValue(photo);  // Используем postValue вместо setValue
        isPlaying.postValue(true);  // Используем postValue вместо setValue
    }

    public void setTrackName(String Name) {
        trackName.setValue(Name);
    }

    public void setTrackArtist(String Artist) {
        trackArtist.setValue(Artist);
    }

    public void setTrackUrl(String Url) {
       trackUrl.setValue(Url);
    }

    public void setTrackPhoto(String Photo) {
        trackPhoto.setValue(Photo); ;
    }
}
