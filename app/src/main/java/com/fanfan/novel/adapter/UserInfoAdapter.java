package com.fanfan.novel.adapter;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fanfan.novel.model.FaceAuth;
import com.fanfan.novel.utils.PreferencesUtils;
import com.fanfan.robot.R;

import java.util.List;

/**
 * Created by zhangyuanyuan on 2017/9/30.
 */

public class UserInfoAdapter extends BaseQuickAdapter<FaceAuth, BaseViewHolder> {

    public UserInfoAdapter(@Nullable List<FaceAuth> data) {
        super(R.layout.item_person, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FaceAuth item) {
        if (item.getAuthId() != null) {
            helper.setText(R.id.tv_info_id, item.getAuthId());
        } else {
            helper.setText(R.id.tv_info_id, "数据丢失，点击获取");
        }
    }

}
