package com.fanfan.novel.presenter;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;

import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.novel.service.listener.IatListener;
import com.fanfan.novel.service.listener.TtsListener;
import com.fanfan.novel.service.stragry.TranficCalculator;
import com.fanfan.novel.service.stragry.local.ArtificialStrategy;
import com.fanfan.novel.service.stragry.local.BackStrategy;
import com.fanfan.novel.service.stragry.local.ControlStrategy;
import com.fanfan.novel.service.stragry.local.FaceStrategy;
import com.fanfan.novel.service.stragry.local.LogoutStrategy;
import com.fanfan.novel.service.stragry.local.MapStrategy;
import com.fanfan.novel.service.stragry.local.MoveStrategy;
import com.fanfan.novel.service.stragry.local.StopStrategy;
import com.fanfan.novel.utils.FucUtil;
import com.fanfan.robot.R;
import com.fanfan.robot.app.RobotInfo;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;

import java.io.File;
import java.util.Random;

/**
 * Created by android on 2017/12/20.
 */

public class LocalSoundPresenter extends ILocalSoundPresenter implements TtsListener.SynListener, IatListener.RecognListener {

    private static final String LOCAL_GRAMMAR_NAME = "local";

    private ILocalSoundPresenter.ILocalSoundView mSoundView;

    private SpeechSynthesizer mTts;
    private SpeechRecognizer mIat;

    private TtsListener mTtsListener;
    private IatListener mIatListener;

    private Handler mHandler = new Handler();

    public LocalSoundPresenter(ILocalSoundView baseView) {
        super(baseView);
        mSoundView = baseView;

        mTtsListener = new TtsListener(this);
        mIatListener = new IatListener(this);
    }

    @Override
    public void start() {
        RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_LOCAL);
        initTts();
        initIat();
    }

    @Override
    public void finish() {
        RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_CLOUD);
//        mIat.cancel();
        mTts.destroy();
        mTtsListener = null;
        mIatListener = null;
    }

    @Override
    public void initTts() {
        if (mTts == null) {
            mTts = SpeechSynthesizer.createSynthesizer(mSoundView.getContext(), new InitListener() {
                @Override
                public void onInit(int code) {
                    if (code != ErrorCode.SUCCESS) {
                        Print.e("初始化失败，错误码：" + code);
                    }
                    Print.e("local initTts success");
                }
            });
        }
    }

    @Override
    public void buildTts() {
        if (mTts == null) {
            throw new NullPointerException(" mTts is null");
        }
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, FucUtil.getResTtsPath(mSoundView.getContext(), RobotInfo.getInstance().getTtsLocalTalker()));
        mTts.setParameter(SpeechConstant.VOICE_NAME, RobotInfo.getInstance().getTtsLocalTalker());
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(RobotInfo.getInstance().getLineSpeed()));
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "" + AudioManager.STREAM_VOICE_CALL);
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "3");
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");

        //开启VAD
        mTts.setParameter(SpeechConstant.VAD_ENABLE, "1");
        //会话最长时间
        mTts.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "100");

        mTts.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");

    }

    @Override
    public void initIat() {
        if (mIat == null) {
            mIat = SpeechRecognizer.createRecognizer(mSoundView.getContext(), new InitListener() {
                @Override
                public void onInit(int code) {
                    if (code != ErrorCode.SUCCESS) {
                        Print.e("初始化失败，错误码：" + code);
                    }
                    Print.e("local initIat success");
                }
            });
        }
    }

    @Override
    public void buildIat() {
        if (mIat == null) {
            throw new NullPointerException(" mIat is null");
        }
        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, FucUtil.getResAsrPath(mSoundView.getContext()));
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, Constants.GRM_PATH);
        mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, LOCAL_GRAMMAR_NAME);
        mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.VAD_BOS, "9000");
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Constants.GRM_PATH + File.separator + "iat.wav");
        Print.e("initIat success ...");
    }

    @Override
    public void stopTts() {
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
    }

    @Override
    public void doAnswer(String answer) {
        stopTts();
        stopRecognizerListener();
        mTts.startSpeaking(answer, mTtsListener);
    }

    @Override
    public void startRecognizerListener() {
        mIat.startListening(mIatListener);
    }

    @Override
    public void stopRecognizerListener() {
        mIat.startListening(null);
        mIat.stopListening();
    }

    @Override
    public void stopAll() {
        stopTts();
        doAnswer(resFoFinal(R.array.wake_up));
    }

    @Override
    public void stopHandler() {
        mHandler.removeCallbacks(runnable);
    }

    private String resFoFinal(int id) {
        String[] arrResult = ((Activity) mSoundView).getResources().getStringArray(id);
        return arrResult[new Random().nextInt(arrResult.length)];
    }

    //**********************************************************************************************

    @Override
    public void onCompleted() {
        mHandler.postDelayed(runnable, 0);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Print.e("启动监听");
            buildIat();
            startRecognizerListener();
            mSoundView.onCompleted();
        }
    };

    @Override
    public void onSpeakBegin() {
//        stopRecognizerListener();
    }

    //**********************************************************************************************
    @Override
    public void onTranslate(String result) {

    }

    @Override
    public void onRecognResult(String result) {
        stopRecognizerListener();
        Print.e("停止监听");
        Print.e(result);

        TranficCalculator calculator = new TranficCalculator();

        SpecialType myType = calculator.specialLocal(result, new MoveStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.spakeMove(myType, result);
            return;
        }
        myType = calculator.specialLocal(result, new LogoutStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.logout();
            return;
        }
        myType = calculator.specialLocal(result, new MapStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.openMap();
            return;
        }
        myType = calculator.specialLocal(result, new StopStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.stopListener();
            return;
        }
        myType = calculator.specialLocal(result, new BackStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.back();
            return;
        }
        myType = calculator.specialLocal(result, new ArtificialStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.artificial();
            return;
        }
        myType = calculator.specialLocal(result, new FaceStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.face(myType, result);
            return;
        }
        myType = calculator.specialLocal(result, new ControlStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.control(myType, result);
            return;
        }
        mSoundView.refLocalPage(result);

    }

    @Override
    public void onErrInfo(int errorCode) {
        startRecognizerListener();
        Print.e("onRecognDown total error ：" + errorCode);
        /*switch (errorCode) {
            case 10118:
                startRecognizerListener();
                break;
            case 20006:
                startRecognizerListener();
                break;
            case 10114:
                startRecognizerListener();
                break;
            case 10108:
                Print.e("网络差");
                startRecognizerListener();
                break;
            case 20005:
                Print.e("本地暂无此命令词");
                startRecognizerListener();
                break;
            case 11201:
                Print.e("授权不足");
                mSoundView.showMsg("授权不足");
                break;
            case 23008:
                Print.e("语音长度超时");
                startRecognizerListener();
                break;
        }*/
    }

    @Override
    public void onRecognDown() {
        startRecognizerListener();
    }

    @Override
    public void onLevelSmall() {
        onCompleted();
    }
}
