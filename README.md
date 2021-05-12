# MNVideoPlayer
SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。
[![](https://jitpack.io/v/maning0303/MNVideoPlayer.svg)](https://jitpack.io/#maning0303/MNVideoPlayer)

## 注意：
一个基础代码，还有很多不完善的地方，只作为一个参考。

一个基础代码，还有很多不完善的地方，只作为一个参考。

一个基础代码，还有很多不完善的地方，只作为一个参考。

## 项目截图：


<div align="center">
<img src = "screenshots/videoplay_001.png" width=200 >
<img src = "screenshots/videoplay_002.png" width=200 >
<img src = "screenshots/videoplay_003.png" width=200 >
<img src = "screenshots/videoplay_004.png" width=200 >
</div>


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
            //AndroidX
	        implementation 'com.github.maning0303:MNVideoPlayer:V1.1.0'
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
                   android:background="#252525"
                   //首次打开是否需要自动播放
                   app:mnFirstNeedPlay="true" />
                
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

### 升级日志:
#### V1.0.9:
        1.修复亮度调节第一次0的问题
        2.优化布局显示

#### V1.0.8:
        1:修复本地视频首次播放无画面问题
        2:优化代码


## 推荐:
Name | Describe |
--- | --- |
[GankMM](https://github.com/maning0303/GankMM) | （Material Design & MVP & Retrofit + OKHttp & RecyclerView ...）Gank.io Android客户端：每天一张美女图片，一个视频短片，若干Android，iOS等程序干货，周一到周五每天更新，数据全部由 干货集中营 提供,持续更新。 |
[MNUpdateAPK](https://github.com/maning0303/MNUpdateAPK) | Android APK 版本更新的下载和安装,适配7.0,简单方便。 |
[MNImageBrowser](https://github.com/maning0303/MNImageBrowser) | 交互特效的图片浏览框架,微信向下滑动动态关闭 |
[MNCalendar](https://github.com/maning0303/MNCalendar) | 简单的日历控件练习，水平方向日历支持手势滑动切换，跳转月份；垂直方向日历选取区间范围。 |
[MClearEditText](https://github.com/maning0303/MClearEditText) | 带有删除功能的EditText |
[MNCrashMonitor](https://github.com/maning0303/MNCrashMonitor) | Debug监听程序崩溃日志,展示崩溃日志列表，方便自己平时调试。 |
[MNProgressHUD](https://github.com/maning0303/MNProgressHUD) | MNProgressHUD是对常用的自定义弹框封装,加载ProgressDialog,状态显示的StatusDialog和自定义Toast,支持背景颜色,圆角,边框和文字的自定义。 |
[MNXUtilsDB](https://github.com/maning0303/MNXUtilsDB) | xUtils3 数据库模块单独抽取出来，方便使用。 |
[MNVideoPlayer](https://github.com/maning0303/MNVideoPlayer) | SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。------代码简单，新手可以看一看。 |
[MNZXingCode](https://github.com/maning0303/MNZXingCode) | 快速集成二维码扫描和生成二维码 |
[MNChangeSkin](https://github.com/maning0303/MNChangeSkin) | Android夜间模式，通过Theme实现 |
[SwitcherView](https://github.com/maning0303/SwitcherView) | 垂直滚动的广告栏文字展示。 |
[MNPasswordEditText](https://github.com/maning0303/MNPasswordEditText) | 类似微信支付宝的密码输入框。 |
[MNSwipeToLoadDemo](https://github.com/maning0303/MNSwipeToLoadDemo) | 利用SwipeToLoadLayout实现的各种下拉刷新效果（饿了吗，京东，百度外卖，美团外卖，天猫下拉刷新等）。 |

