package com.example.finalexer4;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {

    private MediaPlayer startSound;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnStart = view.findViewById(R.id.playbtn);

        btnStart.setOnClickListener(v -> {
            btnStart.setEnabled(false); // para hindi madalawang beses ma-click
            startSound = MediaPlayer.create(requireContext(), R.raw.start);
            startSound.start();
            startSound.setOnCompletionListener(mp -> {
                mp.release();
                startSound = null;
                // navigate AFTER sound finishes
                if (isAdded() && getView() != null) {
                    Navigation.findNavController(getView())
                            .navigate(R.id.action_homeFragment_to_fragmentMenu);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (startSound != null) {
            startSound.release();
            startSound = null;
        }
    }
}