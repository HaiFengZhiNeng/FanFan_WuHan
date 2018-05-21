package com.fanfan.robot.presenter.ipersenter;

import android.graphics.Bitmap;

import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.youtu.api.base.event.BaseEvent;
import com.fanfan.youtu.api.face.bean.FaceIdentify;

/**
 * Created by android on 2018/1/10.
 */

public abstract class IFaceVerifPresenter implements BasePresenter {

    private IFaceverifView mBaseView;

    public IFaceVerifPresenter(IFaceverifView baseView) {
        mBaseView = baseView;
    }

    public abstract Bitmap bitmapSaturation(Bitmap baseBitmap);

    public abstract void faceCompare(Bitmap bitmapA, Bitmap bitmapB);

    public interface IFaceverifView extends BaseView {

        void onError(BaseEvent event);

        void onError(int code, String msg);

        void compareSuccess();

        void similarityLow(float similarity);

        void faceCompare(Bitmap bitmap);
    }
}
