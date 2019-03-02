package org.qxtx.idea.player.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerView;

import org.qxtx.idea.player.IdeaEventListener;
import org.qxtx.idea.player.IdeaPlayer;
import org.qxtx.idea.player.utils.IdeaLog;
import org.qxtx.idea.player.utils.IdeaToast;
import org.qxtx.idea.player.utils.IdeaUtil;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {
    private IdeaToast ideaToast;
    private PlayerView playerView;
    private LinearLayout llLoading;
    private ImageView ivWait;
    private TextView tvLoading;

    private IdeaPlayer IdeaPlayer;
    private ItemBean playlistData;

    private AnimationUtils aLostFocus;
    private AnimationUtils aGetFocus;
    private AnimationUtils aLoading;
    private AnimationUtils aBtnClick;

    private boolean shouldBeInit = true;

    private ScheduledExecutorService tShowKeyStatus;
    private TextView tvTitleBarRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        try {
            llLoading = (LinearLayout)findViewById(R.id.playLoading);
            ivWait = (ImageView)findViewById(R.id.loadingImg);
            tvLoading = (TextView)findViewById(R.id.loadingText);
            ideaToast = IdeaToast.getInstance(this);
            init();
        } catch (Exception e) {
            ideaToast.showToast("something happened, please restart.", Toast.LENGTH_LONG);
        }
    }

    private void init() {
        initData();
    }

    private void initData() {
        getIntentData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        playlistData = (ItemBean)intent.getSerializableExtra(ChooseActivity.EXTRAS_NAME);
        if (playlistData == null) {
            ideaToast.showToast("Empty playlist", Toast.LENGTH_LONG);
        }
    }

    private void initPlayer() {
        playerView = (PlayerView)findViewById(R.id.player);

        if (IdeaPlayer != null) {
            IdeaPlayer.release();
        }

        IdeaPlayer = new IdeaPlayer.Builder(this)
                .setPath(playlistData.getUrl())
//                .setMediaExtension(IdeaUtil.EXTENSION_MPD)
                .setLicenseUrl(playlistData.getServerLicenseUrl())
                .addEventListener(new PlayerStateListener())
                .setDebugMode(true)
//                .setAds("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=")
                .bindView(playerView)
                .build();
        IdeaPlayer.start();

        Button videoSelector = (Button)findViewById(R.id.videoBtn);
        videoSelector.setOnClickListener(v -> {
            IdeaPlayer.showTrackManager(IdeaUtil.TRACK_VIDEO);
        });

        Button audioSelector = (Button)findViewById(R.id.audioBtn);
        audioSelector.setOnClickListener(v -> {
            IdeaPlayer.showTrackManager(IdeaUtil.TRACK_AUDIO);
        });

        initController();
    }

    private void initController() {
        com.google.android.exoplayer2.ui.PlayerControlView controlView = playerView.getController();
        View exo_next = (View) controlView.findViewById(R.id.exo_next);
        View exo_prev = (View) controlView.findViewById(R.id.exo_prev);
        TextView exo_titleBar = (TextView) controlView.findViewById(R.id.exo_title);
        TextView exo_titleRight = (TextView) controlView.findViewById(R.id.exo_titleRight);
        View exo_play = (View) controlView.findViewById(R.id.exo_play);
        View exo_pause = (View) controlView.findViewById(R.id.exo_pause);
        tvTitleBarRight = (TextView)findViewById(R.id.exo_titleRight);

        exo_prev.setOnClickListener(new BtnClickListener());
        exo_next.setOnClickListener(new BtnClickListener());
        exo_next.setOnFocusChangeListener(new ControllerFocusListener());
        exo_prev.setOnFocusChangeListener(new ControllerFocusListener());
        exo_play.setOnFocusChangeListener(new ControllerFocusListener());
        exo_play.setOnFocusChangeListener(new ControllerFocusListener());
        exo_pause.setOnFocusChangeListener(new ControllerFocusListener());

        exo_titleBar.setText(playlistData.getName());

        exo_next.setClickable(false);
        exo_next.setFocusable(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            if (!playerView.isControllerVisible()) {
                playerView.showController();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && shouldBeInit) {
            stopAnimation(aLoading);
            aLoading = new AnimationUtils(ivWait);
            aLoading.startRotation(Animation.INFINITE, 1000, 0f, 360f);

            initPlayer();
            shouldBeInit = false;
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        stopAnimation(aLoading);
        stopAnimation(aGetFocus);
        stopAnimation(aLostFocus);
        stopAnimation(aBtnClick);

        if (tShowKeyStatus != null) {
            tShowKeyStatus.shutdownNow();
        }

        if (playlistData != null) {
            playlistData = null;
        }

        if (ideaToast != null) {
            ideaToast.cancel();
            ideaToast = null;
        }

        if (IdeaPlayer != null) {
            IdeaPlayer.release();
            IdeaPlayer = null;
        }

        super.onDestroy();

        System.gc();
        System.runFinalization();
        System.gc();
    }

    private void stopAnimation(AnimationUtils animationUtils) {
        if (animationUtils != null) {
            animationUtils.endAnimation();
            animationUtils = null;
        }
    }

    private class ControllerFocusListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int[] pos = new int[2];
            v.getLocationInWindow(pos);

            if (hasFocus) {
                stopAnimation(aGetFocus);
                aGetFocus = new AnimationUtils(v);
                aGetFocus.startScale(500, 1f, 1.5f);
            } else {
                stopAnimation(aLostFocus);
                aLostFocus = new AnimationUtils(v);
                aLostFocus.startScale(500, 1.5f, 1f);
            }
        }
    }

    private class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            stopAnimation(aBtnClick);

            aBtnClick = new AnimationUtils(v);
            aBtnClick.startScale(500, 1f, 1.5f);

            if (v.getId() == R.id.exo_next) {
                IdeaPlayer.next();
            } else if (v.getId() == R.id.exo_prev) {
                IdeaPlayer.previous();
            }
        }
    }

    private class PlayerStateListener extends IdeaEventListener {
        private Map<String, String> keyStatus;

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == IdeaUtil.STATE_BUFFERING) {
                if (llLoading.getVisibility() == View.GONE ) {
                    llLoading.setVisibility(View.VISIBLE);

                    stopAnimation(aLoading);

                    aLoading = new AnimationUtils(ivWait);
                    aLoading.startRotation(Animation.INFINITE, 1000, 0f, 360f);
                }
            }

            if (playbackState == IdeaUtil.STATE_READY) {
                if (llLoading != null) {
                    llLoading.setVisibility(View.GONE);
                }
                showKeyStatus();
                stopAnimation(aLoading);
            }

            if (playbackState == IdeaUtil.STATE_ENDED || playbackState == IdeaUtil.STATE_IDLE) {
                stopAnimation(aLoading);

                if (llLoading != null) {
                    llLoading.setVisibility(View.VISIBLE);
                }
                if (tShowKeyStatus != null) {
                    tShowKeyStatus.shutdown();
                }
            }

            if (playbackState == IdeaUtil.STATE_ENDED) {
                stopAnimation(aLoading);

                if (llLoading != null) {
                    llLoading.setVisibility(View.GONE);
                }
                if (tShowKeyStatus != null) {
                    tShowKeyStatus.shutdown();
                }
            }
        }

        @Override
        public void onError(String info, int code) {
            ideaToast.showToast("Fail to play: " + info, Toast.LENGTH_LONG);
            if (llLoading.getVisibility() == View.GONE) {
                llLoading.setVisibility(View.VISIBLE);
            }

            if (tShowKeyStatus != null) {
                tShowKeyStatus.shutdown();
            }

            stopAnimation(aLoading);

            if (ivWait != null) {
                ivWait.setImageResource(R.mipmap.play_fail);
            }
            if (tvLoading != null) {
                tvLoading.setText("Fail to load. Retry it?");
            }
        }

        private void showKeyStatus() {
            if (tShowKeyStatus == null) {
                tShowKeyStatus = Executors.newSingleThreadScheduledExecutor();
                tShowKeyStatus.scheduleWithFixedDelay(() -> {
                            StringBuilder msg = new StringBuilder();
                            try {
                                keyStatus = IdeaPlayer.queryKeyStatus();
                                if (keyStatus == null) {
//                                    IdeaLog.e("Query error: value of null");
                                    return ;
                                }

                                IdeaLog.e(timeFormat(keyStatus.get(IdeaUtil.QKS_PLAYBACK_DURATION)));
                                msg.append("total:")
                                        .append(timeFormat(keyStatus.get(IdeaUtil.QKS_TOTAL_DURATION)))
                                        .append(",cur:")
                                        .append(timeFormat(keyStatus.get(IdeaUtil.QKS_PLAYBACK_DURATION)));

                                tvTitleBarRight.post(() -> tvTitleBarRight.setText(msg.toString()));
                            } catch (Exception e) {
//                                MyLog.e("queryKeyStatus ERROR!");
                            }
                        }, 0, 1, TimeUnit.SECONDS
                );
            }
        }

        private String timeFormat(String timeSec) {
            int totalTime = Integer.parseInt(timeSec);
            int DAY = 3600 * 24;
            int days = totalTime / DAY;
            int hours = totalTime % DAY / 3600;
            int minute = totalTime % DAY % 3600 / 60;
            int second = totalTime % DAY % 3600 % 60;

            return (days == 0 ? "" : days + "d")
                    + ((days == 0 && hours == 0) ? "" : hours + "h")
                    + ((days == 0 && hours == 0 && minute == 0) ? "" : minute + "m")
                    + second + "s";
        }
    }
}
