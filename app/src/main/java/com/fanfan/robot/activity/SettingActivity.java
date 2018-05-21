package com.fanfan.robot.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fanfan.novel.activity.DataNavigationActivity;
import com.fanfan.novel.activity.DataSiteActivity;
import com.fanfan.novel.activity.DataVideoActivity;
import com.fanfan.novel.activity.DataVoiceActivity;
import com.fanfan.novel.activity.FaceDataActivity;
import com.fanfan.novel.activity.SelectCtiyActivity;
import com.fanfan.novel.activity.SettingPwdActivity;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.map.activity.AMapActivity;
import com.fanfan.novel.pointdown.DownloadManager;
import com.fanfan.novel.pointdown.DownloadRunnable;
import com.fanfan.novel.pointdown.db.DownloadDBDao;
import com.fanfan.novel.pointdown.event.ProgressEvent;
import com.fanfan.novel.pointdown.model.Progress;
import com.fanfan.novel.service.LoadFileService;
import com.fanfan.novel.service.cache.LoadFileCache;
import com.fanfan.novel.service.event.LoadStartEvent;
import com.fanfan.novel.service.load.ProgressListener;
import com.fanfan.novel.service.music.Actions;
import com.fanfan.novel.utils.AppUtil;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.robot.R;
import com.fanfan.robot.app.RobotInfo;
import com.fanfan.robot.fragment.ImportFragment;
import com.fanfan.robot.fragment.XfFragment;
import com.fanfan.robot.train.PanoramicMapActivity;
import com.fanfan.youtu.Youtucode;
import com.fanfan.youtu.api.base.Constant;
import com.fanfan.youtu.api.hfrobot.bean.Check;
import com.fanfan.youtu.api.hfrobot.event.CheckEvent;
import com.seabreeze.log.Print;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/6.
 */

public class SettingActivity extends BarBaseActivity implements ProgressListener<File> {

    public static final int LOGOUT_TO_MAIN_REQUESTCODE = 102;
    public static final int LOGOUT_TO_MAIN_RESULTCODE = 202;

    @BindView(R.id.add_video)
    RelativeLayout addVideo;
    @BindView(R.id.add_voice)
    RelativeLayout addVoice;
    @BindView(R.id.add_navigation)
    RelativeLayout addNavigation;
    @BindView(R.id.add_site)
    RelativeLayout addSite;
    @BindView(R.id.import_layout)
    RelativeLayout importLayout;
    @BindView(R.id.rl_face)
    RelativeLayout rlFace;
    @BindView(R.id.rl_dance)
    RelativeLayout rlDance;
    @BindView(R.id.tv_xf)
    TextView tvXf;
    @BindView(R.id.logout)
    TextView logout;
    @BindView(R.id.rl_city)
    RelativeLayout rlCity;
    @BindView(R.id.tv_city)
    TextView tvCity;
    @BindView(R.id.set_test)
    LinearLayout setTest;
    @BindView(R.id.tv_map)
    TextView tvMap;
    @BindView(R.id.tv_vr)
    TextView tvVr;
    @BindView(R.id.rl_setpwd)
    RelativeLayout rlSetpwd;
    @BindView(R.id.tv_cur_code)
    TextView tvCurCode;

    private Youtucode youtucode;

    private Check.CheckBean appVerBean;

    private Dialog loadingDialog;
    private View loadingView;
    private ProgressBar pb;
    private TextView tvProgress;
    private TextView tvBackstage;

    private int curPos = 0;
    private boolean isPosShow;

    private MaterialDialog materialDialog;

