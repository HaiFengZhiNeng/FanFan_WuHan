package com.fanfan.robot.dagger.module;

import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.novel.presenter.ChatPresenter;
import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.SynthesizerPresenter;
import com.fanfan.novel.presenter.ipresenter.IChatPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.presenter.ipresenter.ISynthesizerPresenter;
import com.fanfan.robot.dagger.manager.MainManager;
import com.fanfan.robot.presenter.LineSoundPresenter;
import com.fanfan.robot.presenter.ipersenter.ILineSoundPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Administrator on 2018/3/8/008.
 */

@Module
public class MainModule {

    BaseView baseView;

    public MainModule(BaseView baseView) {
        this.baseView = baseView;
    }

    @Provides
    public ChatPresenter providerChat() {
        return new ChatPresenter((IChatPresenter.IChatView) baseView);
    }

    @Provides
    public LineSoundPresenter providerLineSound() {
        return new LineSoundPresenter((ILineSoundPresenter.ILineSoundView) baseView);
    }

    @Provides
    public SerialPresenter providerSerial() {
        return new SerialPresenter((ISerialPresenter.ISerialView) baseView);
    }

    @Provides
    public SynthesizerPresenter providerSynthesizer() {
        return new SynthesizerPresenter((ISynthesizerPresenter.ITtsView) baseView);
    }

    @Provides
    public MainManager provideMainManager(ChatPresenter chatPresenter, SerialPresenter serialPresenter,
                                          SynthesizerPresenter synthesizerPresenter,
                                          LineSoundPresenter lineSoundPresenter) {
        return new MainManager(chatPresenter, serialPresenter, synthesizerPresenter, lineSoundPresenter);
    }

}
