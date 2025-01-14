package com.project.findsimilartracks.controller;
import com.project.findsimilartracks.dto.DownloadTrack;
import com.project.findsimilartracks.dto.FilePathResponse;
import com.project.findsimilartracks.dto.TrackDTO;
import com.project.findsimilartracks.service.LastFmService;
import com.project.findsimilartracks.service.TrackDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackDownloadService trackDownloadService;

    private final LastFmService lastFmService;

    private static final String MUSIC_DIRECTORY =  System.getenv("TRACKS_DIRECTORY");

    public TrackController(LastFmService lastFmService) {
        this.lastFmService = lastFmService;
    }



    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getTrack(@PathVariable String filename) {
        // Путь к файлу
        File file = new File(MUSIC_DIRECTORY + filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build(); // Возвращаем 404, если файл не найден
        }

        // Создаем ресурс из файла
        Resource resource = new FileSystemResource(file);

        // Устанавливаем заголовки для скачивания
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<FilePathResponse> downloadTrack(@RequestParam String artist,
                                                       @RequestParam String track,
                                                       @RequestParam(defaultValue = "120000") Integer duration) {
        String trackPath = trackDownloadService.getOrDownloadTrack(artist, track, duration);
        if (trackPath == null) {
            return ResponseEntity.notFound().build();
        }

        // Возвращаем URL для скачивания
        String fileUrl = System.getenv("APP_URL") + "/api/tracks/" + new File(trackPath).getName();
        FilePathResponse downloadTrack = new FilePathResponse(fileUrl);
        return ResponseEntity.ok(downloadTrack);
    }

    @GetMapping("/search")
    public ResponseEntity<TrackDTO> searchTrack(@RequestParam String artist, @RequestParam String track) {
        TrackDTO trackInfo = lastFmService.searchTrack(artist, track);
        if (trackInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trackInfo);
    }

    @PostMapping("/similar")
    public ResponseEntity<List<TrackDTO>> getSimilarTracks(
            @RequestParam String artist,
            @RequestParam String track,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            // Вызываем метод с поддержкой повторных попыток
            List<TrackDTO> detailedTracks = fetchTracksWithRetry(artist, track, limit);

            // Проверяем результат
            if (detailedTracks.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(detailedTracks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    /**
     * Метод для повторных попыток получения похожих треков с уменьшением лимита.
     */
    private List<TrackDTO> fetchTracksWithRetry(String artist, String track, int limit) {
        final int MIN_LIMIT = 5; // Минимальный размер лимита
        try {
            // Пытаемся получить похожие треки
            return lastFmService.getDetailedSimilarTracks(artist, track, limit);
        } catch (Exception e) {
            // Логируем ошибку
            System.err.println("Error fetching tracks with limit " + limit + ": " + e.getMessage());

            // Если лимит уже минимальный, прекращаем попытки
            if (limit <= MIN_LIMIT) {
                throw e; // Пробрасываем ошибку дальше
            }

            // Уменьшаем лимит вдвое и повторяем запрос
            int reducedLimit = limit / 2;
            System.out.println("Retrying with reduced limit: " + reducedLimit);
            return fetchTracksWithRetry(artist, track, reducedLimit);
        }
    }

}
