package com.fanfan.novel.service.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fanfan.novel.ui.recyclerview.tree.base.BaseItemData;
import com.fanfan.youtu.api.base.event.BaseEvent;

/**
 * Created by android on 2017/12/20.
 */

public class ChildItemEvent extends BaseEvent<BaseItemData> {

    public ChildItemEvent() {
        super(null);
    }

    public ChildItemEvent(@Nullable String uuid, @NonNull Integer code, @Nullable BaseItemData baseItemData) {
        super(uuid, code, baseItemData);
    }
}
