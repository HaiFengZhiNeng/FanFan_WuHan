package com.fanfan.robot.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.model.hotword.HotWord;
import com.fanfan.novel.model.hotword.Userword;
import com.fanfan.novel.model.xf.Telephone;
import com.fanfan.novel.model.xf.service.Cookbook;
import com.fanfan.novel.model.xf.service.Flight;
import com.fanfan.novel.model.xf.service.Joke;
import com.fanfan.novel.model.xf.service.News;
import com.fanfan.novel.model.xf.service.Poetry;
import com.fanfan.novel.model.xf.service.cmd.Slots;
import com.fanfan.novel.model.xf.service.constellation.Constellation;
import com.fanfan.novel.model.xf.service.constellation.Fortune;
import com.fanfan.novel.model.xf.service.englishEveryday.EnglishEveryday;
import com.fanfan.novel.model.xf.service.radio.Radio;
import com.fanfan.novel.model.xf.service.riddle.Riddle;
import com.fanfan.novel.model.xf.service.stock.Detail;
import com.fanfan.novel.model.xf.service.stock.Stock;
import com.fanfan.novel.model.xf.service.story.Story;
import com.fanfan.novel.model.xf.service.train.Train;
import com.fanfan.novel.model.xf.service.wordFinding.WordFinding;
import com.fanfan.novel.service.listener.AiuiListener;
import com.fanfan.novel.service.listener.IatListener;
import com.fanfan.novel.utils.AppUtil;
import com.fanfan.novel.utils.AudioUtil;
import com.fanfan.novel.utils.FileUtil;
import com.fanfan.novel.utils.FucUtil;
import com.fanfan.novel.utils.SpecialUtils;
import com.fanfan.novel.utils.music.MediaPlayerUtil2;
import com.fanfan.novel.utils.tele.TelNumMatch;
import com.fanfan.novel.utils.tele.TelePhoneUtils;
import com.fanfan.novel.utils.youdao.TranslateData;
import com.fanfan.robot.app.RobotInfo;
import com.fanfan.robot.presenter.ipersenter.ILineSoundPresenter;
import com.fanfan.youtu.utils.GsonUtil;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;
import com.youdao.sdk.app.Language;
import com.youdao.sdk.app.LanguageUtils;
import com.youdao.sdk.ydonlinetranslate.TranslateErrorCode;
import com.youdao.sdk.ydonlinetranslate.TranslateListener;
import com.youdao.sdk.ydonlinetranslate.TranslateParameters;
import com.youdao.sdk.ydonlinetranslate.Translator;
import com.youdao.sdk.ydtranslate.Translate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by android on 2017/12/18.
 */

