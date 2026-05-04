package com.example.finalexer4;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SoundPool setup with low latency flags
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
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

        // Use OnTouchListener for an "instant" click feel
        btnStart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Play sound immediately
                if (soundLoaded) soundPool.play(startSoundId, 1f, 1f, 1, 0, 1f);

                // Navigate immediately
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_fragmentMenu);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}