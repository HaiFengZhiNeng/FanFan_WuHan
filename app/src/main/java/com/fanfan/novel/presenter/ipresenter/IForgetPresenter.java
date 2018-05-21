package com.fanfan.novel.presenter.ipresenter;

import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.novel.model.UserInfo;

/**
 * Created by android on 2017/12/26.
 */

public abstract class IForgetPresenter implements BasePresenter {


    private IForgetView mBaseView;

    public IForgetPresenter(IForgetView baseView) {
        mBaseView = baseView;
    }

    public abstract void doModify(UserInfo info);

    public interface IForgetView extends BaseView {


        void modifySuccess();

        void modifyFail(String errMsg);
    }


}
