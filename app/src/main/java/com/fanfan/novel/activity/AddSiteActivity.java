package com.fanfan.novel.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.db.manager.NavigationDBManager;
import com.fanfan.novel.db.manager.SiteDBManager;
import com.fanfan.novel.db.manager.VideoDBManager;
import com.fanfan.novel.db.manager.VoiceDBManager;
import com.fanfan.novel.model.NavigationBean;
import com.fanfan.novel.model.SiteBean;
import com.fanfan.novel.model.VideoBean;
import com.fanfan.novel.model.VoiceBean;
import com.fanfan.novel.utils.AppUtil;
import com.fanfan.robot.R;
import com.fanfan.robot.app.NovelApp;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;

import java.util.List;

import butterknife.BindView;

/**
 * Created by android on 2018/2/23.
 */

public class AddSiteActivity extends BarBaseActivity {

    public static final String SITE_ID = "siteId";
    public static final String RESULT_CODE = "site_title_result";

    @BindView(R.id.et_site_name)
    EditText etSiteName;
    @BindView(R.id.et_site_url)
    EditText etSiteUrl;

    public static final int ADD_SITE_REQUESTCODE = 223;

    public static void newInstance(Activity context, int requestCode) {
        Intent intent = new Intent(context, AddSiteActivity.class);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void newInstance(Activity context, long id, int requestCode) {
        Intent intent = new Intent(context, AddSiteActivity.class);
        intent.putExtra(SITE_ID, id);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private long saveLocalId;

    private SiteDBManager mSiteDBManager;

    private SiteBean siteBean;

    private SpeechRecognizer mIat;

    private VideoDBManager mVideoDBManager;
    private VoiceDBManager mVoiceDBManager;
    private NavigationDBManager mNavigationDBManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_site;
    }

    @Override
    protected void initData() {
        saveLocalId = getIntent().getLongExtra(SITE_ID, -1);

        mSiteDBManager = new SiteDBManager();

        if (saveLocalId != -1) {
            siteBean = mSiteDBManager.selectByPrimaryKey(saveLocalId);

            etSiteName.setText(siteBean.getName());
            etSiteUrl.setText(siteBean.getUrl());
        } else {
            siteBean = new SiteBean();
        }
    }

    @Override
    protected void onDestroy() {
        if (mIat != null) {
            mIat.cancel();
        }
        listener = null;
        super.onDestroy();
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.finish_black, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish:

                if (siteBean == null) {
                    showToast("error！");
                    break;
                }
                if (isEmpty(etSiteName)) {
                    showToast("名称不能为空！");
                    break;
                }
                if (isEmpty(etSiteUrl)) {
                    showToast("链接不能为空！");
                    break;
                }
                if (etSiteName.getText().toString().trim().length() > 20) {
                    showToast("输入 20 字以内");
                    break;
                }
                if (!AppUtil.matcherUrl(getText(etSiteUrl))) {
                    showToast("输入的网址不合法");
                    break;
                }
                if (saveLocalId == -1) {
                    List<SiteBean> been = mSiteDBManager.querySiteByName(etSiteName.getText().toString().trim());
                    if (!been.isEmpty()) {
                        showToast("请不要添加相同的链接！");
                        break;
                    }
                }
                videoIsexit();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void videoIsexit() {
        siteBean.setName(getText(etSiteName));
        siteBean.setUrl(getText(etSiteUrl));
        siteBean.setSaveTime(System.currentTimeMillis());
        if (saveLocalId == -1) {
            saveLocalId = mSiteDBManager.insertForId(siteBean);
        } else {
            siteBean.setId(saveLocalId);
            mSiteDBManager.update(siteBean);
        }

        if (saveLocalId == -1) {
            throw new IllegalArgumentException("DB error");
        } else {
            mNavigationDBManager = new NavigationDBManager();
            mVideoDBManager = new VideoDBManager();
            mVoiceDBManager = new VoiceDBManager();

            updateContents();
        }
    }

    private boolean isEmpty(TextView textView) {
        return textView.getText().toString().trim().equals("") || textView.getText().toString().trim().equals("");
    }

    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    /**
     * 更新所有
     */
    public void updateContents() {
        if (mVideoDBManager == null || mVoiceDBManager == null || mNavigationDBManager == null) {
            throw new NullPointerException("local loxicon unll");
        }
        StringBuilder lexiconContents = new StringBuilder();
        //本地语音
        List<VoiceBean> voiceBeanList = mVoiceDBManager.loadAll();
        for (VoiceBean voiceBean : voiceBeanList) {
            lexiconContents.append(voiceBean.getShowTitle()).append("\n");
        }
        //本地视频
        List<VideoBean> videoBeanList = mVideoDBManager.loadAll();
        for (VideoBean videoBean : videoBeanList) {
            lexiconContents.append(videoBean.getShowTitle()).append("\n");
        }
        //本地导航
        List<NavigationBean> navigationBeanList = mNavigationDBManager.loadAll();
        for (NavigationBean navigationBean : navigationBeanList) {
            lexiconContents.append(navigationBean.getTitle()).append("\n");
        }
        //本地网址
        List<SiteBean> siteBeanList = mSiteDBManager.loadAll();
        for (SiteBean siteBean : siteBeanList) {
            lexiconContents.append(siteBean.getName()).append("\n");
        }

        lexiconContents.append(AppUtil.words2Contents());
        updateLocation(lexiconContents.toString());
    }

    public void updateLocation(String lexiconContents) {
        mIat = SpeechRecognizer.createRecognizer(this, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Print.e("初始化失败，错误码：" + code);
                }
                Print.e("local initIat success");
            }
        });
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 指定资源路径
        mIat.setParameter(ResourceUtil.ASR_RES_PATH,
                ResourceUtil.generateResourcePath(NovelApp.getInstance().getApplicationContext(),
                        ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        // 指定语法路径
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, Constants.GRM_PATH);
        // 指定语法名字
        mIat.setParameter(SpeechConstant.GRAMMAR_LIST, "local");
        // 设置文本编码格式
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // lexiconName 为词典名字，lexiconContents 为词典内容，lexiconListener 为回调监听器
        int ret = mIat.updateLexicon("voice", lexiconContents, listener);
        if (ret != ErrorCode.SUCCESS) {
            Print.e("更新词典失败,错误码：" + ret);
        }
    }

    private LexiconListener listener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error == null) {
                Print.e("词典更新成功");
                onLexiconSuccess();
            } else {
                Print.e("词典更新失败,错误码：" + error.getErrorCode());
                onLexiconError(error.getErrorDescription());
            }
        }
    };

    public void onLexiconSuccess() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_CODE, saveLocalId);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onLexiconError(String error) {
        showToast(error);
    }
}
