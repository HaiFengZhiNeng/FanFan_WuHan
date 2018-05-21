package com.fanfan.novel.presenter.ipresenter;

import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.novel.model.SerialBean;
import com.fanfan.robot.model.Alarm;

/**
 * Created by android on 2017/12/26.
 */

public abstract class ISerialPresenter implements BasePresenter {

    private ISerialView mBaseView;

    public ISerialPresenter(ISerialView baseView) {
        mBaseView = baseView;
    }

    public abstract void receiveMotion(int type, String motion);

    public abstract void onDataReceiverd(SerialBean serialBean);

    public interface ISerialView extends BaseView {

        void stopAll();

        void onMoveStop();

        void onAlarm(Alarm alarm);
    }
}