    public static void newInstance(Activity context, int requestCode) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initData() {

        youtucode = Youtucode.getSingleInstance();

        tvCity.setText(RobotInfo.getInstance().getCityName());

        if (Constants.unusual) {
            setTest.setVisibility(View.VISIBLE);
        } else {
            setTest.setVisibility(View.GONE);
        }

        tvCurCode.setText(String.format("当前版本 v %s", AppUtil.getVersionName(this)));

        LoadFileService loadFileService = LoadFileCache.get().getFileService();
        if (loadFileService != null) {
            loadFileService.setOnLoadEventListener(this);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @OnClick({R.id.add_video, R.id.add_voice, R.id.add_navigation, R.id.add_site, R.id.import_layout,
            R.id.rl_face, R.id.rl_dance, R.id.tv_xf, R.id.logout, R.id.rl_city, R.id.tv_vr, R.id.tv_map,
            R.id.rl_update, R.id.rl_setpwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_video:
                DataVideoActivity.newInstance(this);
                break;
            case R.id.add_voice:
                DataVoiceActivity.newInstance(this);
                break;
            case R.id.add_navigation:
                DataNavigationActivity.newInstance(this);
                break;
            case R.id.add_site:
                DataSiteActivity.newInstance(this);
                break;
            case R.id.import_layout:
                showLoading();
                ImportFragment importFragment = ImportFragment.newInstance();
                importFragment.show(getSupportFragmentManager(), "IMPORT");
                break;
            case R.id.rl_face:
                FaceDataActivity.newInstance(this);
                break;
            case R.id.rl_dance:
                DanceAddActivity.newInstance(this);
                break;
            case R.id.tv_xf:
                XfFragment xfFragment = XfFragment.newInstance();
                xfFragment.show(getSupportFragmentManager(), "CHANNEL");
                break;
            case R.id.logout:
                setResult(LOGOUT_TO_MAIN_RESULTCODE);
                finish();
                break;
            case R.id.rl_city:
                SelectCtiyActivity.newInstance(this);
                break;
            case R.id.tv_vr:
                PanoramicMapActivity.newInstance(this);
                break;
            case R.id.tv_map:
                AMapActivity.newInstance(this);
                break;
            case R.id.rl_update:
                youtucode.updateProgram(1);
                break;
            case R.id.rl_setpwd:
                SettingPwdActivity.newInstance(this);
                break;
        }
    }

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

    public void dismissLoading() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectCtiyActivity.CITY_REQUEST_CODE) {
            if (resultCode == SelectCtiyActivity.CITY_RESULT_CODE) {
                String city = data.getStringExtra(SelectCtiyActivity.RESULT_CODE);
                tvCity.setText(city);
                RobotInfo.getInstance().setCityName(city);
            }
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
                    DialogUtils.showBasicNoTitleDialog(this, "版本更新", "暂不更新", "更新",
                            new DialogUtils.OnNiftyDialogListener() {
                                @Override
                                public void onClickLeft() {

                                }

                                @Override
                                public void onClickRight() {
                                    startLoad();
                                }
                            });
                } else {
                    showToast("暂时没有检测到新版本");
                }
            } else {
                onError(check.getCode(), check.getMsg());
            }
        } else {
            onError(event);
        }
    }

    private void startLoad() {
        if (LoadFileCache.get().getFileService() == null) {
            Intent intent = new Intent();
            intent.setClass(this, LoadFileService.class);
//            intent.setAction(Actions.ACTION_LOAD_DOWNLOAD);
            startService(intent);
            showLoading();
        } else {
            LoadFileCache.get().getFileService().restart();
        }
//        youtucode.downloadFileWithDynamicUrlSync(Constant.API_ROBOT_BASE + Constants.DOWNLOAD_FILENAME);
    }

    @SuppressLint("NewApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(LoadStartEvent event) {
        if (event.isOk()) {
            LoadFileService loadFileService = LoadFileCache.get().getFileService();
            if (loadFileService != null) {
                isPosShow = true;
                loadFileService.setOnLoadEventListener(SettingActivity.this);
                loadFileService.download();
            } else {
                onError(0, "没有启动下载服务");
            }
        } else {
            onError(event);
        }
    }

    @Override
    public void onStart(Progress progress) {
        showToast("开始下载啦");
        showLoading();
    }

    @Override
    public void onProgress(Progress progress) {
        if (progress.status == Progress.PAUSE || progress.status == Progress.WAITING) {
            showLoading();
        } else {
            dismissLoading();
            if ((int) (progress.fraction * 100) > curPos) {
                curPos = (int) (progress.fraction * 100);
                Print.e("curPos : " + curPos);
                showLoadingDialog((int) (progress.fraction * 100));
            }
        }
    }

    @Override
    public void onError(Progress progress) {
        showFailDialog();
    }

    @Override
    public void onFinish(File file, Progress progress) {
        if (progress.status == Progress.FINISH) {
            showSuccessDialog(file);
        }
    }

    @Override
    public void onRemove(Progress progress) {
    }

    /**
     * 进度条显示
     *
     * @param currentProgress
     */
    private void showLoadingDialog(int currentProgress) {
        Print.e(currentProgress);
        if (!isPosShow) {
            return;
        }
        if (loadingDialog == null) {
            loadingView = LayoutInflater.from(this).inflate(R.layout.downloading_layout, null);
            loadingDialog = new AlertDialog.Builder(this).setTitle("").setView(loadingView).create();
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            pb = loadingView.findViewById(R.id.pb);
            tvProgress = loadingView.findViewById(R.id.tv_progress);
            tvBackstage = loadingView.findViewById(R.id.tv_backstage);
            tvBackstage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isPosShow = false;
                    dismissAllDialog();
                }
            });
        }

        tvProgress.setText(String.format(getString(R.string.progress), currentProgress));
        pb.setMax(100);
        pb.setProgress(currentProgress);

        loadingDialog.show();
    }

    private void dismissAllDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    /**
     * 下载失败提示
     */
    private void showFailDialog() {
        DialogUtils.showBasicNoTitleDialog(this, getString(R.string.download_fail_retry), "取消", "确定",
                new DialogUtils.OnNiftyDialogListener() {
                    @Override
                    public void onClickLeft() {

                    }

                    @Override
                    public void onClickRight() {
                        LoadFileCache.get().getFileService().restart();
                    }
                });
    }

    private void showSuccessDialog(final File file) {
        DialogUtils.showBasicNoTitleDialog(this, getString(R.string.download_finish), "重新下载", "安装",
                new DialogUtils.OnNiftyDialogListener() {
                    @Override
                    public void onClickLeft() {
                        curPos = 0;
                        LoadFileCache.get().getFileService().restart();
                    }

                    @Override
                    public void onClickRight() {
                        stopService(new Intent(SettingActivity.this, LoadFileService.class));
                        AppUtil.installNormal(SettingActivity.this, file);
                    }
                });
    }


}
