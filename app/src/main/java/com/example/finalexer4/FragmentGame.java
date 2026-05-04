package com.example.finalexer4;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import java.util.Random;

public class FragmentGame extends Fragment {

    private String difficulty;
    private int score = 0;
    private long gameSpeed = 1500;
    private long moleStayTime = 1000;

    private TextView tvScore, tvTime;
    private ImageView[] moles = new ImageView[9];
    private boolean[] isMoleUp = new boolean[9];

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable gameRunnable;
    private CountDownTimer countDownTimer;
    private Random random = new Random();
    private boolean isGameActive = true;

    private MediaPlayer gameMusic;

    private SoundPool soundPool;
    private int hitSoundId;
    private boolean hitSoundLoaded = false;

    private int timesUpSoundId;
    private boolean timesUpSoundLoaded = false;

    public FragmentGame() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            difficulty = getArguments().getString("difficulty", "Medium");
        } else {
            difficulty = "Medium";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout layout = view.findViewById(R.id.gameLayout);

        if ("Easy".equalsIgnoreCase(difficulty)) {
            layout.setBackgroundResource(R.drawable.easybg);
        } else if ("Hard".equalsIgnoreCase(difficulty)) {
            layout.setBackgroundResource(R.drawable.hardbg);
        } else {
            layout.setBackgroundResource(R.drawable.medbg);
        }

        tvScore = view.findViewById(R.id.tvScore);
        tvTime = view.findViewById(R.id.tvTime);

        moles[0] = view.findViewById(R.id.mole);
        moles[1] = view.findViewById(R.id.mole1);
        moles[2] = view.findViewById(R.id.mole2);
        moles[3] = view.findViewById(R.id.mole3);
        moles[4] = view.findViewById(R.id.mole4);
        moles[5] = view.findViewById(R.id.mole5);
        moles[6] = view.findViewById(R.id.mole6);
        moles[7] = view.findViewById(R.id.mole7);
        moles[8] = view.findViewById(R.id.mole8);

        // Setup SoundPool
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attributes)
                .build();

        hitSoundId = soundPool.load(requireContext(), R.raw.hit, 1);
        timesUpSoundId = soundPool.load(requireContext(), R.raw.timesup, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                if (sampleId == hitSoundId) hitSoundLoaded = true;
                if (sampleId == timesUpSoundId) timesUpSoundLoaded = true;
            }
        });

        for (int i = 0; i < 9; i++) {
            final int index = i;

            FrameLayout hole = (FrameLayout) moles[i].getParent();

            hole.post(() -> {
                float hideY = moles[index].getHeight();
                moles[index].setTranslationY(hideY);
                isMoleUp[index] = false;
            });

            moles[i].setOnClickListener(v -> {
                if (isMoleUp[index]) {
                    score++;
                    tvScore.setText("Score: " + score);
                    if (hitSoundLoaded) soundPool.play(hitSoundId, 1f, 1f, 0, 0, 1f);
                    hideMole(index);
                }
            });
        }

        // Start game music
        gameMusic = MediaPlayer.create(requireContext(), R.raw.game);
        gameMusic.setLooping(true);
        gameMusic.start();

        setupDifficulty();
        startGame();
    }

    private void setupDifficulty() {
        if ("Easy".equalsIgnoreCase(difficulty)) {
            gameSpeed = 2000;
            moleStayTime = 1500;
        } else if ("Hard".equalsIgnoreCase(difficulty)) {
            gameSpeed = 800;
            moleStayTime = 600;
        } else {
            gameSpeed = 1300;
            moleStayTime = 900;
        }
    }

    private void startGame() {
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                showRandomMole();
                handler.postDelayed(this, gameSpeed);
            }
        };
        handler.post(gameRunnable);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTime.setText("Time: " + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvTime.setText("Time: 0");
                endGame();
            }
        }.start();
    }

    private void showRandomMole() {
        int index = random.nextInt(9);
        if (!isMoleUp[index]) {
            showMole(index);
        }
    }

    private void showMole(int index) {
        if (!isGameActive) return;

        isMoleUp[index] = true;
        moles[index].animate()
                .translationY(0)
                .setDuration(300)
                .withEndAction(() -> {
                    if (!isGameActive) return;

                    handler.postDelayed(() -> {
                        if (isGameActive && isMoleUp[index]) {
                            hideMole(index);
                        }
                    }, moleStayTime);
                });
    }

    private void hideMole(int index) {
        float hideY = 200 * getResources().getDisplayMetrics().density;
        moles[index].animate()
                .translationY(hideY)
                .setDuration(300)
                .withEndAction(() -> isMoleUp[index] = false);
    }

    private void endGame() {
        if (!isGameActive) return;

        isGameActive = false;

        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) countDownTimer.cancel();

        // Stop game music
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }

        // Play times up sound
        if (timesUpSoundLoaded) {
            soundPool.play(timesUpSoundId, 1f, 1f, 0, 0, 1f);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("score", score);
        bundle.putString("difficulty", difficulty);

        // Delay navigation para marinig muna yung sound
        handler.postDelayed(() -> {
            if (isAdded() && getView() != null) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_fragmentGame_to_goMessageFragment, bundle);
            }
        }, 1500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        isGameActive = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Stop game music
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }

        // Release SoundPool
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}