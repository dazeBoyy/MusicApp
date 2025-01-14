package com.app.musicapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.musicapp.service.Track;
import com.app.musicapp.track_adapter.TrackAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class FavoriteTracks extends Fragment {

    private List<Track> favoriteTracks; // Список треков для отображения

    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_favorite_tracks, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewFavoriteTracks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализация списка, если это ещё не сделано
        if (favoriteTracks == null) {
            favoriteTracks = new ArrayList<>();
        }

        trackAdapter = new TrackAdapter(getContext(), favoriteTracks);
        recyclerView.setAdapter(trackAdapter);
        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Call loadFavoriteTracks after the fragment view is fully created
        loadFavoriteTracks();
    }


    public void loadFavoriteTracks() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference favoriteTracksRef = database.getReference("favorite_tracks/" + userId);

        favoriteTracksRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                List<Track> favoriteTracksList = new ArrayList<>();
                for (DataSnapshot trackSnapshot : dataSnapshot.getChildren()) {
                    Track track = trackSnapshot.getValue(Track.class);
                    favoriteTracksList.add(track);
                }
                updateUI(favoriteTracksList);
                Log.d("FavoriteTracks", "Loaded " + favoriteTracksList.size() + " favorite tracks");
            } else {
                Log.d("FavoriteTracks", "No favorite tracks found");
                updateUI(new ArrayList<>()); // Пустой список
            }
        }).addOnFailureListener(e -> {
            Log.e("FavoriteTracks", "Failed to load favorite tracks", e);
        });
    }
    private void updateUI(List<Track> favoriteTracksList) {
        if (favoriteTracks == null) {
            favoriteTracks = new ArrayList<>();
        }
        favoriteTracks.clear(); // Очищаем текущий список
        favoriteTracks.addAll(favoriteTracksList); // Добавляем новые данные
        trackAdapter.notifyDataSetChanged(); // Сообщаем адаптеру об изменениях
    }
}