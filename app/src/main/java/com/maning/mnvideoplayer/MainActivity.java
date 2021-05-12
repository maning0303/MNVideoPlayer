package com.maning.mnvideoplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.maning.mnvideoplayerlibrary.listener.OnCompletionListener;
import com.maning.mnvideoplayerlibrary.listener.OnNetChangeListener;
import com.maning.mnvideoplayerlibrary.listener.OnScreenOrientationListener;
import com.maning.mnvideoplayerlibrary.player.MNViderPlayer;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MNViderPlayer";

    private final String url1 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319125415785691.mp4";
    private final String url2 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319125415785691.mp4";
    //这个地址是错误的
    private final String url3 = "http://weibo.com/xxxx";

    private MNViderPlayer mnViderPlayer;
    private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initPermission();
        initPlayer();
    }

    private void initPermission() {
        XXPermissions.with(this)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {

                    }
                });
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
        mnViderPlayer.setDataSource(url1, "标题1");
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
        mnViderPlayer.playVideo(url2, "标题2", 30 * 1000);
    }

    public void btn03(View view) {
        mnViderPlayer.playVideo(url3, "错误的播放地址");
    }

    public void btn04(View view) {
        String name = "local_video.mp4";
        String path = getExternalCacheDir().getPath();
        String url_local = path + "/" + name;
        //判断本地有没有这个文件
        File file = new File(url_local);
        if (file.exists()) {
            mnViderPlayer.playVideo(url_local, "本地视频播放");
        } else {
            Utils.copy(this, name, path, name);
            mnViderPlayer.playVideo(url_local, "本地视频播放");
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

}
