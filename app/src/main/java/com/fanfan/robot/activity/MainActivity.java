package com.fanfan.robot.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fanfan.novel.activity.DanceActivity;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.RobotType;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.db.manager.VoiceDBManager;
import com.fanfan.novel.im.init.LoginBusiness;
import com.fanfan.novel.map.activity.AMapActivity;
import com.fanfan.novel.model.RobotBean;
import com.fanfan.novel.model.SerialBean;
import com.fanfan.novel.model.UserInfo;
import com.fanfan.novel.model.VoiceBean;
import com.fanfan.novel.model.xf.service.Cookbook;
import com.fanfan.novel.model.xf.service.News;
import com.fanfan.novel.model.xf.service.Poetry;
import com.fanfan.novel.model.xf.service.englishEveryday.EnglishEveryday;
import com.fanfan.novel.model.xf.service.radio.Radio;
import com.fanfan.novel.model.xf.service.train.Train;
import com.fanfan.novel.pointdown.db.DownloadDBDao;
import com.fanfan.novel.pointdown.model.Progress;
import com.fanfan.novel.presenter.ipresenter.IChatPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.presenter.ipresenter.ISynthesizerPresenter;
import com.fanfan.novel.service.LoadFileService;
import com.fanfan.novel.service.PlayService;
import com.fanfan.novel.service.SerialService;
import com.fanfan.novel.service.UdpService;
import com.fanfan.novel.service.cache.LoadFileCache;
import com.fanfan.novel.service.cache.MusicCache;
import com.fanfan.novel.service.event.ReceiveEvent;
import com.fanfan.novel.service.event.ServiceToActivityEvent;
import com.fanfan.novel.service.music.EventCallback;
import com.fanfan.novel.service.udp.SocketManager;
import com.fanfan.novel.ui.ChatTextView;
import com.fanfan.novel.utils.AppUtil;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.novel.utils.FileUtil;
import com.fanfan.novel.utils.ImageLoader;
import com.fanfan.novel.utils.PreferencesUtils;
import com.fanfan.novel.utils.customtabs.IntentUtil;
import com.fanfan.robot.R;
import com.fanfan.robot.app.RobotInfo;
import com.fanfan.robot.dagger.componet.DaggerMainComponet;
import com.fanfan.robot.dagger.manager.MainManager;
import com.fanfan.robot.dagger.module.MainModule;
import com.fanfan.robot.db.DanceDBManager;
import com.fanfan.robot.model.Alarm;
import com.fanfan.robot.model.Dance;
import com.fanfan.robot.presenter.ipersenter.ILineSoundPresenter;
import com.fanfan.robot.train.PanoramicMapActivity;
import com.fanfan.youtu.Youtucode;
import com.fanfan.youtu.api.base.Constant;
import com.fanfan.youtu.api.hfrobot.bean.Check;
import com.fanfan.youtu.api.hfrobot.bean.RequestProblem;
import com.fanfan.youtu.api.hfrobot.bean.SetBean;
import com.fanfan.youtu.api.hfrobot.event.CheckEvent;
import com.fanfan.youtu.api.hfrobot.event.RequestProblemEvent;
import com.fanfan.youtu.api.hfrobot.event.SetEvent;
import com.fanfan.youtu.utils.GsonUtil;
import com.github.florent37.viewanimator.ViewAnimator;
import com.iflytek.cloud.SpeechConstant;
import com.seabreeze.log.Print;
import com.tencent.TIMCallBack;
import com.tencent.TIMMessage;
import com.tencent.openqq.protocol.imsdk.msg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;

