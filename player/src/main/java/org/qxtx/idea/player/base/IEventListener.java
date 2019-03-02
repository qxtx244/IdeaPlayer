package org.qxtx.idea.player.base;

import org.qxtx.idea.player.IdeaPlayer;
import org.qxtx.idea.player.utils.IdeaUtil;

/**
 * Created by QXTX-GOSPELL on 2018/12/4 0004.
 */
public interface IEventListener {
    /**
     * 当播放器状态发生变更时，将会通知此事件。
     * @param playWhenReady  true:已经调用了{@link IdeaPlayer#start()}方法以表明外部允许播放  false:外部未允许播放
     * @param state  状态等效值，见{@link IdeaUtil @code STATE}
     */
    void onPlayerStateChanged(boolean playWhenReady, @IdeaUtil.PlayerState int state);

    /**
     * 播放器错误通知事件。当播放器中任一过程发生不可预期的错误，将会通知此事件。
     * @param info  错误详细信息,
     * @param code  错误码常量见{@link IdeaUtil @code CODE}
     */
    void onError(String info, int code);

    /**
     * 播放参数变更事件。当调用{@link IdeaPlayer#setSpeed}方法时，将会通知此事件。
     * @param speed  视频播放速率
     * @param pitch  声调
     * @param keepSilence  静音开关
     */
    void onPlaybackParametersChanged(float speed, float pitch, boolean keepSilence);

    /**
     * 进度跳转事件。当调用{@link IdeaPlayer#seekTo}方法时，将会通知此事件。
     * 当指定非法窗口序号或者timeline尚未创建完成时进行跳转，此通知将会抛出IllegalSeekPositionException(timeline, windowIndex, positionMs)异常。
     * 此方法将会伴随着{@link #onPositionDiscontinuity(int)}事件的通知
     */
    void onSeekProcessed();

    /**
     * 播放连续性被中断事件。当进行{@link IdeaPlayer#seekTo}方法时，将会通知此事件。
     * @param reason  连续性被中断的原因等效值，见{@link IdeaUtil @code DC}
     */
    void onPositionDiscontinuity(@IdeaUtil.DisContinuityReason int reason);

    /**
     * 重复播放模式变更事件。当调用{@link IdeaPlayer#setRepeatMode(int)}方法时，将会通知此事件。
     * @param repeatMode  重复模式等效值，见{@link IdeaUtil @code REPEAT_MODE}
     */
    void onRepeatModeChanged(@IdeaUtil.RepeatMode int repeatMode);

    /**
     * 资源下载状态变更事件。当播放器变更到{@link IdeaUtil#STATE_BUFFERING}状态
     * 或者从此状态离开时，将会通知此事件。
     * @param isLoading  true:资源需要开始下载  false:未处于资源下载状态
     */
    void onLoadingChanged(boolean isLoading);

    /**
     * 当可用的或者当前选定的轨道变更时，将会回调此方法。
     * @param trackGroups  （未实现）
     * @param trackSelections  （未实现）
     */
    void onTracksChanged(Object trackGroups, Object trackSelections);

    /**
     * 暂未提供相关方法，因此{@link IdeaPlayer}不会通知此事件。
     * @param isShuffle
     */
    void onShuffleModeEnabledChanged(boolean isShuffle);
}
