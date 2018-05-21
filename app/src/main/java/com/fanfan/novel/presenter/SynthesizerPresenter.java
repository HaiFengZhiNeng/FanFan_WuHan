package com.fanfan.novel.presenter;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;

import com.fanfan.novel.common.Constants;
import com.fanfan.novel.presenter.ipresenter.ISynthesizerPresenter;
import com.fanfan.novel.service.listener.TtsListener;
import com.fanfan.novel.utils.FucUtil;
import com.fanfan.robot.app.RobotInfo;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;

import java.util.Random;

/**
 * Created by android on 2018/1/3.
 */

public class SynthesizerPresenter extends ISynthesizerPresenter implements TtsListener.SynListener {

    private ITtsView mTtsView;

    private SpeechSynthesizer mTts;
    private TtsListener mTtsListener;

    private Handler mHandler = new Handler();

    public SynthesizerPresenter(ITtsView baseView) {
        super(baseView);
        this.mTtsView = baseView;
        mTtsListener = new TtsListener(this);
    }

    @Override
    public void start() {
        initTts();
    }

    @Override
    public void finish() {
        stopTts();
        if (mTts != null) {
            mTts.destroy();
        }
        mTtsListener = null;
    }

    @Override
    public void initTts() {

        if (mTts == null) {
            mTts = SpeechSynthesizer.createSynthesizer(mTtsView.getContext(), new InitListener() {
                @Override
                public void onInit(int code) {
                    if (code != ErrorCode.SUCCESS) {
                        Print.e("初始化失败，错误码：" + code);
                    }
                }
            });
        }
    }

    @Override
    public void buildTts() {
        if (mTts == null) {
            initTts();
        }
/*        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.LANGUAGE, "en_us");
        mTts.setParameter(SpeechConstant.VOICE_NAME, RobotInfo.getInstance().getTtsLineTalker());
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(RobotInfo.getInstance().getLineSpeed()));
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, String.valueOf(RobotInfo.getInstance().getLineVolume()));
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Constants.PROJECT_PATH + "/msc/tts.wav");*/


        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, FucUtil.getResTtsPath(mTtsView.getContext(), RobotInfo.getInstance().getTtsLocalTalker()));
        mTts.setParameter(SpeechConstant.VOICE_NAME, RobotInfo.getInstance().getTtsLocalTalker());
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(RobotInfo.getInstance().getLineSpeed()));
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "100");
   /*     mTts.setParameter(SpeechConstant.VOLUME, String.valueOf(RobotInfo.getInstance().getLineVolume()));*/
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "" + AudioManager.STREAM_VOICE_CALL);
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "3");
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
        //开启VAD
        mTts.setParameter(SpeechConstant.VAD_ENABLE, "1");
        //会话最长时间
        mTts.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "100");

        mTts.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        Print.e("initTts success ...");
    }

    @Override
    public void stopTts() {
        Print.e("isSpeaking1 : " + mTts.isSpeaking());
        if (mTts.isSpeaking()) {
            Print.e("isSpeaking2 : " + mTts.isSpeaking());
            mTts.stopSpeaking();
        }
    }

    @Override
    public void doAnswer(String answer) {
        mTtsView.stopSound();
     //   Constants.Speak = true;
        Print.e("onSpeakBegin : " + "====开始说话2222222");
        mTts.startSpeaking(answer, mTtsListener);
        mTtsView.onSpeakBegin(answer);
    }

    @Override
    public void stopHandler() {
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public void stopAll(String wakeUp) {
        stopTts();
        doAnswer(wakeUp);
    }

    @Override
    public boolean isSpeaking() {
        if (mTts == null)
            return false;
        return mTts.isSpeaking();
    }

    private String resFoFinal(int id) {
        String[] arrResult = ((Activity) mTtsView).getResources().getStringArray(id);
        return arrResult[new Random().nextInt(arrResult.length)];
    }

    @Override
    public void onCompleted() {
        if(isSpeaking()){
            return;
        }
        Print.e("doAnswer: " +"======= 结束说话");
        Print.e("doAnswer1 : " + isSpeaking());
   /*     Print.e("doAnswer2 : " + Constants.Speak);*/
        mHandler.postDelayed(runnable, 800);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mTtsView.onRunable();
        }
    };

    @Override
    public void onSpeakBegin() {
    }
}
