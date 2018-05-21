//package com.fanfan.novel.utils;
//
//import android.app.Activity;
//
//import com.fanfan.novel.common.Constants;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import okhttp3.ResponseBody;
//
///**
// * Created by Administrator on 2018/3/27/027.
// */
//
//public class UpdateUtil {
//
//    public UpdateUtil() {
//    }
//
//    public static void writeResponseBodyToDisk(Activity context, ResponseBody body, String fileName, final OnDownloadListener listener) {
//        File dir = new File(Constants.DOWNLOAD_PATH + File.separator);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        final File downFile = new File(Constants.DOWNLOAD_PATH + File.separator + fileName);
//
//        if (downFile.exists()) {
//            FileUtil.deleteFile(downFile.getAbsolutePath());
//        }
//        InputStream is = null;
//        FileOutputStream fos = null;
//        byte[] buf = new byte[2048];
//        int len = 0;
//
//        is = body.byteStream();
//        long total = body.contentLength();
//
//        try {
//            fos = new FileOutputStream(downFile);
//
//            long sum = 0;
//            int oldProgress = 0;
//
//            while ((len = is.read(buf)) != -1) {
//                fos.write(buf, 0, len);
//                sum += len;
//                final int progress = (int) (sum * 1.0f / total * 100);
//
//                if (oldProgress < progress) {
//                    // 下载中
//                    context.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            listener.onDownloading(progress);
//                        }
//                    });
//                    oldProgress = progress;
//                }
//            }
//            fos.flush();
//
//            context.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    listener.onDownloadSuccess(downFile);
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            context.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    listener.onDownloadFailed();
//                }
//            });
//
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public interface OnDownloadListener {
//        /**
//         * 下载成功
//         */
//        void onDownloadSuccess(File downFile);
//
//        /**
//         * @param progress 下载进度
//         */
//        void onDownloading(int progress);
//
//        /**
//         * 下载失败
//         */
//        void onDownloadFailed();
//
//    }
//
//}
