package com.fanfan.novel.service.stragry.local;

import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.service.stragry.SpecialStrategy;
import com.fanfan.novel.service.stragry.Strategy;
import com.fanfan.robot.R;

/**
 * Created by android on 2018/2/6.
 */

public class ControlStrategy extends Strategy implements SpecialStrategy {

    @Override
    public SpecialType specialLocal(String speakTxt) {
        if (txtInTxt(speakTxt, R.string.Next)) {
            return SpecialType.Next;
        } else if (txtInTxt(speakTxt, R.string.Lase)) {
            return SpecialType.Lase;
        }
        return SpecialType.NoSpecial;
    }
}
