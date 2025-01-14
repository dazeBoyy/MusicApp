package com.app.musicapp.track_player;


import android.media.AudioAttributes;
import android.media.MediaPlayer;

import java.io.IOException;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;

public class MusicPlayer {
    private static MusicPlayer instance;
    private MediaPlayer mediaPlayer;
    private String currentTrack;
    private boolean isPlaying = false;
    private Handler handler;

    private MusicPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        handler = new Handler(Looper.getMainLooper());  // Для работы с UI потоком
    }

    public static synchronized MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void playTrack(String url) {
        try {
            // Убедитесь, что mediaPlayer инициализирован
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            // Если тот же трек уже воспроизводится, не запускаем снова
            if (currentTrack != null && currentTrack.equals(url) && isPlaying) {
                return;
            }

            // Сбрасываем плеер для нового трека
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);

            // Асинхронная подготовка и воспроизведение
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                currentTrack = url;

                // Обновление UI
                handler.post(() -> {
                    // Ваш код для обновления UI, например, управление SeekBar
                });
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                currentTrack = null;
                // Обновить UI по окончании воспроизведения
                handler.post(() -> {
                    release();
                    // Код для обработки завершения трека
                });
            });

            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    // Для безопасности очистим ресурсы при уничтожении экземпляра
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
