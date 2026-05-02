package com.example.finalexer4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class GameOverFragment extends Fragment {

    private int score = 0;

    public GameOverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            score = getArguments().getInt("score", 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_over, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvScore = view.findViewById(R.id.textView4);
        tvScore.setText("Score: " + score);

        Button btnReplay = view.findViewById(R.id.btn_replay);
        Button btnMenu = view.findViewById(R.id.btn_menu);

        btnReplay.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_gameOverFragment_to_fragmentGame));
        btnMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_gameOverFragment_to_fragmentMenu));
    }
}
