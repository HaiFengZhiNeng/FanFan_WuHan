package com.fanfan.novel.model;

import com.fanfan.novel.ui.recyclerview.tree.base.BaseItemData;

import java.util.List;

/**
 * Created by android on 2017/12/20.
 */

public class LocalBean<T> extends BaseItemData {

    private String title;

    private List<T> singleBeen;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<T> getSingleBeen() {
        return singleBeen;
    }

    public void setSingleBeen(List<T> singleBeen) {
        this.singleBeen = singleBeen;
    }
}
