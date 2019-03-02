package org.qxtx.idea.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaDrm;
import android.media.MediaDrmResetException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionEventListener;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.KeysExpiredException;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import junit.framework.Assert;

import org.qxtx.idea.player.base.IPlayer;
import org.qxtx.idea.player.exception.ParseUrlException;
import org.qxtx.idea.player.exception.UnknownException;
import org.qxtx.idea.player.utils.IdeaUtil;
import org.qxtx.idea.player.utils.IdeaLog;
import org.qxtx.idea.player.utils.IdeaPermission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author QXTX <br/>
 * <br/>描述：IdeaPlayer支持HLS/DASH加密流的drm解密播放，提供快速集成方案。依赖ExoPlayer版本：2.8.4
 */
public final class IdeaPlayer implements IPlayer {
    private static final String TAG = "IdeaPlayer";

    /** 单播放模式 **/
    private static final int MODE_SINGLE = 0;
    /** 组合播放模式。**/
    private static final int MODE_CONCATENATE = 1;
    /** 循环模式。**/
    private static final int MODE_LOOPING = 2;
    /** 附带vast广告模式。**/
    private static final int MODE_WITH_AD = 3;

    /* 播放器状态 */
    private static final int STATE_IDLE = 4;
    private static final int STATE_NEED_START = 5;
    private static final int STATE_NEED_READY = 6;
    private static final int STATE_READY = 7;
    private static final int STATE_ERROR = 8;

    /** 默认的步退增量，单位为毫秒。 **/
    public int DEFAULT_REWIND_MS = 5000;
    /** 默认步进增量，单位为毫秒。 **/
    public int DEFAULT_FAST_FORWARD_MS = 10000;
    /** The default position seek to previous increment, in milliseconds. **/
    private long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private Timeline.Window window;
    private FrameworkMediaDrm mediaDrm;

    private WeakReference<Context> context;
    private static IdeaPlayer IdeaPlayer;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;
    private PlayerStateListener playerStateListener;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

    private DataSource.Factory dataSource;
    private DefaultTrackSelector trackSelector;
    private LoadControl loadControl;
    private RenderersFactory renderersFactory;

    /** 外部监听器。**/
    private IdeaEventListener listener;

    private int rewindMs;
    private int fastForwardMs;

    private UUID uuid;
    private String[] path;
    private String[] mediaExtension;

    /** RI服务器地址。**/
    private String serverLicenseUrl;

    /** 播放模式,见{@code MODE}。**/
    private int playMode;

    /** vast广告标签，可以是url或者合法字符串。**/
    private String adsTag;

    /** 作为向RI服务器传递OTT系统必要ID信息的临时方案。**/
    private String ottDeviceID;
    private String ottContentID;

    /** 必须为IdeaPlayerView类或其子类对象。**/
    private Object playView;

    /** 这是IdeaPlayer自有状态，不能等效于ExoPlayer内部状态，等效值见{@code MODE}。**/
    private volatile int state;

    /** 用于管理过于提前调用的方法事务。**/
    private PostMethodHandler postMethodHandler;

    private LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> methodMap;

    /** 用于收集离散资源，以便释放它们。**/
    private ArrayList<LinkedHashMap<Class<?>, Object>> paramMapList;

    /** 防止重复启动task任务导致崩溃。**/
    private PostTaskHandler postTaskHandler;

    private ExecutorService logThread;

    private IdeaPlayer(Context context) {
        this.context = new WeakReference<Context>(context);
        Context mContext = this.context.get();

        if (mContext != null) {
            dataSource = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, mContext.getApplicationContext().getPackageName()));
            loadControl = new DefaultLoadControl();
            trackSelector = new DefaultTrackSelector();
            ((DefaultTrackSelector)trackSelector).setParameters(new DefaultTrackSelector.ParametersBuilder().build());
            renderersFactory = new DefaultRenderersFactory(mContext);
            window = new Timeline.Window();
            rewindMs = DEFAULT_REWIND_MS;
            fastForwardMs = DEFAULT_FAST_FORWARD_MS;

            adsTag = null;

