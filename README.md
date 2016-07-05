# MNVideoPlayer
SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。

##项目截图：
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/001.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/002.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/003.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/004.jpg)

##使用步骤：
####1：添加mnvideoplayerlibrary为moudle
####2：布局文件添加
            <com.maning.mnvideoplayerlibrary.player.MNViderPlayer
                android:id="@+id/mn_videoplayer"
                android:layout_width="match_parent"
                android:layout_height="200dp"
                android:background="#363636"
                app:mnFirstNeedPlay="false"     //true：初始化完成后立马播放
                />

####3：代码调用
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

            //----------------------------------
            //第二次播放调用：
            mnViderPlayer.playVideo(url1, "标题1");


