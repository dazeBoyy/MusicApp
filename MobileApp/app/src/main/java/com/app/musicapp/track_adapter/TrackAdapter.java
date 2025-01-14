package com.app.musicapp.track_adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.app.musicapp.MainActivity;
import com.app.musicapp.MiniPlayerFragment;
import com.app.musicapp.R;
import com.app.musicapp.service.FilePathResponse;
import com.app.musicapp.service.Track;
import com.app.musicapp.service.TrackApiService;
import com.app.musicapp.track_player.PlayerViewModel;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> trackList;
    private Context context;


    public TrackAdapter(Context context, List<Track> trackList) {
        this.context = context;
        this.trackList = trackList;
    }


    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.trackName.setText(track.getName());
        holder.trackArtist.setText(track.getArtist());
        holder.trackDuration.setText("Duration: " + formatDuration(track.getDuration()));
        // Load the image using Glide
        Glide.with(context)
                .load(track.getImageLarge())  // Provide the image URL here
                .placeholder(R.drawable.vector)  // Optional: placeholder image while loading
                .error(R.drawable.vector)             // Optional: error image if loading fails
                .into(holder.trackImage);

        holder.playerButton.setOnClickListener(v -> {
            String artist = track.getArtist();
            String name = track.getName();
            int duration = track.getDuration();
            String photo = track.getImageLarge();

            downloadTrack(artist, name, duration, context, new Callback() {
                @Override
                public void onSuccess(String filePath) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        Log.d("PLAYER", "Файл найден: " + file.getAbsolutePath());

                        // Обновляем данные в ViewModel
                        PlayerViewModel viewModel = new ViewModelProvider((MainActivity) context).get(PlayerViewModel.class);
                        // Обновляем данные в ViewModel
                        viewModel.setTrack(filePath, name, artist, photo);  // Передаем в ViewModel

                    } else {
                        Log.d("PLAYER", "Файл не найден по пути: " + filePath);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.d("PLAYER", errorMessage);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackName, trackArtist, trackDuration;
        ImageView trackImage;
        Button playerButton;


        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.track_name);
            trackArtist = itemView.findViewById(R.id.track_artist);
            trackDuration = itemView.findViewById(R.id.track_duration);
            trackImage = itemView.findViewById(R.id.track_image);
            playerButton = itemView.findViewById(R.id.player_button);
        }
    }

    private void downloadTrack(String artist, String track, int duration, Context context, Callback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // Базовый URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TrackApiService trackApiService = retrofit.create(TrackApiService.class);
        if(duration == 0 ){
            duration = 120000;
        }
        // Асинхронный запрос к API
        trackApiService.downloadTrack(artist, track, duration).enqueue(new retrofit2.Callback<FilePathResponse>() {
            @Override
            public void onResponse(Call<FilePathResponse> call, Response<FilePathResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FilePathResponse filePathResponse = response.body(); // JSON объект с путем

                    // Получаем путь к файлу
                    String filePath = filePathResponse.getFilePath();
                    Log.d("on Response данные о пути", filePath);
                    // Проверяем, существует ли путь и скачиваем файл
                    if (filePath != null && !filePath.isEmpty()) {
                        // Скачиваем файл по локальному пути
                        new Thread(() -> {
                            try {
                                File downloadedFile = saveFileFromPath(filePath, artist + "_" + track + ".mp3", context);
                                callback.onSuccess(downloadedFile.getAbsolutePath());
                            } catch (IOException e) {
                                callback.onError("Ошибка при сохранении файла: " + e.getMessage());
                                Log.d("on не получилось скачать", "((((((((((r");
                            }
                        }).start();
                    } else {
                        callback.onError("Ошибка: неверный путь к файлу");
                    }
                } else {
                    callback.onError("Ошибка: пустой или неверный ответ сервера");
                }
            }

            @Override
            public void onFailure(Call<FilePathResponse> call, Throwable t) {
                callback.onError("Ошибка запроса: " + t.getMessage());
            }
        });
    }

    private File saveFileFromPath(String fileUrl, String fileName, Context context) throws IOException {
        // Создаем локальный файл для сохранения
        File downloadDir = context.getExternalFilesDir("Music"); // Директория для музыки
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        File outputFile = new File(downloadDir, fileName);

        // Скачиваем файл по URL
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        // Проверка, был ли успешный ответ
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Ошибка загрузки: " + connection.getResponseMessage());
        }

        // Чтение из потока и сохранение в локальный файл
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            connection.disconnect();
        }

        return outputFile;
    }

    private String formatDuration(int duration) {
        int minutes = duration / 60000;
        int seconds = (duration % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }



}