            methodMap = new LinkedHashMap<>();
            paramMapList = new ArrayList<>();
            postMethodHandler = new PostMethodHandler(methodMap, paramMapList);
            postTaskHandler = new PostTaskHandler();
        } else {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "IdeaPlayer initialization fail!");
            release();
        }
    }

    private static IdeaPlayer getInstance(Context context) {
        if (IdeaPlayer == null) {
            synchronized (IdeaPlayer.class) {
                if (IdeaPlayer == null) {
                    IdeaPlayer = new IdeaPlayer(context);
                }
            }
        }
        return IdeaPlayer;
    }

    @Override
    public void fastForward() {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
            long durationMs = player.getDuration();
            long seekPositionMs = player.getCurrentPosition() + fastForwardMs;
            if (durationMs != C.TIME_UNSET) {
                seekPositionMs = Math.min(seekPositionMs, durationMs);
            }
            seekTo(seekPositionMs);
        } else {
            putMsgInfoToMap("fastForward", null, null);
        }
    }

    @Override
    public long getBufferedPosition() {
        if (state != STATE_NEED_READY && state != STATE_READY) {
            return -1;
        } else {
            return player == null ? 0 : player.getBufferedPosition();
        }
    }

    @Override
    public long getCurrentPosition() {
        if (state != STATE_NEED_READY && state != STATE_READY) {
            return -1;
        } else {
            return player == null ? 0 : player.getCurrentPosition();
        }
    }

    @Override
    public long getDuration() {
        return player == null ? 0 : player.getDuration();
    }

    @Override
    public String[] getOttIDs() {
        ottDeviceID = ottDeviceID == null ? "unset" : ottDeviceID;
        ottContentID = ottContentID == null ? "unset" : ottContentID;
        return new String[] {ottDeviceID, ottContentID};
    }

    @Override
    public float[] getPlaybackParams() {
        Assert.assertTrue("Player is not start now! Fail to get player's volume.", state == STATE_NEED_READY || state == STATE_READY);
        if (state == STATE_NEED_READY) {
            return new float[] {1.0f, 1.0f};
        } else {
            Assert.assertNotNull("Player is not ready! Call it later?", player);
            PlaybackParameters playbackParameters = player.getPlaybackParameters();
            return new float[]{playbackParameters.speed, playbackParameters.pitch};
        }
    }

    @Override
    public int getPlaybackState() {
        return player == null ? Player.STATE_IDLE : player.getPlaybackState();
    }

    @Override
    public float getPlaybackSpeed() {
        return getPlaybackParams()[0];
    }

    @Override
    public float getVolume() {
        Assert.assertTrue("Player is not start now! Fail to get player's volume.", state == STATE_NEED_READY || state == STATE_READY);
        if (state == STATE_NEED_READY) {
            return 1.0f;
        } else {
            Assert.assertNotNull(player);
            return player.getVolume();
        }
    }

    @Override
    public int getCurrentWindowIndex() {
        return player == null ? -1 : player.getCurrentWindowIndex();
    }

    @Override
    public void showTrackManager(@IdeaUtil.TrackType int type) {
        if (context == null || context.get() == null || !(context.get() instanceof Activity)) {
            IdeaLog.e("Activity is not ready!");
            return ;
        }

        String title;
        int rendererIndex = getTrackIndex(type);
        if (rendererIndex == -1) {
            IdeaLog.e("Track is not found.");
            return ;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
            boolean isAudio = rendererType == C.TRACK_TYPE_AUDIO;
            boolean isVideo = rendererType == C.TRACK_TYPE_VIDEO;
            if (isAudio) {
                title = "Audio";
            } else if (isVideo) {
                title = "Video";
            } else {
                title = "Text";
            }

            boolean canSelection = isVideo
                    || (isAudio && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);

            ((Activity)context.get()).runOnUiThread(() -> {
                // 这里需要在UI中执行
//                try {
//                    Class<?> clazz = Class.forName("com.google.android.exoplayer2.ui.TrackSelectionView");
//                    Method method = clazz.getMethod("getDialog",
//                            Activity.class, CharSequence.class, DefaultTrackSelector.class, int.class);
//                    Pair<AlertDialog, Object> dialogPair = (Pair<AlertDialog, Object>)method.invoke(null,
//                            context.get(), title, trackSelector, rendererIndex);
//                    Method viewSet1 = clazz.getDeclaredMethod("setShowDisableOption", boolean.class);
//                    viewSet1.invoke(dialogPair.second, true);
//                    Method viewSet2 = clazz.getDeclaredMethod("setAllowAdaptiveSelections", boolean.class);
//                    viewSet2.invoke(dialogPair.second, canSelection);
//                    dialogPair.first.show();

                    Pair<AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog((Activity)context.get(), title, trackSelector, rendererIndex);
                    dialogPair.second.setShowDisableOption(true);
                    dialogPair.second.setAllowAdaptiveSelections(canSelection);
                    dialogPair.first.show();
//                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                    e.printStackTrace();
//                }
            });
        }
    }

    @Override
    public boolean isLoading() {
        return player != null && player.isLoading();
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY;
    }

    @Override
    public boolean isPlayingAd() {
        return player != null && player.isPlayingAd();
    }

    @Override
    public boolean isSeekable() {
        if (player == null) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "Player is not ready! Call it later?");
            return false;
        }
        return player.isCurrentWindowSeekable();
    }

    @Override
    public boolean isPlayWhenReady() {
        return state == STATE_NEED_READY || state == STATE_READY;
    }

    @Override
    public void next() {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
            Timeline timeline = player.getCurrentTimeline();
            if (timeline.isEmpty()) {
                return;
            }
            int windowIndex = player.getCurrentWindowIndex();
            int nextWindowIndex = player.getNextWindowIndex();
            if (nextWindowIndex != C.INDEX_UNSET) {
                seekTo(nextWindowIndex, C.TIME_UNSET);
            } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
                seekTo(windowIndex, C.TIME_UNSET);
            }
        } else {
            putMsgInfoToMap("next", null, null);
        }
    }

    @Override
    public void pause() {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
            player.setPlayWhenReady(false);
        } else {
            putMsgInfoToMap("pause", null, null);
        }
    }

    @Override
    public void previous() {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
            Timeline timeline = player.getCurrentTimeline();
            if (timeline.isEmpty()) {
                return;
            }
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            int previousWindowIndex = player.getPreviousWindowIndex();
            if (previousWindowIndex != C.INDEX_UNSET
                    && (getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                    || (window.isDynamic && !window.isSeekable))) {
                seekTo(previousWindowIndex, C.TIME_UNSET);
            } else {
                seekTo(0);
            }
        } else {
            putMsgInfoToMap("previous", null, null);
        }
    }

    @Override
    public Map<String, String> queryKeyStatus() {
        if (drmSessionManager == null) {
//            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "Query not allowed before player is ready or media was unencrypted.");
            return null;
        }
        return drmSessionManager.queryKeyStatus();
    }

    @Override
    public void release() {
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "\r\n");

        state = STATE_ERROR;

        if (postTaskHandler != null) {
            postTaskHandler.releaseHandler();
            postTaskHandler = null;
        }

        listener = null;

        if (player != null) {
            player.release();
            player = null;
        }

        releaseMediaDrm();

        drmSessionManager = null;
        context = null;

        if (postMethodHandler != null) {
            postMethodHandler.releaseHandler();
            postMethodHandler = null;
        } else {
            if (methodMap != null) {
                methodMap.clear();
                methodMap = null;
            }
            if (paramMapList != null) {
                paramMapList.clear();
                paramMapList = null;
            }
        }

        if (logThread != null) {
            logThread.shutdownNow();
            logThread = null;
        }

        IdeaPlayer = null;

        System.gc();
        System.runFinalization();
        System.gc();
    }

    @Override
    public void restart() {
        seekTo(0);
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
    }

    @Override
    public void rewind() {
        if (isPlayerReady()) {
            seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
        } else {
            putMsgInfoToMap("rewind", null, null);
        }

    }

    @Override
    public void seekTo(long positionMs) {
        if (isPlayerReady()) {
            seekTo(player.getCurrentWindowIndex(), positionMs);
        } else {
            putMsgInfoToMap("seekTo", new Class<?>[] {int.class}, new Object[] {positionMs});
        }
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "windowIndex=" + windowIndex + ", positionMs=" + positionMs);
            player.seekTo(windowIndex, positionMs);
        } else {
            putMsgInfoToMap("seekTo", new Class<?>[] {int.class, long.class}, new Object[] {windowIndex, positionMs});
        }
    }

    @Override
    public void setAspectRatio(@IdeaUtil.AspectRatio String ratio) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "ratio=" + ratio);

            if (player == null) {
                IdeaLog.e("IdeaPlayer is a illegal object now. Please rebuild it.");
                return ;
            }

            if (playView == null || !(playView instanceof View)) {
                return ;
            }

            int width = ((View) playView).getWidth();
            int height = ((View) playView).getHeight();
            switch (ratio) {
                case IdeaUtil.RATIO_21_9:
//                    height = width * 9 / 21;
                    break;
                case IdeaUtil.RATIO_16_10:
                    height = width * 10 / 16;
                    break;
                case IdeaUtil.RATIO_16_9:
                    height = width * 9 / 16;
                    break;
                case IdeaUtil.RATIO_4_3:
                    height = width * 3 / 4;
                    break;
            }
            player.setAspectRatio(width, height, 0, 1.0f);
        } else {
            putMsgInfoToMap("setAspectRatio", new Class<?>[] {String.class}, new Object[] {ratio});
        }
    }

    @Override
    public void setFastForwardMs(int fastForwardMs) {
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "fastForewardMs=" + fastForwardMs);
        if (fastForwardMs < 1000 || fastForwardMs > 600000) {
            Log.e(TAG, "The value of fastForward is out of range that is allowed! use auto fastForward");
            return;
        }
        this.fastForwardMs = fastForwardMs;
    }

    @Override
    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "repeatMode=" + repeatMode);
            player.setRepeatMode(repeatMode);
        } else {
            putMsgInfoToMap("setRepeatMode", new Class<?>[] {int.class}, new Object[] {repeatMode});
        }

    }

    @Override
    public void setRewindMs(int rewindMs) {
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "rewindMs=" + rewindMs);
        if (rewindMs < 1000 || rewindMs > 600000) {
            Log.e(TAG, "The value of rewind is out of range that is allowed! use auto rewind");
            return;
        }
        this.rewindMs = rewindMs;
    }

    @Override
    public void setSpeed(float speed) {
        setSpeed(speed, 1.0F);
    }

    @Override
    public void setSpeed(float speed, float pitch) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "speed=" + speed + ", pitch=" + pitch);
            if (speed <= 0.25f || speed > 2.0f) {
                Log.e(TAG, "The value of speed  is out of range that is allowed! Speed is not changed.");
                speed = getPlaybackSpeed();
            }

            if (pitch <= 0.25f || pitch > 2.0f) {
                Log.e(TAG, "The value of pitch  is out of range that is allowed! Pitch is not changed.");
                pitch = getPlaybackParams()[1];
            }
            player.setPlaybackParameters(new PlaybackParameters(speed, pitch));
        } else {
            putMsgInfoToMap("setSpeed", new Class<?>[] {float.class, float.class}, new Object[] {speed, pitch});
        }
    }

    @Override
    public void setVolume(float audioVolume) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "audioVolume=" + audioVolume);
            if (audioVolume < 0f || audioVolume > 1.0f) {
                Log.e(TAG, "The value of volume  is out of range that is allowed! Volume is not changed.");
                audioVolume = getVolume();
            }
            player.setVolume(audioVolume);
        } else {
            putMsgInfoToMap("setVolume", new Class<?>[] {float.class}, new Object[] {audioVolume});
        }
    }

    @Override
    public void start() {
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
        if (state == STATE_NEED_START || state == STATE_READY) {
            player.setPlayWhenReady(true);
            state = STATE_READY;
        } else {
            if (state != STATE_NEED_READY) {
                putMsgInfoToMap("start", null, null);
                state = STATE_NEED_READY;
            }
        }
    }

    @Override
    public void stop() {
        stop(false);
    }

    @Override
    public void stop(boolean reset) {
        if (isPlayerReady()) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "reset=" + reset);
            state = STATE_IDLE;
            player.stop(reset);
            drmSessionManager = null;

            if (postMethodHandler != null) {
                postMethodHandler.releaseHandler();
            }

            if (postTaskHandler != null) {
                postTaskHandler.releaseHandler();
            }
        } else {
            putMsgInfoToMap("stop", new Class<?>[] {boolean.class}, new Object[] {reset});
        }
    }

