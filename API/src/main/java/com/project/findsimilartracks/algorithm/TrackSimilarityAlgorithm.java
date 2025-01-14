package com.project.findsimilartracks.algorithm;


import com.project.findsimilartracks.dto.TrackDTO;
import com.project.findsimilartracks.dto.TrackRequest;

import java.util.*;
import java.util.stream.Collectors;

public class TrackSimilarityAlgorithm {


    private static final double TAGS_WEIGHT = 0.4;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double MATCH_WEIGHT = 0.1;
    private static final double LISTENERS_WEIGHT = 0.3;
    private static final double PLAYCOUNT_WEIGHT = 0.3;
    private static final double RANDOMNESS_FACTOR = 0.3;

    public List<TrackDTO> findSimilarTracks(TrackDTO referenceTrack, List<TrackDTO> trackList, int limit) {
        Map<TrackDTO, Double> similarityScores = new HashMap<>();
        Random random = new Random();

        for (TrackDTO track : trackList) {
            if (track.getName().equals(referenceTrack.getName())
                    && track.getAlbum().equals(referenceTrack.getArtist())) {
                continue;
            }

            double similarityScore = calculateSimilarity(referenceTrack, track);


            // Добавляем случайный шум
            double randomNoise = RANDOMNESS_FACTOR * random.nextDouble();
            similarityScore += randomNoise;


            similarityScores.put(track, similarityScore);
        }

        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<TrackDTO, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateSimilarity(TrackDTO reference, TrackDTO canndidate){
        double tagsSimilarity = calculateTagsSimilarity(reference.getTags(), canndidate.getTags());
        double durationSimilarity = calculateNumericSimilarity(reference.getDuration(), canndidate.getDuration(), 600000);
        double matchSimilarity = canndidate.getMatch();
        double listenersSimilarity = calculateNumericSimilarity(reference.getListeners(), canndidate.getListeners(),5000000);
        double playcountsSimilarity = calculateNumericSimilarity(reference.getPlayCount(), canndidate.getPlayCount(),99000000);

        return (tagsSimilarity * TAGS_WEIGHT) +
                (durationSimilarity + DURATION_WEIGHT) +
                (matchSimilarity * MATCH_WEIGHT) +
                (listenersSimilarity * LISTENERS_WEIGHT) +
                (playcountsSimilarity * PLAYCOUNT_WEIGHT);
    }


    private  double calculateNumericSimilarity(double value1, double value2, double maxDifference){
        double difference = Math.abs(value1 - value2);
        return Math.max(0, 1 - (difference / maxDifference));
    }

    private double calculateTagsSimilarity(List<String> tags1, List<String> tags2) {
        Set<String> set1 = new HashSet<>(tags1);
        Set<String> set2 = new HashSet<>(tags2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(tags1);
        union.addAll(set2);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }



}