public class MainActivity extends BarBaseActivity implements ISynthesizerPresenter.ITtsView, IChatPresenter.IChatView,
        ISerialPresenter.ISerialView, ILineSoundPresenter.ILineSoundView {

    @BindView(R.id.iv_fanfan)
    ImageView ivFanfan;
    @BindView(R.id.iv_video)
    ImageView ivVideo;
    @BindView(R.id.iv_problem)
    ImageView ivProblem;
    @BindView(R.id.iv_multi_media)
    ImageView ivMultiMedia;
    @BindView(R.id.iv_face)
    ImageView ivFace;
    @BindView(R.id.iv_seting_up)
    ImageView ivSetingUp;
    @BindView(R.id.iv_public)
    ImageView ivPublic;
    @BindView(R.id.iv_navigation)
    ImageView ivNavigation;
    @BindView(R.id.chat_content)
    ChatTextView chatContent;
    @BindView(R.id.iv_main)
    ImageView ivMain;
    @BindView(R.id.tv_alarm)
    TextView tvAlarm;
    private boolean quit;

    @Inject
    MainManager mMainManager;

    private VoiceDBManager mVoiceDBManager;

    private ServiceConnection mPlayServiceConnection;

    private MaterialDialog materialDialog;

    private boolean isPlay;

    private Youtucode youtucode;

    private int id;

    private String alarmDust;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main1;
    }

    @Override
    protected void initView() {
        super.initView();
        DaggerMainComponet.builder().mainModule(new MainModule(this)).build().inject(this);
        mMainManager.onCreate();
        youtucode = Youtucode.getSingleInstance();
        youtucode.updateProgram(1);
    }

    @Override
    protected void initData() {
        sendOrder(SerialService.DEV_BAUDRATE, "A50C80F3AA");
        mVoiceDBManager = new VoiceDBManager();

        loadImage(R.mipmap.fanfan_hand, R.mipmap.fanfan_lift_hand);

    }

    @Override
    protected void callStop() {
        mMainManager.callStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.isDance = false;

        RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_CLOUD);
        mMainManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setChatView(false);
        loadImage(R.mipmap.fanfan_hand, R.mipmap.fanfan_lift_hand);
        mMainManager.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        stopService(new Intent(this, UdpService.class));
        stopService(new Intent(this, SerialService.class));
        super.onDestroy();
        mMainManager.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!quit) {
            showToast("再按一次退出程序");
            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false;
                }
            }, 2000);
            quit = true;
        } else {
            super.onBackPressed();
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @OnClick({R.id.iv_fanfan, R.id.iv_video, R.id.iv_problem, R.id.iv_multi_media, R.id.iv_face, R.id.iv_seting_up,
            R.id.iv_public, R.id.iv_navigation, R.id.iv_main})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_fanfan:
                animateSequentially(ivFanfan);
                break;
            case R.id.iv_video:
                VideoIntroductionActivity.newInstance(this);
                break;
            case R.id.iv_problem:
                ProblemConsultingActivity.newInstance(this);
                break;
            case R.id.iv_multi_media:
                bindService(false);
                break;
            case R.id.iv_face:
                WuHanActivity.newInstance(this);
                //   FaceRecognitionActivity.newInstance(this);
                break;
            case R.id.iv_seting_up:
                clickSetting();
                break;
            case R.id.iv_public:
                WuHanCRActivity.newInstance(this);
//                PublicNumberActivity.newInstance(this);
                break;
            case R.id.iv_navigation:
                NavigationActivity.newInstance(this);
                break;
            case R.id.iv_main:
                ivMain.setVisibility(View.GONE);
                break;
        }
    }

    private String mInput;

    private void clickSetting() {
        ivSetingUp.setEnabled(false);
        youtucode.selectSet(UserInfo.getInstance().getIdentifier());
    }

    @SuppressLint("NewApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(SetEvent event) {
        ivSetingUp.setEnabled(true);
        if (event.isOk()) {
            SetBean bean = event.getBean();
            Print.e(bean);
            if (bean.getCode() == 0) {
                showSetDialog(bean.getData());
            } else if (bean.getCode() == 1) {
                SettingActivity.newInstance(MainActivity.this, SettingActivity.LOGOUT_TO_MAIN_REQUESTCODE);
            } else {
                onError(bean.getCode(), bean.getMsg());
            }
        } else {
            onError(event);
        }
    }

    private void showSetDialog(final SetBean.Data data) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.title_setting_pwd)
                .inputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER)
                .negativeText(R.string.cancel)
                .positiveText(R.string.confirm)
                .inputRange(6, 10)
                .alwaysCallInputCallback()
                .input(getString(R.string.input_hint_pwd), "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Print.e(input);
                        mInput = String.valueOf(input);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onCompleted();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (data.getSet_pwd().equals(mInput)) {
                            SettingActivity.newInstance(MainActivity.this, SettingActivity.LOGOUT_TO_MAIN_REQUESTCODE);
                        } else {
                            showMsg("密码错误");
                        }
                    }
                })
                .build();
        materialDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        materialDialog.setCancelable(false);
        materialDialog.show();
    }


    private void bindService(boolean isPlay) {
        this.isPlay = isPlay;
        if (!PreferencesUtils.getBoolean(MainActivity.this, Constants.MUSIC_UPDATE, false))
            showLoading();
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingActivity.LOGOUT_TO_MAIN_REQUESTCODE) {
            if (resultCode == SettingActivity.LOGOUT_TO_MAIN_RESULTCODE) {
                spakeLogout();
            }
        } else if (requestCode == MultimediaActivity.MULTIMEDIA_REQUESTCODE) {
            if (resultCode == MultimediaActivity.MULTIMEDIA_RESULTCODE) {
                Print.e("断开与音乐服务的连接");
                unbindService(mPlayServiceConnection);
                mMainManager.onResume();
            }
        }
    }

    //**********************************************************************************************

    private boolean isSuspendAction;
    private boolean isAutoAction;

    private void sendOrder(int type, String motion) {
        mMainManager.receiveMotion(type, motion);
    }

    private void sendCustom(RobotBean localVoice) {
        mMainManager.sendCustomMessage(localVoice);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP://19
                if (!isSuspendAction) {
                    sendMsg(keyCode);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN://20
                if (!isSuspendAction) {
                    sendMsg(keyCode);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://21
                if (!isSuspendAction) {
                    sendMsg(keyCode);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT://20
                if (!isSuspendAction) {
                    sendMsg(keyCode);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_L1:
//                onEventLR();
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
//                onEventLR();
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                sendOrder(SerialService.DEV_BAUDRATE, "A50C80F3AA");
                break;
            case KeyEvent.KEYCODE_BUTTON_X:
                sendOrder(SerialService.DEV_BAUDRATE, "A50C80F2AA");
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                sendAutoAction();
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                stopAutoAction();
                break;
        }
        return false;
    }

    private void sendMsg(final int keyCode) {
        isSuspendAction = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP://19
                        sendOrder(SerialService.DEV_BAUDRATE, "A5038002AA");
                        Print.e("up");
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN://20
                        sendOrder(SerialService.DEV_BAUDRATE, "A5038008AA");
                        Print.e("down");
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT://21
                        sendOrder(SerialService.DEV_BAUDRATE, "A5038004AA");
                        Print.e("left");
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT://20
                        sendOrder(SerialService.DEV_BAUDRATE, "A5038006AA");
                        Print.e("right");
                        break;
                    default:
                        isSuspendAction = false;
                }

            }
        }, 500);
    }

    public void sendAutoAction() {
        if (isAutoAction) {
            stopAutoAction();
        } else {
            isAutoAction = true;
            sendOrder(SerialService.DEV_BAUDRATE, "A503800AAA");
            Print.e("自由运动(开)");
            mHandler.postDelayed(runnable, 200);
        }
    }

    public void stopAutoAction() {
        if (isAutoAction) {
            Print.e("自由运动(关)");
            mHandler.removeCallbacks(runnable);
            sendOrder(SerialService.DEV_BAUDRATE, "A5038005AA");
            isAutoAction = false;
        }
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoAction) {
                sendOrder(SerialService.DEV_BAUDRATE, "A503800AAA");
                mHandler.postDelayed(runnable, 200);
                Print.e("自由运动(开)");
            }
        }
    };

    //**********************************************************************************************

    private void addSpeakAnswer(String messageContent, boolean isAction) {
        mMainManager.stopVoice();
        mMainManager.stopHandler();
        mMainManager.doAnswer(messageContent);
        if (isAction) {
            speakingAddAction(messageContent.length());
        }
    }

    private void setChatContent(String messageContent) {

        chatContent.setSpanText(mHandler, messageContent, true);
    }

    private void speakingAddAction(int length) {
//        if (length <= 13) {
//            mSerialPresenter.sendOrder(SerialService.DEV_BAUDRATE, "A50C8001AA");
//        } else if (length > 13 && length <= 40) {
//            mSerialPresenter.sendOrder(SerialService.DEV_BAUDRATE, "A50C8003AA");
//        } else {
//            mSerialPresenter.sendOrder(SerialService.DEV_BAUDRATE, "A50C8021AA");
//        }
        sendOrder(SerialService.DEV_BAUDRATE, Constants.SPEAK_ACTION);
    }

    //************************anim****************************
    protected void animateSequentially(View view) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                FanFanIntroduceActivity.newInstance(MainActivity.this);
                //               PPTActivity.newInstance(MainActivity.this);
                String speak = "您好，我是办公大厅的智能小秘书，关于出入境证件办理，常见问题咨询您都可以问我，虽然我还没有哥哥姐姐们那么优秀，但我一定会尽心为您服务。另外，除了主页上显示的功能，我还会唱歌，跳舞，除此之外我还有很多隐藏的小能力，比如您想知道目前所在的位置，您可以对我说出“打开地图”，另外，您也可以尝试对我说“打开新闻、武汉风情”，会有惊喜哦";
