package com.fanfan.robot.dagger.module;

import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Administrator on 2018/3/8/008.
 */

@Module
public class SerialModel {

    ISerialPresenter.ISerialView baseView;

    public SerialModel(ISerialPresenter.ISerialView baseView) {
        this.baseView = baseView;
    }

    @Provides
    public SerialPresenter providerSerial() {
        return new SerialPresenter(baseView);
    }
}
