package org.qxtx.idea.player.utils;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;

import org.qxtx.idea.player.IdeaPlayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * DisContinuityReason：发生播放连续性被中断的事件原因码 <br/>
 * PlayerState：播放器状态等效值 <br/>
 * RepeatMode：播放器循环播放模式等效值，{@link IdeaPlayer#setRepeatMode(int)}方法需使用 <br/>
 * TimelineChange：播放器时间轴变更事件码 <br/>
 * ErrorKey：异常事件键码 <br/>
 * ErrorCode：异常事件错误码 <br/>
 * QueryKeyStatus：查询方法queryKeyStatus()返回的HashMap的全部键名
 * ErrorInfo：错误描述信息
 */
public final class IdeaUtil {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({RATIO_STRETCH, RATIO_21_9, RATIO_16_10, RATIO_16_9, RATIO_4_3})
    public @interface  AspectRatio {}
    public static final String RATIO_STRETCH = "stretch";
    public static final String RATIO_21_9 = "21:9";
    public static final String RATIO_16_10 = "16:10";
    public static final String RATIO_16_9 = "16:9";
    public static final String RATIO_4_3 = "4:3";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRACK_AUDIO, TRACK_VIDEO, TRACK_TEXT})
    public @interface TrackType {}
    public static final int TRACK_AUDIO = C.TRACK_TYPE_AUDIO;
    public static final int TRACK_VIDEO = C.TRACK_TYPE_VIDEO;
    public static final int TRACK_TEXT = C.TRACK_TYPE_TEXT;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DC_REASON_PERIOD_TRANSITION, DC_REASON_SEKEY, DC_REASON_SEKEY_ADJUSTMENT, DC_REASON_AD_INSERTION, DC_REASON_INTERNAL})
    public @interface DisContinuityReason {}
    /** Automatic playback transition from one period in the TL to the next.
     * The period index maybe the same as it was before the discontinuity in case the current period is repeated.
     */
    public static final int DC_REASON_PERIOD_TRANSITION = 0;
    /** 播放进度发生跳跃。**/
    public static final int DC_REASON_SEKEY = 1;
    /** 跳跃播放失败。**/
    public static final int DC_REASON_SEKEY_ADJUSTMENT = 2;
    /** 广告的切换播放。**/
    public static final int DC_REASON_AD_INSERTION = 3;
    /** 播放源内部的不连续性播放。 */
    public static final int DC_REASON_INTERNAL = 4;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_IDLE, STATE_BUFFERING, STATE_READY, STATE_ENDED})
    public @interface PlayerState {}
    public static final int STATE_IDLE = Player.STATE_IDLE;
    public static final int STATE_BUFFERING = Player.STATE_BUFFERING;
    public static final int STATE_READY = Player.STATE_READY;
    public static final int STATE_ENDED = Player.STATE_ENDED;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REPEAT_MODE_OFF, REPEAT_MODE_ONE, REPEAT_MODE_ALL})
    public @interface RepeatMode {}
    /** 禁止循环播放。 **/
    public static final int REPEAT_MODE_OFF = Player.REPEAT_MODE_OFF;
    /** 对当前流进行无限循环播放。 **/
    public static final int REPEAT_MODE_ONE = Player.REPEAT_MODE_ONE;
    /** 对所有流进行无限循环播放。 **/
    public static final int REPEAT_MODE_ALL = Player.REPEAT_MODE_ALL;


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({EXTENSION_MPD, EXTENSION_M3U8})
    public @interface MediaExtension {}
    public static final String EXTENSION_MPD = ".mpd";
    public static final String EXTENSION_M3U8 = ".m3u8";
    public static final String EXTENSION_RTMP = "rtmp://";
    public static final String EXTENSION_SS = "smoothStream";


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TLC_REASON_PREPARED, TLC_REASON_RESET, TLC_REASON_DYNAMIC})
    public @interface TimelineChange {}
    /** 播放器刷新一个新的媒体流。 **/
    public static final int TLC_REASON_PREPARED = 0;
    /** 播放器被重置。**/
    public static final int TLC_REASON_RESET = 1;
    /** 播放器动态更新导致的变更。**/
    public static final int TLC_REASON_DYNAMIC = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CODE_UNKNOWN_ERROR, CODE_UNKNOWN_UUID, CODE_BIND_VIEW_FAIL, CORE_MEDIA_SOURCE_ERROR, CODE_DRM_INIT_FAIL,
            CODE_PARSE_PATH_FAIL, CODE_SOURCE_ERROR, CODE_RENDERER_ERROR, CODE_UNEXPECTED_ERROR})
    public @interface ErrorCode {}
    /** 播放器引起的未知错误码。**/
    public static final int CODE_UNKNOWN_ERROR = 99;
    /** 不可解析的uuid。**/
    public static final int CODE_UNKNOWN_UUID = 990;
    /** 播放器绑定view失败。**/
    public static final int CODE_BIND_VIEW_FAIL = 991;
    /** 未知的媒体资源类错误。**/
    public static final int CORE_MEDIA_SOURCE_ERROR = 992;
    /** DRM引起的未知错误码。 **/
    public static final int CODE_DRM_INIT_FAIL = 993;
    /** 解析媒体流url的错误，意味着{@link IdeaPlayer}无法初始化。 **/
    public static final int CODE_PARSE_PATH_FAIL = 994;

    /** 媒体流错误。**/
    public static final int CODE_SOURCE_ERROR = 995;
    /** 渲染器错误。**/
    public static final int CODE_RENDERER_ERROR = 996;
    /** ExoPlayer Core引起的错误。**/
    public static final int CODE_UNEXPECTED_ERROR = 997;


    //queryKeyStatus() -> key
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({QKS_PLAY_ALLOWED, QKS_RENEWAL_SERVER_URL, QKS_TOTAL_DURATION, QKS_PERSIST_ALLOWED, 
            QKS_RENEW_ALLOWED, QKS_LICENSE_TYPE, QKS_PLAYBACK_DURATION})
    public @interface QueryKeyStatus {}
    public static final String QKS_PLAY_ALLOWED = "PlayAllowed";
    public static final String QKS_RENEWAL_SERVER_URL = "RenewalServerUrl";
    /** License总可用时长。 **/
    public static final String QKS_TOTAL_DURATION = "LicenseDurationRemaining";
    public static final String QKS_PERSIST_ALLOWED = "PersistAllowed";
    public static final String QKS_RENEW_ALLOWED = "RenewAllowed";
    public static final String QKS_LICENSE_TYPE = "LicenseType";
    /** 当前媒体流剩余播放时长（单位：秒）。 **/
    public static final String QKS_PLAYBACK_DURATION = "PlaybackDurationRemaining";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({INFO_CONTEXT_NULL, INFO_PARSE_PATH_FAIL, INFO_UNKNOWN_ERROR, INFO_UNKNOWN_UUID,
            INFO_BIND_VIEW_FAIL, INFO_MEDIA_SOURCE_ERROR, INFO_DRM_INIT_FAIL})
    public @interface ErrorInfo {}
    public static final String INFO_UNKNOWN_ERROR = "Unknown error.";
    public static final String INFO_PARSE_PATH_FAIL = "Can't to parse manifest of media. Check network?";
    public static final String INFO_UNKNOWN_UUID = "Unknown uuid. Set uuid to NULL.";
    public static final String INFO_BIND_VIEW_FAIL = "Bind view error. IdeaPlayer create failed.";
    public static final String INFO_MEDIA_SOURCE_ERROR = "Init mediaSource error.";
    public static final String INFO_DRM_INIT_FAIL = "DefaultDrmSessionManager init failed. IdeaPlayer will not to support DRM.";

    public static final String INFO_CONTEXT_NULL = "Context is null.";
    public static final String INFO_DRM_VENDOR_DEFINED = "DRM vendor-defined error: ";
}
