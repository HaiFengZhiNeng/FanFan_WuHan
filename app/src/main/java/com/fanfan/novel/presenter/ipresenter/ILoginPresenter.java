package com.fanfan.novel.presenter.ipresenter;

import com.fanfan.novel.common.presenter.BasePresenter;
import com.fanfan.novel.common.presenter.BaseView;
import com.fanfan.novel.model.UserInfo;

/**
 * Created by android on 2017/12/25.
 */

public abstract class ILoginPresenter implements BasePresenter {

    private ILoginView mBaseView;

    public ILoginPresenter(ILoginView baseView) {
        mBaseView = baseView;
    }

    public abstract void doLogin(UserInfo info);

    public interface ILoginView extends BaseView {


        void loginSuccess();

        void loginFail(String errMsg);
    }

}
