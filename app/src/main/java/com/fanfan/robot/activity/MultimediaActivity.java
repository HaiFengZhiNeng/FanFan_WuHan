package com.fanfan.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanfan.novel.activity.AddNavigationActivity;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.common.glide.GlideRoundTransform;
import com.fanfan.novel.model.SerialBean;
import com.fanfan.novel.presenter.ChatPresenter;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.ipresenter.IChatPresenter;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.service.PlayService;
import com.fanfan.novel.service.SerialService;
import com.fanfan.novel.service.cache.MusicCache;
import com.fanfan.novel.service.event.ReceiveEvent;
import com.fanfan.novel.service.event.ServiceToActivityEvent;
import com.fanfan.novel.service.music.OnPlayerEventListener;
import com.fanfan.novel.service.udp.SocketManager;
import com.fanfan.novel.utils.music.MusicUtils;
import com.fanfan.robot.R;
import com.fanfan.robot.adapter.FragmentAdapter;
import com.fanfan.robot.fragment.DanceFragment;
import com.fanfan.robot.fragment.PlayFragment;
import com.fanfan.robot.fragment.SongFragment;
import com.fanfan.robot.model.Alarm;
import com.fanfan.robot.model.Music;
import com.seabreeze.log.Print;
import com.tencent.TIMMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.DatagramPacket;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/6.
 */

public class MultimediaActivity extends BarBaseActivity implements
        OnPlayerEventListener, ViewPager.OnPageChangeListener,
        ILocalSoundPresenter.ILocalSoundView,
        ISerialPresenter.ISerialView  , IChatPresenter.IChatView{

    @BindView(R.id.tv_local_music)
    TextView tvLocalMusic;
    @BindView(R.id.tv_local_dance)
    TextView tvLocalDance;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.v_music_select)
    View vMusicSelect;
    @BindView(R.id.v_dance_select)
    View vDanceSelect;

    private boolean isPlayFragmentShow = false;

    public static final int MULTIMEDIA_REQUESTCODE = 0xff - 3;
    public static final int MULTIMEDIA_RESULTCODE = 0xff - 2;

    public static final String IS_PLAY = "is_play";

    public static void newInstance(Activity context, boolean isPlay, int requestCode) {
        Intent intent = new Intent(context, MultimediaActivity.class);
        intent.putExtra(IS_PLAY, isPlay);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private SongFragment songFragment;
    private DanceFragment danceFragment;

    private PlayFragment mPlayFragment;

    private Menu mMenu;

    private LocalSoundPresenter mSoundPresenter;
    private SerialPresenter mSerialPresenter;
    private ChatPresenter mChatPresenter;

    private boolean isFinish;
    private boolean isPlay;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_multimedia;
    }

    @Override
    protected void initView() {
        super.initView();

        isPlay = getIntent().getBooleanExtra(IS_PLAY, false);
        Bundle bundle = new Bundle();
        bundle.putBoolean(SongFragment.FRAG_IS_PLAY, isPlay);

        songFragment = SongFragment.newInstance();
        songFragment.setArguments(bundle);
        danceFragment = DanceFragment.newInstance();

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(songFragment);
        adapter.addFragment(danceFragment);
        mViewPager.setAdapter(adapter);

        tvLocalMusic.setSelected(true);
        vMusicSelect.setVisibility(View.VISIBLE);

        mSoundPresenter = new LocalSoundPresenter(this);
        mSoundPresenter.start();
        mSerialPresenter = new SerialPresenter(this);
        mSerialPresenter.start();
        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.start();
    }

    @Override
    protected void initData() {
        PlayService playService = MusicCache.get().getPlayService();
        if (playService == null) {
            throw new NullPointerException("play service is null");
        }
        playService.setOnPlayEventListener(this);
    }

    @Override
    protected void setListener() {
        mViewPager.addOnPageChangeListener(this);
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
        mChatPresenter.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mMenu = menu;
        getMenuInflater().inflate(R.menu.add_white, menu);
        mMenu.findItem(R.id.add).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (danceFragment != null && danceFragment.isAdded()) {
                    danceFragment.add();
                }
                break;
            case android.R.id.home:
                back();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                songFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
    }

    @OnClick({R.id.tv_local_music, R.id.tv_local_dance})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_local_dance:
                mViewPager.setCurrentItem(1);
                break;
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

    private void addSpeakAnswer(String messageContent) {
        mSoundPresenter.doAnswer(messageContent);
    }

    private void addSpeakAnswer(int res) {
        mSoundPresenter.doAnswer(getResources().getString(res));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            tvLocalMusic.setSelected(true);
            vMusicSelect.setVisibility(View.VISIBLE);
            tvLocalDance.setSelected(false);
            vDanceSelect.setVisibility(View.GONE);
            mMenu.findItem(R.id.add).setVisible(false);
        } else {
            mSoundPresenter.onCompleted();
            if (songFragment != null && songFragment.isAdded()) {
                songFragment.stopMusic();
            }
            tvLocalMusic.setSelected(false);
            vMusicSelect.setVisibility(View.GONE);
            tvLocalDance.setSelected(true);
            vDanceSelect.setVisibility(View.VISIBLE);
            mMenu.findItem(R.id.add).setVisible(true);
        }
        startListener();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        back();
        super.onBackPressed();
    }

    @Override
    public void onChange(Music music) {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onChange(music);
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onChange(music);
        }

    }

    @Override
    public void onPlayerStart() {
        Print.e("onPlayerStart");
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onPlayerStart();
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onPlayerPause();
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerPause();
        }
        if (isFinish) {
            setResult(MULTIMEDIA_RESULTCODE);
            finish();
        }
    }

    @Override
    public void onPublish(int progress) {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onPublish(progress);
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onBufferingUpdate(percent);
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onTimer(long remain) {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onTimer(remain);
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onTimer(remain);
        }
    }

    @Override
    public void onMusicListUpdate() {
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.onMusicListUpdate();
        }
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onMusicListUpdate();
        }
    }

    public void startListener() {
        mSoundPresenter.onCompleted();
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

    //**********************************************************************************************
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
        isFinish = true;
        if (songFragment != null && songFragment.isAdded()) {
            songFragment.back();
        }
    //   finish();
    }

    @Override
    public void artificial() {
        addSpeakAnswer(R.string.open_artificial);
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
        addSpeakAnswer(R.string.open_local);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void stopAll() {
        back();
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

        txt = txt.trim();
        if (txt.equals("b")) {
            isFinish = true;
            if (songFragment != null && songFragment.isAdded()) {
                songFragment.back();
            }
   //    finish();

        }
    }
}
