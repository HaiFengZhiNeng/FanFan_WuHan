package com.fanfan.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.db.manager.FaceAuthDBManager;
import com.fanfan.novel.model.FaceAuth;
import com.fanfan.novel.model.SerialBean;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.service.SerialService;
import com.fanfan.novel.service.event.ReceiveEvent;
import com.fanfan.novel.service.event.ServiceToActivityEvent;
import com.fanfan.novel.service.udp.SocketManager;
import com.fanfan.robot.R;
import com.fanfan.robot.model.Alarm;
import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.seabreeze.log.Print;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.OpenCVLoader;

import java.net.DatagramPacket;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/6.
 */

public class FaceRecognitionActivity extends BarBaseActivity implements ILocalSoundPresenter.ILocalSoundView, ISerialPresenter.ISerialView {

    @BindView(R.id.iv_face_check_in)
    ImageView ivFaceCheckIn;
    @BindView(R.id.iv_face_instagram)
    ImageView ivFaceInstagram;
    @BindView(R.id.iv_face_witness_contrast)
    ImageView ivFaceWitnessContrast;
    @BindView(R.id.iv_face_extraction)
    ImageView ivFaceExtraction;

    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, FaceRecognitionActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private String mInput;
    private FaceAuthDBManager mFaceAuthDBManager;

    private LocalSoundPresenter mSoundPresenter;
    private SerialPresenter mSerialPresenter;

    static {
        if (!OpenCVLoader.initDebug()) {
            System.out.println("opencv 初始化失败！");
        } else {
            System.loadLibrary("detection_based_tracker");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_face_recognition;
    }

    @Override
    protected void initData() {

        mSoundPresenter = new LocalSoundPresenter(this);
        mSoundPresenter.start();
        mSerialPresenter = new SerialPresenter(this);
        mSerialPresenter.start();

        mFaceAuthDBManager = new FaceAuthDBManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mSoundPresenter.startRecognizerListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSoundPresenter.buildTts();
        mSoundPresenter.buildIat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSoundPresenter.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FaceCheckinActivity.CHECK_REQUESTCODE) {
            if (resultCode == FaceCheckinActivity.CHECK_RESULTCODE) {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(ServiceToActivityEvent event) {
        if (event.isOk()) {
            SerialBean serialBean = event.getBean();
            mSerialPresenter.onDataReceiverd(serialBean);
        } else {
            Print.e("ReceiveEvent error");
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onResultEvent(ReceiveEvent event) {
        if (event.isOk()) {
            DatagramPacket packet = event.getBean();
            if (!SocketManager.getInstance().isGetTcpIp) {
                SocketManager.getInstance().setUdpIp(packet.getAddress().getHostAddress(), packet.getPort());
            }
            String recvStr = new String(packet.getData(), 0, packet.getLength());
            mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, recvStr);
            Print.e(recvStr);
        } else {
            Print.e("ReceiveEvent error");
        }
    }

    @OnClick({R.id.iv_face_check_in, R.id.iv_face_instagram, R.id.iv_face_witness_contrast, R.id.iv_face_extraction})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_face_check_in:
                viewAnimator(ivFaceCheckIn, -30, -30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        FaceCheckinActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
            case R.id.iv_face_instagram:
                viewAnimator(ivFaceInstagram, 30, -30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        InstagramPhotoActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
            case R.id.iv_face_witness_contrast:
                viewAnimator(ivFaceWitnessContrast, -30, 30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        AuthenticationActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
            case R.id.iv_face_extraction:
                viewAnimator(ivFaceExtraction, 30, 30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        startExtraction();
                    }
                });
                break;
        }
    }

    private void viewAnimator(View view, int x, int y, AnimationListener.Stop stop) {
        ViewAnimator
                .animate(view)
                .scale(1f, 1.1f, 1f)
                .alpha(1, 0.7f, 1)
                .translationX(0, x, 0)
                .translationY(0, y, 0)
                .interpolator(new LinearInterpolator())
                .duration(500)
                .start()
                .onStop(stop);
    }

    private void addSpeakAnswer(String messageContent) {
        mSoundPresenter.doAnswer(messageContent);
    }

    private void addSpeakAnswer(int res) {
        mSoundPresenter.doAnswer(getResources().getString(res));
    }

    private void startExtraction() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.title_face_extraction)
                .content(R.string.input_content)
                .inputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .negativeText(R.string.cancel)
                .positiveText(R.string.confirm)
                .inputRange(2, 6)
                .alwaysCallInputCallback()
                .input(getString(R.string.input_hint), "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Print.e(input);
                        mInput = String.valueOf(input);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mSoundPresenter.onCompleted();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        judgeInput();
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

    private void judgeInput() {
        FaceAuth faceAuth = mFaceAuthDBManager.queryByAuth(String.valueOf(mInput));
        if (faceAuth != null) {
            long faceAithId = faceAuth.getId();
            FaceRegisterActivity.newInstance(FaceRecognitionActivity.this, faceAithId);
        } else {
            FaceRegisterActivity.newInstance(FaceRecognitionActivity.this, String.valueOf(mInput));
        }

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismissLoading() {

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

    @Override
    public void spakeMove(SpecialType type, String result) {
        mSoundPresenter.onCompleted();
        switch (type) {
            case Forward:
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, "A5038002AA");
                break;
            case Backoff:
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, "A5038008AA");
                break;
            case Turnleft:
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, "A5038004AA");
                break;
            case Turnright:
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, "A5038006AA");
                break;
        }
    }

    @Override
    public void openMap() {
        addSpeakAnswer(R.string.open_map);
    }

    @Override
    public void stopListener() {
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    @Override
    public void back() {
        finish();
    }

    @Override
    public void artificial() {
        addSpeakAnswer(R.string.open_artificial);
    }

    @Override
    public void face(SpecialType type, String result) {
        switch (type) {
            case Face_lifting_area:
                viewAnimator(ivFaceExtraction, 30, 30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        startExtraction();
                    }
                });
                break;
            case Face_check_in:
                viewAnimator(ivFaceCheckIn, -30, -30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        FaceCheckinActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
            case Instagram:
                viewAnimator(ivFaceInstagram, 30, -30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        InstagramPhotoActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
            case Witness_contrast:
                viewAnimator(ivFaceWitnessContrast, -30, 30, new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        AuthenticationActivity.newInstance(FaceRecognitionActivity.this);
                    }
                });
                break;
        }
    }

    @Override
    public void control(SpecialType type, String result) {
        addSpeakAnswer(R.string.open_control);
    }

    @Override
    public void refLocalPage(String result) {
        addSpeakAnswer(R.string.open_local);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void stopAll() {
        super.stopAll();
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
        addSpeakAnswer("你好，这里是人脸识别页面");
    }

    @Override
    public void onMoveStop() {

    }

    @Override
    public void onAlarm(Alarm alarm) {

    }
}
