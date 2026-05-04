package com.example.finalexer4;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class FragmentMenu extends Fragment {

    private SoundPool soundPool;
    private int selectSoundId;
    private boolean soundLoaded = false;

    private MediaPlayer homeMusic;

    public FragmentMenu() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Home background music
        homeMusic = MediaPlayer.create(requireContext(), R.raw.home);
        homeMusic.setLooping(true);
        homeMusic.start();

        // SoundPool para sa select button sound
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();

        selectSoundId = soundPool.load(requireContext(), R.raw.select, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) soundLoaded = true;
        });

        Button btnEasy = view.findViewById(R.id.btn_easy);
        Button btnMed = view.findViewById(R.id.btn_med);
        Button btnHard = view.findViewById(R.id.btn_hard);

        btnEasy.setOnClickListener(v -> startGame("Easy", v));
        btnMed.setOnClickListener(v -> startGame("Medium", v));
        btnHard.setOnClickListener(v -> startGame("Hard", v));
    }

    private void startGame(String difficulty, View view) {
        if (soundLoaded) soundPool.play(selectSoundId, 1f, 1f, 0, 0, 1f);

        // Stop home music bago mag-navigate
        if (homeMusic != null) {
            homeMusic.stop();
            homeMusic.release();
            homeMusic = null;
        }

        Bundle bundle = new Bundle();
        bundle.putString("difficulty", difficulty);
        Navigation.findNavController(view).navigate(
                R.id.fragmentGame,
                bundle,
                new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.fragmentMenu, false)
                        .build()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (homeMusic != null) {
            homeMusic.stop();
            homeMusic.release();
            homeMusic = null;
        }
    }
}