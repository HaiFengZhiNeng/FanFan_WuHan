package com.fanfan.robot.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.presenter.ChatPresenter;
import com.fanfan.novel.presenter.LocalSoundPresenter;
import com.fanfan.novel.presenter.ipresenter.IChatPresenter;
import com.fanfan.novel.presenter.ipresenter.ILocalSoundPresenter;
import com.fanfan.robot.R;
import com.fanfan.robot.model.VrImage;
import com.fanfan.robot.train.PanoramicMapActivity;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.tencent.TIMMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WuHanCRActivity extends BarBaseActivity implements ILocalSoundPresenter.ILocalSoundView, IChatPresenter.IChatView{
    @BindView(R.id.cr_webView)
    WebView webView;
    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, WuHanCRActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }



    private LocalSoundPresenter mSoundPresenter;
    private ChatPresenter mChatPresenter;
    private ProgressDialog dialog;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_wu_han_cr;
    }

    @Override
    protected void initData() {


        mSoundPresenter = new LocalSoundPresenter(this);
        mSoundPresenter.start();
        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.start();

        //WebView加载本地资源
//        webView.loadUrl("file:///android_asset/example.html");
        //WebView加载web资源
        //启用支持Javascript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        //WebView加载页面优先使用缓存加载
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
   //     webView.loadUrl("http://crj.wuhan.gov.cn/");
        webView.loadUrl("http://www.wuhan.gov.cn/");
        //覆盖WebView默认通过第三方或者是系统浏览器打开网页的行为，使得网页可以在WebView中打开
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候是控制网页在WebView中去打开，如果为false调用系统浏览器或第三方浏览器打开
                view.loadUrl(url);
                return true;
            }
            //WebViewClient帮助WebView去处理一些页面控制和请求通知
        });

        //页面加载
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //newProgress   1-100之间的整数
                if (newProgress == 100) {
                    //页面加载完成，关闭ProgressDialog
                    closeDialog();
                } else {
                    //网页正在加载，打开ProgressDialog
                    openDialog(newProgress);
       /*             try{


                    }catch (Exception e){
                        e.printStackTrace();
                    }*/

                }
            }

            private void openDialog(int newProgress) {
                if (dialog == null) {
                    dialog = new ProgressDialog(getContext());
                    dialog.setTitle("正在加载");
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setProgress(newProgress);
                    dialog.setCancelable(true);
                    try {
                        dialog.show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    dialog.setProgress(newProgress);
                }
            }

            private void closeDialog() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        mVrPanoramaView.resumeRendering();*/

        mSoundPresenter.buildTts();
        mSoundPresenter.buildIat();

        addSpeakAnswer("为您打开武汉市政网站！");

    }

    @Override
    protected void onPause() {
        super.onPause();
/*        mVrPanoramaView.pauseRendering();*/

        mSoundPresenter.stopTts();
        mSoundPresenter.stopRecognizerListener();
        mSoundPresenter.stopHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*        mVrPanoramaView.shutdown();*/

        if(dialog!=null){
            dialog.cancel();
            dialog = null;
        }

        mSoundPresenter.finish();
        mChatPresenter.finish();
    }

/*    @OnClick(R.id.vr_panorama_view)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vr_panorama_view:

                break;
        }
    }*/

    private void addSpeakAnswer(String messageContent) {
        mSoundPresenter.doAnswer(messageContent);
    }

    private void addSpeakAnswer(int res) {
        mSoundPresenter.doAnswer(getResources().getString(res));
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
        addSpeakAnswer("此页面暂不支持此功能");
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
            finish();

        }
    }
}

