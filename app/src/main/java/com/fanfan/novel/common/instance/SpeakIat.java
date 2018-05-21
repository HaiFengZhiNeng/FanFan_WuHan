//package com.fanfan.novel.common.instance;
//
//import android.content.Context;
//
//import com.iflytek.cloud.InitListener;
//import com.iflytek.cloud.SpeechRecognizer;
//
///**
// * Created by android on 2017/12/21.
// */
//
//public class SpeakIat {
//
//    private SpeechRecognizer mIat;
//
//    private volatile static SpeakIat mSpeakIat;
//
//    //    public static SpeakIat getInstance() {
////        if (mSpeakIat == null) {
////            synchronized (SpeakIat.class) {
////                if (mSpeakIat == null) {
////                    mSpeakIat = new SpeakIat();
////                }
////            }
////        }
////        return mSpeakIat;
////    }
//    public static SpeakIat getInstance() {
//        return new SpeakIat();
//    }
//
//    private SpeakIat() {
//
//    }
//
//    public void initIat(Context context, InitListener listener) {
//        if (mIat == null) {
//            mIat = SpeechRecognizer.createRecognizer(context, listener);
//        }
//    }
//
//    public SpeechRecognizer mIat() {
//        return mIat;
//    }
//}
