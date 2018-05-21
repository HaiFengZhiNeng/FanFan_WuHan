package com.fanfan.novel.service.stragry.local;

import android.content.Context;

import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.service.stragry.SpecialStrategy;
import com.fanfan.novel.service.stragry.Strategy;
import com.fanfan.robot.R;

/**
 * Created by android on 2018/2/6.
 */

public class ArtificialStrategy extends Strategy implements SpecialStrategy {

    @Override
    public SpecialType specialLocal(String speakTxt) {
        return txtInTxt(speakTxt, R.string.Artificial) ? SpecialType.Artificial : SpecialType.NoSpecial;
    }
}
