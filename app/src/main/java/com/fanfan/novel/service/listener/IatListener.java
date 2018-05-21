package com.fanfan.novel.service.listener;

import android.os.Bundle;
import android.text.TextUtils;

import com.fanfan.novel.model.local.Trans;
import com.fanfan.robot.app.NovelApp;
import com.fanfan.novel.model.local.Asr;
import com.fanfan.novel.model.local.Cw;
import com.fanfan.novel.model.local.Ws;
import com.fanfan.robot.app.RobotInfo;
import com.fanfan.youtu.utils.GsonUtil;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.seabreeze.log.Print;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * Created by dell on 2017/9/20.
 */

public class IatListener implements RecognizerListener {

    private StringBuffer mosaicSb;

    public IatListener(RecognListener recognListener) {
        mosaicSb = new StringBuffer();
        this.recognListener = recognListener;
    }

    @Override
    public void onVolumeChanged(int i, byte[] bytes) {

    }

    @Override
    public void onBeginOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }


    @Override
    public void onResult(RecognizerResult recognizerResult, boolean isHass) {
        Print.e(recognizerResult.getResultString());
        Print.e(mosaicSb.toString());
        JSONTokener tokener = new JSONTokener(recognizerResult.getResultString());
            int sn  ;
        try {
            JSONObject joResult = new JSONObject(tokener);
             sn = joResult.getInt("sn");

            if(sn == 2){
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String engineType = RobotInfo.getInstance().getEngineType();
        if (engineType.equals(SpeechConstant.TYPE_LOCAL)) {

            Asr local = GsonUtil.GsonToBean(recognizerResult.getResultString(), Asr.class);

            if (local.getSc() > 30) {

                List<Ws> wsList = local.getWs();
                for (int i = 0; i < wsList.size(); i++) {
                    Ws ws = wsList.get(i);
                    List<Cw> cwList = ws.getCw();
                    for (int j = 0; j < cwList.size(); j++) {
                        Cw cw = cwList.get(j);
                        if (!cw.getW().equals(mosaicSb.toString())) {
                            mosaicSb.append(cw.getW());
                        }
                    }
                }

                recognListener.onRecognResult(mosaicSb.toString().trim());
                mosaicSb.delete(0, mosaicSb.length());
            } else {
                Print.e("置信度小 : " + local.getSc());
                recognListener.onLevelSmall();
            }

        } else if (engineType.equals(SpeechConstant.TYPE_CLOUD)) {

            if (RobotInfo.getInstance().isTranslateEnable()) {
                Trans trans = GsonUtil.GsonToBean(recognizerResult.getResultString(), Trans.class);
                recognListener.onTranslate(trans.getTrans_result().getDst());
            } else {

                Asr line = GsonUtil.GsonToBean(recognizerResult.getResultString(), Asr.class);

                List<Ws> wsList = line.getWs();
                Print.e("3");
                for (int i = 0; i < wsList.size(); i++) {
                    Ws ws = wsList.get(i);
                    List<Cw> cwList = ws.getCw();
                    for (int j = 0; j < cwList.size(); j++) {
                        Cw cw = cwList.get(j);
                        mosaicSb.append(cw.getW());
                    }
                }

                    mosaicComplete(mosaicSb.toString());
                    mosaicSb.delete(0, mosaicSb.length());
            }
        }
    }

    private void mosaicComplete(String mosaic) {
        if (recognListener != null) {
            if (!TextUtils.isEmpty(mosaic)) {
             //   long ss = System.currentTimeMillis() - ddd;
           //     Print.e("时间 1    "+ss);
                recognListener.onRecognResult(mosaic.trim());
            } else {
                recognListener.onRecognDown();
            }
        } else {
            recognListener.onRecognDown();
        }
    }

    @Override
    public void onError(SpeechError speechError) {
        if (recognListener != null) {
            recognListener.onErrInfo(speechError.getErrorCode());
        }
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {
    }

    private RecognListener recognListener;

    public interface RecognListener {

        void onTranslate(String result);

        void onRecognResult(String result);

        void onErrInfo(int errorCode);

        void onRecognDown();

        void onLevelSmall();


    }
}
