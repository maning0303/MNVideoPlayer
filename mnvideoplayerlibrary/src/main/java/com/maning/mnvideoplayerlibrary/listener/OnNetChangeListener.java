package com.maning.mnvideoplayerlibrary.listener;

import android.media.MediaPlayer;

/**
 * Created by maning on 2017/11/8.
 */

public interface OnNetChangeListener {

    //wifi
    void onWifi(MediaPlayer mediaPlayer);

    //手机
    void onMobile(MediaPlayer mediaPlayer);

    //不可用
    void onNoAvailable(MediaPlayer mediaPlayer);

}
