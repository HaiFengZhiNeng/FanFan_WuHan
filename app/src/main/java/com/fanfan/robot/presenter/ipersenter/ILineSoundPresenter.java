package com.fanfan.robot.presenter.ipersenter;

import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.novel.model.VoiceBean;
import com.fanfan.novel.model.xf.service.Cookbook;
import com.fanfan.novel.model.xf.service.News;
import com.fanfan.novel.model.xf.service.Poetry;
import com.fanfan.novel.model.xf.service.englishEveryday.EnglishEveryday;
import com.fanfan.novel.model.xf.service.radio.Radio;
import com.fanfan.novel.model.xf.service.train.Train;

import java.util.List;

/**
 * Created by android on 2018/1/6.
 */

public abstract class ILineSoundPresenter implements BasePresenter {

    private ILineSoundView mBaseView;

    public ILineSoundPresenter(ILineSoundView baseView) {
        mBaseView = baseView;
    }

    public abstract void initIat();

    public abstract void initAiui();

    public abstract void buildIat();

    public abstract void startRecognizerListener();

    public abstract void stopRecognizerListener();

    public abstract void onlineResult(String result);

    public abstract void aiuiWriteText(String text);

    public abstract void stopVoice();

    public abstract void setSpeech(boolean speech);

    public abstract void setOpening(boolean isOpen);

    public interface ILineSoundView extends BaseView {


        void aiuiForLocal(String result);

        void doAiuiAnwer(String anwer);

        void refHomePage(VoiceBean voiceBean);

        void refHomePage(String question, String finalText);

        void refHomePage(String question, String finalText, String url);

        void refHomePage(String question, News news);

        void refHomePage(String question, Radio radio);

        void refHomePage(String question, Poetry poetry);

        void refHomePage(String question, Cookbook cookbook);

        void refHomePage(String question, EnglishEveryday englishEveryday);

        void special(String result, SpecialType type);

        void doCallPhone(String value);

        void train(List<Train> trains);

        void startPage(SpecialType specialType);

        void spakeMove(SpecialType specialType, String result);

        void openMap();

        void openVr();

        void spakeLogout();

        void onSpeakBegin(String s);

        void onCompleted();

        void noAnswer(String question);
/*
        void  timeTest();*/
    }


}