public class LineSoundPresenter extends ILineSoundPresenter implements IatListener.RecognListener,
        AiuiListener.AiListener {

    private static final String GRAMMAR_BNF = "bnf";
    private static final String GRAMMAR_ABNF = "abnf";

    private static final String LOCAL_GRAMMAR_NAME = "local";
    private static final String GRAMMAR_LOCAL_FILE_NAME = "local.bnf";
    private static final String GRAMMAR_CLOUD_FILE_NAME = "abnf.abnf";

    private static final String STANDARD_TEXT_ENCODING = "utf-8";

    private static final String ASSESTS_AIUI_CFG = "cfg/aiui_phone.cfg";

    private ILineSoundView mSoundView;

    private SpeechRecognizer mIat;
    private AIUIAgent mAIUIAgent;

    private IatListener mIatListener;
    private AiuiListener aiuiListener;

    private boolean isMedia;
    private boolean isTrans;

    private String mOtherText;

    private boolean isOpening;

    public LineSoundPresenter(ILineSoundView baseView) {
        super(baseView);
        mSoundView = baseView;

        mIatListener = new IatListener(this);
        aiuiListener = new AiuiListener((Activity) mSoundView.getContext(), this);


    }

    @Override
    public void start() {
        initAiui();
        initIat();
        isMedia = true;
    }

    @Override
    public void finish() {
        if (mAIUIAgent != null) {
            mAIUIAgent.destroy();
        }
        if (mIat != null) {
            mIat.cancel();
        }
        aiuiListener = null;
        mIatListener = null;
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
    public void initAiui() {
        String params = FucUtil.readAssets(mSoundView.getContext(), ASSESTS_AIUI_CFG);
        mAIUIAgent = AIUIAgent.createAgent(mSoundView.getContext(), params, aiuiListener);
        AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
        mAIUIAgent.sendMessage(startMsg);
    }


    @Override
    public void buildIat() {
        if (mIat == null) {
            initIat();
        }
        if (RobotInfo.getInstance().isInitialization()) {
            startRecognizerListener();
        } else {
            structure();
        }
    }

    private void structure() {
        String grammarType;
        String content;
        if (RobotInfo.getInstance().isCloudBuild()) {
            RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_LOCAL);
            if (!RobotInfo.getInstance().isLocalBuild()) {
                mIat.setParameter(SpeechConstant.PARAMS, null);
                mIat.setParameter(SpeechConstant.ENGINE_TYPE, RobotInfo.getInstance().getEngineType());
                mIat.setParameter(SpeechConstant.TEXT_ENCODING, STANDARD_TEXT_ENCODING);
                FileUtil.mkdir(Constants.GRM_PATH);
                mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, Constants.GRM_PATH);
                mIat.setParameter(ResourceUtil.ASR_RES_PATH, FucUtil.getResAsrPath(mSoundView.getContext()));
                mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, LOCAL_GRAMMAR_NAME);
                mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
                content = FucUtil.readFile(mSoundView.getContext(), GRAMMAR_LOCAL_FILE_NAME, STANDARD_TEXT_ENCODING);
                grammarType = GRAMMAR_BNF;
                mIat.buildGrammar(grammarType, content, mGrammarListener);
            }

        } else {
            mIat.setParameter(SpeechConstant.PARAMS, null);
            mIat.setParameter(SpeechConstant.ENGINE_TYPE, RobotInfo.getInstance().getEngineType());
            mIat.setParameter(SpeechConstant.TEXT_ENCODING, STANDARD_TEXT_ENCODING);
            content = FucUtil.readFile(mSoundView.getContext(), GRAMMAR_CLOUD_FILE_NAME, STANDARD_TEXT_ENCODING);
            grammarType = GRAMMAR_ABNF;
            mIat.buildGrammar(grammarType, content, mGrammarListener);
        }
        if (RobotInfo.getInstance().isCloudBuild() && RobotInfo.getInstance().isLocalBuild() &&
                RobotInfo.getInstance().isCloudUpdatelexicon() && RobotInfo.getInstance().isLocalUpdatelexicon()) {
            RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_CLOUD);
            RobotInfo.getInstance().setInitialization(true);
            buildIat();
        }
    }

    private void updateLocation(String lexiconName, String lexiconContents) {
        mIat.setParameter(SpeechConstant.PARAMS, null);
        if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_CLOUD)) {
            mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        } else if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_LOCAL)) {
            mIat.setParameter(SpeechConstant.ENGINE_TYPE, RobotInfo.getInstance().getEngineType());
            mIat.setParameter(ResourceUtil.ASR_RES_PATH, FucUtil.getResAsrPath(mSoundView.getContext()));
            mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, Constants.GRM_PATH);
            mIat.setParameter(SpeechConstant.GRAMMAR_LIST, LOCAL_GRAMMAR_NAME);
            mIat.setParameter(SpeechConstant.TEXT_ENCODING, STANDARD_TEXT_ENCODING);
        }
        mIat.updateLexicon(lexiconName, lexiconContents, mLexiconListener);
    }

    @Override
    public void startRecognizerListener() {
        if (isOpening) {
            setIatparameter();
            mIat.startListening(mIatListener);
            Print.e("startListening ...");
        }
    }

    private void setIatparameter() {
        if (mIat == null) {
            return;
        }
        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, RobotInfo.getInstance().getEngineType());
        //麦克风阵列开启远场识别
        mIat. setParameter("domain","fariat");
        mIat. setParameter("aue","speex-wb;10");


        if (!RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_CLOUD)) {
            if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_LOCAL)) {
                mIat.setParameter(ResourceUtil.ASR_RES_PATH, FucUtil.getResAsrPath(mSoundView.getContext()));
                mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, Constants.GRM_PATH);
                mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, LOCAL_GRAMMAR_NAME);
                mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
            }
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        if (RobotInfo.getInstance().isTranslateEnable()) {
            mIat.setParameter(SpeechConstant.ASR_SCH, "1");
            mIat.setParameter(SpeechConstant.ADD_CAP, "translate");
            mIat.setParameter(SpeechConstant.TRS_SRC, "its");
        }

        mIat.setParameter(SpeechConstant.LANGUAGE, RobotInfo.getInstance().getLineLanguage());
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, RobotInfo.getInstance().getIatLineLanguage());

        //英语转中文     是中文不用转
        if (RobotInfo.getInstance().isTranslateEnable()) {
            mIat.setParameter(SpeechConstant.ORI_LANG, "en");
            mIat.setParameter(SpeechConstant.TRANS_LANG, "cn");
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Constants.GRM_PATH + File.separator + "iat.wav");
        Print.e("initIat success ...");
    }

    @Override
    public void stopRecognizerListener() {
        if (mIat != null) {
            mIat.startListening(null);
            mIat.stopListening();
            Print.e("stopListening ...");
        }
    }

    @Override
    public void onlineResult(String result) {
        if (result.length() == 1) {
            mSoundView.onCompleted();
            return;
        }
        SpecialType specialType = SpecialUtils.doesExist(mSoundView.getContext().getResources(), result);
        if (specialType == SpecialType.NoSpecial) {
            aiuiWriteText(result);
        } else if (specialType == SpecialType.Music) {
            mSoundView.special(result, SpecialType.Music);
        } else if (specialType == SpecialType.Dance) {
            mSoundView.special(result, SpecialType.Dance);
        } else if (specialType == SpecialType.Hand) {
            mSoundView.special(result, SpecialType.Hand);
        } else if (specialType == SpecialType.Story) {
            aiuiWriteText(result);
        } else if (specialType == SpecialType.Joke) {
            aiuiWriteText(result);
        } else if (specialType == SpecialType.StopListener) {
            setSpeech(false);
        } else if (specialType == SpecialType.Fanfan || specialType == SpecialType.Video
                || specialType == SpecialType.Problem || specialType == SpecialType.Face
                || specialType == SpecialType.Seting_up || specialType == SpecialType.Public_num
                || specialType == SpecialType.Navigation || specialType == SpecialType.MultiMedia
                || specialType == SpecialType.TrainInquiry || specialType == SpecialType.PanoramicMap
                || specialType == SpecialType.TalkBack || specialType == SpecialType.StationService
                || specialType == SpecialType.InternalNavigation || specialType == SpecialType.TrafficTravel) {
            mSoundView.startPage(specialType);
        } else if (specialType == SpecialType.Forward || specialType == SpecialType.Backoff ||
                specialType == SpecialType.Turnleft || specialType == SpecialType.Turnright|| specialType == SpecialType.StartMove|| specialType == SpecialType.StopMove) {
            mSoundView.spakeMove(specialType, result);
        } else if (specialType == SpecialType.Map) {
            mSoundView.openMap();
        } else if (specialType == SpecialType.Vr) {
            mSoundView.openVr();
        } else if (specialType == SpecialType.Logout) {
            mSoundView.spakeLogout();
        }
    }

    @Override
    public void aiuiWriteText(String result) {
        String params = "data_type=text";
        AIUIMessage msgWakeup = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null, null);
        mAIUIAgent.sendMessage(msgWakeup);
        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, result.trim().getBytes());
        mAIUIAgent.sendMessage(msg);
    }


    @Override
    public void stopVoice() {
        MediaPlayerUtil2.getInstance().stopMusic();
    }


    @Override
    public void setSpeech(boolean speech) {
        if (speech) {
            startRecognizerListener();
        } else {
            stopRecognizerListener();
        }
    }

    @Override
    public void setOpening(boolean isOpen) {
        isOpening = isOpen;
    }


    //**********************************************************************************************
    @Override
    public void onTranslate(String result) {
        Print.e("!!!!---- " + result);
        stopRecognizerListener();
        mSoundView.aiuiForLocal(result);
    }

    @Override
    public void onRecognResult(String result) {
       // mSoundView.timeTest();
        Print.e("!!!!---- " + result);
        stopRecognizerListener();
        mSoundView.aiuiForLocal(result);
    }

    @Override
    public void onErrInfo(int errorCode) {
        Print.e("onRecognDown total error ：" + errorCode);
        startRecognizerListener();
      /*  switch (errorCode) {

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
            case 12404:
                startRecognizerListener();
                break;
            case 10700:
                startRecognizerListener();
                break;
            case 23008:
                Print.e("语音长度超时");
                startRecognizerListener();
                break;
            case 20002:
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

    }

    //**********************************************************************************************

    @Override
    public void onDoAnswer(String question, String finalText) {
        if (finalText == null) {
            onCompleted();
            mSoundView.refHomePage(question, finalText);
        } else {
            if (RobotInfo.getInstance().isQueryLanage()) {
                if (isTrans) {
                    isTrans = false;
                    mSoundView.doAiuiAnwer(finalText);
                    mSoundView.refHomePage(question, finalText);
                } else {
                    query(finalText);
                }
            } else {
                mSoundView.doAiuiAnwer(finalText);
                mSoundView.refHomePage(question, finalText);
            }
        }
    }

    @Override
    public void onDoAnswer(String question, String text, News news) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                if (isMedia) {
                    playVoice(news.getUrl());
                    mSoundView.refHomePage(question, news);
                } else {
                    mSoundView.doAiuiAnwer(text + ", " + news.getContent());
                }
            } else {
                query(news.getContent());
            }
        } else {
            if (isMedia) {
                playVoice(news.getUrl());
                mSoundView.refHomePage(question, news);
            } else {
                mSoundView.doAiuiAnwer(text + ", " + news.getContent());
            }
        }
    }

    @Override
    public void onDoAnswer(String question, String text, Cookbook cookbook) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(text + ", " + cookbook.getSteps());
                mSoundView.refHomePage(question, cookbook);
            } else {
                query(cookbook.getSteps());
            }
        } else {
            mSoundView.doAiuiAnwer(text + ", " + cookbook.getSteps());
            mSoundView.refHomePage(question, cookbook);
        }
    }

    @Override
    public void onDoAnswer(String question, Poetry poetry) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(poetry.getContent());
                mSoundView.refHomePage(question, poetry);
            } else {
                query(poetry.getContent());
            }
        } else {
            mSoundView.doAiuiAnwer(poetry.getContent());
            mSoundView.refHomePage(question, poetry);
        }
    }

    @Override
    public void onDoAnswer(String question, String finalText, Joke joke) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                if (isMedia) {
                    if (TextUtils.isEmpty(joke.getMp3Url())) {
                        mSoundView.doAiuiAnwer(joke.getTitle() + " : " + joke.getContent());
                        mSoundView.refHomePage(question, joke.getTitle() + " : " + joke.getContent());
                    } else {
                        mSoundView.refHomePage(question, finalText);
                        playVoice(joke.getMp3Url());
                    }
                } else {
                    stopRecognizerListener();
                    mSoundView.special(question, SpecialType.Joke);
                }
            } else {
                query(finalText);
            }
        } else {
            if (isMedia) {
                if (TextUtils.isEmpty(joke.getMp3Url())) {
                    mSoundView.doAiuiAnwer(joke.getTitle() + " : " + joke.getContent());
                    mSoundView.refHomePage(question, joke.getTitle() + " : " + joke.getContent());
                } else {
                    mSoundView.refHomePage(question, finalText);
                    playVoice(joke.getMp3Url());
                }
            } else {
                stopRecognizerListener();
                mSoundView.special(question, SpecialType.Joke);
            }
        }
    }

    @Override
    public void onDoAnswer(String question, String finalText, Story story) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                if (isMedia) {
                    mSoundView.refHomePage(question, finalText);
                    playVoice(story.getPlayUrl());
                } else {
                    stopRecognizerListener();
                    mSoundView.special(question, SpecialType.Joke);
                }
            } else {
                query(finalText);
            }
        } else {
            if (isMedia) {
                mSoundView.refHomePage(question, finalText);
                playVoice(story.getPlayUrl());
            } else {
                stopRecognizerListener();
                mSoundView.special(question, SpecialType.Joke);
            }
        }
    }

    @Override
    public void onDoAnswer(String question, String
            finalText, List<Train> trains, Train train0) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(finalText);
                mSoundView.refHomePage(question, finalText);
                if (Constants.isTrain) {
                    mSoundView.train(trains);
                }
                for (int i = 0; i < trains.size(); i++) {
                    Train train = trains.get(i);
                    mSoundView.refHomePage(null, train.getEndtime_for_voice() + "的" + train.getTrainType() + " " + train.getTrainNo() + "" +
                            " " + train.getOriginStation() + " -- " + train.getTerminalStation()
                            + " , 运行时间：" + train.getRunTime());
                }
            } else {
                query(finalText);
            }
        } else {
            mSoundView.doAiuiAnwer(finalText);
            mSoundView.refHomePage(question, finalText);
            if (Constants.isTrain) {
                mSoundView.train(trains);
            }
            for (int i = 0; i < trains.size(); i++) {
                Train train = trains.get(i);
                mSoundView.refHomePage(null, train.getEndtime_for_voice() + "的" + train.getTrainType() + " " + train.getTrainNo() + "" +
                        " " + train.getOriginStation() + " -- " + train.getTerminalStation()
                        + " , 运行时间：" + train.getRunTime());
            }
        }
    }

    @Override
    public void onDoAnswer(String question, String finalText, List<Flight> flights, Flight flight0) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(finalText);
                mSoundView.refHomePage(question, finalText);
                int total;
                if (flights.size() < 10) {
                    total = flights.size();
                } else {
                    total = 10;
                }
                for (int i = 0; i < total; i++) {
                    Flight flight = flights.get(i);
                    mSoundView.refHomePage(null, flight.getEndtime_for_voice() + "从" + flight.getDepartCity() + "出发， "
                            + flight.getEndtime_for_voice() + "到达" + flight.getArriveCity() + ", " +
                            flight.getCabinInfo() + "价格是：" + flight.getPrice());
                }
            } else {
                query(finalText);
            }
        } else {
            mSoundView.doAiuiAnwer(finalText);
            mSoundView.refHomePage(question, finalText);
            int total;
            if (flights.size() < 10) {
                total = flights.size();
            } else {
                total = 10;
            }
            for (int i = 0; i < total; i++) {
                Flight flight = flights.get(i);
                mSoundView.refHomePage(null, flight.getEndtime_for_voice() + "从" + flight.getDepartCity() + "出发， "
                        + flight.getEndtime_for_voice() + "到达" + flight.getArriveCity() + ", " +
                        flight.getCabinInfo() + "价格是：" + flight.getPrice());
            }
        }
    }

    @Override
    public void onNoAnswer(String question, String finalText, String otherText) {
        mOtherText = otherText;
        onDoAnswer(question, finalText);
    }

    @Override
    public void onDoAnswer(String question, String finalText, Radio radio) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                if (isMedia) {
                    mSoundView.refHomePage(question, radio);
                    playVoice(radio.getUrl());
                } else {
                    stopRecognizerListener();
                    mSoundView.special(question, SpecialType.Joke);
                }
            } else {
                query(finalText);
            }
        } else {
            if (isMedia) {
                mSoundView.refHomePage(question, radio);
                playVoice(radio.getUrl());
            } else {
                stopRecognizerListener();
                mSoundView.special(question, SpecialType.Joke);
            }
        }
    }

    @Override
    public void onMusic(String question, String finalText) {
        mOtherText = "音乐播放中...";
        onDoAnswer(question, finalText);
    }

    @Override
    public void onTranslation(String question, String value) {
        onDoAnswer(question, value);
        Print.e(value);
    }

    @Override
    public void onDoAnswer(String question, Slots slotsCmd) {
        int volume = AudioUtil.getInstance(mSoundView.getContext()).getMediaVolume();
        int maxVolume = AudioUtil.getInstance(mSoundView.getContext()).getMediaMaxVolume();
        int node = maxVolume / 5;
        String answer = "不支持此音量控制";
        if (slotsCmd.getName().equals("insType")) {
            switch (slotsCmd.getValue()) {
                case "volume_plus":
                    if (volume == maxVolume) {
                        answer = "当前已是最大音量了";
                    } else {
                        answer = "已增大音量";
                        volume = volume + node;
                        if (volume > maxVolume) {
                            volume = maxVolume;
                        }
                        AudioUtil.getInstance(mSoundView.getContext()).setMediaVolume(volume);
                    }
                    break;
                case "volume_minus":
                    if (volume == 0) {
                        answer = "当前已是最小音量了";
                    } else {
                        answer = "已减小音量";
                        volume = volume - node;
                        if (volume < 0) {
                            volume = 0;
                        }
                        AudioUtil.getInstance(mSoundView.getContext()).setMediaVolume(volume);
                    }
                    break;
                case "unmute":
                    answer = "您可以说 “增大音量” 或 “减小音量” ，我会帮您改变的";
                    break;
            }
        }

        onDoAnswer(question, answer);
    }

    @Override
    public void onDoAnswer(String question, String
            finalText, EnglishEveryday englishEveryday) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(englishEveryday.getContent());
                mSoundView.refHomePage(question, englishEveryday);
            } else {
                query(finalText);
            }
        } else {
            mSoundView.doAiuiAnwer(englishEveryday.getContent());
            mSoundView.refHomePage(question, englishEveryday);
        }
    }

    @Override
    public void onDoAnswer(String question, String
            finalText, Constellation constellation) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                StringBuilder sb = new StringBuilder();
                List<Fortune> fortunes = constellation.getFortune();
                sb.append(finalText);
                for (int i = 0; i < fortunes.size(); i++) {
                    Fortune fortune = fortunes.get(i);
                    sb.append(fortune.getName()).append(" : ").append(fortune.getDescription());
                }
                mSoundView.doAiuiAnwer(sb.toString());
                mSoundView.refHomePage(question, sb.toString());
            } else {
                query(finalText);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            List<Fortune> fortunes = constellation.getFortune();
            sb.append(finalText);
            for (int i = 0; i < fortunes.size(); i++) {
                Fortune fortune = fortunes.get(i);
                sb.append(fortune.getName()).append(" : ").append(fortune.getDescription());
            }
            mSoundView.doAiuiAnwer(sb.toString());
            mSoundView.refHomePage(question, sb.toString());
        }
    }

    @Override
    public void onDoAnswer(String question, String finalText, Stock
            stock) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                StringBuilder sb = new StringBuilder();
                sb.append(finalText);
                sb.append("\n截止到").append(stock.getUpdateDateTime()).append(", ").append(stock.getName()).append(" ")
                        .append(stock.getStockCode()).append(", 当前价格为 ： ").append(stock.getOpeningPrice()).append(", 上升率为 ： ")
                        .append(stock.getRiseRate()).append(" 详情请查看列表信息");
                mSoundView.doAiuiAnwer(sb.toString());

                sb.append("\n最高价 ： ").append(stock.getHighPrice());
                sb.append("  最低价 ： ").append(stock.getLowPrice());
                List<Detail> details = stock.getDetail();
                for (int i = 0; i < details.size(); i++) {
                    Detail detail = details.get(i);
                    sb.append("\n").append(detail.getCount()).append(" ").append(detail.getRole()).append(" ").append(detail.getPrice());
                }
                mSoundView.refHomePage(question, sb.toString());
            } else {
                query(finalText);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(finalText);
            sb.append("\n截止到").append(stock.getUpdateDateTime()).append(", ").append(stock.getName()).append(" ")
                    .append(stock.getStockCode()).append(", 当前价格为 ： ").append(stock.getOpeningPrice()).append(", 上升率为 ： ")
                    .append(stock.getRiseRate()).append(" 详情请查看列表信息");
            mSoundView.doAiuiAnwer(sb.toString());

            sb.append("\n最高价 ： ").append(stock.getHighPrice());
            sb.append("  最低价 ： ").append(stock.getLowPrice());
            List<Detail> details = stock.getDetail();
            for (int i = 0; i < details.size(); i++) {
                Detail detail = details.get(i);
                sb.append("\n").append(detail.getCount()).append(" ").append(detail.getRole()).append(" ").append(detail.getPrice());
            }
            mSoundView.refHomePage(question, sb.toString());
        }
    }

    @Override
    public void onDoAnswer(String question, String
            finalText, Riddle riddle) {
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(riddle.getTitle() + "\n谜底请查看列表");
                mSoundView.refHomePage(question, riddle.getTitle() + "\n\n" + riddle.getAnswer() + "\n");
            } else {
                query(finalText);
            }
        } else {
            mSoundView.doAiuiAnwer(riddle.getTitle() + "\n谜底请查看列表");
            mSoundView.refHomePage(question, riddle.getTitle() + "\n\n" + riddle.getAnswer() + "\n");
        }
    }

    @Override
    public void onDoAnswer(String question, String
            finalText, WordFinding wordFinding) {
        List<String> results;
        int count = 5;
        StringBuilder sb = new StringBuilder();
        if (finalText.contains("反义词")) {
            results = wordFinding.getAntonym();
        } else {
            results = wordFinding.getSynonym();
        }
        if (results.size() > count) {
            int random = new Random().nextInt(results.size() - count);
            for (int i = 0; i < count; i++) {
                sb.append("\n").append(results.get(random + i));
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                sb.append("\n").append(results.get(i));
            }
        }
        if (RobotInfo.getInstance().isQueryLanage()) {
            if (isTrans) {
                isTrans = false;
                mSoundView.doAiuiAnwer(sb.toString());
                mSoundView.refHomePage(question, sb.toString());
            } else {
                query(finalText);
            }
        } else {
            mSoundView.doAiuiAnwer(sb.toString());
            mSoundView.refHomePage(question, sb.toString());
        }
    }

    @Override
    public void onDoDial(String question, String value) {
        if (TelNumMatch.matchNum(value) == 5 || TelNumMatch.matchNum(value) == 4) {
            List<Telephone> telephones = TelePhoneUtils.queryContacts(mSoundView.getContext(), value);
            if (telephones != null && telephones.size() > 0) {
                if (telephones.size() == 1) {
                    List<String> phones = telephones.get(0).getPhone();
                    if (phones != null && phones.size() > 0) {
                        if (phones.size() == 1) {
                            String phoneNumber = phones.get(0);
                            mSoundView.doAiuiAnwer("为您拨打 ： " + phoneNumber);
//                            mSoundView.refHomePage(question, "为您拨打 ： " + phoneNumber);
                            mSoundView.doCallPhone(phoneNumber);
                        } else {
                            mSoundView.doAiuiAnwer("为您找到如下号码 ： ");
                            mSoundView.refHomePage(question, "为您找到如下号码 ： ");
                            for (String phone : phones) {
                                mSoundView.refHomePage(null, phone);
                            }
                        }
                    } else {
                        mSoundView.doAiuiAnwer("暂无此名字电话号码");
                        mSoundView.refHomePage(question, "通讯录中暂无");
                    }
                } else {
                    mSoundView.doAiuiAnwer("为您匹配到如下姓名 ： ");
                    mSoundView.refHomePage(question, "为您匹配到如下姓名 ： ");
                    for (Telephone telephone : telephones) {
                        mSoundView.refHomePage(null, telephone.getName());
                    }
                }
            } else {
                mSoundView.doAiuiAnwer("通讯录中暂无" + value);
                mSoundView.refHomePage(question, "通讯录中暂无" + value);
            }
        } else {
            mSoundView.doAiuiAnwer("为您拨打 ： " + value);
            mSoundView.doCallPhone(value);
        }
    }

    @Override
    public void onError() {
        initAiui();
    }

    @Override
    public void onAIUIDowm() {

    }

    @Override
    public void onNoAnswer(String question) {
        Print.e("noAnswer : " + question);
        mSoundView.noAnswer(question);
    }

    private void onCompleted() {
        mSoundView.onCompleted();
    }

    private void query(final String source) {
        Language langFrom = LanguageUtils.getLangByName("中文");
        Language langTo = LanguageUtils.getLangByName("英文");

        TranslateParameters tps = new TranslateParameters.Builder()
                .source("youdao").from(langFrom).to(langTo).timeout(3000).build();// appkey可以省略

        Translator translator = Translator.getInstance(tps);

        translator.lookup(source, new TranslateListener() {

            @Override
            public void onResult(Translate result, String input) {
                TranslateData translateData = new TranslateData(System.currentTimeMillis(), result);
                isTrans = true;
                onDoAnswer(source, translateData.translates());
            }

            @Override
            public void onError(TranslateErrorCode error) {
            }
        });
    }

    private void playVoice(String url) {
        if (TextUtils.isEmpty(url))
            return;

        MediaPlayerUtil2.getInstance().setListener(new MediaPlayerUtil2.OnMusicCompletionListener() {
            @Override
            public void onFastForwarding() {
                onCompleted();
            }
        });
        MediaPlayerUtil2.getInstance().playMusic( url);
    }

    private GrammarListener mGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_LOCAL)) {
                    if (RobotInfo.getInstance().isLocalUpdatelexicon()) {
                        RobotInfo.getInstance().setLocalBuild();
                        structure();
                    } else {
                        String lexiconContents = AppUtil.words2Contents();
                        updateLocation("voice", lexiconContents);
                    }
                } else if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_CLOUD)) {

                    if (!RobotInfo.getInstance().isCloudUpdatelexicon()) {
                        List<String> words = AppUtil.getLocalStrings();

                        Userword userword = new Userword();
                        userword.setName("userword");
                        userword.setWords(words);
                        List<Userword> userwordList = new ArrayList<>();
                        userwordList.add(userword);
                        HotWord hotWord = new HotWord(userwordList);

                        updateLocation("userword", GsonUtil.GsonString(hotWord));
                    } else {
                        RobotInfo.getInstance().setCloudBuild();
                        structure();
                    }
                }
            } else {
                Print.e("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };


    private LexiconListener mLexiconListener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String s, SpeechError error) {
            if (error == null) {

                if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_CLOUD)) {
                    RobotInfo.getInstance().setCloudBuild();
                    RobotInfo.getInstance().setCloudUpdatelexicon();
                    structure();
                } else if (RobotInfo.getInstance().getEngineType().equals(SpeechConstant.TYPE_LOCAL)) {
                    RobotInfo.getInstance().setLocalBuild();
                    RobotInfo.getInstance().setLocalUpdatelexicon();
                    structure();
                }
            } else {
                Print.e("词典更新失败,错误码：" + error.getErrorCode());
            }
        }
    };
}
