package com.maning.mnvideoplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.maning.mnvideoplayerlibrary.player.MNViderPlayer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MNViderPlayer";

    private final String url1 = "http://svideo.spriteapp.com/video/2016/0703/7b5bc740-4134-11e6-ac2b-d4ae5296039d_wpd.mp4";
    private final String url2 = "http://bvideo.spriteapp.cn/video/2016/0704/577a4c29e1f14_wpd.mp4";
    //这个地址是错误的
    private final String url3 = "http://weibo.com/p/23044451f0e5c4b762b9e1aa49c3091eea4d94";
    private MNViderPlayer mnViderPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mnViderPlayer = (MNViderPlayer) findViewById(R.id.mn_videoplayer);

        initPlayer();

    }

    private void initPlayer() {
        //初始化相关参数(必须放在Play前面)
        mnViderPlayer.setIsNeedBatteryListen(true);
        mnViderPlayer.setIsNeedNetChangeListen(true);
        //第一次进来先设置数据
        mnViderPlayer.setDataSource(url2, "标题2");

        //播放完成监听
        mnViderPlayer.setOnCompletionListener(new MNViderPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i(TAG, "播放完成----");
            }
        });

        //网络监听
        mnViderPlayer.setOnNetChangeListener(new MNViderPlayer.OnNetChangeListener() {
            @Override
            public void onWifi(MediaPlayer mediaPlayer) {
            }

            @Override
            public void onMobile(MediaPlayer mediaPlayer) {
                Toast.makeText(MainActivity.this, "请注意,当前网络状态切换为3G/4G网络", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNoAvailable(MediaPlayer mediaPlayer) {
                Toast.makeText(MainActivity.this, "当前网络不可用,检查网络设置", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void btn01(View view) {
        mnViderPlayer.playVideo(url1, "标题1");
    }

    public void btn02(View view) {
        //position表示需要跳转到的位置
        mnViderPlayer.playVideo(url2, "标题2", 1000);
    }

    public void btn03(View view) {
        mnViderPlayer.playVideo(url3, "标题3");
    }

    @Override
    public void onBackPressed() {
        if (mnViderPlayer.isFullScreen()) {
            mnViderPlayer.setOrientationPortrait();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mnViderPlayer.pauseVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //一定要记得销毁View
        if(mnViderPlayer != null){
            mnViderPlayer.destroyVideo();
            mnViderPlayer = null;
        }
        super.onDestroy();
    }
}
