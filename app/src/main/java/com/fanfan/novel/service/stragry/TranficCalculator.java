package com.fanfan.novel.service.stragry;

import android.content.Context;

import com.fanfan.novel.common.enums.SpecialType;

/**
 * Created by android on 2018/2/6.
 */

public class TranficCalculator {

    public SpecialType specialLocal(String speakTxt, SpecialStrategy mStrategy) {
        return mStrategy.specialLocal(speakTxt);
    }
}
