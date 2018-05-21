package com.fanfan.novel.presenter.ipresenter;

import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;

/**
 * Created by android on 2017/12/20.
 */

public abstract class ILocalSoundPresenter implements BasePresenter {

    private ILocalSoundView mBaseView;

    public ILocalSoundPresenter(ILocalSoundView baseView) {
        mBaseView = baseView;
    }

    public abstract void initTts();

    public abstract void buildTts();

    public abstract void initIat();

    public abstract void buildIat();

    public abstract void stopTts();

    public abstract void doAnswer(String answer);

    public abstract void startRecognizerListener();

    public abstract void stopRecognizerListener();

    public abstract void stopAll();

    public abstract void stopHandler();

    public interface ILocalSoundView extends BaseView {

        /**
         * 移动
         *
         * @param type
         * @param result
         */
        void spakeMove(SpecialType type, String result);

        /**
         * 退出登陆
         */
        void logout();

        /**
         * 打开地图
         */
        void openMap();

        /**
         * 停止监听
         */
        void stopListener();

        /**
         * 返回
         */
        void back();

        /**
         * 人工客服
         */
        void artificial();

        /**
         * 人脸识别
         *
         * @param type
         * @param result
         */
        void face(SpecialType type, String result);

        /**
         * 上下页
         *
         * @param type
         * @param result
         */
        void control(SpecialType type, String result);

        /**
         * 普通
         *
         * @param result
         */
        void refLocalPage(String result);

        /**
         * 说话完成
         */
        void onCompleted();
    }
}
