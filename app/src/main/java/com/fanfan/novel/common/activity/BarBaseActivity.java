package com.fanfan.novel.common.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.fanfan.novel.common.Constants;
import com.fanfan.robot.R;
import com.fanfan.youtu.api.base.event.BaseEvent;
import com.fanfan.youtu.utils.ErrorMsg;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.StaticPagerAdapter;
import com.jude.rollviewpager.hintview.ColorPointHintView;
import com.seabreeze.log.Print;

import java.util.Random;

/**
 * Created by zhangyuanyuan on 2017/12/15.
 */

public abstract class BarBaseActivity extends IMBaseActivity {

    @Override
    protected int setBackgroundGlide() {
        return 0;
    }

    @Override
    protected void setResult() {

    }

    public void onError(int code, String msg) {
        Print.e("onError  code : " + code + " ; msg : " + msg + " ; describe : " + ErrorMsg.getCodeDescribe(code));
    }

    public void onError(BaseEvent event) {
        Print.e("onError : " + event.getCode() + "  " + event.getCodeDescribe());
    }

    public String resFoFinal(int id) {
        String[] arrResult = getResources().getStringArray(id);
        return arrResult[new Random().nextInt(arrResult.length)];
    }

    public int resFoInter(String[] res) {
        return new Random().nextInt(res.length);
    }

    protected void stopAll() {

    }

    protected void isEmpty() {
        RelativeLayout rlLayout = findViewById(R.id.rl_empty);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        if (rlLayout != null && tvEmpty != null) {
            rlLayout.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    protected void isNuEmpty() {
        RelativeLayout rlLayout = findViewById(R.id.rl_empty);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        if (rlLayout != null && tvEmpty != null) {
            rlLayout.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
