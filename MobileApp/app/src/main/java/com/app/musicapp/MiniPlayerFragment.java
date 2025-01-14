package com.app.musicapp;

import androidx.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.musicapp.track_player.MusicPlayer;
import com.app.musicapp.track_player.PlayerViewModel;

import com.app.musicapp.track_player.MusicPlayer;
import com.app.musicapp.track_player.PlayerViewModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MiniPlayerFragment extends Fragment {

    public MiniPlayerFragment(){

    }

    private ImageButton miniPlayPauseButton, miniFavoriteButton;
    private TextView miniSongTitle;
    private TextView miniArtistName;
    private MusicPlayer musicPlayer;

    private ImageView miniAlbumArt;
    private View rootView;
    private PlayerViewModel viewModel;

    private Handler handler = new Handler(Looper.getMainLooper());

    private MiniPlayerClickListener listener;

    public interface MiniPlayerClickListener {
        void onMiniPlayerClicked();
    }

    public void setMiniPlayerClickListener(MiniPlayerClickListener listener) {
        this.listener = listener;
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            // Обновление SeekBar
            // Ваш код для обновления SeekBar
            handler.postDelayed(this, 1000); // Повторяем каждые 1000 миллисекунд
        }
    };
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Устанавливаем обработчик нажатия
        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMiniPlayerClicked();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.mini_player, container, false);
        musicPlayer = MusicPlayer.getInstance();

        miniPlayPauseButton = rootView.findViewById(R.id.miniPlayPauseButton);
        miniSongTitle = rootView.findViewById(R.id.miniSongTitle);
        miniArtistName = rootView.findViewById(R.id.miniArtistName);
        miniFavoriteButton = rootView.findViewById(R.id.miniFavoriteButton);
        miniAlbumArt = rootView.findViewById(R.id.miniAlbumArt);

        viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);



        // Подписка на данные трека
        viewModel.getTrackName().observe(getViewLifecycleOwner(), trackName -> {
            Log.d("MiniPlayer", "Track name updated: " + trackName);  // Логируем приходящее значение
            updateTrackTitle(trackName);  // Обновляем UI
            checkFavoriteState(trackName);
        });

        viewModel.getTrackArtist().observe(getViewLifecycleOwner(), artist -> {
            Log.d("MiniPlayer", "Artist name updated: " + artist);  // Логируем приходящее значение
            updateArtistName(artist);  // Обновляем UI
        });

        viewModel.getTrackPhoto().observe(getViewLifecycleOwner(), photo -> {
            Log.d("MiniPlayer", "Artist photo updated: " + photo);  // Логируем приходящее значение
            updateAlbumArt(photo);  // Обновляем UI
        });

        viewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            Log.d("MiniPlayer", "Play/Pause state updated: " + isPlaying);  // Логируем приходящее значение
            updatePlayPauseState(isPlaying);  // Обновляем UI
        });

            // Подписка на URL трека для воспроизведения
        viewModel.getTrackUrl().observe(getViewLifecycleOwner(), trackUrl -> {
            Log.d("MiniPlayer", "Track URL updated: " + trackUrl);  // Логируем приходящее значение
            startTrack(trackUrl);  // Воспроизводим трек
        });

        miniPlayPauseButton.setOnClickListener(v -> togglePlayPause());

        miniFavoriteButton.setOnClickListener(v -> toggleFavorite());

        return rootView;
    }

    public void updateTrackTitle(String trackName) {
        Log.d("MiniPlayer", "Updating track title in UI: " + trackName);
        miniSongTitle.setText(trackName);
    }

    public void updateAlbumArt(String albumUrl) {
        Log.d("MiniPlayer", "Updating track albumUrl in UI: " + albumUrl);
        Glide.with(this)
                .load(albumUrl)  // Provide the image URL here
                .placeholder(R.drawable.album_temp)  // Optional: placeholder image while loading
                .error(R.drawable.album_temp)             // Optional: error image if loading fails
                .into(miniAlbumArt);
    }

    public void updateArtistName(String artistName) {
        Log.d("MiniPlayer", "Updating artist name in UI: " + artistName);
        miniArtistName.setText(artistName);
    }

    public void updatePlayPauseState(boolean isPlaying) {
        Log.d("MiniPlayer", "Updating play/pause state in UI: " + isPlaying);
        miniPlayPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    public void startTrack(String trackUrl) {
        Log.d("MiniPlayer", "Starting track: " + trackUrl);
        musicPlayer.playTrack(trackUrl);
    }

    private void togglePlayPause() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            miniPlayPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            musicPlayer.resume();
            miniPlayPauseButton.setImageResource(R.drawable.ic_pause);
            handler.post(updateSeekBar);
        }
    }

    // Проверка, добавлен ли трек в избранное
    private void checkFavoriteState(String trackName) {
        if (trackName == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference trackRef = database.getReference("favorite_tracks/" + userId + "/" + trackName);
        trackRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                // Трек в избранном
                miniFavoriteButton.setImageResource(R.drawable.mini_favorite_true); // Устанавливаем "лайкнутую" иконку
            } else {
                // Трек не в избранном
                miniFavoriteButton.setImageResource(R.drawable.mini_favorite); // Устанавливаем "не лайкнутую" иконку
            }
        }).addOnFailureListener(e -> Log.e("MiniPlayer", "Failed to check favorite state", e));
    }

    // Добавление/удаление трека из избранного
    private void toggleFavorite() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String trackName = viewModel.getTrackName().getValue();
        String artist = viewModel.getTrackArtist().getValue();
        String photo = viewModel.getTrackPhoto().getValue();
        Log.e("MiniPlayer", photo+ " путь");
        int duration = musicPlayer.getDuration();

        if (trackName == null || artist == null) {
            Log.e("MiniPlayer", "Track or artist is null, cannot toggle favorite");
            return;
        }

        DatabaseReference trackRef = database.getReference("favorite_tracks/" + userId + "/" + trackName);

        trackRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                // Если трек уже добавлен, удаляем его
                trackRef.removeValue().addOnSuccessListener(aVoid -> {
                    Log.d("MiniPlayer", "Track removed from favorites");
                    miniFavoriteButton.setImageResource(R.drawable.mini_favorite); // Меняем иконку на "не избранное"
                }).addOnFailureListener(e -> Log.e("MiniPlayer", "Failed to remove track from favorites", e));
            } else {
                // Если трека нет, добавляем его
                Map<String, Object> track = new HashMap<>();
                track.put("name", trackName);
                track.put("artist", artist);
                track.put("duration", duration);
                track.put("imageLarge", photo);

                trackRef.setValue(track).addOnSuccessListener(aVoid -> {
                    Log.d("MiniPlayer", "Track added to favorites");
                    miniFavoriteButton.setImageResource(R.drawable.mini_favorite_true); // Меняем иконку на "избранное"
                }).addOnFailureListener(e -> Log.e("MiniPlayer", "Failed to add track to favorites", e));
            }
        }).addOnFailureListener(e -> Log.e("MiniPlayer", "Error checking track in database", e));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Запуск обновления SeekBar
        handler.post(updateSeekBar);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Остановка обновления SeekBar, чтобы избежать утечек памяти
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов, связанных с этим фрагментом
        handler.removeCallbacks(updateSeekBar);
    }

}
