package com.example.finalexer4;

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

    public FragmentMenu() {
        // Required empty public constructor
    }

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

        btnEasy.setOnClickListener(v -> startGame("Easy", v));
        btnMed.setOnClickListener(v -> startGame("Medium", v));
        btnHard.setOnClickListener(v -> startGame("Hard", v));
    }

    private void startGame(String difficulty, View view) {
        Bundle bundle = new Bundle();
        bundle.putString("difficulty", difficulty);
        Navigation.findNavController(view).navigate(R.id.action_fragmentMenu_to_fragmentGame, bundle);
    }
}
