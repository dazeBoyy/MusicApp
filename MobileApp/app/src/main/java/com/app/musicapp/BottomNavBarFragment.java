package com.app.musicapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.musicapp.service.Track;
import com.app.musicapp.track_player.PlayerViewModel;

import java.util.List;


public class BottomNavBarFragment extends Fragment {
    private PlayerViewModel viewModel;
    public BottomNavBarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_nav_bar, container, false);

        view.findViewById(R.id.favorite_image).setOnClickListener(v -> goToFavoriteScreen());
        view.findViewById(R.id.home_image).setOnClickListener(v -> goToHomeScreen());

        return view;
    }


    public void goToFavoriteScreen() {
        // Get the fragment manager and the favorite fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        // Check if the fragment is already added to the fragment manager
        FavoriteTracks favoriteTracksFragment = (FavoriteTracks) fragmentManager.findFragmentByTag(FavoriteTracks.class.getSimpleName());

        if (favoriteTracksFragment == null) {
            // If the fragment isn't already in the fragment manager, add it
            favoriteTracksFragment = new FavoriteTracks();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, favoriteTracksFragment, FavoriteTracks.class.getSimpleName()) // Replace if already added
                    .commit();
        }

        // Make the fragment container visible
        View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Show the fragment with favorite tracks
        fragmentManager.beginTransaction()
                .show(favoriteTracksFragment) // Show the fragment
                .commit();

        // Handle UI visibility for main content
        View mainContentView = getActivity().findViewById(R.id.container);
        View exit = getActivity().findViewById(R.id.sign_out_button);
        View bottom_bar = getActivity().findViewById(R.id.fragment_bottom_nav_bar);
        View logo = getActivity().findViewById(R.id.logoImage);

        // Hide all child views in the main content
        for (int i = 0; i < ((ViewGroup) mainContentView).getChildCount(); i++) {
            View child = ((ViewGroup) mainContentView).getChildAt(i);
            child.setVisibility(View.GONE);  // Hide all child views
        }

        // Show necessary UI elements
        logo.setVisibility(View.VISIBLE);
        exit.setVisibility(View.VISIBLE); // Show exit button
        bottom_bar.setVisibility(View.VISIBLE); // Show bottom bar
    }

        // Переход на главный экран
        public void goToHomeScreen() {
            // Vосстанавливаем видимость элементов
            View mainContentView = getActivity().findViewById(R.id.container);
            View progress = getActivity().findViewById(R.id.progress_bar);

            // Show all child views in the main content
            for (int i = 0; i < ((ViewGroup) mainContentView).getChildCount(); i++) {
                View child = ((ViewGroup) mainContentView).getChildAt(i);
                child.setVisibility(View.VISIBLE);  // Show all child views
            }

            // Hide the progress bar
            progress.setVisibility(View.GONE);

            // Remove the fragment from the fragment manager
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FavoriteTracks favoriteTracksFragment = (FavoriteTracks) fragmentManager.findFragmentByTag(FavoriteTracks.class.getSimpleName());
            if (favoriteTracksFragment != null) {
                fragmentManager.beginTransaction()
                        .remove(favoriteTracksFragment)  // Remove the fragment
                        .commit();
            }

            // Optionally, you can also pop the fragment from the back stack to remove it entirely.
            fragmentManager.popBackStack();
        }
}