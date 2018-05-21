package com.fanfan.novel.common.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.fanfan.novel.activity.SimpleCallActivity;
import com.fanfan.novel.activity.SplashActivity;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.base.BaseActivity;
import com.fanfan.novel.im.event.MessageEvent;
import com.fanfan.novel.im.init.StatusObservable;
import com.fanfan.novel.im.init.TlsBusiness;
import com.fanfan.novel.model.UserInfo;
import com.fanfan.novel.service.cache.UserInfoCache;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.robot.R;
import com.fanfan.robot.service.CallSerivice;
import com.seabreeze.log.Print;
import com.tencent.TIMUserStatusListener;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;

/**
 * Created by android on 2017/12/26.
 */

public abstract class IMBaseActivity extends BaseActivity implements ILVIncomingListener, TIMUserStatusListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusObservable.getInstance().addObserver(this);

        ILVCallManager.getInstance().init(new ILVCallConfig()
                .setAutoBusy(true));
        ILVCallManager.getInstance().addIncomingListener(this);//添加来电回调
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ILVCallManager.getInstance().removeIncomingListener(this);
        StatusObservable.getInstance().deleteObserver(this);
    }

    @Override
    public void onNewIncomingCall(final int callId, final int callType, final ILVIncomingNotification notification) {
        Print.e("视频来电 新的来电 : " + notification);
        callStop();
        if (Constants.isTrain) {
            Intent intent = new Intent(this, CallSerivice.class);
            intent.putExtra(CallSerivice.CALL_ID, callId);
            intent.putExtra(CallSerivice.CALL_TYPE, callType);
            intent.putExtra(CallSerivice.SENDER, notification.getSender());
            startService(intent);
        } else {
            SimpleCallActivity.newInstance(this, callId, callType, notification.getSender());
        }
    }

    protected void callStop() {

    }

    @Override
    public void onForceOffline() {
        onReceiveExitMsg();
    }

    @Override
    public void onUserSigExpired() {

    }

    private void onReceiveExitMsg() {
        DialogUtils.showBasicIconDialog(IMBaseActivity.this, R.mipmap.ic_logo, "登陆提示",
                "您的帐号已在其它地方登陆", "退出", "重新登陆",
                new DialogUtils.OnNiftyDialogListener() {
                    @Override
                    public void onClickLeft() {
                        LocalBroadcastManager.getInstance(IMBaseActivity.this).sendBroadcast(new Intent(Constants.NET_LOONGGG_EXITAPP));
                    }

                    @Override
                    public void onClickRight() {
                        logout();
                    }
                });
    }


    public void logout() {
        TlsBusiness.logout(UserInfo.getInstance().getIdentifier());
        UserInfoCache.clearCache(this);
        UserInfo.getInstance().setIdentifier(null);
        MessageEvent.getInstance().clear();
//        FriendshipInfo.getInstance().clear();
//        GroupInfo.getInstance().clear();
        Intent intent = new Intent(IMBaseActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

}
