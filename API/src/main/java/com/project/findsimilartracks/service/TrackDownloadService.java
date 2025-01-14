package com.project.findsimilartracks.service;

import com.project.findsimilartracks.parser.TrackDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Service
public class TrackDownloadService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "downloadedTrack:";

    private static final String DEFAULT_TRACKS_DIRECTORY = "/tracks/";

    /**
     * Проверяет наличие трека в кэше или скачивает его, если отсутствует.
     *
     * @param artist   Имя артиста.
     * @param track    Название трека
     * @param duration  Название длительность
     * @return Путь к скачанному треку.
     */
    public String getOrDownloadTrack(String artist, String track, Integer duration) {
        String cacheKey = CACHE_KEY_PREFIX + artist + ":" + track + duration;


        String TRACKS_DIRECTORY = System.getenv("TRACKS_DIRECTORY");
        if (TRACKS_DIRECTORY == null || TRACKS_DIRECTORY.isEmpty()) {
            TRACKS_DIRECTORY = DEFAULT_TRACKS_DIRECTORY;
        }

        // Проверяем кэш
        String cachedPath = redisTemplate.opsForValue().get(cacheKey);
        if (cachedPath != null) {
            File cachedFile = new File(cachedPath);
            if (cachedFile.exists()) {
                // Если файл существует, возвращаем его путь
                System.out.println("Трек найден в кэше: " + cachedPath);
                return cachedPath;
            } else {
                // Если файл не существует, удаляем его из кэша
                redisTemplate.delete(cacheKey);
                System.out.println("Трек в кэше не существует, скачиваем заново.");
            }
        }

        // Также проверяем наличие трека в папке
        File fileInFolder = new File(TRACKS_DIRECTORY + track + " - " + artist + ".mp3");
        if (fileInFolder.exists()) {
            System.out.println("Трек найден в папке: " + fileInFolder.getAbsolutePath());
            return fileInFolder.getAbsolutePath();
        }

        // Если в папке нет, скачиваем трек
        try {
            String downloadedPath = TrackDownloader.downloadTrack(artist, track, duration);
            if (downloadedPath != null) {
                // Сохраняем путь в кэш с TTL (например, 24 часа)
                redisTemplate.opsForValue().set(cacheKey, downloadedPath, Duration.ofHours(24));
                return downloadedPath;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при скачивании трека: " + e.getMessage());
        }

        return null;
    }
}