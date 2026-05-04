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

public class HomeFragment extends Fragment {

    private SoundPool soundPool;
    private int startSoundId;
    private boolean soundLoaded = false;

    private MediaPlayer homeMusic;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Home background music
        homeMusic = MediaPlayer.create(requireContext(), R.raw.home);
        homeMusic.setLooping(true);
        homeMusic.start();

        // SoundPool para sa start button sound
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();

        startSoundId = soundPool.load(requireContext(), R.raw.start, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) soundLoaded = true;
        });

        Button btnStart = view.findViewById(R.id.playbtn);

        btnStart.setOnClickListener(v -> {
            if (soundLoaded) soundPool.play(startSoundId, 1f, 1f, 0, 0, 1f);

            // Stop home music bago mag-navigate
            if (homeMusic != null) {
                homeMusic.stop();
                homeMusic.release();
                homeMusic = null;
            }

            Navigation.findNavController(v)
                    .navigate(R.id.action_homeFragment_to_fragmentMenu);
        });
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