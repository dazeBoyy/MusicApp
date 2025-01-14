package com.app.musicapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import com.app.musicapp.service.Track;
import com.app.musicapp.service.TrackApiService;
import com.app.musicapp.track_adapter.TrackAdapter;
import com.app.musicapp.track_player.MusicPlayer;
import com.app.musicapp.track_player.PlayerViewModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.musicapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    private FirebaseAuth mAuth;

    private EditText artistInput, trackInput;
    private Button searchButton;
    private Retrofit retrofit;



    private PlayerViewModel viewModel;
    private TrackApiService trackApiService;
    private Switch similarTracksSwitch;
    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;

    private PlayerViewModel playerViewModel;

    private ProgressBar progressBar;
    private List<Track> trackList = new ArrayList<>();

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isBigPlayerVisible = false; // Флаг для проверки, какой плеер отображается

    private Player playerFragment;

    private MiniPlayerFragment miniPlayerFragment;

    private View bottomNav;



    private ImageView signOutImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);



        // Инициализация полей

        artistInput = findViewById(R.id.et_artist);
        trackInput = findViewById(R.id.et_track);
        searchButton = findViewById(R.id.btn_search);
        signOutImage = findViewById(R.id.sign_out_button);
        similarTracksSwitch = findViewById(R.id.similar_tracks_switch);
        recyclerView = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progress_bar);
        bottomNav = findViewById(R.id.fragment_bottom_nav_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        trackAdapter = new TrackAdapter(this, trackList);
        recyclerView.setAdapter(trackAdapter);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Тайм-аут подключения
                .writeTimeout(60, TimeUnit.SECONDS)   // Тайм-аут записи
                .readTimeout(60, TimeUnit.SECONDS)    // Тайм-аут чтения
                .build();

        // Настройка Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // Ваш адрес сервера
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        trackApiService = retrofit.create(TrackApiService.class);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Найти Bottom Sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(320);
        // Добавить слушатель для обработки изменений состояния
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("BottomSheet", "State changed: " + newState);
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    isBigPlayerVisible = false;
                    bottomSheet.setVisibility(View.VISIBLE);
                    showMiniPlayer();
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    isBigPlayerVisible = true;
                    bottomSheet.setVisibility(View.VISIBLE);
                    showBigPlayer();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                handleBottomSheetSlide(bottomSheet, slideOffset);
            }
        });


        // Настройка ViewModel
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        viewModel.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null && isPlaying) {
                bottomSheet.setVisibility(View.VISIBLE);
                // Если музыка играет, инициализируем мини-плеер
                showMiniPlayer();


            }
        });


        // Инициализация Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        signOutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut(); // Вызов метода выхода
            }
        });

        searchButton.setOnClickListener(v -> {
            String artist = artistInput.getText().toString();
            String track = trackInput.getText().toString();

            if (!artist.isEmpty() && !track.isEmpty()) {
                if (similarTracksSwitch.isChecked()) {
                    // Выполняем поиск похожих треков
                    searchSimilarTracks(artist, track);
                    Log.e("MainActivity", "Выполняю поиск похожих треков");
                } else {
                    // Выполняем обычный поиск трека
                    searchTrack(artist, track);
                    Log.e("MainActivity", "Выполняю поиск трека");
                }
            } else {
                Log.e("MainActivity", "Поля не заполнены");
            }
        });

    }

    private void showMiniPlayer() {
        if (!isBigPlayerVisible) {
            miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentByTag("MiniPlayerFragment");
            if (miniPlayerFragment == null) {
                miniPlayerFragment = new MiniPlayerFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottom_sheet, miniPlayerFragment, "MiniPlayerFragment")
                        .commit();
                miniPlayerFragment.setMiniPlayerClickListener(() -> {
                    // Expand Bottom Sheet when mini player is clicked
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                });
            }
        }
    }


    // Show Big Player
    private void showBigPlayer() {
        if (isBigPlayerVisible) {
            playerFragment = (Player) getSupportFragmentManager().findFragmentByTag("PlayerFragment");
            if (playerFragment == null) {
                playerFragment = new Player();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottom_sheet, playerFragment, "PlayerFragment")
                        .commit();
                playerFragment.setBigPlayerClickListener(() -> {
                    // Collapse Bottom Sheet when big player is clicked
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                });
            }
        }
    }

    // Handle BottomSheet slide to adjust fragment visibility or opacity
    private void handleBottomSheetSlide(View bottomSheet, float slideOffset) {
        if (playerFragment != null && playerFragment.getView() != null) {
            playerFragment.getView().setAlpha(slideOffset);
        }

        if (miniPlayerFragment != null && miniPlayerFragment.getView() != null) {
            miniPlayerFragment.getView().setAlpha(1f - slideOffset);
        }

        // Manage fragment visibility based on slide offset
        if (slideOffset == 0) {
            if (miniPlayerFragment != null && miniPlayerFragment.getView() != null) {
                miniPlayerFragment.getView().setVisibility(View.VISIBLE);
            }
            if (playerFragment != null && playerFragment.getView() != null) {
                playerFragment.getView().setVisibility(View.INVISIBLE);
            }
        } else if (slideOffset == 1) {
            if (miniPlayerFragment != null && miniPlayerFragment.getView() != null) {
                miniPlayerFragment.getView().setVisibility(View.INVISIBLE);
            }
            if (playerFragment != null && playerFragment.getView() != null) {
                playerFragment.getView().setVisibility(View.VISIBLE);
            }
        }
    }

    private void searchTrack(String artist, String track) {
        recyclerView.setVisibility(View.GONE);
        Call<Track> call = trackApiService.searchTrack(artist, track);
        call.enqueue(new Callback<Track>() {
            @Override
            public void onResponse(Call<Track> call, Response<Track> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    trackList.clear();
                    trackList.add(response.body());
                    trackAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Track> call, Throwable t) {
                Log.e("API Error", t.getMessage());
            }
        });
    }

    private void searchSimilarTracks(String artist, String track) {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        // Make a POST request to fetch similar tracks
        Call<List<Track>> call = trackApiService.getSimilarTracks(artist, track);
        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE); // Скрыть прогресс
                if (response.isSuccessful() && response.body() != null) {
                    trackList.clear();
                    trackList.addAll(response.body());
                    trackAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API Error", t.getMessage());
            }
        });
    }


    private void signOut() {
        // Выход из Firebase и Google
        GoogleSignInClient mGoogleSignInClient = GoogleSignInClientManager.getClient(this);
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // После выхода возвращаемся на экран входа

            if (MusicPlayer.getInstance().isPlaying()){
                MusicPlayer.getInstance().stop();
                MusicPlayer.getInstance().release();
            }else {
                Log.d("Main", "Нет песен для загрузки");
            }
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicPlayer.getInstance().release();
    }

}