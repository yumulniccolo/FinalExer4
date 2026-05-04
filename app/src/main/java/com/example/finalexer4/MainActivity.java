package com.example.finalexer4;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer homeMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Start Home Music asynchronously to keep the main thread responsive
        startHomeMusic();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void startHomeMusic() {
        if (homeMusic == null) {
            // Run on a separate thread to avoid UI stutter
            new Thread(() -> {
                try {
                    homeMusic = MediaPlayer.create(MainActivity.this, R.raw.home);
                    if (homeMusic != null) {
                        homeMusic.setLooping(true);
                        homeMusic.setVolume(0.6f, 0.6f);
                        homeMusic.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (!homeMusic.isPlaying()) {
            homeMusic.start();
        }
    }

    public void stopHomeMusic() {
        if (homeMusic != null) {
            try {
                if (homeMusic.isPlaying()) {
                    homeMusic.stop();
                }
                homeMusic.release();
                homeMusic = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHomeMusic();
    }
}