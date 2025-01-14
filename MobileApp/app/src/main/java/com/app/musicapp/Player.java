package com.app.musicapp;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.musicapp.track_player.MusicPlayer;
import com.app.musicapp.track_player.PlayerViewModel;
import com.bumptech.glide.Glide;

import java.util.Locale;

public class Player extends Fragment {
    private ImageButton playPauseButton;
    private ImageView albumPhoto;
    private SeekBar seekBar;
    private TextView currentTimeText, totalTimeText, trackNameText, trackArtistText;
    private MusicPlayer musicPlayer;
    private Handler handler;
    private Runnable updateSeekBar;

    private View rootView;

    public interface BigPlayerClickListener {
        void onBigPlayerClick();
    }

    private BigPlayerClickListener listener;


    public Player() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    public void setBigPlayerClickListener(BigPlayerClickListener listener) {
        this.listener = listener;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trackNameText = view.findViewById(R.id.songTitle);
        trackArtistText = view.findViewById(R.id.artistName);
        albumPhoto = view.findViewById(R.id.albumArt);
        totalTimeText = view.findViewById(R.id.totalTimeText);
        currentTimeText = view.findViewById(R.id.currentTimeText);
        seekBar = view.findViewById(R.id.seekBar);
        playPauseButton = view.findViewById(R.id.playPauseButton);

        musicPlayer = MusicPlayer.getInstance();

        initializeViews();
        setupSeekBar();

        // Получаем ViewModel
        PlayerViewModel viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        // Подписываемся на обновления данных трека
        viewModel.getTrackUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null) {
                musicPlayer.playTrack(url);
                int duration = musicPlayer.getDuration();
                seekBar.setMax(duration);
                totalTimeText.setText(formatTime(duration));
                handler.post(updateSeekBar);
            }
        });

        // Устанавливаем обработчик нажатия
        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBigPlayerClick();
            }
        });



        viewModel.getTrackName().observe(getViewLifecycleOwner(), trackNameText::setText);
        viewModel.getTrackArtist().observe(getViewLifecycleOwner(), trackArtistText::setText);
        viewModel.getTrackPhoto().observe(getViewLifecycleOwner(), photo ->
                Glide.with(this)
                        .load(photo)
                        .placeholder(R.drawable.album_temp)
                        .error(R.drawable.album_temp)
                        .into(albumPhoto));

        viewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            Log.d("MiniPlayer", "Play/Pause state updated: " + isPlaying);  // Логируем приходящее значение
            updatePlayPauseState(isPlaying);  // Обновляем UI
        });
    }


    private void initializeViews() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
    }

    private void setupSeekBar() {
        handler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (musicPlayer != null) {
                    int currentPosition = musicPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTimeText.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicPlayer.seekTo(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void togglePlayPause() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            musicPlayer.resume();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            handler.post(updateSeekBar);
        }
    }

    public void updatePlayPauseState(boolean isPlaying) {
        Log.d("MiniPlayer", "Updating play/pause state in UI: " + isPlaying);
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateSeekBar);
    }
}