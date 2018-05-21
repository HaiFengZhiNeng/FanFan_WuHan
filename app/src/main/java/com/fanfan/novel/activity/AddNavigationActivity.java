package com.fanfan.novel.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.fanfan.novel.utils.BitmapUtils;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.novel.utils.ImageLoader;
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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/8.
 */

public class AddNavigationActivity extends BarBaseActivity {

    @BindView(R.id.et_title)
    EditText etTitle;
    @BindView(R.id.et_guide)
    EditText etGuide;
    @BindView(R.id.et_datail)
    EditText etDatail;
    @BindView(R.id.tv_navigation)
    TextView tvNavigation;
    @BindView(R.id.tv_img)
    TextView tvImg;
    @BindView(R.id.img_navigation)
    ImageView imgNavigation;
    @BindView(R.id.card_view)
    CardView cardView;

    private String imagePath;//打开相册选择照片的路径
    private Uri outputUri;//裁剪万照片保存地址
    private boolean isClickCamera;//是否是拍照裁剪
    private String noOutputPath;

    public static final int CHOOSE_PHOTO = 2;//选择相册
    public static final int PICTURE_CUT = 3;//剪切图片
    public static final int SELECT_NO_PICTURE = 4;//剪切图片

    private static final int REQCODE_SELALBUM = 102;

    public static final String NAVIGATION_TITLE = "navigation_title";
    public static final String NAVIGATION_ID = "navigation_id";
    public static final int ADD_NAVIGATION_REQUESTCODE = 222;
    public static final String RESULT_CODE = "navigation_title_result";