/*/********************* IdeaPlayer方法 **********************/

    /**
     * 使用Builder模式对IdeaPlayer进行创建和初始化。最后调用{@link #build()}方法可初始化IdeaPlayer，
     * 并且得到IdeaPlayer单例。这是创建IdeaPlayer的唯一途径。
     */
    public static class Builder {
        private IdeaPlayer IdeaPlayer;

        public Builder(Context context) {
            //待取决：默认每一次创建播放器，都将被初始化，所有参数重新配置，否则可能导致使用者对创建流程产生混乱
            if (IdeaPlayer.IdeaPlayer != null) {
                IdeaPlayer.IdeaPlayer.release();
            }

            IdeaPlayer = IdeaPlayer.getInstance(context);
            IdeaPlayer.state = STATE_IDLE;
            IdeaPlayer.adsTag = null;
            IdeaPlayer.playMode = MODE_CONCATENATE;

            try {
                Process exec = Runtime.getRuntime().exec(new String[] {"logcat", "-c"});
                exec.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 为播放器添加播放器状态事件监听器，属于低级事件监听。
         * @param listener  监听器
         * @return  {@link Builder}对象
         */
        public Builder addEventListener(IdeaEventListener listener) {
            logcat(listener.getClass().getSimpleName());
            IdeaPlayer.listener = listener;
            return this;
        }

        /**
         * 设置RI服务器地址，如未设置则使用默认地址
         * @param licenseUrl  RI服务器地址
         * @return  {@link Builder}对象
         */
        public Builder setLicenseUrl(String licenseUrl) {
            logcat("serverLicenseUrl= " + licenseUrl);
            if (licenseUrl != null && licenseUrl.startsWith("http")) {
                IdeaPlayer.serverLicenseUrl = licenseUrl;
            }
            return this;
        }

        /**
         * 为播放器设置媒体地址，可以传入至少一个媒体地址进行顺序播放，但当调用{@link #setAds(String)}方法后，
         * 将只有第一个地址有效。并且，仅支持以下媒体流组合形式：<br/>
         * (1)未加密流<br/>
         * (2)相同加密方案的加密流<br/>
         * (3)未加密流 + 相同加密方案的加密流
         * @param mediaPath  一个或多个媒体地址
         * @return  {@link Builder}对象
         */
        public Builder setPath(@NonNull String... mediaPath) {
            logcat("size = " + "first mediaPath=" + mediaPath[0] + ", num= " + mediaPath.length);

            IdeaPlayer.path = mediaPath;

            if (IdeaPlayer.adsTag != null) {
                IdeaPlayer.playMode = MODE_WITH_AD;
            }
            return this;
        }

        /**
         * 如果为hls或者dash，此设置必要的，帮助播放器识别url指向的媒体流类型以便解析它。当传入多个媒体地址，
         *  则也应当传入相同数量的extension
         * @param extensions
         * @return  {@link Builder}对象
         */
        public Builder setMediaExtension(@NonNull @IdeaUtil.MediaExtension String... extensions) {
            if (extensions.length == 0 || extensions.length != IdeaPlayer.path.length) {
                IdeaLog.e("Set extension error.");
                return this;
            }
            if (IdeaPlayer.mediaExtension == null) {
                IdeaPlayer.mediaExtension = new String[extensions.length];
            }
            System.arraycopy(extensions, 0, IdeaPlayer.mediaExtension, 0, extensions.length);
            return this;
        }

        /**
         * 添加VAST广告。如果需要使用此功能，请在module的build.gradle中添加依赖
         * 'com.google.android.gms:play-services-ads:11.4.2'。<br/>
         * 为播放器设置一个符合VASTv3.0规范的字符串，可以是一个有效URL，也可以是一个符合规范的VAST标签。详细内容见VASTv3.0标准规范。<br/>
         * 当前只能为单一媒体流匹配VAST广告，因此调用此方法后无论传入多少媒体路径，都将只使用第一个媒体路径。
         * @param adsTag  一个URL或者符合规范的VAST标签字符串
         * @return  {@link Builder}对象
         */
        public Builder setAds(@NonNull String adsTag) {
            logcat(adsTag);
            IdeaPlayer.playMode = MODE_WITH_AD;
            IdeaPlayer.adsTag = adsTag.trim();
            return this;
        }

        /**
         * IdeaPlayer记录OTT系统的一些id信息。
         * @param ottDeviceID  盒子唯一识别id(UserId)
         * @param ottContentID  OTT节目id
         * @return  {@link Builder}对象
         */
        public Builder setOttIDs(String ottDeviceID, String ottContentID) {
            logcat("ottDeviceID=" + ottDeviceID + ", ottContentID=" + ottDeviceID);
            IdeaPlayer.ottDeviceID = ottDeviceID;
            IdeaPlayer.ottContentID = ottContentID;
            return this;
        }

        /**
         * IdeaPlayer记录一个View对象，并在播放器初始化时自动地与此对象进行绑定以显示画面。
         * @param playView  必须为com.google.android.exoplayer2.ui.PlayerView或其子类对象
         * @return  {@link Builder}对象
         */
        public Builder bindView(@NonNull Object playView) {
            logcat(playView.getClass().getSimpleName());
            IdeaPlayer.playView = playView;
            return this;
        }

        /**
         * 是否保存播放器产生的日志消息到本地文件中。<br/>
         * 日志文件路径：优先保存到USB存储设备中，若无USB存储设备则保存到存储卡根目录，文件名称为IdeaPlayer.log"
         * @param allowSaveLog  true:允许记录日志消息到本地文件中  false:禁止记录消息日志
         * @return  {@link Builder}对象
         */
        public Builder setDebugMode(boolean allowSaveLog) {
            IdeaLog.setFlag(allowSaveLog);

            logcat("setDebugMode=" + allowSaveLog);

            if (allowSaveLog && IdeaPlayer.logThread == null) {
                IdeaPermission.checkPermission(IdeaPlayer.context.get());

                IdeaPlayer.logThread = Executors.newSingleThreadScheduledExecutor();
                IdeaPlayer.logThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        Process exec = null;
                        BufferedReader reader = null;
                        BufferedWriter writer = null;
                        try {
                            exec = Runtime.getRuntime().exec(new String[]{"logcat"});
                            reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                            String path = findUDisk();
                            path = path == null ? Environment.getExternalStorageDirectory() + "" : path;
                            File file = new File(path + "/IdeaPlayer.log");
                            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                            String line;
                            while (IdeaLog.isAllowDebug()) {
                                while ((line = reader.readLine()) != null) {
                                    writer.write(line);
                                    writer.newLine();
                                    writer.flush();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                exec.destroy();
                                reader.close();
                                writer.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            return this;
        }

        /**
         * 设置自定义缓冲策略，替换默认策略。
         * @param loadControl  自定义缓冲策略，必须为{@link DefaultLoadControl}子类
         * @return  {@link Builder}对象
         */
        public Builder setLoadControl(@NonNull IdeaLoadControl loadControl) {
            logcat(loadControl.getClass().getSimpleName());
            IdeaPlayer.loadControl = loadControl;
            return this;
        }

        /**
         * 使用一个自定义轨道选择策略替换默认策略。
         * @param trackSelector  自定义轨道选择策略，必须为{@link IdeaTrackSelector}子类
         * @return  {@link Builder}对象
         */
        public Builder setTrackSelector(@NonNull IdeaTrackSelector trackSelector) {
            logcat(trackSelector.getClass().getSimpleName());
            IdeaPlayer.trackSelector = trackSelector;
            return this;
        }

        /**
         * 使用一个自定义数据源工厂替换默认实现类。
         * @param factory  自定义数据源工厂，必须为{@link IdeaDataSourceFactory}子类
         * @return  {@link Builder}对象
         */
        public Builder setDataSourceFactory(IdeaDataSourceFactory factory) {
            logcat(factory.getClass().getSimpleName());
            IdeaPlayer.dataSource = factory;
            return this;
        }

        /**
         * 使用一个自定义渲染器工厂替换默认实现类。
         * @param factory  自定义渲染器工厂，必须为{@link IdeaRenderersFactory}子类
         * @return  {@link Builder}对象
         */
        public Builder setRenderersFactory(@NonNull IdeaRenderersFactory factory) {
            logcat(factory.getClass().getSimpleName());
            IdeaPlayer.renderersFactory = factory;
            return this;
        }

        /**
         * 初始化播放器，并返回IdeaPlayer对象。
         * @return {@link IdeaPlayer}对象
         */
        public IdeaPlayer build() {
            if (IdeaPlayer.path.length == 0) {
                IdeaPlayer.release();
                return null;
            }

            IdeaPlayer.postTaskHandler.postTask();
            return IdeaPlayer;
        }

        private void logcat(String msg) {
            showMsg(Thread.currentThread().getStackTrace()[3].getMethodName(), msg);
        }
    }

    /**
     * @return {@link IdeaPlayer}单例
     */
    public static IdeaPlayer getIdeaPlayer() {
        return IdeaPlayer;
    }

    /** @see #showTrackManager(int). */
    private int getTrackIndex(@IdeaUtil.TrackType int type) {
        int trackIndex = -1;
        if (trackSelector == null) {
            IdeaLog.e("Get track index error. Player is not ready! ");
            return trackIndex;
        }

        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (trackInfo == null) {
            return trackIndex;
        }

        for (int i = 0; i < trackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = trackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                int rendererType = player.getRendererType(i);
                if (rendererType == type) {
                    trackIndex = i;
                }
            }
        }
        return trackIndex;
    }

    /**
     * 检查调试开关allowSaveLog，打印日志到终端的同时决定是否需要保存日志到本地文件
     */
    private static void showMsg(String callerName, String msg) {
        IdeaLog.e(callerName + "() execute: " + msg);
    }

    private static String findUDisk() {
        char pathFlag = 'a';
        File file;
        //适配公司安卓7.0盒子，发现U盘
        for (int i = 0; i < 26; i++) {
            for (int j = -1; j < 10; j++) {
                file = (j == -1) ? new File("storage/sd" + pathFlag + "/") : new File("storage/sd" + pathFlag + j + "/");
                if (file.exists()) {
                    return file.getPath();
                }
            }
            pathFlag = (char)((int)pathFlag + 1);
        }

        return null;
    }

    private boolean isPlayerReady() {
        return state == STATE_READY;
    }

    private void putMsgInfoToMap(String methodName, Class<?>[] c, Object[] obj) {
        LinkedHashMap<Class<?>, Object> params = null;
        if (c != null && c.length > 0) {
            params = new LinkedHashMap<>();
            for (int i = 0; i < c.length; i++) {
                Object param = (obj == null || obj.length == 0) ? null : obj[i];
                params.put(c[i], param);
            }
        }

        methodMap.put(methodName, params);
        paramMapList.add(params);
    }

    private void releaseMediaDrm() {
        showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), null);
        if (mediaDrm != null) {
            mediaDrm.setOnEventListener(null);
            mediaDrm.setOnKeyStatusChangeListener(null);
            mediaDrm.release();
            mediaDrm = null;
        }
    }

    private ParseUuidTask newParseUuidTask() {
        return new ParseUuidTask();
    }

    // LYX_TODO: 2019/2/28 0028 待处理 这里可能需要长期更改，以便增强对url的识别、媒体流的识别逻辑。
    /**
     * Builder主要工作实现线程。
     */
    private final class ParseUuidTask extends AsyncTask<String, Integer, String> {
        private final String PATH_HTTP = "http";
        private final String PATH_HTTPS = "https";

        @Override
        protected String doInBackground(String... paths) {
            String strUuid = null;
            String manifestType;

            for (int i = 0; i < paths.length; i++) {
                String aPath = paths[i].trim();
                boolean isHls = aPath.endsWith(IdeaUtil.EXTENSION_M3U8);
                boolean isDash = aPath.endsWith(IdeaUtil.EXTENSION_MPD);
                boolean isHttp = aPath.startsWith(PATH_HTTP);
                boolean isHttps = aPath.startsWith(PATH_HTTPS);
                boolean isRtmp = aPath.startsWith(IdeaUtil.EXTENSION_RTMP);

                if (mediaExtension != null) {
                    manifestType = mediaExtension[i];
                } else if (isDash) {
                    manifestType = IdeaUtil.EXTENSION_MPD;
                } else if (isHls) {
                    manifestType = IdeaUtil.EXTENSION_M3U8;
                } else {
                    manifestType = "other";
                }

                //从文件中解析或者下载解析
                if (isRtmp) {
                    return null;
                } else {
                    strUuid = (isHttp || isHttps) ? parseUuidForHttp(aPath, manifestType) : parseUuidForFile(aPath, manifestType);
                }

                if (strUuid != null) { //解析到第一个uuid，结束整个解析过程
                    return strUuid;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            try {
                if (context == null || context.get() == null) {
                    throw new UnknownException(IdeaUtil.INFO_CONTEXT_NULL);
                }

                if (result != null && result.equals(IdeaUtil.CODE_PARSE_PATH_FAIL + "")) {
                    throw new ParseUrlException(IdeaUtil.INFO_PARSE_PATH_FAIL);
                }
                uuid = result == null ? null : UUID.fromString(result);
            } catch (Exception e) {
                int code;
                String info = e.getLocalizedMessage();
                if (e instanceof ParseUrlException) {
                    code = IdeaUtil.CODE_PARSE_PATH_FAIL;
                } else if (e instanceof UnknownException) {
                    code = IdeaUtil.CODE_UNKNOWN_ERROR;
                } else {
                    code = IdeaUtil.CODE_UNKNOWN_UUID;
                }
                showMsg(methodName, info);
                listener.onError(info, code);
                release();
                return ;
            }

            //初始化drmSessionManager，如果uuid为空则不创建，进行未加密流播放形式
            drmSessionManager = uuid == null ? null : setDrmSessionManager();
            if (drmSessionManager != null) {
                drmSessionManager.addListener(new Handler((context.get()).getMainLooper()), new DrmStateListener(listener));
            }

            if (player == null) {
                synchronized (IdeaPlayer.class) {
                    if (player == null) {
                        player = ExoPlayerFactory.newSimpleInstance(
                                renderersFactory,
                                trackSelector,
                                loadControl,
                                drmSessionManager);

                        showMsg(methodName, "SimpleExoPlayer create success.");
                        if (listener != null) {
                            playerStateListener = new PlayerStateListener(listener);
                            player.addListener(playerStateListener);
                        }
                    }
                }
            }

            mediaSource = selectMediaSource();
            if (mediaSource == null) {
                showMsg(methodName, IdeaUtil.INFO_MEDIA_SOURCE_ERROR);
                listener.onError(IdeaUtil.INFO_MEDIA_SOURCE_ERROR, IdeaUtil.CORE_MEDIA_SOURCE_ERROR);
                release();
                return ;
            }

            if (!bindPlayerView()) {
                showMsg(methodName, IdeaUtil.INFO_BIND_VIEW_FAIL);
                if (listener != null) {
                    listener.onError(IdeaUtil.INFO_BIND_VIEW_FAIL, IdeaUtil.CODE_BIND_VIEW_FAIL);
                }
                release();
                return ;
            }

            player.prepare(mediaSource);

            showMsg(methodName, "IdeaPlayer init completed.");
            state = STATE_NEED_START;

            postMethodHandler.doMsg();
        }

        private DefaultDrmSessionManager<FrameworkMediaCrypto> setDrmSessionManager() {
            boolean forceDefaultUrl = false;
            serverLicenseUrl = serverLicenseUrl == null ? "unknownLicense" : serverLicenseUrl;

            try {
                releaseMediaDrm();
                mediaDrm = FrameworkMediaDrm.newInstance(uuid);

                mediaDrm.setOnKeyStatusChangeListener(new OnKeyStateChangeListener());
                mediaDrm.setOnEventListener(new OnEventListener());

                HttpDataSource.Factory lisenceHttpDataSource = new DefaultHttpDataSourceFactory(Util.getUserAgent(context.get(), context.get().getApplicationContext().getPackageName()), null);
                HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(serverLicenseUrl, forceDefaultUrl, lisenceHttpDataSource);
                return new DefaultDrmSessionManager<FrameworkMediaCrypto>(
                        uuid,
                        mediaDrm,
                        drmCallback,
                        null);
            } catch (UnsupportedDrmException e) {
                showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "UnsupportedDrmException==" + e);
                listener.onError(IdeaUtil.INFO_DRM_INIT_FAIL, IdeaUtil.CODE_DRM_INIT_FAIL);
            }

            return null;
        }

        private MediaSource selectMediaSource() {
            ConcatenatingMediaSource mediaSources = null;
            Uri uri;

            String mathodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            for (int i = 0; i < path.length; i++) {
                String aPath = path[i];
                boolean isHls = aPath.endsWith(IdeaUtil.EXTENSION_M3U8);
                boolean isDash = aPath.endsWith(IdeaUtil.EXTENSION_MPD);
                boolean isRtmp = aPath.startsWith(IdeaUtil.EXTENSION_RTMP);
                boolean isSS = aPath.matches(".*\\.ism(l)?(/manifest(\\(.+\\))?)?");

                if (mediaExtension != null) {
                    isHls = isHls || mediaExtension[i].equals(IdeaUtil.EXTENSION_M3U8);
                    isDash = isDash || mediaExtension[i].equals(IdeaUtil.EXTENSION_MPD);
                    isRtmp = isRtmp || mediaExtension[i].equals(IdeaUtil.EXTENSION_RTMP);
                    isSS = isSS || mediaExtension[i].equals(IdeaUtil.EXTENSION_SS);
                }

                MediaSource source = null;
                if (aPath.startsWith(PATH_HTTP) || isRtmp) {
                    uri = Uri.parse(aPath);
                } else {
                    uri = Uri.fromFile(new File(aPath));
                }

                if (isHls) {
                    source = new HlsMediaSource.Factory(dataSource).createMediaSource(uri);
                } else if (isDash) {
                    source = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSource), dataSource).createMediaSource(uri);
                } else if (isSS) {
                    showMsg(mathodName, "SmoothStreaming is not support.");
                } else if (isRtmp) {
                    IdeaLog.e("isRTMP");
                    source = new ExtractorMediaSource.Factory(new RtmpDataSourceFactory()).createMediaSource(uri);
                } else {
                    source = new ExtractorMediaSource.Factory(dataSource).createMediaSource(uri);
                }

                if (source == null) {
                    return null;
                }

                if (playMode == MODE_WITH_AD) {
                    ImaAdsLoader loader = null;
                    if (adsTag.startsWith(PATH_HTTP)) {
                        loader = new ImaAdsLoader.Builder(context.get()).buildForAdTag(Uri.parse(adsTag));
                    } else if (adsTag.startsWith("<?xml") && adsTag.endsWith("<VAST/>")) {
                        loader = new ImaAdsLoader.Builder(context.get()).buildForAdsResponse(adsTag);
                    } else { //广告tag无法识别，自动转换成其它播放模式
                        playMode = MODE_CONCATENATE;
                        mediaSources = mediaSources == null ? new ConcatenatingMediaSource() : mediaSources;
                        mediaSources.addMediaSource(source);
                        continue;
                    }

                    try {
                        Class<?> c = playView.getClass();
                        Method method = c.getDeclaredMethod("getOverlayFrameLayout");
                        ViewGroup viewGroup = (ViewGroup) method.invoke(playView);
                        return viewGroup != null ? new AdsMediaSource(source, dataSource, loader, viewGroup) : null;
                    } catch (Exception e) {
                        showMsg(mathodName, "PlayerView was not found!");
                        return null;
                    }
                } else {
                    mediaSources = mediaSources == null ? new ConcatenatingMediaSource() : mediaSources;
                    mediaSources.addMediaSource(source);
                }
            }

            return mediaSources;
        }

        private boolean bindPlayerView() {
            try {
                Class<?> c = playView.getClass();
                Method method = c.getDeclaredMethod("setPlayer", Player.class);
                method.invoke(playView, player);
                return true;
            } catch (Exception e) {
                showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "BindPlayerViewException: " + e.getLocalizedMessage());
                return false;
            }
        }

        private String parseUuidForHttp(String path, String manifestType) {
            Call call = null;
            Response response = null;
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(path).build();
                call = okHttpClient.newCall(request);
                response = call.execute();

                /*
                 * 目前仅针对处理单视频轨，并且音频轨不单独加密的情况
                 * 如果有更好的替代方法，请取代此递归调用方案
                 */
                String manifest = response.body().string();
                if (manifestType.equals(IdeaUtil.EXTENSION_M3U8) && !manifest.contains("#EXT-X-KEY")) {
                    /* 未找到#EXT-X-KEY标签但却包含#EXT-X-TARGETDURATION标签，说明此轨道未被加密 */
                    if (manifest.contains("#EXT-X-TARGETDURATION")) {
                        return null;
                    }
                    return parseUuidForHttp(getSubPath(path, manifest), IdeaUtil.EXTENSION_M3U8);
                }

                return getUuidForManifest(manifest, manifestType);
            } catch (IOException e) {
                IdeaLog.e("OkHttp3 error：" + e.getMessage());
                return IdeaUtil.CODE_PARSE_PATH_FAIL + "";
            } finally {
                if (call != null) {
                    call.cancel();
                }
                if (response != null) {
                    response.close();
                }
            }
        }

        private String parseUuidForFile(String path, String manifestType) {
            String method = Thread.currentThread().getStackTrace()[2].getMethodName();
            File file = new File(path);
            if (!file.exists()) {
                showMsg(method, "Media File was not found.");
                return IdeaUtil.CODE_PARSE_PATH_FAIL + "";
            }

            StringBuilder manifestData = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line = reader.readLine()) != null) {
                    manifestData.append(line);
                }

                String manifest = manifestData.toString().trim();
                if (manifestType.equals(IdeaUtil.EXTENSION_M3U8) && !manifest.contains("#EXT-X-KEY")) {
                    if (manifest.contains("#EXT-X-TARGETDURATION")) {
                        reader.close();
                        return null;
                    }
                    return parseUuidForFile(getSubPath(path, manifest), IdeaUtil.EXTENSION_M3U8);
                }

                reader.close();
                return getUuidForManifest(manifest, manifestType);
            } catch (IOException e) {
                showMsg(method, "ReadMediaFileException==" + e);
                return IdeaUtil.CODE_PARSE_PATH_FAIL + "";
            }
        }

        private String getUuidForManifest(String manifestData, String manifestType) {
            int uuidStartPos;
            int uuidEndPos;
            String strUuid = null;

            int uuidIndex;
            switch (manifestType) {
                case IdeaUtil.EXTENSION_MPD:
                    uuidIndex = manifestData.indexOf("urn:uuid:");
                    if (uuidIndex == -1) {
                        strUuid = null;
                        break;
                    }

                    uuidStartPos = uuidIndex + 9;
                    uuidEndPos = manifestData.indexOf("\">", uuidStartPos);
                    strUuid = manifestData.substring(uuidStartPos, uuidEndPos);
                    break;
                case IdeaUtil.EXTENSION_M3U8:
                    uuidIndex = manifestData.indexOf("METHOD=");
                    if (uuidIndex == -1) {
                        strUuid = null;
                        break;
                    }

                    uuidStartPos = uuidIndex + 7;
                    uuidEndPos = manifestData.indexOf(",", uuidStartPos);
                    strUuid = manifestData.substring(uuidStartPos, uuidEndPos);
                    break;
                default:
                    strUuid = null;
                    break;
            }

            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "parse uuid in manifest=" + strUuid);
            return strUuid;
        }

        private String getSubPath(String path, String manifest) {
            int pos = path.lastIndexOf("/");
            int subPathTag = manifest.indexOf("#EXT-X-STREAM-INF:");
            int subPathStartPos = manifest.indexOf("\n", subPathTag) + 1;
            int subPathEndPos = manifest.indexOf("\n", subPathStartPos);
            String subUrl = "/" + manifest.substring(subPathStartPos, subPathEndPos);
            return path.substring(0, pos) + subUrl;
        }
    }

    private static final class PostTaskHandler extends Handler {
        private ParseUuidTask task;

        private void postTask() {
            sendEmptyMessage(0);
        }

        @Override
        public void handleMessage(Message msg) {
            if (task != null) {
                task.cancel(true);
            }
            task = IdeaPlayer.newParseUuidTask();
            task.execute(IdeaPlayer.path);
        }

        private void releaseHandler() {
            removeMessages(0);
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }
    }

    /** 解决线程同步的临时方案，将播放器的提前动作全部按照队列执行。**/
    private static final class PostMethodHandler extends Handler  {
        private LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> methodMap;
        private ArrayList<LinkedHashMap<Class<?>, Object>> paramsList;

        PostMethodHandler(LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> methodMap, ArrayList<LinkedHashMap<Class<?>, Object>> paramsList) {
            this.methodMap = methodMap;
            this.paramsList = paramsList;
        }

        void doMsg() {
            sendEmptyMessage(0);
        }

        void releaseHandler() {
            removeMessages(0);
            if (methodMap != null) {
                methodMap.clear();
                methodMap = null;
            }

            if (paramsList != null) {
                paramsList.clear();
                paramsList = null;
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (methodMap == null || methodMap.size() == 0) {
                return ;
            }

            String mName = Thread.currentThread().getStackTrace()[2].getMethodName();
            Iterator<String> keyIterator = methodMap.keySet().iterator();
            Iterator<LinkedHashMap<Class<?>, Object>> valueIterator = methodMap.values().iterator();
            for (int i = 0; i < methodMap.size(); i++) {
                String methodName = keyIterator.hasNext() ? (String)keyIterator.next() : null;
                LinkedHashMap<Class<?>, Object> paramInfo = valueIterator.hasNext() ? valueIterator.next() : null;
                try {
                    Class<?> c = getIdeaPlayer().getClass();

                    Object[] params = null;
                    Class<?>[] classes = null;
                    if (paramInfo != null) {
                        classes = new Class<?>[paramInfo.size()];
                        params = new Object[paramInfo.size()];
                        paramInfo.keySet().toArray(classes);
                        paramInfo.values().toArray(params);
                    }
                    Method method = c.getDeclaredMethod(methodName, classes);
                    method.invoke(getIdeaPlayer(), params);

                    //标记为提前调用的方法全部执行完毕
                    if (i + 1 == methodMap.size()) {
                        getIdeaPlayer().state = STATE_READY;
                        showMsg(mName, "Post Player early-action fully.");
                        releaseHandler();
                        break;
                    }
                } catch (Exception e) {
                    showMsg(mName, "Call methods error== " + e);
                }
            }
        }
    }

    /** 详细描述见{@link MediaDrm#setOnKeyStatusChangeListener(MediaDrm.OnKeyStatusChangeListener, Handler)}。**/
    private static final class OnKeyStateChangeListener implements ExoMediaDrm.OnKeyStatusChangeListener<FrameworkMediaCrypto> {
        @Override
        public void onKeyStatusChange(ExoMediaDrm exoMediaDrm, byte[] sessionId, List keyInfo, boolean hasNewUsableKey) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "OnKeyStateChangeListener$onKeyStatusChange, do nothing.");
        }
    }

    /** 详细描述见{@link MediaDrm#setOnEventListener(MediaDrm.OnEventListener)}。**/
    private static final class OnEventListener implements ExoMediaDrm.OnEventListener<FrameworkMediaCrypto> {
        @Override
        public void onEvent(ExoMediaDrm exoMediaDrm, byte[] sessionId, int event, int extra, @Nullable byte[] data) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "OnEventListener$onEvent, do nothing.");
            //nothing
        }
    }

    /** 详细描述见{@link MediaDrm#setOnExpirationUpdateListener(MediaDrm.OnExpirationUpdateListener, Handler)}。**/
    private static final class OnExpirationUpdateListener implements ExoMediaDrm.OnExpirationUpdateListener<FrameworkMediaCrypto> {
        @Override
        public void onExpirationUpdate(MediaDrm mediaDrm, byte[] bytes, long expirationTime) {
            showMsg(Thread.currentThread().getStackTrace()[2].getMethodName(), "OnExpirationUpdateListener$onExpirationUpdate, do nothing.");
            //nothing
        }
    }

    /** DefaultDrmSessionManager 监听事件。**/
    private final class DrmStateListener implements DefaultDrmSessionEventListener {
        private IdeaEventListener listener;
        private Thread cntThread;

        private DrmStateListener(IdeaEventListener listener) {
            this.listener = listener;
            cntThread = Thread.currentThread();
        }

        @Override
        public void onDrmKeysLoaded() {
            showMsg(cntThread.getStackTrace()[2].getMethodName(), "Drm keys loaded");
        }

        @Override
        public void onDrmSessionManagerError(Exception error) {
            String e;
            if (error instanceof KeysExpiredException) {
                e = "KeysExpiredException. Timeout";
            } else if (error instanceof MediaDrmResetException) {
                e = "MediaServer died. Play failed";
            } else {
                e = "Unknown DrmSessionManagerError happened: " + error.getLocalizedMessage();
            }
            showMsg(cntThread.getStackTrace()[2].getMethodName(), e);

            for (Player.EventListener listener : player.getListeners()) {
                listener.onPlayerError(ExoPlaybackException.createForSource(new IOException("DrmSessionManager error: " + error)));
            }
        }

        @Override
        public void onDrmKeysRestored() {
            showMsg(cntThread.getStackTrace()[2].getMethodName(), "Drm keys restored");
        }

        @Override
        public void onDrmKeysRemoved() {
            showMsg(cntThread.getStackTrace()[2].getMethodName(), "Drm keys removed");
        }
    }

    /** 接收播放器事件，整理后传递给外部APP监听器。**/
    private static final class PlayerStateListener extends Player.DefaultEventListener {
        private IdeaEventListener listener;
        private PlayerStateListener(IdeaEventListener listener) {
            this.listener = listener;
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            Throwable error = e.getCause();
            int code;
            boolean isMediaDrmStateException = error instanceof MediaDrm.MediaDrmStateException;
            boolean isBehindLiveWindowException = error instanceof BehindLiveWindowException;

            if (isMediaDrmStateException) {
                String errorCode = error.getLocalizedMessage().split(IdeaUtil.INFO_DRM_VENDOR_DEFINED)[1];
                code = Integer.parseInt(errorCode, 10);
            } else if (isBehindLiveWindowException) {
                if (IdeaPlayer.context == null || IdeaPlayer.context.get() == null) {
                    return ;
                }

                int windowIndex = IdeaPlayer.player.getCurrentWindowIndex();
                IdeaPlayer.player.prepare(IdeaPlayer.mediaSource, false, false);
                IdeaPlayer.player.setPlayWhenReady(true);
                IdeaPlayer.seekTo(windowIndex, 0);
                return ;
            } else {
                switch (e.type) {
                    case ExoPlaybackException.TYPE_SOURCE:
                        code = IdeaUtil.CODE_SOURCE_ERROR;
                        break;
                    case ExoPlaybackException.TYPE_RENDERER:
                        code = IdeaUtil.CODE_RENDERER_ERROR;
                        break;
                    default:
                        code = IdeaUtil.CODE_UNEXPECTED_ERROR;
                        break;
                }
            }

            showMsg(getMethodName(), "errorCode=" + code + "\r\n exception== " + e.getLocalizedMessage());
            listener.onError(e.getLocalizedMessage(), code);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int state) {
            showMsg(getMethodName(), "playWhenReady=" + playWhenReady + ", state=" + state);
            listener.onPlayerStateChanged(playWhenReady, state);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {
            showMsg(getMethodName(), null);
            listener.onTracksChanged(null, null);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            showMsg(getMethodName(), "isLoading=" + isLoading);
            listener.onLoadingChanged(isLoading);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            showMsg(getMethodName(), "repeatMode=" + repeatMode);
            listener.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean isShuffle) {
            showMsg(getMethodName(), "isShuffle=" + isShuffle);
            listener.onShuffleModeEnabledChanged(isShuffle);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            showMsg(getMethodName(), "reason=" + reason);
            listener.onPositionDiscontinuity(reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            showMsg(getMethodName(), "change speed=" + playbackParameters.speed + ", change pitch=" + playbackParameters.pitch);
            listener.onPlaybackParametersChanged(playbackParameters.speed, playbackParameters.pitch, playbackParameters.skipSilence);
        }

        @Override
        public void onSeekProcessed() {
            showMsg(getMethodName(), null);
            listener.onSeekProcessed();
        }

        private String getMethodName() {
            return Thread.currentThread().getStackTrace()[3].getMethodName();
        }
    }
}