package org.qxtx.idea.player;

import org.qxtx.idea.player.base.IEventListener;

/**
 * Created by QXTX-GOSPELL on 2018/12/4 0004.
 */
public abstract class IdeaEventListener implements IEventListener {
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {

    }

    @Override
    public void onError(String info, int code) {

    }

    @Override
    public void onPlaybackParametersChanged(float speed, float pitch, boolean keepSilence) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onTracksChanged(Object trackGroups, Object trackSelections) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean isShuffle) {

    }
}

