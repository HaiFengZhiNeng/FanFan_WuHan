package com.fanfan.novel.utils.music;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by zhangyuanyuan on 2017/11/10.
 */

public class MediaPlayerUtil implements MediaPlayer.OnBufferingUpdateListener {

    private static MediaPlayerUtil mMusicPlayerManager;

    private MediaPlayer mediaPlayer;

    public static synchronized MediaPlayerUtil getInstance() {
        if (mMusicPlayerManager == null) {
            synchronized (MediaPlayerUtil.class) {
                if (mMusicPlayerManager == null) {
                    mMusicPlayerManager = new MediaPlayerUtil();
                }
            }
        }
        return mMusicPlayerManager;
    }


    private void initMediaplayer() {
        try {
            mediaPlayer = new MediaPlayer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMusic(String url, final OnMusicCompletionListener onCompletionListener) {
        if (TextUtils.isEmpty(url)) {
            onCompletionListener.onCompletion(false);
        }

        try {
            if (mediaPlayer == null) {
                initMediaplayer();
            } else {
                mediaPlayer.reset();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
            mediaPlayer.setOnBufferingUpdateListener(this);

            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {

                    try {
                        onCompletionListener.onPrepare();
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        onCompletionListener.onCompletion(false);
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(final MediaPlayer mp) {

                    onCompletionListener.onCompletion(true);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(final MediaPlayer mp, final int what, final int extra) {
                    onCompletionListener.onCompletion(false);
                    return false;
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            onCompletionListener.onCompletion(false);
        }

    }


    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    public interface OnMusicCompletionListener {
        void onCompletion(boolean isPlaySuccess);

        void onPrepare();
    }


}