//                setChatContent(speak);
                addSpeakAnswer(speak, true);
            }
        }, 400);
        ViewAnimator
                .animate(view)
                .scale(1f, 1.3f, 1f)
                .alpha(1, 0.3f, 1)
                .translationX(0, 200, 0)
                .translationY(0, 300, 0)
                .interpolator(new LinearInterpolator())
                .duration(1200)
                .start();
    }

    //**********************************************************************************************
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onResultEvent(ReceiveEvent event) {
        if (event.isOk()) {
            DatagramPacket packet = event.getBean();
            if (!SocketManager.getInstance().isGetTcpIp) {
                SocketManager.getInstance().setUdpIp(packet.getAddress().getHostAddress(), packet.getPort());
            }
            String recvStr = new String(packet.getData(), 0, packet.getLength());
            Print.e("udp发送过来消息 ： " + recvStr);
            sendOrder(SerialService.DEV_BAUDRATE, recvStr);
        } else {
            Print.e("ReceiveEvent error");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(ServiceToActivityEvent event) {
        if (event.isOk()) {
            SerialBean serialBean = event.getBean();
            mMainManager.onDataReceiverd(serialBean);
        } else {
            Print.e("ReceiveEvent error");
        }
    }

    @SuppressLint("NewApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(CheckEvent event) {
        if (event.isOk()) {
            Check check = event.getBean();
            Print.e(check);
            if (check.getCode() == 0) {
                Check.CheckBean appVerBean = check.getCheck();
                int curVersion = AppUtil.getVersionCode(this);
                int newversioncode = appVerBean.getVersionCode();

                if (curVersion < newversioncode) {

                    Progress progress = DownloadDBDao.getInstance().get(Constant.APK_URL);
                    if (progress == null) {
                        return;
                    }
                    if (progress.status != Progress.FINISH) {
                        return;
                    }
                    final File file = new File(progress.folder, progress.fileName);
                    if (!file.exists()) {
                        return;
                    }
                    long fileSize = FileUtil.getFileSize(file);
                    if (progress.totalSize != fileSize) {
                        return;
                    }
                    DialogUtils.showBasicNoTitleDialog(this, getString(R.string.download_check_finish), "取消", "安装",
                            new DialogUtils.OnNiftyDialogListener() {
                                @Override
                                public void onClickLeft() {

                                }

                                @Override
                                public void onClickRight() {
                                    stopService(new Intent(MainActivity.this, LoadFileService.class));
                                    AppUtil.installNormal(MainActivity.this, file);
                                }
                            });
                } else {
                    Print.e("暂时没有检测到新版本");
                }
            } else {
                onError(check.getCode(), check.getMsg());
            }
        } else {
            onError(event);
        }
    }

    //**********************************************************************************************
    @Override
    public void showLoading() {
        if (materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(this)
                    .title("请稍等...")
                    .content("正在获取中...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
        }
        materialDialog.show();
    }

    @Override
    public void dismissLoading() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }

    @Override
    public void showMsg(String msg) {
        showToast(msg);
    }

    @Override
    public void showMsg(int msg) {
        showToast(msg);
    }

    @Override
    public Context getContext() {
        return this;
    }

    //**********************************************************************************************
    @Override
    public void onSpeakBegin(String answer) {

        Print.e("开始说话    ");

        setChatContent(answer);
        setChatView(true);
        loadImage(R.mipmap.fanfan_lift_hand, R.mipmap.fanfan_hand);
/*
        long current = System.currentTimeMillis() - curTime;
        showMsg(current + "");*/
    }

    @Override
    public void onRunable() {
        setChatView(false);
        loadImage(R.mipmap.fanfan_hand, R.mipmap.fanfan_lift_hand);
        sendOrder(SerialService.DEV_BAUDRATE, Constants.STOP_DANCE);
        mMainManager.startVoice();
    }

    @Override
    public void stopSound() {
        mMainManager.stopVoice();
        mMainManager.stopHandler();
    }

    private void loadImage(int load, int place) {
        ImageLoader.loadImage(this, ivFanfan, load, false, place, 1000);
    }


    private void setChatView(final boolean isShow) {
//        if(chatContent.getMyText() != null) {
//            if (chatContent.getMyText().equals("")){
//                showMsg("chat null");
//            }
//        }else{
//            showMsg("getMyText null");
//        }
        if (isShow) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setDuration(300);
            alphaAnimation.setFillAfter(true);
            chatContent.bringToFront();
            chatContent.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    chatContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            if (TextUtils.isEmpty(chatContent.getMyText())) {
                chatContent.setVisibility(View.GONE);
                return;
            }
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(1000);
            alphaAnimation.setFillAfter(true);
            chatContent.bringToFront();
            chatContent.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    chatContent.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(chatContent.getMyText())) {
                        setChatContent("");
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }


    }

    //**********************************************************************************************
    @Override
    public void onSendMessageSuccess(TIMMessage message) {
        Print.e("发送消息成功");
    }

    @Override
    public void onSendMessageFail(int code, String desc, TIMMessage message) {
        Print.e("发送消息失败");
    }

    @Override
    public void parseMsgcomplete(String str) {

        addSpeakAnswer(str, true);
//    setChatContent(str);


    }

    @Override
    public void parseCustomMsgcomplete(String customMsg) {
        RobotBean bean = GsonUtil.GsonToBean(customMsg, RobotBean.class);
        if (bean == null) {
            return;
        }
        if (bean.getType() == null || bean.getType().equals("")) {
            return;
        }
        if (bean.getOrder() == null || bean.getOrder().equals("")) {
            return;
        }
        RobotType robotType = bean.getType();
        switch (robotType) {
            case AutoAction:
                break;
            case VoiceSwitch:
                boolean isSpeech = bean.getOrder().equals("语音开");
                mMainManager.setSpeech(isSpeech);
                break;
            case Text:
/*                if(Constants.Speak ){
                    return;
                }*/
                addSpeakAnswer(bean.getOrder(), true);
//                setChatContent(bean.getOrder());
                break;
            case SmartChat:

                break;
            case Motion:
                sendOrder(SerialService.DEV_BAUDRATE, bean.getOrder());
                break;
            case GETIP:
                Constants.CONNECT_IP = bean.getOrder();
                if (Constants.IP != null && Constants.PORT > 0) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("robotIp", Constants.IP);
                        object.put("robotPort", Constants.PORT);
                        RobotBean robotBean = new RobotBean();
                        robotBean.setOrder(object.toString());
                        robotBean.setType(RobotType.GETIP);
                        Print.e("发送: " + object.toString());
                        showToast("发送: " + object.toString());
                        sendCustom(robotBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("robotIp", "");
                        object.put("robotPort", Constants.PORT);
                        RobotBean robotBean = new RobotBean();
                        robotBean.setOrder(object.toString());
                        robotBean.setType(RobotType.GETIP);
                        Print.e("发送: " + object.toString());
                        sendCustom(robotBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case LocalVoice:

                List<VoiceBean> voiceBeanList = mVoiceDBManager.loadAll();
                List<String> anwers = new ArrayList<>();
                if (voiceBeanList != null && voiceBeanList.size() > 0) {
                    for (VoiceBean voiceBean : voiceBeanList) {
                        anwers.add(voiceBean.getShowTitle());
                    }
                    String voiceJson = GsonUtil.GsonString(anwers);
                    RobotBean localVoice = new RobotBean();
                    localVoice.setType(RobotType.LocalVoice);
                    localVoice.setOrder(voiceJson);
                    sendCustom(localVoice);
                }
                break;
            case Anwser:
                mMainManager.stopVoice();
                aiuiForLocal(bean.getOrder());
                break;
        }
    }

    @Override
    public void parseServerMsgcomplete(String txt) {
        Print.e("接收到客服发来的消息： " + txt);

        txt = txt.trim();
        if (txt.equals("a")) {
            mMainManager.stopVoice();
            Print.e("onRecognDown stop ：" + "收到客服的停止监听命令");
        } else if (txt.equals("s")) {
            mMainManager.startVoice();
            Print.e("onRecognDown start ：" + "收到客服的停止监听命令");
        } else if (txt.equals("g")) {
            stopAll();

        } else if (txt.equals("h")) {//前进
            sendOrder(SerialService.DEV_BAUDRATE, "A5038002AA");

        } else if (txt.equals("j")) {//后退
            sendOrder(SerialService.DEV_BAUDRATE, "A5038008AA");

        } else if (txt.equals("k")) {//左转
            sendOrder(SerialService.DEV_BAUDRATE, "A5038004AA");

        } else if (txt.equals("l")) {//右转
            sendOrder(SerialService.DEV_BAUDRATE, "A5038006AA");

        } else if (txt.equals("zz")) {
            addSpeakAnswer("请打开支付宝扫描屏幕上面的二维码进行缴费！", false);
            ivMain.setVisibility(View.VISIBLE);
            ImageLoader.loadImage(this, ivMain, R.mipmap.zfb);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivMain.setVisibility(View.GONE);
                }
            }, 10000);
        } else if (txt.equals("xx")) {
            addSpeakAnswer("请打开微信扫描屏幕上面的二维码进行表格填写！", false);
            ivMain.setVisibility(View.VISIBLE);
            ImageLoader.loadImage(this, ivMain, R.mipmap.tianbiao);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivMain.setVisibility(View.GONE);
                }
            }, 10000);
        } else if (txt.equals("q")) {
            animateSequentially(ivFanfan);

        } else if (txt.equals("w")) {
            ProblemConsultingActivity.newInstance(this);

        } else if (txt.equals("e")) {
            VideoIntroductionActivity.newInstance(this);
        } else if (txt.equals("r")) {
            NavigationActivity.newInstance(this);
        } else if (txt.equals("t")) {
            if (Constants.isDance) {
                Print.e("正在跳舞，return");
                return;
            }
            beginDance();
        } else if (txt.equals("y")) {
            GDMap();
        } else if (txt.equals("u")) {
            sendOrder(SerialService.DEV_BAUDRATE, "A50C800CAA");
//            setChatContent("你好");
            addSpeakAnswer("你好", false);
        } else if (txt.equals("d")) {
            if (isAutoAction) {

                return;
            }

            sendAutoAction();
        } else if (txt.equals("f")) {
            stopAutoAction();
        } else if (txt.equals("z")) {
            WuHanActivity.newInstance(this);
        } else if (txt.equals("x")) {
            WuHanCRActivity.newInstance(this);
        } else if (txt.equals("c")) {
            WuHanFQActivity.newInstance(this);
        } else if (txt.equals("v")) {
            WuHanHomeActivity.newInstance(this);
        } else if (txt.equals("b")) {
            onCompleted();
            //  WuHanHomeActivity.newInstance(this);
        } else {
            if (txt.equals("1") || txt.equals("2") || txt.equals("3") || txt.equals("4") || txt.equals("5")
                    || txt.equals("6") || txt.equals("7") || txt.equals("8") || txt.equals("9")) {
                int i = Integer.valueOf(txt);
                switch (i) {
                    case 1:
                        txt = "正在为您检索答案，请您稍等";
                        break;
                    case 2:
                        txt = "数据库中未检索到符合的答案，如需咨询业务相关，您可以对我说“业务办理”";
                        break;
                    case 3:
                        txt = "您好，请问您想办理什么业务";
                        break;
                    case 4:
                        txt = "您好，很高兴见到您";
                        break;
                    case 5:
                        txt = "这个问题好难啊，我还没学过这个，您能教我么";
                        break;
                    case 6:
                        txt = "领导您好，我是境境，希望您能喜欢我";
                        break;
                    case 7:
                        txt = "我是服务机器人境境";
                        break;
                    case 8:
                        txt = "您在我前面站了这么久  要和我说点什么么";
                        break;
                    case 9:
                        txt = "您好，您可以和我对话聊天，提问解答，我很乐意为您服务";
                        break;

                }
            }
            addSpeakAnswer(txt, true);
//            setChatContent(txt);
        }
    }

    //**********************************************************************************************
    @Override
    public void stopAll() {
        sendOrder(SerialService.DEV_BAUDRATE, Constants.STOP_DANCE);
        mMainManager.stopVoice();
        String wakeUp = resFoFinal(R.array.wake_up);
//        setChatContent(wakeUp);
        mMainManager.stopAll(wakeUp);
    }

    @Override
    public void onMoveStop() {

    }

    @Override
    public void onAlarm(Alarm alarm) {
        if(0<=alarm.getDust()&&alarm.getDust()<10){
        alarmDust="很好";
        }else if(10<=alarm.getDust()&&alarm.getDust()<30){
            alarmDust="好";
        } else if(30<=alarm.getDust()&&alarm.getDust()<150){
            alarmDust="一般";
        }else if(150<=alarm.getDust()&&alarm.getDust()<250){
            alarmDust="差";
        }else if(250<=alarm.getDust()){
            alarmDust="很差";
        }
        if(alarm.getFlame()==1){
            tvAlarm.setText("火情警报！！！");
            addSpeakAnswer("火情警报请注意！", false);
        }else if(alarm.getFog()>=200){
            tvAlarm.setText("可燃气体超标警报！！！");
            addSpeakAnswer("可燃气体超标警报注意！", false);
        }else{
            tvAlarm.setText( "可燃气体 ：" +alarm.getFog()+"\r\n"+"灰尘 ：" +alarm.getDust()+"|质量"+alarmDust+"\r\n"+"湿度 ：" +alarm.getHumidity()+" %"+"\r\n"+"温度 ： " +alarm.getTemperature()+" ℃");
        }


    }

    //**********************************************************************************************
    @Override
    public void aiuiForLocal(String result) {
//        Print.e("接收到发来的消息： "+"==========当前是否在说话： "+Constants.Speak);
/*        if(Constants.Speak ){
            return;
        }*/

        Print.e("aiuiForLocal : " + result);
        //String unicode = result.replaceAll("\\p{P}", "");
        Print.e("aiuiForLocal : " + result);
        if (result.equals("百度")) {
            IntentUtil.openUrl(mContext, "http://www.baidu.com/");
        } else if (result.equals("武汉风情") || result.equals("武汉丰田") || result.equals("武汉风行")) {
            //IntentUtil.openUrl(mContext, "http://www.cepb.gov.cn/");
            WuHanFQActivity.newInstance(this);
            // IntentUtil.openUrl(mContext, "http://www.mafengwo.cn/travel-scenic-spot/mafengwo/10133.html");
        } else if (result.equals("打开新闻") || result.equals("新闻") || result.equals("打开心扉")) {
            //IntentUtil.openUrl(mContext, "http://www.cepb.gov.cn/");
            WuHanHomeActivity.newInstance(this);
            // IntentUtil.openUrl(mContext, "http://www.mafengwo.cn/travel-scenic-spot/mafengwo/10133.html");
        } else {
            List<VoiceBean> voiceBeanList = mVoiceDBManager.loadAll();
            if (voiceBeanList != null && voiceBeanList.size() > 0) {
                for (VoiceBean voiceBean : voiceBeanList) {
                    if (voiceBean.getShowTitle().equals(result)) {
                        refHomePage(voiceBean);
                        return;
                    }
                }
            }
            mMainManager.onlineResult(result);
        }
    }

    @Override
    public void doAiuiAnwer(String anwer) {
/*        Print.e("接收到消息： "+"==========当前是否在说话： "+Constants.Speak);
        if(Constants.Speak ){
            return;
        }*/

        addSpeakAnswer(anwer, true);
    }

    @Override
    public void refHomePage(VoiceBean voiceBean) {
        if (voiceBean.getActionData() != null)
            sendOrder(SerialService.DEV_BAUDRATE, voiceBean.getActionData());
        if (voiceBean.getExpressionData() != null)
            sendOrder(SerialService.DEV_BAUDRATE, voiceBean.getExpressionData());

//        setChatContent(voiceBean.getVoiceAnswer());
        addSpeakAnswer(voiceBean.getVoiceAnswer(), true);
    }


    @Override
    public void refHomePage(String question, String finalText) {
//        setChatContent(finalText);
    }

    @Override
    public void refHomePage(String question, String finalText, String url) {
//        setChatContent(finalText);
    }

    @Override
    public void refHomePage(String question, News news) {
//        setChatContent(news.getContent());
    }

    @Override
    public void refHomePage(String question, Radio radio) {
//        setChatContent(radio.getDescription());
    }

    @Override
    public void refHomePage(String question, Poetry poetry) {
//        setChatContent(poetry.getContent());
    }

    @Override
    public void refHomePage(String question, Cookbook cookbook) {
//        setChatContent(cookbook.getSteps());
    }

    @Override
    public void refHomePage(String question, EnglishEveryday englishEveryday) {
//        setChatContent(englishEveryday.getContent());
    }

    @Override
    public void special(String result, SpecialType type) {
        switch (type) {
  /*          case Story:
                break;*/
            case Music:
                bindService(true);
                break;
/*            case Joke:
                break;*/
            case Dance:
                beginDance();


                break;
            case Hand:
                sendOrder(SerialService.DEV_BAUDRATE, "A50C800CAA");
                addSpeakAnswer("你好", false);
                break;
        }
    }

    private void beginDance() {
        Constants.isDance = true;
        addSpeakAnswer("请您欣赏舞蹈", false);
        //  setChatContent("请您欣赏舞蹈");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DanceDBManager danceDBManager = new DanceDBManager();
                List<Dance> dances = danceDBManager.loadAll();
                if (dances != null && dances.size() > 0) {
                    Dance dance = dances.get(0);
                    DanceActivity.newInstance(MainActivity.this, dance.getId());
                } else {
                    //  setChatContent("本地暂未添加舞蹈，请到设置或多媒体中添加舞蹈");
                    addSpeakAnswer("本地暂未添加舞蹈，请到设置或多媒体中添加舞蹈", true);
                }
                Print.e("开始跳舞");
            }
        }, 2000);
    }


    @Override
    public void doCallPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void train(List<Train> trains) {

    }

    @Override
    public void startPage(SpecialType specialType) {
        switch (specialType) {
            case Fanfan:
                animateSequentially(ivFanfan);
                break;
            case Video:
                VideoIntroductionActivity.newInstance(this);
                break;
            case Problem:
                ProblemConsultingActivity.newInstance(this);
                break;
            case MultiMedia:
                bindService(false);
                break;
            case Face:
                WuHanActivity.newInstance(this);
                //   FaceRecognitionActivity.newInstance(this);
                break;
            case Seting_up:
                clickSetting();
                break;
            case Public_num:
                WuHanCRActivity.newInstance(this);
//                PublicNumberActivity.newInstance(this);
                break;
            case Navigation:
                NavigationActivity.newInstance(this);
                break;
            default:
                onCompleted();
                break;
        }
    }

    @Override
    public void spakeMove(SpecialType type, String result) {
        mMainManager.onCompleted();
        switch (type) {
            case Forward:
                sendOrder(SerialService.DEV_BAUDRATE, "A5038002AA");
                break;
            case Backoff:
                sendOrder(SerialService.DEV_BAUDRATE, "A5038008AA");
                break;
            case Turnleft:
                sendOrder(SerialService.DEV_BAUDRATE, "A5038004AA");
                break;
            case Turnright:
                sendOrder(SerialService.DEV_BAUDRATE, "A5038006AA");
                break;
            case StartMove:
                sendAutoAction();
                break;
            case StopMove:
                stopAutoAction();
                break;
        }
    }

    @Override
    public void openMap() {
        GDMap();
        //    AMapActivity.newInstance(this);
    }

    private void GDMap() {

        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("androidamap://showTraffic?sourceApplication=softname&amp;poiid=BGVIS1&amp;lat=36.2&amp;lon=116.1&amp;level=10&amp;dev=0"));
        intent.setPackage("com.autonavi.minimap");
        startActivity(intent);
    }

    @Override
    public void openVr() {
        PanoramicMapActivity.newInstance(this);
    }

    @Override
    public void spakeLogout() {
        LoginBusiness.logout(new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                showMsg("退出登录失败，请稍后重试");
            }

            @Override
            public void onSuccess() {
//                liveLogout();
                logout();
            }
        });
    }

    @Override
    public void onCompleted() {
        mMainManager.onCompleted();
    }

    @Override
    public void noAnswer(String question) {
        Print.e("noAnswer : " + question);
        String identifier = UserInfo.getInstance().getIdentifier();

        youtucode.requestProblem(identifier, question, id);
    }




