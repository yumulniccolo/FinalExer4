package com.example.finalexer4;

import android.media.MediaPlayer;
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

    private MediaPlayer selectSound;

    public FragmentMenu() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnEasy = view.findViewById(R.id.btn_easy);
        Button btnMed = view.findViewById(R.id.btn_med);
        Button btnHard = view.findViewById(R.id.btn_hard);

        btnEasy.setOnClickListener(v -> startGame("Easy", v, btnEasy, btnMed, btnHard));
        btnMed.setOnClickListener(v -> startGame("Medium", v, btnEasy, btnMed, btnHard));
        btnHard.setOnClickListener(v -> startGame("Hard", v, btnEasy, btnMed, btnHard));
    }

    private void startGame(String difficulty, View view, Button... buttons) {
        // Disable all buttons para hindi madalawang beses ma-click
        for (Button b : buttons) b.setEnabled(false);

        selectSound = MediaPlayer.create(requireContext(), R.raw.select);
        selectSound.start();
        selectSound.setOnCompletionListener(mp -> {
            mp.release();
            selectSound = null;
            // navigate AFTER sound finishes
            if (isAdded()) {
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
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (selectSound != null) {
            selectSound.release();
            selectSound = null;
        }
    }
}