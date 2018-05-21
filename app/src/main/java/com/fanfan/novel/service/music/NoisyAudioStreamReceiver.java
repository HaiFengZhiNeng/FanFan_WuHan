package com.fanfan.novel.service.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fanfan.novel.service.PlayService;

/**
 * Created by android on 2018/1/10.
 */

public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
    }
}
