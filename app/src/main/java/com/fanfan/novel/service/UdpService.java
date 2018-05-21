package com.fanfan.novel.service;

import com.fanfan.novel.common.base.BaseService;
import com.fanfan.novel.service.event.SendUdpEvent;
import com.fanfan.novel.service.udp.SocketManager;
import com.seabreeze.log.Print;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by zhangyuanyuan on 2017/10/20.
 */

public class UdpService extends BaseService {

    @Override
    public void onCreate() {
        super.onCreate();
        SocketManager.getInstance().registerUdpServer();
    }

    @Override
    public void onDestroy() {
        SocketManager.getInstance().unregisterUdpServer();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(SendUdpEvent event) {
        if (event.isOk()) {
            String sendMsg = event.getBean();
            SocketManager.getInstance().sendTextByUDP(sendMsg);
            Print.e(sendMsg);
        } else {
            Print.e("ReceiveEvent error");
        }
    }

}
