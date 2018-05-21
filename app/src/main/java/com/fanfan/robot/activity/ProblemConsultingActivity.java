package com.fanfan.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fanfan.novel.activity.SimpleCallActivity;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.db.manager.VoiceDBManager;
import com.fanfan.novel.model.SerialBean;
import com.fanfan.novel.model.VoiceBean;
import com.fanfan.novel.presenter.ChatPresenter;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.ipresenter.IChatPresenter;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.service.SerialService;
import com.fanfan.novel.service.event.ReceiveEvent;
import com.fanfan.novel.service.event.ServiceToActivityEvent;
import com.fanfan.novel.service.udp.SocketManager;
import com.fanfan.novel.utils.ImageLoader;
import com.fanfan.robot.R;
import com.fanfan.robot.adapter.VoiceAdapter;
import com.fanfan.robot.app.RobotInfo;
import com.fanfan.robot.fragment.ImageFragment;
import com.fanfan.robot.model.Alarm;
import com.fanfan.robot.model.ImageBean;
import com.seabreeze.log.Print;
import com.tencent.TIMMessage;
import com.tencent.callsdk.ILVCallConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/6.
 */

public class ProblemConsultingActivity extends BarBaseActivity implements ILocalSoundPresenter.ILocalSoundView, ISerialPresenter.ISerialView , IChatPresenter.IChatView{

    @BindView(R.id.recycler_view)
    RecyclerView recyclerVoice;
    @BindView(R.id.iv_artificial)
    ImageView ivArtificial;
    @BindView(R.id.iv_voice_image)
    ImageView ivVoiceImage;

    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, ProblemConsultingActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private VoiceDBManager mVoiceDBManager;

    private List<VoiceBean> voiceBeanList = new ArrayList<>();

    private VoiceAdapter voiceAdapter;

    private LocalSoundPresenter mSoundPresenter;
    private SerialPresenter mSerialPresenter;

    private String speakText;

    private int mCurrentPos;

    private ImageFragment imageFragment;
    private boolean isShow;

