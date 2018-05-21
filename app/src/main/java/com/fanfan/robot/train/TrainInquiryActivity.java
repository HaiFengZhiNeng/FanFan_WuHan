package com.fanfan.robot.train;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fanfan.novel.adapter.AirAdapter;
import com.fanfan.novel.adapter.animator.RegisterAnimation;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.model.AirQuery;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.SerialPresenter;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.novel.presenter.ipresenter.ISerialPresenter;
import com.fanfan.novel.service.SerialService;
import com.fanfan.novel.service.animator.SlideInOutBottomItemAnimator;
import com.fanfan.robot.R;
import com.fanfan.robot.model.Alarm;
import com.seabreeze.log.Print;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/3/7/007.
 */

public class TrainInquiryActivity extends BarBaseActivity implements ILocalSoundPresenter.ILocalSoundView,
        ISerialPresenter.ISerialView {
    public static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    private static final int REQUEST_CODE_LEFT = 200;
    private static final int REQUEST_CODE_RIGHT = 400;

    @BindView(R.id.tv_selectNum)
    TextView tvSelectNum;
    @BindView(R.id.tv_selectAdd)
    TextView tvSelectAdd;
    @BindView(R.id.et_selectNum)
    EditText etSelectNum;
    @BindView(R.id.ll_selectTime)
    LinearLayout llSelectTime;
    @BindView(R.id.tv_select)
    TextView tvSelect;
    @BindView(R.id.tv_selectTime)
    TextView tvSelectTime;
    @BindView(R.id.rv_air)
    RecyclerView rvAir;
    @BindView(R.id.iv_changeAdd)
    ImageView ivChangeAdd;
    @BindView(R.id.tv_selectLeft)
    TextView tvSelectLeft;
    @BindView(R.id.rl_selectLeft)
    RelativeLayout rlSelectLeft;
    @BindView(R.id.tv_selectRight)
    TextView tvSelectRight;
    @BindView(R.id.rl_selectRight)
    RelativeLayout rlSelectRight;
    @BindView(R.id.ll_selectAddress)
    LinearLayout llSelectAddress;
    @BindView(R.id.ll_inputNum)
    LinearLayout llInputNum;
    /**
     * 左边tv的镜像view
     */
    private ImageView copyViewLeft;
    /**
     * 右边tv的镜像view
     */
    private ImageView copyViewRight;
    private WindowManager mWindowManager;
    private int[] mLeftLocation;
    private int[] mRightLocation;
    private Bitmap mLeftCacheBitmap;
    private Bitmap mRightCacheBitmap;

    private Animation animation;

    private List<AirQuery> airListQuery = new ArrayList<>();
    private AirAdapter airAdapter;
    private LocalSoundPresenter mSoundPresenter;
    private SerialPresenter mSerialPresenter;

    private boolean isSelectWhat = true;

    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, TrainInquiryActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_train_inquiry;
    }

    @Override
    protected void initView() {
        super.initView();
        mWindowManager = getWindowManager();
        mSoundPresenter = new LocalSoundPresenter(this);
        mSoundPresenter.start();
        mSerialPresenter = new SerialPresenter(this);
        mSerialPresenter.start();
        airAdapter = new AirAdapter(airListQuery);

        rvAir.setAdapter(airAdapter);
    }

    @Override
    protected void initData() {


        //布局格式
        rvAir.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        rvAir.setItemAnimator(new SlideInOutBottomItemAnimator(rvAir));
        //进场动画
        rvAir.setLayoutAnimation(RegisterAnimation.getInstance());

        addTrainData();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @OnClick({R.id.tv_selectNum, R.id.tv_selectAdd, R.id.ll_selectTime, R.id.tv_select, R.id.iv_changeAdd, R.id.tv_selectLeft, R.id.tv_selectRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_selectNum://按照航班号
                isSelectWhat = true;

                break;
            case R.id.tv_selectAdd://按照地点
                isSelectWhat = false;

                break;
            case R.id.ll_selectTime://选择时间
                //   new CalendarDialogFragment().show(getSupportFragmentManager(), "test-simple-calendar");
                break;
            case R.id.tv_select://查询
        /*        if (isSelectWhat) {
                    doSelectByNum();
                } else {
                    doSelectByAddress();
                }*/
                break;
            case R.id.iv_changeAdd:
                doChange();
                ivChangeAdd.setEnabled(false);
                break;
            case R.id.tv_selectLeft:
                //   JumpItent.jump(AirQueryView.this, CitySelectView.class, REQUEST_CODE_LEFT);
                break;
            case R.id.tv_selectRight:
                //  JumpItent.jump(AirQueryView.this, CitySelectView.class, REQUEST_CODE_RIGHT);
                break;
        }
    }


    // 按照列车号查询

    public void selectNum(boolean isSelect) {

        tvSelectNum.setBackgroundResource(R.color.dodgerblue);
        tvSelectAdd.setBackgroundResource(R.color.white);

        tvSelectNum.setTextColor(getResources().getColor(R.color.white));
        tvSelectAdd.setTextColor(getResources().getColor(R.color.color_nav_select_one));

        llInputNum.setVisibility(View.VISIBLE);
        llSelectAddress.setVisibility(View.GONE);
    }

    // 按照始发地地查询

    public void selectAddress(boolean isSelect) {
        tvSelectNum.setBackgroundResource(R.color.white);
        tvSelectAdd.setBackgroundResource(R.color.color_nav_select_one);

        tvSelectNum.setTextColor(getResources().getColor(R.color.color_nav_select_one));
        tvSelectAdd.setTextColor(getResources().getColor(R.color.white));

        llInputNum.setVisibility(View.GONE);
        llSelectAddress.setVisibility(View.VISIBLE);
    }

/*
    // 按照航班号查询
    public void doSelectByNum() {
        String airNum = etSelectNum.getText().toString().trim();
        String airTime = tvSelectTime.getText().toString().trim();
        if (!TextUtils.isEmpty(airNum) || !TextUtils.isEmpty(airTime)) {
            airListQuery = mAirQueryPresenter.doSelectByNum(airNum);
            if (airListQuery != null && airListQuery.size() > 0) {
                airAdapter.refreshData(airListQuery);
            } else {
                showToast("暂无相应航班");
            }
        } else {
            showToast("请输入航班号或出发时间");
        }
    }
*/

  /*  //   按照 地点 查询
    public void doSelectByAddress() {
        String airTime = tvSelectTime.getText().toString().trim();
        String start = tvSelectLeft.getText().toString().trim();
        String end = tvSelectRight.getText().toString().trim();
        if (!TextUtils.isEmpty(airTime)) {
            airListQuery = mAirQueryPresenter.doSelectByAddress(start, end);
            if (airListQuery != null && airListQuery.size() > 0) {
                airAdapter.refreshData(airListQuery);
            } else {
                showToast("暂无相应航班");
            }
        } else {
            showToast("请输入航班号或出发时间");
        }
    }*/

    // 出发地和目的地进行交换

    /**
     * 获取tv的属性,计算偏移量,
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void doChange() {

        //获取tv控件距离父控件的位置
        int leftRight = tvSelectLeft.getRight();
        int rightLeft = tvSelectRight.getLeft();

        //包裹右侧tv距离父控件的距离
        int rlRight = rlSelectRight.getRight();
        int rlLeft = rlSelectRight.getLeft();
        //在哪里设的padding就要用哪个控件来获取padding值
        int paddingStart = llSelectAddress.getPaddingStart();

        //左侧textview需要移动的距离
        int leftOffset = rlRight - leftRight - paddingStart;
        //右侧textview需要移动的距离
        int rightOffset = rlLeft + rightLeft - paddingStart;

        //创建出镜像view
        createCopyView();

        //隐藏掉两边的tv
        tvSelectLeft.setVisibility(View.INVISIBLE);
        tvSelectRight.setVisibility(View.INVISIBLE);

        //开启镜像view的动画
        leftAnim(leftOffset, mLeftLocation[0]);
        rightAnim(rightOffset, mRightLocation[0]);

        //箭头旋转动画
        ivChangeAdd.startAnimation(animation);
    }

    /**
     * 创建镜像view
     */
    private void createCopyView() {
        mLeftLocation = new int[2];
        mRightLocation = new int[2];

        //获取相对window的坐标
        tvSelectLeft.getLocationInWindow(mLeftLocation);
        tvSelectRight.getLocationInWindow(mRightLocation);

        //获取左边tv的缓存bitmap
        tvSelectLeft.setDrawingCacheEnabled(true);
        mLeftCacheBitmap = Bitmap.createBitmap(tvSelectLeft.getDrawingCache());
        tvSelectLeft.destroyDrawingCache();

        //获取右边tv的缓存bitmap
        tvSelectRight.setDrawingCacheEnabled(true);
        mRightCacheBitmap = Bitmap.createBitmap(tvSelectRight.getDrawingCache());
        tvSelectRight.destroyDrawingCache();

        //创建出两个镜像view
        copyViewLeft = createCopyView(mLeftLocation[0], mLeftLocation[1], mLeftCacheBitmap);
        copyViewRight = createCopyView(mRightLocation[0], mRightLocation[1], mRightCacheBitmap);
        //释放bitmap资源...这我不确定是不是这么做
        mLeftCacheBitmap = null;
        mRightCacheBitmap = null;
    }

    /**
     * 左侧镜像view的动画
     *
     * @param offset 偏移量
     * @param defX   原始位置的x
     */
    private void leftAnim(int offset, final int defX) {
        ValueAnimator leftAnimV = ValueAnimator.ofInt(0, offset);
        leftAnimV.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) copyViewLeft.getLayoutParams();
                //往右边移动所以x是变大的
                layoutParams.x = defX + animatedValue;
                mWindowManager.updateViewLayout(copyViewLeft, layoutParams);
            }
        });
        leftAnimV.setDuration(600);
        leftAnimV.start();
        //左侧动画监听
        leftAnimV.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //改变值
                String sLeft = tvSelectLeft.getText().toString();
                String sRight = tvSelectRight.getText().toString();
                tvSelectLeft.setText(sRight);
                tvSelectRight.setText(sLeft);
                tvSelectLeft.setVisibility(View.VISIBLE);
                mWindowManager.removeView(copyViewLeft);
                copyViewLeft = null;
                ivChangeAdd.setEnabled(true);
            }
        });
    }

    /**
     * 右侧镜像view动画
     *
     * @param offset 偏移量
     * @param defX   原始位置的x
     */
    private void rightAnim(int offset, final int defX) {
        ValueAnimator rightAnimV = ValueAnimator.ofInt(0, offset);
        rightAnimV.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) copyViewRight.getLayoutParams();
                layoutParams.x = defX - animatedValue;
                mWindowManager.updateViewLayout(copyViewRight, layoutParams);
            }
        });
        rightAnimV.setDuration(600);
        rightAnimV.start();
        rightAnimV.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvSelectRight.setVisibility(View.VISIBLE);
                mWindowManager.removeView(copyViewRight);
                copyViewRight = null;
            }
        });
    }

    /**
     * 创建镜像view
     *
     * @param x
     * @param y
     * @param bitmap
     */
    private ImageView createCopyView(int x, int y, Bitmap bitmap) {
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT;            //图片之外其他地方透明
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = x;   //设置imageView的原点
        mLayoutParams.y = y;
        mLayoutParams.alpha = 1f;                                //设置透明度
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ImageView copyView = new ImageView(this);
        copyView = new ImageView(this);
        copyView.setImageBitmap(bitmap);
        mWindowManager.addView(copyView, mLayoutParams);   //添加该iamgeView到window
        return copyView;
    }


    private void addTrainData() {


        airListQuery.add(new AirQuery("T298", "00:06", "10:50", "长春", "32张", "北京", "10小时44分", "候车中"));
        airListQuery.add(new AirQuery("G400", "07:52", "14:48", "长春", "0张", "北京", "6小时12分", "正在检票"));
        airListQuery.add(new AirQuery("Z4016", "08:46", "20:30", "长春", "125张", "北京南", "11小时44分", "未发车"));
        airListQuery.add(new AirQuery("K4022", "09:40", "01:06", "长春", "0张", "北京南", "15小时26分", "正在检票"));
        airListQuery.add(new AirQuery("D74", "11:05", "17:47", "长春", "13张", "北京南", "6小时42分", "大约晚点5分钟"));
        airListQuery.add(new AirQuery("D26", "11:06", "17:28", "长春西", "56张", "北京南", "6小时22分", "候车中"));
        airListQuery.add(new AirQuery("G240", "11:14", "16:29", "长春", "2张", "北京南", "5小时15分", "候车中"));
        airListQuery.add(new AirQuery("D102", "12:02", "18:31", "长春西", "133张", "北京", "6小时29分", "候车中"));
        airListQuery.add(new AirQuery("Z158", "12:48", "21:08", "长春", "0张", "北京南", "8小时20分", "候车中"));
        airListQuery.add(new AirQuery("T3018", "14:19", "03:18", "长春", "0张", "北京", "12小时59分", "候车中"));
        airListQuery.add(new AirQuery("D30", "16:00", "08:45", "长春", "78张", "北京南", "6小时22分%", "候车中"));
        airListQuery.add(new AirQuery("D22", "16:01", "08:45", "长春", "37张", "北京南", "6小时29分", "候车中"));

    }

    private void stopAction() {
        mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, Constants.STOP_DANCE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSoundPresenter.buildTts();
        addSpeakAnswer("你好，这里是列车查询页面");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAction();
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSoundPresenter.finish();
    }

    private void addSpeakAnswer(String messageContent, boolean isAction) {
        if (messageContent.length() > 0) {
            mSoundPresenter.doAnswer(messageContent);
            if (isAction) {
                speakingAddAction();
            }
        } else {
            onCompleted();
        }
    }

    private void addSpeakAnswer(String messageContent) {
        mSoundPresenter.doAnswer(messageContent);
    }

    private void addSpeakAnswer(int res) {
        mSoundPresenter.doAnswer(getResources().getString(res));
    }

    private void speakingAddAction() {
        Print.e("STOP_DANCE");
        mSerialPresenter.receiveMotion(SerialService.DEV_BAUDRATE, Constants.SPEAK_ACTION);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismissLoading() {

    }

    @Override
    public void showMsg(String msg) {

    }

    @Override
    public void showMsg(int msg) {

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void stopAll() {
        super.stopAll();
        stopAction();
        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    @Override
    public void onMoveStop() {

    }

    @Override
    public void onAlarm(Alarm alarm) {

    }

    @Override
    public void spakeMove(SpecialType type, String result) {

    }

    @Override
    public void openMap() {

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

    }

    @Override
    public void face(SpecialType type, String result) {

    }

    @Override
    public void control(SpecialType type, String result) {

    }

    @Override
    public void refLocalPage(String result) {

    }

    @Override
    public void onCompleted() {

    }
}





