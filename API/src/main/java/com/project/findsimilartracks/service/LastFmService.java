package com.project.findsimilartracks.service;

import com.project.findsimilartracks.algorithm.TrackSimilarityAlgorithm;
import com.project.findsimilartracks.dto.TrackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class LastFmService {

    @Value("${lastfm.api.key}")
    private String apiKey;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String LASTFM_API_URL = "https://ws.audioscrobbler.com/2.0/";
    private final RestTemplate restTemplate;

    public LastFmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<TrackDTO> getDetailedSimilarTracks(String artist, String track, int limit) {
        // Получаем список похожих треков
        List<Map<String, Object>> similarTracks = getSimilarTracks(artist, track, limit);

        // Если похожих треков нет, возвращаем только информацию о заданном треке
        if (similarTracks == null || similarTracks.isEmpty()) {
            System.out.println("Похожие треки не найдены. Возвращаем информацию о введённом треке.");
            Map<String, Object> trackInfo = getTrackInfo(artist, track);
            if (trackInfo != null) {
                TrackDTO singleTrack = convertToTrackDTO(trackInfo);
                return singleTrack != null ? Collections.singletonList(singleTrack) : Collections.emptyList();
            } else {
                return Collections.emptyList();
            }
        }

        // Формируем список подробной информации о похожих треках
        List<TrackDTO> detailedTracks = new ArrayList<>();
        for (Map<String, Object> trackData : similarTracks) {
            // Проверяем наличие данных
            Map<String, Object> artistData = (Map<String, Object>) trackData.get("artist");
            String similarArtist = artistData != null ? (String) artistData.get("name") : null;
            String similarTrackName = (String) trackData.get("name");
            Double match = (Double) trackData.get("match");

            // Если данные существуют, получаем подробную информацию
            if (similarArtist != null && similarTrackName != null) {
                Map<String, Object> trackInfo = getTrackInfo(similarArtist, similarTrackName);
                if (trackInfo != null) {
                    TrackDTO trackDTO = convertToTrackDTO(trackInfo);
                    if (trackDTO != null) {
                        trackDTO.setMatch(match); // Устанавливаем значение "match"
                        detailedTracks.add(trackDTO);
                    }
                }
            }
        }

        // Получаем информацию о референсном треке
        Map<String, Object> referenceTrackData = getTrackInfo(artist, track);
        if (referenceTrackData == null) {
            return Collections.emptyList();
        }
        TrackDTO referenceTrack = convertToTrackDTO(referenceTrackData);

        // Сортируем треки по схожести с использованием алгоритма
        TrackSimilarityAlgorithm similarityAlgorithm = new TrackSimilarityAlgorithm();
        List<TrackDTO> sortedTracks = similarityAlgorithm.findSimilarTracks(referenceTrack, detailedTracks, limit);

        return sortedTracks;
    }

    private List<Map<String, Object>> getSimilarTracks(String artist, String track, int limit) {
        String cacheKey = String.format("similarTracks:%s:%s:%d", artist, track, limit);

        // Проверяем кэш
        List<Map<String, Object>> cachedTracks = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTracks != null) {
            System.out.println("Returning from cache");
            return cachedTracks;
        }

        String url = String.format(
                "%s?method=track.getsimilar&artist=%s&track=%s&api_key=%s&limit=%d&format=json",
                LASTFM_API_URL, artist, track, apiKey, limit
        );

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody();

        List<Map<String, Object>> tracks = new ArrayList<>();
        if (body != null && body.containsKey("similartracks")) {
            Map<String, Object> similarTracks = (Map<String, Object>) body.get("similartracks");
            tracks = (List<Map<String, Object>>) similarTracks.get("track");

            // Извлекаем значение "match" для каждого трека и сохраняем его в дополнительном контейнере
            if (tracks != null) {
                for (Map<String, Object> trackData : tracks) {
                    Double match = (Double) trackData.get("match");
                    if (match != null) {
                        trackData.put("match", match);  // Сохраняем значение "match" в каждом треке
                    }
                }
            }
        }

        // Сохраняем результат в Redis с TTL (например, 1 час)
        redisTemplate.opsForValue().set(cacheKey, tracks, Duration.ofHours(1));

        return tracks;
    }
    private Map<String, Object> getTrackInfo(String artist, String track) {
        String url = String.format(
                "%s?method=track.getInfo&artist=%s&track=%s&api_key=%s&format=json",
                LASTFM_API_URL, artist, track, apiKey
        );

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public TrackDTO searchTrack (String artist, String track) {
        String cacheKey = String.format("searchTrack:%s:%s", artist, track);

        // Проверяем кэш
        TrackDTO cachedTracks = (TrackDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTracks != null) {
            System.out.println("Returning from cache");
            return cachedTracks;
        }
        else {
            TrackDTO info = convertToTrackDTO(getTrackInfo(artist,track));
            redisTemplate.opsForValue().set(cacheKey, info, Duration.ofHours(1));
            return info;
        }

    }

    public TrackDTO convertToTrackDTO(Map<String, Object> trackData) {

        if (trackData == null) {
            return null;
        }

        TrackDTO trackDTO = new TrackDTO();

        // Извлекаем основную информацию о треке из поля "track"
        Map<String, Object> track = (Map<String, Object>) trackData.get("track");

        if (track != null) {
            // Обработка названия трека
            String trackName = (String) track.get("name");
            if (trackName != null) {
                trackDTO.setName(trackName);
            } else {
                System.out.println("Track name is null");
                trackDTO.setName(null);
            }

            // Обработка данных артиста
            Map<String, Object> artistData = (Map<String, Object>) track.get("artist");
            if (artistData != null) {
                String artistName = (String) artistData.get("name");
                if (artistName != null) {
                    trackDTO.setArtist(artistName);
                } else {
                    System.out.println("Artist name is null");
                    trackDTO.setArtist(null);
                }
            } else {
                System.out.println("Artist data is null");
                trackDTO.setArtist(null);
            }

            // Обработка данных об альбоме
            Map<String, Object> albumData = (Map<String, Object>) track.get("album");
            if (albumData != null) {
                String albumTitle = (String) albumData.get("title");
                if (albumTitle != null) {
                    trackDTO.setAlbum(albumTitle);
                } else {
                    System.out.println("Album title is null");
                    trackDTO.setAlbum(null);
                }
                String albumUrl = (String) albumData.get("url");
                trackDTO.setAlbumUrl(albumUrl);
            } else {
                System.out.println("Album data is null");
                trackDTO.setAlbum(null);
                trackDTO.setAlbumUrl(null);
            }

            // Обработка количества воспроизведений
            String playCountStr = (String) track.get("playcount");
            if (playCountStr != null) {
                try {
                    Integer playCount = Integer.parseInt(playCountStr);
                    trackDTO.setPlayCount(playCount);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid playcount format");
                    trackDTO.setPlayCount(0);
                }
            } else {
                trackDTO.setPlayCount(0);
            }

            // Обработка количества слушателей
            String listenersStr = (String) track.get("listeners");
            if (listenersStr != null) {
                try {
                    Integer listeners = Integer.parseInt(listenersStr);
                    trackDTO.setListeners(listeners);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid listeners format");
                    trackDTO.setListeners(0);
                }
            } else {
                trackDTO.setListeners(0);
            }

            // Обработка URL трека
            String trackUrl = (String) track.get("url");
            if (trackUrl != null) {
                trackDTO.setUrl(trackUrl);
            } else {
                trackDTO.setUrl(null);
            }
        }
        // Обработка тегов
        Map<String, Object> toptags = (Map<String, Object>) track.get("toptags");
        if (toptags != null) {
            List<Map<String, Object>> tagsList = (List<Map<String, Object>>) toptags.get("tag");
            if (tagsList != null) {
                List<String> tags = new ArrayList<>();
                for (Map<String, Object> tag : tagsList) {
                    String tagName = (String) tag.get("name");
                    String tagUrl = (String) tag.get("url");
                    if (tagName != null) {
                        tags.add(tagName);
                    }
                    // Вы можете добавить обработку URL тега, если нужно
                    // if (tagUrl != null) { ... }
                }
                trackDTO.setTags(tags);
            }
        } else {
            trackDTO.setTags(Collections.emptyList()); // В случае отсутствия тегов, передаем пустой список
        }
        // Добавление параметра match
        String matchStr = (String) track.get("match");
        if (matchStr != null) {
            try {
                Double match = Double.parseDouble(matchStr);
                trackDTO.setMatch(match);
            } catch (NumberFormatException e) {
                System.out.println("Invalid match format");
                trackDTO.setMatch(0.0);
            }
        } else {
            trackDTO.setMatch(0.0); // Если match отсутствует, присваиваем значение по умолчанию
        }

        // Добавление параметра duration
        String durationStr = (String) track.get("duration");
        if (durationStr != null) {
            try {
                Integer duration = Integer.parseInt(durationStr);
                trackDTO.setDuration(duration);
            } catch (NumberFormatException e) {
                System.out.println("Invalid duration format");
                trackDTO.setDuration(0); // Если формат неверный, присваиваем значение по умолчанию
            }
        } else {
            trackDTO.setDuration(0); // Если duration отсутствует, присваиваем значение по умолчанию
        }
        // Обработка изображений
        Map<String, Object> album = (Map<String, Object>) track.get("album");
        if (album != null) {
            List<Map<String, Object>> imageData = (List<Map<String, Object>>) album.get("image");
            if (imageData != null) {
                String smallImageUrl = null;
                String mediumImageUrl = null;
                String largeImageUrl = null;

                for (Map<String, Object> image : imageData) {
                    String size = (String) image.get("size");
                    String imageUrl = (String) image.get("#text");

                    if (imageUrl != null) {
                        switch (size) {
                            case "small":
                                smallImageUrl = imageUrl;
                                break;
                            case "medium":
                                mediumImageUrl = imageUrl;
                                break;
                            case "large":
                                largeImageUrl = imageUrl;
                                break;
                            default:
                                // Можно обработать другие размеры, если они присутствуют
                                break;
                        }
                    }
                }

                trackDTO.setImageSmall(smallImageUrl);
                trackDTO.setImageMedium(mediumImageUrl);
                trackDTO.setImageLarge(largeImageUrl);
            } else {
                // В случае отсутствия изображений, можно передать null или другие значения
                trackDTO.setImageSmall(null);
                trackDTO.setImageMedium(null);
                trackDTO.setImageLarge(null);

            }
        } else {
            // В случае отсутствия данных "album"
            trackDTO.setImageSmall(null);
            trackDTO.setImageMedium(null);
            trackDTO.setImageLarge(null);
        }


        return trackDTO;
    }


}