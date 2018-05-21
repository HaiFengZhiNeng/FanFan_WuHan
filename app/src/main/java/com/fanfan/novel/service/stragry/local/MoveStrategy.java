package com.fanfan.novel.service.stragry.local;

import android.content.Context;

import com.fanfan.novel.common.enums.SpecialType;
import com.fanfan.novel.service.stragry.SpecialStrategy;
import com.fanfan.novel.service.stragry.Strategy;
import com.fanfan.robot.R;

/**
 * Created by android on 2018/2/6.
 */

public class MoveStrategy extends Strategy implements SpecialStrategy {

    @Override
    public SpecialType specialLocal(String speakTxt) {

        if (txtInTxt(speakTxt, R.string.Forward)) {
            return SpecialType.Forward;
        } else if (txtInTxt(speakTxt, R.string.Backoff)) {
            return SpecialType.Backoff;
        } else if (txtInTxt(speakTxt, R.string.Turnleft)) {
            return SpecialType.Turnleft;
        } else if (txtInTxt(speakTxt, R.string.Turnright)) {
            return SpecialType.Turnright;
        }
        return SpecialType.NoSpecial;
    }
}
