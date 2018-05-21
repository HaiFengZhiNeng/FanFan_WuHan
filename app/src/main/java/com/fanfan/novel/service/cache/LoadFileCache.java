package com.fanfan.novel.service.cache;

import com.fanfan.novel.service.LoadFileService;

public class LoadFileCache {

    private LoadFileService mFileService;

    private LoadFileCache() {
    }

    private static class SingletonHolder {
        private static LoadFileCache instance = new LoadFileCache();
    }

    public static LoadFileCache get() {
        return LoadFileCache.SingletonHolder.instance;
    }

    public void setFileService(LoadFileService service) {
        mFileService = service;
    }

    public LoadFileService getFileService() {
        return mFileService;
    }
}
