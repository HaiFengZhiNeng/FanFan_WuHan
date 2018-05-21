//package com.fanfan.novel.common.instance;
//
//import android.content.Context;
//
//import com.iflytek.cloud.InitListener;
//import com.iflytek.cloud.SpeechSynthesizer;
//
///**
// * Created by android on 2017/12/21.
// */
//
//public class SpeakTts {
//
//    private SpeechSynthesizer mTts;
//
//    private volatile static SpeakTts mSpeakTts;
//
////    public static SpeakTts getInstance() {
////        if (mSpeakTts == null) {
////            synchronized (SpeakTts.class) {
////                if (mSpeakTts == null) {
////                    mSpeakTts = new SpeakTts();
////                }
////            }
////        }
////        return mSpeakTts;
////    }
//
//    public static SpeakTts getInstance() {
//        return new SpeakTts();
//    }
//
//    private SpeakTts() {
//    }
//
//    public void initTts(Context context, InitListener listener) {
//        if (mTts == null) {
//            mTts = SpeechSynthesizer.createSynthesizer(context, listener);
//        }
//    }
//
//    public SpeechSynthesizer mTts() {
//        return mTts;
//    }
//
//}
