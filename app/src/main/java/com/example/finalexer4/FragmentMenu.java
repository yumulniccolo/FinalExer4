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

public class FragmentMenu extends Fragment {

    private SoundPool soundPool;
    private int selectSoundId;
    private boolean soundLoaded = false;

    public FragmentMenu() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button backBtn = view.findViewById(R.id.backbtn);

        backBtn.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();

        });

        // Ensure home music is playing (handled by MainActivity)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).startHomeMusic();
        }

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
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

        // Instant touch response for menu buttons
        View.OnTouchListener menuTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                String difficulty = "Medium";
                if (v.getId() == R.id.btn_easy) difficulty = "Easy";
                else if (v.getId() == R.id.btn_hard) difficulty = "Hard";

                startGame(difficulty, v);
                return true;
            }
            return false;
        };

        btnEasy.setOnTouchListener(menuTouchListener);
        btnMed.setOnTouchListener(menuTouchListener);
        btnHard.setOnTouchListener(menuTouchListener);
    }

    private void startGame(String difficulty, View view) {
        if (soundLoaded) soundPool.play(selectSoundId, 1f, 1f, 1, 0, 1f);

        // Stop home music from MainActivity before entering the game
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).stopHomeMusic();
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
    }
}