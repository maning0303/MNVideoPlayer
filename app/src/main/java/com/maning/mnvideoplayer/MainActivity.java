package com.maning.mnvideoplayer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.maning.mnvideoplayerlibrary.listener.OnCompletionListener;
import com.maning.mnvideoplayerlibrary.listener.OnNetChangeListener;
import com.maning.mnvideoplayerlibrary.listener.OnScreenOrientationListener;
import com.maning.mnvideoplayerlibrary.player.MNViderPlayer;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MNViderPlayer";

    private final String url1 = "http://mp4.vjshi.com/2016-12-22/e54d476ad49891bd1adda49280a20692.mp4";
    private final String url2 = "http://mp4.vjshi.com/2016-12-22/e54d476ad49891bd1adda49280a20692.mp4";
    //这个地址是错误的
    private final String url3 = "http://weibo.com/p/23044451f0e5c4b762b9e1aa49c3091eea4d94";
    //本地视频
    private final String url4 = "/storage/emulated/0/test.mp4";

    private MNViderPlayer mnViderPlayer;
    private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initPlayer();
    }

    private void initViews() {
        mnViderPlayer = (MNViderPlayer) findViewById(R.id.mn_videoplayer);
    }

    private void initPlayer() {
        if (isInit) {
            return;
        }
        isInit = true;
        //初始化相关参数(必须放在Play前面)
        mnViderPlayer.setWidthAndHeightProportion(16, 9);   //设置宽高比
        mnViderPlayer.setIsNeedBatteryListen(true);         //设置电量监听
        mnViderPlayer.setIsNeedNetChangeListen(true);       //设置网络监听
        //第一次进来先设置数据
        mnViderPlayer.setDataSource(url1, "标题");
        //播放完成监听
        mnViderPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i(TAG, "播放完成----");
            }
        });

        mnViderPlayer.setOnScreenOrientationListener(new OnScreenOrientationListener() {
            @Override
            public void orientation_landscape() {
                Toast.makeText(MainActivity.this, "横屏", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void orientation_portrait() {
                Toast.makeText(MainActivity.this, "竖屏", Toast.LENGTH_SHORT).show();
            }
        });

        //网络监听
        mnViderPlayer.setOnNetChangeListener(new OnNetChangeListener() {
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
        mnViderPlayer.playVideo(url2, "标题2", 30000);
    }

    public void btn03(View view) {
        mnViderPlayer.playVideo(url3, "标题3");
    }

    public void btn04(View view) {
        if (hasPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            //判断本地有没有这个文件
            File file = new File(url4);
            if (file.exists()) {
                mnViderPlayer.playVideo(url4, "标题4");
            } else {
                Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
            }
        } else {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停
        mnViderPlayer.pauseVideo();
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
    protected void onDestroy() {
        //一定要记得销毁View
        if (mnViderPlayer != null) {
            mnViderPlayer.destroyVideo();
            mnViderPlayer = null;
        }
        super.onDestroy();
    }


    public boolean hasPermission(Context context, String permission) {
        int perm = context.checkCallingOrSelfPermission(permission);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "存储权限申请成功", Toast.LENGTH_SHORT).show();
                    initPlayer();
                } else {
                    Toast.makeText(this, "存储权限申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