/*    private long curTime;

    @Override
    public void testTime() {
        curTime = System.currentTimeMillis();
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(RequestProblemEvent event) {
        if (!event.isOk()) {
            onError(event);
            onCompleted();
            return;
        }
        String identifier = UserInfo.getInstance().getIdentifier();
        RequestProblem requestProblem = event.getBean();

        if (requestProblem.getCode() == 2) {//添加成功
            Print.e(requestProblem.getMsg());

            mMainManager.sendMessage(identifier, requestProblem.getQuestion());
            onCompleted();
        } else if (requestProblem.getCode() == 0) {//已经添加过，有答案
            RequestProblem.AnswerBean answerBean = requestProblem.getAnswerBean();
            if (answerBean == null) {
                mMainManager.sendMessage(identifier, requestProblem.getQuestion());
                onCompleted();
                return;
            }
            Print.e(requestProblem);
            String anwer = answerBean.getAnswer();
            id = answerBean.getId();
            if (anwer == null || anwer.length() < 1) {
                mMainManager.sendMessage(identifier, requestProblem.getQuestion());
                onCompleted();
            } else {
                //setChatContent(anwer);
                addSpeakAnswer(anwer, true);
            }
        } else {
            mMainManager.sendMessage(identifier, requestProblem.getQuestion());
            onError(requestProblem.getCode(), requestProblem.getMsg());
            onCompleted();
        }
    }

    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final PlayService playService = ((PlayService.PlayBinder) service).getService();
            MusicCache.get().setPlayService(playService);
            playService.updateMusicList(new EventCallback<Void>() {
                @Override
                public void onEvent(Void aVoid) {
                    dismissLoading();
                    MultimediaActivity.newInstance(MainActivity.this, isPlay, MultimediaActivity.MULTIMEDIA_REQUESTCODE);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
