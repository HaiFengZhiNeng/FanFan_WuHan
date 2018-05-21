package com.fanfan.novel.common;

import com.fanfan.novel.service.udp.SocketManager;
import com.fanfan.robot.app.NovelApp;
import com.fanfan.novel.utils.FileUtil;

import java.io.File;

/**
 * Created by android on 2017/12/18.
 */

public class Constants {

 public static int displayWidth;
    public static int displayHeight;

    public static int IMSDK_APPID = 1400043768;

    public static int IMSDK_ACCOUNT_TYPE = 17967;

    public static String controlId = "fanc106";

    public static String serverId = "fanw105";
    public static String server2Id = "fanw106";
    public static String server3Id = "fanw107";
    public static String server4Id = "fanw108";
    public static final String roomAVId = "@TGS#2GOCMM6EN";

   /* public static boolean  Speak;*/
    public static boolean isDance;
    public static final String NET_LOONGGG_EXITAPP = "net.loonggg.exitapp";

    private static final String M_SDROOT_CACHE_PATH = FileUtil.getCacheDir(NovelApp.getInstance().getApplicationContext()) + File.separator;

    private static final String M_SDROOT_FILE_PATH = FileUtil.getExternalFileDir(NovelApp.getInstance().getApplicationContext()) + File.separator;

    public static final String PROJECT_PATH = M_SDROOT_FILE_PATH + "fHotel" + File.separator;
    public static final String PRINT_LOG_PATH = M_SDROOT_CACHE_PATH + "print";
    public static final String PRINT_TIMLOG_PATH = M_SDROOT_CACHE_PATH + "log" + File.separator;
    public static final String CRASH_PATH = M_SDROOT_CACHE_PATH + "crash";
    public static final String RECORDER_PATH = PROJECT_PATH + "Camera";
    public static final String DOWNLOAD_PATH = PROJECT_PATH + "download";

    public static final String GRM_PATH = PROJECT_PATH + "msc";

    public static final String PICTURETAKEN = "pictureTaken";

    public static String RES_DIR_NAME = "robotResources";

    public static final String IAT_CLOUD_BUILD = "iat_cloud_build";
    public static final String IAT_LOCAL_BUILD = "iat_local_build";
    public static final String IAT_CLOUD_UPDATELEXICON = "iat_cloud_updatelexicon";

    public static final String IAT_LOCAL_UPDATELEXICON = "iat_local_updatelexicon";

    public static final String QUERYLANAGE = "query_lanage";
    public static final String IAT_LINE_LANGUAGE = "iat_line_language";

    public static final String IAT_LOCAL_LANGUAGE = "iat_local_language";
    public static final String IAT_LINE_LANGUAGE_TALKER = "iat_line_language_talker";
    public static final String IAT_LOCAL_LANGUAGE_TALKER = "iat_local_language_talker";
    public static final String IAT_LINE_LANGUAGE_HEAR = "iat_line_language_hear";

    public static final String IAT_LOCAL_LANGUAGE_HEAR = "iat_local_language_hear";

    public static final String IS_INITIALIZATION = "is_initialization";

    public static final String LINE_SPEED = "line_speed";
    public static final String LINE_VOLUME = "line_volume";

    public static final String CITY_NAME = "city_name";

 public static final String IS_SET_PWD = "is_set_pwd";
    public static final String SET_PWD = "set_pwd";

    //udp
    public static String IP;
    public static int PORT = SocketManager.DEFAULT_UDPSERVER_PORT;

    public static String CONNECT_IP = null;

    public static int CONNECT_PORT = 0;

    //视频
    public static final String ASSEST_PATH = "file:///android_asset/";

    public static final String[] NAVIGATIONS = {"image_navigation"};

    public static final String MUSIC_UPDATE = "music_update";
    public static long lockingTime = 60 * 1000 * 100;
    public static String SPEAK_ACTION = "A50C8020AA";
    public static String STOP_DANCE = "A50C80E2AA";

    //true 是火车段的
    public static boolean isTrain = false;

    //true 是手机端的
    public static boolean unusual = false;

    public static String DOWNLOAD_FILENAME = "files/robot.apk";

}
