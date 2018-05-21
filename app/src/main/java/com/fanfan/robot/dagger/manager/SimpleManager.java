package com.fanfan.robot.dagger.manager;

import com.fanfan.novel.model.SerialBean;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.SerialPresenter;

import javax.inject.Inject;

/**
 * Created by Administrator on 2018/3/8/008.
 */

public class SimpleManager {

    LocalSoundPresenter mSoundPresenter;
    SerialPresenter mSerialPresenter;

    @Inject
    public SimpleManager(LocalSoundPresenter localSoundPresenter, SerialPresenter serialPresenter) {
        this.mSoundPresenter = localSoundPresenter;
        this.mSerialPresenter = serialPresenter;
    }

    public void onCreate() {
        mSoundPresenter.start();
        mSerialPresenter.start();
    }

    public void onStart() {
        mSoundPresenter.startRecognizerListener();
    }

    public void onResume() {
        mSoundPresenter.buildTts();
        mSoundPresenter.buildIat();
    }

    public void onPause() {
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    public void onDestroy() {
        mSoundPresenter.finish();
    }

    public void onDataReceiverd(SerialBean serialBean) {
        mSerialPresenter.onDataReceiverd(serialBean);
    }

    public void receiveMotion(int type, String motion) {
        mSerialPresenter.receiveMotion(type, motion);
    }

    public void doAnswer(String messageContent) {
        mSoundPresenter.doAnswer(messageContent);
    }

    public void onCompleted() {
        mSoundPresenter.onCompleted();
    }

    public void stopVoice() {
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }
}
