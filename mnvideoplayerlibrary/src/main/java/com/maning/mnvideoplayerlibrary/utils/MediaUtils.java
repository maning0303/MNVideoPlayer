package com.maning.mnvideoplayerlibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : maning
 * @date : 2020-04-24
 * @desc :
 */
public class MediaUtils {

    private static Map<String, VideoInfoBean> mVideoInfoCacheMap = new HashMap<>();

    public static class VideoInfoBean implements Serializable {
        private static final long serialVersionUID = 432751628698075887L;
        public int duration;
        public int width;
        public int height;
        public Bitmap bitmap;

        @Override
        public String toString() {
            return "VideoInfoBean{" +
                    "duration=" + duration +
                    ", width=" + width +
                    ", height=" + height +
                    ", bitmap=" + bitmap +
                    '}';
        }
    }

    public static VideoInfoBean getLocalVideoInfo(String path) {
        if (mVideoInfoCacheMap.containsKey(path)) {
            VideoInfoBean videoInfoBeanCache = mVideoInfoCacheMap.get(path);
            return videoInfoBeanCache;
        }
        VideoInfoBean videoInfoBean = new VideoInfoBean();
        //视频支持的格式有：3gp、mp4、mkv、avi、m4v、ts、swf等
        //不支持：rmvb、rm、wmv。
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int videoWidth = 0;
        int videoHeight = 0;
        int duration = 0;
        try {
            //根据url获取缩略图
            retriever.setDataSource(path);
            //获得第一帧图片
            Bitmap bitmap = retriever.getFrameAtTime();
            if (bitmap != null) {
                videoInfoBean.bitmap = bitmap;
                videoWidth = bitmap.getWidth();
                videoHeight = bitmap.getHeight();
            } else {
                videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                //竖屏的时候 orientation = 90，横屏 orientation = 0
                String orientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if ("90".equals(orientation) || "270".equals(orientation)) {
                    //交换一下高度
                    int tempWidth = videoWidth;
                    videoWidth = videoHeight;
                    videoHeight = tempWidth;
                }
            }
            duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            videoWidth = 0;
            videoHeight = 0;
            duration = 0;
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        videoInfoBean.width = videoWidth;
        videoInfoBean.height = videoHeight;
        videoInfoBean.duration = duration / 1000;
        mVideoInfoCacheMap.put(path, videoInfoBean);
        return videoInfoBean;
    }

    /**
     * 获取视频的第一帧图片
     */
    public static void getImageForVideo(Context context, String path, OnLoadVideoImageListener listener) {
        getImageForVideo(context, path, 0, 0, listener);
    }

    public static void getImageForVideo(Context context, String path, int width, int height, OnLoadVideoImageListener listener) {
        if (mVideoInfoCacheMap.containsKey(path)) {
            VideoInfoBean videoInfoBean = mVideoInfoCacheMap.get(path);
            listener.onLoadImage(videoInfoBean);
            return;
        }
        LoadVideoImageTask task = new LoadVideoImageTask(listener, width, height);
        task.execute(path);
    }

    public static class LoadVideoImageTask extends AsyncTask<String, Integer, VideoInfoBean> {
        private OnLoadVideoImageListener listener;
        private int width;
        private int height;

        public LoadVideoImageTask(OnLoadVideoImageListener listener, int width, int height) {
            this.listener = listener;
            this.width = width;
            this.height = height;
        }

        @Override
        protected VideoInfoBean doInBackground(String... params) {
            String path = params[0];
            VideoInfoBean videoInfoBean = getNetVideoBitmap(path, width, height);
            mVideoInfoCacheMap.put(path, videoInfoBean);
            return videoInfoBean;
        }

        @Override
        protected void onPostExecute(VideoInfoBean videoInfoBean) {
            super.onPostExecute(videoInfoBean);
            if (listener != null) {
                listener.onLoadImage(videoInfoBean);
            }
        }
    }

    public interface OnLoadVideoImageListener {
        void onLoadImage(VideoInfoBean videoInfoBean);

    }

    public static VideoInfoBean getNetVideoBitmap(String videoUrl, int width, int height) {
        VideoInfoBean videoInfoBean = new VideoInfoBean();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            int videoWidth;
            int videoHeight;

            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            Bitmap bitmap = retriever.getFrameAtTime();
            if (bitmap != null) {
                if (width > 0 && height > 0) {
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
                //尝试压缩
                bitmap = compressBitmap(bitmap);

                videoWidth = bitmap.getWidth();
                videoHeight = bitmap.getHeight();
            } else {
                videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                //竖屏的时候 orientation = 90，横屏 orientation = 0
                String orientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if ("90".equals(orientation) || "270".equals(orientation)) {
                    //交换一下高度
                    int tempWidth = videoWidth;
                    videoWidth = videoHeight;
                    videoHeight = tempWidth;
                }
            }
            int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            videoInfoBean.bitmap = bitmap;
            videoInfoBean.width = videoWidth;
            videoInfoBean.height = videoHeight;
            videoInfoBean.duration = duration / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return videoInfoBean;
    }

    /**
     * 压缩图片
     *
     * @param bitmap 被压缩的图片
     * @return 压缩后的图片
     */
    private static Bitmap compressBitmap(Bitmap bitmap) {
        int bitmapSize = getBitmapSize(bitmap);
        Log.e("======", "压缩前：bitmapSize:" + bitmapSize);
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        int bitmapSizeCompress = getBitmapSize(newBitmap);
        Log.e("======", "压缩后：bitmapSizeCompress:" + bitmapSizeCompress);
        return newBitmap;
    }


    /**
     * 得到bitmap的大小
     */
    private static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        // 在低版本中用一行的字节x高度
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    //加密字符串
    private static String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("utf-8"));
            byte[] encryption = md5.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    stringBuffer.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    stringBuffer.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            return info;
        }
    }

}
