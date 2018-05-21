package com.fanfan.novel.service.item;

import android.view.View;

import com.fanfan.robot.R;
import com.fanfan.novel.service.event.ChildItemEvent;
import com.fanfan.novel.ui.recyclerview.tree.ViewHolder;
import com.fanfan.novel.ui.recyclerview.tree.base.BaseItemData;
import com.fanfan.novel.ui.recyclerview.tree.item.TreeItem;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by android on 2017/12/20.
 */

public abstract class LocalChildItem<T> extends TreeItem<T> {

    @Override
    protected int initLayoutId() {
        return R.layout.item_sort_child;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder) {
        holder.setOnClickListener(R.id.child, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ChildItemEvent().setEvent(0, (BaseItemData) getData()));
            }
        });
    }

}
