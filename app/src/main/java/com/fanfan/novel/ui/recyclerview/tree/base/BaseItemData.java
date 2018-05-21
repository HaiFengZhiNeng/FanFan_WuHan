package com.fanfan.novel.ui.recyclerview.tree.base;

/**
 * Created by android on 2017/12/20.
 */

public abstract class BaseItemData {

    private int viewItemType;

    public void setViewItemType(int viewItemType) {
        this.viewItemType = viewItemType;
    }

    public int getViewItemType() {
        return viewItemType;
    }
}