    ChatPresenter mChatPresenter;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_problem_consulting;
    }


    @Override
    protected void initView() {
        super.initView();

        mSoundPresenter = new LocalSoundPresenter(this);
        mSoundPresenter.start();
        mSerialPresenter = new SerialPresenter(this);
        mSerialPresenter.start();
        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.start();
        initSimpleAdapter();

    }

    @Override
    protected void initData() {
        mVoiceDBManager = new VoiceDBManager();

        voiceBeanList = mVoiceDBManager.loadAll();
        if (voiceBeanList != null && voiceBeanList.size() > 0) {
            isNuEmpty();
            voiceAdapter.replaceData(voiceBeanList);
            mCurrentPos = 0;
            voiceAdapter.notifyClick(mCurrentPos);
            ImageLoader.loadLargeImage(mContext, ivVoiceImage, voiceBeanList.get(0).getImgUrl(), R.mipmap.video_image);
        } else {
            mCurrentPos = -1;
            isEmpty();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSoundPresenter.buildTts();

        addSpeakAnswer("你好，这里是问题咨询页面，点击上方列表或说出列表中问题可得到答案");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        mChatPresenter.finish();
        mSoundPresenter.finish();
        super.onDestroy();
    }

    @OnClick({R.id.iv_voice_image})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_voice_image:
                if (mCurrentPos > -1) {
                    VoiceBean bean = voiceBeanList.get(mCurrentPos);
                    showImage(bean);
                }
                break;
        }
    }

    private void showImage(VoiceBean bean) {
        isShow(true);
        ImageBean imageBean = new ImageBean();
        imageBean.setTop(bean.getShowTitle());
        imageBean.setBottom(bean.getVoiceAnswer());
        imageBean.setImgUrl(bean.getImgUrl());
        imageFragment = ImageFragment.newInstance(imageBean);
        imageFragment.show(getSupportFragmentManager(), "IMAGE");
    }

    private void dismissImage() {
        if (imageFragment != null) {
            imageFragment.dismiss();
            imageFragment = null;
        }
    }

    public void isShow(boolean isShow) {
        this.isShow = isShow;
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

    @OnClick({R.id.iv_artificial, R.id.iv_voice_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_artificial:
                artificial();
                break;
            case R.id.iv_voice_image:
                break;
        }
    }

    private void addSpeakAnswer(String messageContent) {
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.doAnswer(messageContent);
    }

    private void addSpeakAnswer(int res) {
        mSoundPresenter.doAnswer(getResources().getString(res));
    }

    private void initSimpleAdapter() {
        voiceAdapter = new VoiceAdapter(voiceBeanList);
        voiceAdapter.openLoadAnimation();
        voiceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                refVoice(voiceBeanList.get(position), position);
            }
        });

        recyclerVoice.setAdapter(voiceAdapter);
        recyclerVoice.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerVoice.setItemAnimator(new DefaultItemAnimator());
    }

    private void refVoice(VoiceBean itemData, int position) {
        mCurrentPos = position;
        voiceAdapter.notifyClick(mCurrentPos);
        recyclerVoice.scrollToPosition(mCurrentPos);
        speakText = itemData.getVoiceAnswer();
        addSpeakAnswer(speakText);
        if (!Constants.isTrain) {
            if (itemData.getActionData() != null) {
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, itemData.getActionData());
            }
            if (itemData.getExpressionData() != null) {
                mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, itemData.getExpressionData());
            }
        }
        ImageLoader.loadLargeImage(mContext, ivVoiceImage, itemData.getImgUrl(), R.mipmap.load_image);

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
        if (isShow) {
            dismissImage();
            mSoundPresenter.onCompleted();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (isShow) {
            dismissImage();
            mSoundPresenter.stopRecognizerListener();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void artificial() {
        ArrayList<String> nums = new ArrayList<>();
        nums.add(RobotInfo.getInstance().getControlId());
        SimpleCallActivity.newInstance(this, ILVCallConstants.CALL_TYPE_VIDEO, nums);
    }

    @Override
    public void face(SpecialType type, String result) {
        addSpeakAnswer(R.string.open_face);
    }

    @Override
    public void control(SpecialType type, String result) {
        addSpeakAnswer(R.string.open_control);
    }

    @Override
    public void refLocalPage(String result) {
        dismissImage();
        List<VoiceBean> voiceBeans = mVoiceDBManager.queryLikeVoiceByQuestion(result);
        if (voiceBeans != null && voiceBeans.size() > 0) {
            VoiceBean itemData = null;
            if (voiceBeans.size() == 1) {
                itemData = voiceBeans.get(voiceBeans.size() - 1);
            } else {
                itemData = voiceBeans.get(new Random().nextInt(voiceBeans.size()));
            }
            int index = voiceBeanList.indexOf(itemData);
            if (index != -1) {
                refVoice(itemData, index);
            } else {
                showMsg("数据有误");
            }
        } else {
            if (new Random().nextBoolean()) {
                addSpeakAnswer(resFoFinal(R.array.no_result));
            } else {
                addSpeakAnswer(resFoFinal(R.array.no_voice));
            }
        }
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
//        addSpeakAnswer("你好，这里是问题咨询页面，点击上方列表或说出列表中问题可得到答案");
        addSpeakAnswer("您好");
    }

    @Override
    public void onMoveStop() {

    }

    @Override
    public void onAlarm(Alarm alarm) {

    }

    @Override
    public void onSendMessageSuccess(TIMMessage message) {

    }

    @Override
    public void onSendMessageFail(int code, String desc, TIMMessage message) {

    }

    @Override
    public void parseMsgcomplete(String str) {

    }

    @Override
    public void parseCustomMsgcomplete(String customMsg) {

    }

    @Override
    public void parseServerMsgcomplete(String txt) {
        Print.e("本地语音接收到客服发来的消息： " + txt);
        txt = txt.trim();
        if (txt.equals("b")) {
            if (isShow) {
                dismissImage();
                mSoundPresenter.onCompleted();
            } else {
                finish();
            }
            Print.e("onRecognDown stop ：" + "收到客服的停止监听命令");
        }
    }
}
