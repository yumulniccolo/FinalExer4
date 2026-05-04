package com.example.finalexer4;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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

    @SuppressLint("ClickableViewAccessibility")
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

        // Setup SoundPool with high priority attributes
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
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
                if (hideY == 0) hideY = 500; 
                moles[index].setTranslationY(hideY);
                isMoleUp[index] = false;
            });

            // Use OnTouchListener for INSTANT response on touch down (faster than OnClick)
            moles[i].setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isMoleUp[index]) {
                        // 1. DISABLE MOLE IMMEDIATELY so it can't be clicked twice
                        isMoleUp[index] = false;

                        // 2. Play sound IMMEDIATELY
                        if (hitSoundLoaded) {
                            soundPool.play(hitSoundId, 1f, 1f, 1, 0, 1f);
                        }

                        // 3. Game logic
                        if (isImposter[index]) {
                            score = Math.max(0, score - 5);
                            imposterHits++;
                            showImposterHitMessage();
                            updateImposterStats();
                        } else {
                            score++;
                        }
                        tvScore.setText("Score: " + score);

                        // 4. "Pop" Feedback: Shrink slightly and drop down faster
                        moles[index].animate().cancel(); // Stop current "up" animation
                        moles[index].animate()
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .translationY(moles[index].getHeight())
                                .setDuration(150) // Faster drop for hits
                                .withEndAction(() -> {
                                    moles[index].setScaleX(1f);
                                    moles[index].setScaleY(1f);
                                });
                        return true;
                    }
                }
                return false;
            });
        }

        // Initialize music in background thread to avoid UI hang
        new Thread(() -> {
            try {
                gameMusic = MediaPlayer.create(requireContext(), R.raw.game);
                if (gameMusic != null) {
                    gameMusic.setLooping(true);
                    gameMusic.setVolume(0.5f, 0.5f); // Lower volume to help SFX stand out
                    gameMusic.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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

        countDownTimer = new CountDownTimer(30000, 1000) {
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
        if (!isGameActive) return;
        int index = random.nextInt(9);
        if (!isMoleUp[index]) {
            boolean spawnImposter = false;
            if ("Medium".equalsIgnoreCase(difficulty)) {
                spawnImposter = random.nextInt(10) == 0;
            } else if ("Hard".equalsIgnoreCase(difficulty)) {
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
                .setDuration(250) // Faster pop up
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
        if (!isMoleUp[index]) return; // Already going down from a hit

        float hideY = moles[index].getHeight();
        if (hideY == 0) hideY = 500;
        
        moles[index].animate()
                .translationY(hideY)
                .setDuration(250)
                .withEndAction(() -> isMoleUp[index] = false);
    }

    private void endGame() {
        if (!isGameActive) return;
        isGameActive = false;

        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) countDownTimer.cancel();

        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }

        if (timesUpSoundLoaded) {
            soundPool.play(timesUpSoundId, 1f, 1f, 2, 0, 1f);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("score", score);
        bundle.putString("difficulty", difficulty);
        bundle.putInt("imposterHits", imposterHits);

        handler.postDelayed(() -> {
            if (isAdded() && getView() != null) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_fragmentGame_to_goMessageFragment, bundle);
            }
        }, 1200);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isGameActive = false;
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) countDownTimer.cancel();
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}