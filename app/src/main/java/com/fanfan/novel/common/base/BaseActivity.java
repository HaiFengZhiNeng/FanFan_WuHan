package com.fanfan.novel.common.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.utils.PhoneUtil;
import com.fanfan.robot.R;
import com.fanfan.robot.activity.LockActivity;
import com.fanfan.robot.presenter.ScreenPresenter;
import com.fanfan.robot.presenter.ipersenter.IScreenPresenter;
import com.fanfan.robot.service.ScreenService;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

import static com.fanfan.novel.common.Constants.unusual;


public abstract class BaseActivity extends AppCompatActivity implements IScreenPresenter.ISreenView {

    protected Context mContext;
    protected Handler mHandler = new Handler();

    protected RelativeLayout backdrop;
    protected Toolbar toolbar;

    private ScreenPresenter mScreenPresenter;

    private static OnPermissionCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCollector.addActivity(this);
        setRequestedOrientation(getOrientation());
        super.onCreate(savedInstanceState);
        BaseApplication.addActivity(this);
        setBeforeLayout();
        mContext = this;
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        ButterKnife.bind(this);
        initView();
        initDb();
        initData();
        setListener();

        if (unusual) {
            Intent intent = new Intent(this, ScreenService.class);
            startService(intent);
        } else {
            mScreenPresenter = new ScreenPresenter(this);
        }
    }


    public int getOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    protected void setBeforeLayout() {
        PhoneUtil.getDispaly(this);
    }

    /**
     * 返回当前界面布局文件
     */
    protected abstract int getLayoutId();

    /**
     * 此方法描述的是： 初始化所有view
     */
    protected void initView() {
        backdrop();
        toolbar();
    }

    protected abstract int setBackgroundGlide();

    /**
     * 初始化数据库
     */
    protected void initDb() {

    }

    /**
     * 此方法描述的是： 初始化所有数据的方法
     */
    protected abstract void initData();


    /**
     * 此方法描述的是： 设置所有事件监听
     */
    protected void setListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.NET_LOONGGG_EXITAPP);
        this.registerReceiver(this.finishAppReceiver, filter);

        if (mScreenPresenter != null) {
            mScreenPresenter.startTipsTimer();
        }
    }

    /**
     * 显示toast
     *
     * @param resId
     */
    public void showToast(final int resId) {
        showToast(getString(resId));
    }

    /**
     * 显示toast
     *
     * @param resStr
     * @return Toast对象，便于控制toast的显示与关闭
     */
    public void showToast(final String resStr) {

        if (TextUtils.isEmpty(resStr)) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(BaseActivity.this, resStr, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mScreenPresenter != null) {
            mScreenPresenter.endTipsTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.finishAppReceiver);
        BaseApplication.removeActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract void setResult();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }

            if (deniedPermissions.isEmpty()) {
                if (callback != null) {
                    callback.onGranted();
                }
            } else {
                if (callback != null) {
                    callback.onDenied(deniedPermissions);
                }
            }

        }
    }

    public static void requestPermission(String[] permissions, OnPermissionCallback onPermissionCallback) {
        if (BaseApplication.getTopAcitivity() == null) {
            return;
        }
        callback = onPermissionCallback;
        List<String> permissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(BaseApplication.getTopAcitivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (!permissionsList.isEmpty()) {
            ActivityCompat.requestPermissions(BaseApplication.getTopAcitivity(), permissionsList.toArray(new String[permissionsList.size()]), 1);
        } else {
            if (callback != null) {
                callback.onGranted();
            }
        }
    }

    /**
     * 关闭Activity的广播，放在自定义的基类中，让其他的Activity继承这个Activity就行
     */
    protected BroadcastReceiver finishAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void backdrop() {
        backdrop = findViewById(R.id.backdrop);
        if (backdrop != null) {
            if (setBackgroundGlide() != 0) {
                Glide.with(this)
                        .asBitmap()
                        .load(setBackgroundGlide())
                        .into(new SimpleTarget<Bitmap>(Constants.displayWidth / 2, Constants.displayHeight / 2) {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                Drawable drawable = new BitmapDrawable(resource);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    backdrop.setBackground(drawable);
                                }
                            }
                        });
            }
        }
    }

    private void toolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setTitle("");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void finish() {
        super.finish();
        ActivityCollector.finishActivity(this);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mScreenPresenter != null) {
            mScreenPresenter.resetTipsTimer();
        }
    }

    @Override
    public void showTipsView() {
        LockActivity.newInstance(this);
    }

    public interface OnPermissionCallback {
        void onGranted();

        void onDenied(List<String> deniedPermissions);
    }
}
