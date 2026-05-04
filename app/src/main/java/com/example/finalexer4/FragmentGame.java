package com.example.finalexer4;

import android.media.MediaPlayer;
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
    private int imposterHits = 0;
    private long gameSpeed = 1500;
    private long moleStayTime = 1000;

    private TextView tvScore, tvTime, tvImposterHits;
    private ImageView[] moles = new ImageView[9];
    private ImageView ivImposterHitMsg;
    private View imposterStatsLayout;
    private boolean[] isMoleUp = new boolean[9];
    private boolean[] isImposter = new boolean[9];

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable gameRunnable;
    private CountDownTimer countDownTimer;
    private Random random = new Random();
    private boolean isGameActive = true;

    private MediaPlayer gameMusic;

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
        tvImposterHits = view.findViewById(R.id.tvImposterHits);
        imposterStatsLayout = view.findViewById(R.id.imposter_stats_layout);
        ivImposterHitMsg = view.findViewById(R.id.ivImposterHitMsg);

        if ("Easy".equalsIgnoreCase(difficulty)) {
            if (imposterStatsLayout != null) imposterStatsLayout.setVisibility(View.GONE);
        }

        moles[0] = view.findViewById(R.id.mole);
        moles[1] = view.findViewById(R.id.mole1);
        moles[2] = view.findViewById(R.id.mole2);
        moles[3] = view.findViewById(R.id.mole3);
        moles[4] = view.findViewById(R.id.mole4);
        moles[5] = view.findViewById(R.id.mole5);
        moles[6] = view.findViewById(R.id.mole6);
        moles[7] = view.findViewById(R.id.mole7);
        moles[8] = view.findViewById(R.id.mole8);

        for (int i = 0; i < 9; i++) {
            final int index = i;

            FrameLayout hole = (FrameLayout) moles[i].getParent();

            hole.post(() -> {
                float hideY = moles[index].getHeight();
                moles[index].setTranslationY(hideY);
                isMoleUp[index] = false;
            });

            View.OnClickListener clickListener = v -> {
                if (isMoleUp[index]) {
                    if (isImposter[index]) {
                        score = Math.max(0, score - 5);
                        imposterHits++;
                        showImposterHitMessage();
                        updateImposterStats();
                    } else {
                        score++;
                    }
                    tvScore.setText("Score: " + score);
                    hideMole(index);
                }
            };

            moles[i].setOnClickListener(clickListener);
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
            boolean spawnImposter = false;
            if ("Medium".equalsIgnoreCase(difficulty)) {
                // 10% chance to be an imposter in Medium
                spawnImposter = random.nextInt(10) == 0;
            } else if ("Hard".equalsIgnoreCase(difficulty)) {
                // 20% chance to be an imposter in Hard
                spawnImposter = random.nextInt(5) == 0;
            }
            showMole(index, spawnImposter);
        }
    }

    private void showMole(int index, boolean spawnImposter) {
        if (!isGameActive) return;

        isMoleUp[index] = true;
        isImposter[index] = spawnImposter;

        if (spawnImposter) {
            moles[index].setImageResource(R.drawable.imposter);
        } else {
            moles[index].setImageResource(R.drawable.mole);
        }

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

    private void showImposterHitMessage() {
        if (ivImposterHitMsg != null) {
            ivImposterHitMsg.setVisibility(View.VISIBLE);
            handler.postDelayed(() -> ivImposterHitMsg.setVisibility(View.GONE), 1000);
        }
    }

    private void updateImposterStats() {
        if (tvImposterHits != null) {
            tvImposterHits.setText("Imposter: " + imposterHits);
        }
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

        Bundle bundle = new Bundle();
        bundle.putInt("score", score);
        bundle.putString("difficulty", difficulty);
        bundle.putInt("imposterHits", imposterHits);

        if (isAdded() && getView() != null) {
            Navigation.findNavController(getView())
                    .navigate(R.id.action_fragmentGame_to_goMessageFragment, bundle);
        }
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
    }
}