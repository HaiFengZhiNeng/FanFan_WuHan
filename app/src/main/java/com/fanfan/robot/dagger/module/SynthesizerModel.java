package com.fanfan.robot.dagger.module;

import com.fanfan.novel.presenter.SynthesizerPresenter;
import com.fanfan.novel.presenter.ipresenter.ISynthesizerPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Administrator on 2018/3/8/008.
 */

@Module
public class SynthesizerModel {

    ISynthesizerPresenter.ITtsView baseView;

    public SynthesizerModel(ISynthesizerPresenter.ITtsView baseView) {
        this.baseView = baseView;
    }

    @Provides
    public SynthesizerPresenter providerSynthesizer() {
        return new SynthesizerPresenter(baseView);
    }
}