    public static void newInstance(Activity context, String title, int requestCode) {
        Intent intent = new Intent(context, AddNavigationActivity.class);
        intent.putExtra(NAVIGATION_TITLE, title);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void newInstance(Activity context, int requestCode) {
        Intent intent = new Intent(context, AddNavigationActivity.class);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void newInstance(Activity context, long id, int requestCode) {
        Intent intent = new Intent(context, AddNavigationActivity.class);
        intent.putExtra(NAVIGATION_ID, id);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private long saveLocalId;

    private NavigationDBManager mNavigationDBManager;

    private NavigationBean navigationBean;

    private int curNavigation;

    private boolean isClick;

    private SpeechRecognizer mIat;
    private VideoDBManager mVideoDBManager;
    private VoiceDBManager mVoiceDBManager;
    private SiteDBManager mSiteDBManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_navigation;
    }

    @Override
    protected void initData() {
        saveLocalId = getIntent().getLongExtra(NAVIGATION_ID, -1);
        String title = getIntent().getStringExtra(NAVIGATION_TITLE);

        mNavigationDBManager = new NavigationDBManager();

        if (saveLocalId != -1) {

            navigationBean = mNavigationDBManager.selectByPrimaryKey(saveLocalId);
            etTitle.setText(navigationBean.getTitle());
            etGuide.setText(navigationBean.getGuide());
            etDatail.setText(navigationBean.getDatail());
            curNavigation = valueForArray(R.array.navigation, navigationBean.getNavigation());
            String savePath = navigationBean.getImgUrl();
            if (savePath != null) {
                if (new File(savePath).exists()) {
                    loadImgNavigation(savePath);
                }
            }
        }
        if (curNavigation < 0)
            curNavigation = 0;

        tvNavigation.setText(resArray(R.array.navigation)[curNavigation]);
    }

    @Override
    protected void onDestroy() {
        if (mIat != null) {
            mIat.cancel();
        }
//        if(aw.g != null) {
//            aw.g = null;
//        }
        listener = null;
        super.onDestroy();
        System.gc();
    }

    @OnClick({R.id.tv_img, R.id.tv_navigation})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_img:
                if (isEmpty(etTitle)) {
                    showToast("地点不能为空!");
                } else {
                    selectFromAlbum();//打开相册
                }
                break;
            case R.id.tv_navigation:
                DialogUtils.showLongListDialog(AddNavigationActivity.this, "目的地", R.array.navigation, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        curNavigation = position;
                        tvNavigation.setText(text);
                    }
                });
                break;
        }

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
                if (isClick) {
                    break;
                }
                isClick = true;
                if (isEmpty(etTitle)) {
                    showToast("地点不能为空!");
                    break;
                }
                if (isEmpty(etDatail)) {
                    showToast("地点详情不能为空!");
                    break;
                }
                if (isEmpty(etGuide)) {
                    showToast("引导语不能为空!");
                    break;
                }
                if (saveLocalId == -1) {
                    List<NavigationBean> been = mNavigationDBManager.queryNavigationByQuestion(etTitle.getText().toString().trim());
                    if (!been.isEmpty()) {
                        showToast("请不要添加相同的地点！");
                        break;
                    }
                }
                navigationIsexit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO://打开相册
                // 判断手机系统版本号
                if (data != null) {
                    Uri uri = data.getData();
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        imagePath = BitmapUtils.handleImageOnKitKat(this, uri);
                        outputUri = BitmapUtils.cropPhoto(this, uri, "imgnav", etTitle.getText().toString() + ".jpg", PICTURE_CUT);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        imagePath = BitmapUtils.getImagePath(this, uri, null);
                        outputUri = BitmapUtils.cropPhoto(AddNavigationActivity.this, uri, "imgnav", etTitle.getText().toString() + ".jpg", PICTURE_CUT);
                    }
                }
                break;
            case PICTURE_CUT://裁剪完成
                isClickCamera = true;
                if (isClickCamera) {
                    loadImgNavigation(outputUri);
                } else {
                    loadImgNavigation(outputUri);
                }
                break;
            case SELECT_NO_PICTURE:
                if (data != null) {
                    Uri uri = data.getData();
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        noOutputPath = BitmapUtils.handleImageOnKitKat(this, uri);
                        loadImgNavigation(noOutputPath);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        noOutputPath = BitmapUtils.getImagePath(this, uri, null);
                        loadImgNavigation(noOutputPath);
                    }
                }
                break;
        }
    }

    private void loadImgNavigation(String path) {
        cardView.setVisibility(View.VISIBLE);
        ImageLoader.loadLargeImage(AddNavigationActivity.this, imgNavigation, path, R.mipmap.video_image);
    }

    private void loadImgNavigation(Uri uri) {
        cardView.setVisibility(View.VISIBLE);
        ImageLoader.loadLargeImage(AddNavigationActivity.this, imgNavigation, uri, R.mipmap.video_image);
    }

    private void selectFromAlbum() {
        if (ContextCompat.checkSelfPermission(AddNavigationActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddNavigationActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQCODE_SELALBUM);
        } else {
//            openAlbum();
            selectPicture();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    /**
     * 从相冊选择照片（不裁切）
     */
    private void selectPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);//Pick an item from the data
        intent.setType("image/*");//从全部图片中进行选择
        startActivityForResult(intent, SELECT_NO_PICTURE);
    }


    private void navigationIsexit() {
        if (navigationBean == null) {
            navigationBean = new NavigationBean();
        }
        navigationBean.setSaveTime(System.currentTimeMillis());
        navigationBean.setTitle(getText(etTitle));
        navigationBean.setGuide(getText(etGuide));
        navigationBean.setDatail(getText(etDatail));
        navigationBean.setNavigation(resArray(R.array.navigation)[curNavigation]);
        navigationBean.setNavigationData(resArray(R.array.navigation_data)[curNavigation]);
        setNavigationimg(navigationBean);

        if (saveLocalId == -1) {//直接添加
            saveLocalId = mNavigationDBManager.insertForId(navigationBean);
        } else {//更新
            navigationBean.setId(saveLocalId);
            mNavigationDBManager.update(navigationBean);
        }

        if (saveLocalId == -1) {
            throw new IllegalArgumentException("DB error");
        } else {
            mVideoDBManager = new VideoDBManager();
            mVoiceDBManager = new VoiceDBManager();
            mSiteDBManager = new SiteDBManager();

            updateContents();
        }
    }

    private void setNavigationimg(NavigationBean bean) {
        if (outputUri != null) {
            String imagePath = BitmapUtils.getPathByUri4kitkat(AddNavigationActivity.this, outputUri);
            Print.e(imagePath);
            bean.setImgUrl(imagePath);
        } else {
            if (noOutputPath != null) {
                bean.setImgUrl(noOutputPath);
            }
        }
    }

    private int valueForArray(int resId, String compare) {
        String[] arrays = resArray(resId);
        return Arrays.binarySearch(arrays, compare);
    }

    private boolean isEmpty(TextView textView) {
        return textView.getText().toString().trim().equals("") || textView.getText().toString().trim().equals("");
    }

    private String[] resArray(int resId) {
        return getResources().getStringArray(resId);
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
