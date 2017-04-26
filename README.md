# MNVideoPlayer
SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。

## 注意：
一个基础代码，还有很多不完善的地方，可以作为一个参考，代码不多，建议看看代码。

## 项目截图：

![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/001.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/002.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/003.jpg)
![](https://github.com/maning0303/MNVideoPlayer/raw/master/screenshots/004.jpg)


## 如何添加
### Gradle添加：
#### 1.在Project的build.gradle中添加仓库地址
``` gradle
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

#### 2.在app目录下的build.gradle中添加依赖
``` gradle
	dependencies {
	     compile 'com.github.maning0303:MNVideoPlayer:V1.0.0'
	}
```

### moudle添加(建议这种方式代码简单,便于修改)：
#### 1：添加mnvideoplayerlibrary为moudle

## 使用步骤：
#### 1：布局文件添加
``` java

            <com.maning.mnvideoplayerlibrary.player.MNViderPlayer
                android:id="@+id/mn_videoplayer"
                android:layout_width="match_parent"
                android:layout_height="200dp"
                android:background="#363636"
                app:mnFirstNeedPlay="false"     //true：初始化完成后立马播放
                />
                
```

#### 2：代码调用
``` java
            mnViderPlayer.setWidthAndHeightProportion(16, 9);   //设置宽高比
            mnViderPlayer.setIsNeedBatteryListen(true);         //设置电量监听
            mnViderPlayer.setIsNeedNetChangeListen(true);       //设置网络监听
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


            //退出销毁
             @Override
             protected void onDestroy() {
                    //一定要记得销毁View
                    if (mnViderPlayer != null) {
                        mnViderPlayer.destroyVideo();
                        mnViderPlayer = null;
                    }
                    super.onDestroy();
             }
            
```

#### 2：注意事项:
``` java

             //问题:亮度调节不了是权限问题
             private void requestPermission() {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     if (!Settings.System.canWrite(this)) {
                         AlertDialog.Builder builder = new AlertDialog.Builder(this);
                         builder.setTitle("提示");
                         builder.setMessage("视频播放调节亮度需要申请权限");
                         builder.setNegativeButton("取消", null);
                         builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                         Uri.parse("package:" + getPackageName()));
                                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                 startActivityForResult(intent, 100);
                             }
                         });
                         builder.show();
                     }
                 }
             }

```

