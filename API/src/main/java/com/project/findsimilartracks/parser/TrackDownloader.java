package com.project.findsimilartracks.parser;

import com.project.findsimilartracks.dto.DownloadTrack;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TrackDownloader {


    private static final String BASE_URL = "https://rus.hitmotop.com/search?q=";

    public static String downloadTrack(String artistName, String trackName, Integer targetDurationMillis) {
        try {
            // Формируем URL поиска
            String searchUrl = BASE_URL + "+" + trackName.replace(" ", "+") + "+-+" + artistName.replace(" ", "+") + "+";
            System.out.println("Поиск по URL: " + searchUrl);

            // Загружаем и парсим HTML
            Document document;
            try {
                document = Jsoup.connect(searchUrl).get();
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке страницы: " + e.getMessage());
                return null;
            }

            // Ищем блок с треками
            Elements trackElements = document.select("ul.tracks__list li.tracks__item");
            if (trackElements.isEmpty()) {
                System.out.println("Треки не найдены.");
                return null;
            }

            // Создаем список треков для дальнейшего сравнения
            List<DownloadTrack> tracks = new ArrayList<>();
            for (Element track : trackElements) {
                String trackTitle = track.select(".track__title").text();
                String trackArtist = track.select(".track__desc").text();
                String durationText = track.select(".track__fulltime").text();
                Integer durationMillis = convertDurationToMillis(durationText);

                // Игнорируем треки, длительность которых не удается извлечь
                if (durationMillis == -1) continue;

                String downloadUrl = track.select("a.track__download-btn").attr("href");
                if (!downloadUrl.isEmpty()) {
                    tracks.add(new DownloadTrack(trackTitle, trackArtist, durationMillis, downloadUrl));
                }
            }

            if (tracks.isEmpty()) {
                System.out.println("Не найдено треков для скачивания.");
                return null;
            }

            // Ограничиваем выбор первыми 5 треками
            List<DownloadTrack> firstFiveTracks = tracks.subList(0, Math.min(3, tracks.size()));

            // Сортируем треки по близости их длительности к целевой
            DownloadTrack closestTrack = findClosestTrack(firstFiveTracks, targetDurationMillis);

            // Санитизируем имя файла
            String sanitizedFileName = sanitizeFileName(closestTrack.getTrackTitle() + " - " + closestTrack.getArtistName() + ".mp3");

            // Скачиваем трек
            File outputFile = new File("tracks/" + sanitizedFileName);


            try {
                FileUtils.copyURLToFile(new URL(closestTrack.getDownloadUrl()), outputFile);
            } catch (IOException e) {
                System.err.println("Ошибка при скачивании трека: " + e.getMessage());
                return null;
            }

            System.out.println("Трек успешно скачан в файл: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            System.err.println("Общая ошибка в процессе работы: " + e.getMessage());
            return null;
        }
    }

    // Преобразование строки длительности в миллисекунды
    private static Integer convertDurationToMillis(String durationText) {
        String[] parts = durationText.split(":");
        if (parts.length != 2) return -1;

        try {
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            // Переводим в миллисекунды
            return (minutes * 60 + seconds) * 1000;  // В миллисекундах
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    // Поиск трека, ближайшего к целевой длительности
    private static DownloadTrack findClosestTrack(List<DownloadTrack> tracks, long targetDurationMillis) {
        DownloadTrack closestTrack = null;
        long closestDiff = Long.MAX_VALUE;

        for (DownloadTrack track : tracks) {
            long diff = Math.abs(track.getDurationMillis() - targetDurationMillis);
            if (diff < closestDiff) {
                closestTrack = track;
                closestDiff = diff;
            }
        }

        // Если не найдено подходящее совпадение, просто возвращаем первый трек
        if (closestTrack == null) {
            closestTrack = tracks.get(0);
        }

        return closestTrack;
    }
    // Метод для очистки имени файла от недопустимых символов
    private static String sanitizeFileName(String fileName) {
        // Заменяем недопустимые символы на "_"
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
