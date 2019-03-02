package org.qxtx.idea.player.base;

import org.qxtx.idea.player.utils.IdeaUtil;

import java.util.Map;

public interface IPlayer {
    /**
     * 步进播放，修改步进大小见{@link #setFastForwardMs(int)}方法。
     */
    void fastForward();

    /**
     * @return  当前媒体流的已缓冲时长（单位：毫秒）
     */
    long getBufferedPosition();

    /**
     * @return  当前媒体流的播放位置（单位：毫秒）
     */
    long getCurrentPosition();

    /**
     * 播放器无法立即获得播放总时长，因此需要等待播放器解析视频信息。<br/>
     * 例：当{@link #isPlaying()}返回true时已能获取到正确时长值。
     * @return  当前媒体流的总播放时长（单位：毫秒）
     */
    long getDuration();

    /**
     * 获得IdeaPlayer记录的OTT系统id信息。
     * @return  string[0]:ottDeviceID  string[1]:ottContentID
     */
    String[] getOttIDs();

    /**
     * 获得相关播放参数，
     * @return  float[0]:播放速率  float[1]:声调速率
     */
    float[] getPlaybackParams();

    /**
     * 获得播放器当前状态，配合isPlayWhenReady()可得到播放器精确状态，
     * 状态值详细描述见{{@link IdeaUtil.PlayerState}。
     * @return  播放器内部状态等效值（1~4）
     */
    int getPlaybackState();

    /**
     * @return  播放器当前播放速率，默认为1.0
     */
    float getPlaybackSpeed();

    /**
     * 获得播放器音量，此数值为播放器自身音量大小，不等同于实际系统媒体音量值。
     *  范围为0~1.0，1.0对应系统当前媒体音量大小。
     * @return  播放器内部音量值
     */
    float getVolume();

    /**
     * 在播放器时间轴上存在多个媒体流的时候，获得当前媒体流的索引值。
     * @return  当前播放媒体的索引值
     */
    int getCurrentWindowIndex();

    /**
     * 弹出轨道选择。如果播放器未完成播放资源的载入，将不会显示轨道选择器。</br>
     * 示例1(视频轨)： {"1":{"1920x1080,0.5Mbps", "1280x720,3Mbps"}}</br>
     * 示例2(音频轨)： {"2":{"英文,立体声,0.13Mbps", "中文,左声道,0.13Mbps", "中文,立体声,0.13Mbps"}}
     *
     * @param type 轨道类型，见{@link IdeaUtil.TrackType}
     *
     * 将来可能会将轨道名称返回，匹配轨道逻辑在内部进行，以便用户自行设计界面。
     */
    void showTrackManager(@IdeaUtil.TrackType int type);

    /**
     * 检查播放器载入媒体资源状态。
     * @return true:播放器正在载入媒体资源  false:播放器未处于载入状态
     */
    boolean isLoading();

    /**
     * 检查播放器是否处于正在播放状态。
     * @return  true:播放器正在播放或缓冲  false:播放器未开始播放或缓冲
     */
    boolean isPlaying();

    /**
     * 检查是否正在播放VAST广告。
     * @return true: 播放器正在播放广告  false: 未播放广告
     */
    boolean isPlayingAd();

    /**
     * 检查播放器是否已经得到外部播放允许，配合{@link #getPlaybackState()}方法可以获得
     * 播放器精确的状态。
     * @return  true:外部允许立即播放  false:外部不允许播放
     */
    boolean isPlayWhenReady();

    /**
     * 检查当前播放媒体是否可以进行跳跃播放。
     * @return  true:允许跳跃播放  false:不允许跳跃播放
     */
    boolean isSeekable();

    /**
     * 如果存在多个媒体资源，调用此方法播放下一个。
     */
    void next();

    /**
     * 暂停播放，可调用{@link #start()}继续播放。
     */
    void pause();

    /**
     * 如果存在多个媒体资源，调用此方法播放上一个，否则重新播放当前流。
     */
    void previous();

    /**
     * 查询播放权限状态，当查询失败或者非法，将返回null。
     * 必须在播放器开始播放之后调用。<br/>
     * 详细键名称见{@link IdeaUtil @code QueryKeyStatus}。
     * @return  Map<String，String>对象，包含当前详细的状态信息
     */
    Map<String, String> queryKeyStatus();

    /**
     * 销毁播放器并释放相关资源。
     */
    void release();

    /**
     * 使当前播放的媒体流重新开始播放。
     */
    void restart();

    /**
     * 步退播放，修改默认步退幅值请调用{@link #setRewindMs(int)}方法。
     */
    void rewind();

    /**
     * 跳跃播放，在当前媒体流中跳跃到指定时间点。
     * @param position  指定时间点，单位为毫秒
     */
    void seekTo(long position);

    /**
     * 跳跃播放，跳跃到指定媒体流中指定的时间点。
     * @param windowIndex  当存在多个播放地址时，指定媒体流索引
     * @param positionMs  指定时间点，单位为毫秒
     */
    void seekTo(int windowIndex, long positionMs);

    /**
     * 设置视频宽高比。
     * @param ratio 画面比例，等效常量见{@link IdeaUtil @code RATIO}
     */
    void setAspectRatio(@IdeaUtil.AspectRatio String ratio);

    /**
     * 设置播放的步进增量，默认为10秒,最大允许设置范围为1000毫秒~600000毫秒
     * @param fastForwardMs  步进大小，单位为毫秒
     */
    void setFastForwardMs(int fastForwardMs);

    /**
     * 立即改变播放器播放模式，可在播放后调用。等效值见{@link IdeaUtil.RepeatMode}
     * @param repeatMode  播放模式
     */
    void setRepeatMode(@IdeaUtil.RepeatMode int repeatMode);

    /**
     * 设置播放的步退大小，默认为5秒，最大允许设置范围为1000毫秒~600000毫秒。
     * @param rewindMs  步退大小，单位为毫秒
     */
    void setRewindMs(int rewindMs);

    /**
     * 设置播放速率，默认值为1.0。
     * @param speed  播放速率，允许范围为0.5~2.0
     */
    void setSpeed(float speed);

    /**
     * 设置播放速率和声调，默认均为1.0。
     * @param speed  播放速率
     * @param pitch  声调速率
     */
    void setSpeed(float speed, float pitch);

    /**
     * 设置播放器内部音量，默认为1.0，对应当前系统媒体音量。
     * @param audioVolume  指定音量值，有效范围为0~1.0
     */
    void setVolume(float audioVolume);

    /**
     * 使播放器开始播放。
     */
    void start();

    /**
     * 开始播放，必须在{@link #start()}之后使用。
     */
    void stop();

    /**
     * 使播放器停止播放，将播放器状态置为Player.STATE_IDLE，失去所有播放资源，属不可逆状态。
     * 如果希望在此之后能继续播放，请使用pause()方法。播放器停止后，如需继续使用，
     * 请重新初始化播放器。
     * 调用此方法并不会释放播放器，因此如有需要请手动调用{@link #release()}方法。
     * @param reset  是否重置播放器播放状态
     */
    void stop(boolean reset);
}
