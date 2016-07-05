package com.maning.mnvideoplayerlibrary.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maning.mnvideoplayerlibrary.R;
import com.maning.mnvideoplayerlibrary.utils.LightnessControl;
import com.maning.mnvideoplayerlibrary.utils.PlayerUtils;
import com.maning.mnvideoplayerlibrary.view.ProgressWheel;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by maning on 16/6/14.
 * 播放器
 */
public class MNViderPlayer extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        SurfaceHolder.Callback, GestureDetector.OnGestureListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = "MNViderPlayer";
    private Context context;
    private Activity activity;

    static final Handler myHandler = new Handler(Looper.getMainLooper()) {
    };

    // SurfaceView的创建比较耗时，要注意
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    //地址
    private String videoPath;
    private String videoTitle;
    private int video_position = 0;

    //控件的位置信息
    private float mediaPlayerX;
    private float mediaPlayerY;

    // 计时器
    private Timer timer_video_time;
    private TimerTask task_video_timer;
    private Timer timer_controller;
    private TimerTask task_controller;

    //是否是横屏
    private boolean isFullscreen = false;
    private boolean isLockScreen = false;
    private boolean isPrepare = false;
    private boolean isNeedBatteryListen = true;
    private boolean isNeedNetChangeListen = true;
    private boolean isFirstPlay = false;

    //控件
    private RelativeLayout mn_rl_bottom_menu;
    private SurfaceView mn_palyer_surfaceView;
    private ImageView mn_iv_play_pause;
    private ImageView mn_iv_fullScreen;
    private TextView mn_tv_time;
    private SeekBar mn_seekBar;
    private ImageView mn_iv_back;
    private TextView mn_tv_title;
    private TextView mn_tv_system_time;
    private RelativeLayout mn_rl_top_menu;
    private RelativeLayout mn_player_rl_progress;
    private ImageView mn_player_iv_lock;
    private LinearLayout mn_player_ll_error;
    private LinearLayout mn_player_ll_net;
    private ProgressWheel mn_player_progressBar;
    private ImageView mn_iv_battery;
    private ImageView mn_player_iv_play_center;

    public MNViderPlayer(Context context) {
        this(context, null);
    }

    public MNViderPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MNViderPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        activity = (Activity) this.context;
        //自定义属性相关
        initAttrs(context, attrs);
        //其他
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MNViderPlayer);
        //遍历拿到自定义属性
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.MNViderPlayer_mnFirstNeedPlay) {
                isFirstPlay = typedArray.getBoolean(R.styleable.MNViderPlayer_mnFirstNeedPlay, false);
            }
        }
        //销毁
        typedArray.recycle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int screenWidth = PlayerUtils.getScreenWidth(activity);
        int screenHeight = PlayerUtils.getScreenHeight(activity);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        //newConfig.orientation获得当前屏幕状态是横向或者竖向
        //Configuration.ORIENTATION_PORTRAIT 表示竖向
        //Configuration.ORIENTATION_LANDSCAPE 表示横屏
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //计算视频的大小16：9
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 9 / 16;

            setX(mediaPlayerX);
            setY(mediaPlayerY);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            layoutParams.width = screenWidth;
            layoutParams.height = screenHeight;

            setX(0);
            setY(0);
        }
        setLayoutParams(layoutParams);
    }

    //初始化
    private void init() {
        View inflate = View.inflate(context, R.layout.mn_player_view, this);
        mn_rl_bottom_menu = (RelativeLayout) inflate.findViewById(R.id.mn_rl_bottom_menu);
        mn_palyer_surfaceView = (SurfaceView) inflate.findViewById(R.id.mn_palyer_surfaceView);
        mn_iv_play_pause = (ImageView) inflate.findViewById(R.id.mn_iv_play_pause);
        mn_iv_fullScreen = (ImageView) inflate.findViewById(R.id.mn_iv_fullScreen);
        mn_tv_time = (TextView) inflate.findViewById(R.id.mn_tv_time);
        mn_tv_system_time = (TextView) inflate.findViewById(R.id.mn_tv_system_time);
        mn_seekBar = (SeekBar) inflate.findViewById(R.id.mn_seekBar);
        mn_iv_back = (ImageView) inflate.findViewById(R.id.mn_iv_back);
        mn_tv_title = (TextView) inflate.findViewById(R.id.mn_tv_title);
        mn_rl_top_menu = (RelativeLayout) inflate.findViewById(R.id.mn_rl_top_menu);
        mn_player_rl_progress = (RelativeLayout) inflate.findViewById(R.id.mn_player_rl_progress);
        mn_player_iv_lock = (ImageView) inflate.findViewById(R.id.mn_player_iv_lock);
        mn_player_ll_error = (LinearLayout) inflate.findViewById(R.id.mn_player_ll_error);
        mn_player_ll_net = (LinearLayout) inflate.findViewById(R.id.mn_player_ll_net);
        mn_player_progressBar = (ProgressWheel) inflate.findViewById(R.id.mn_player_progressBar);
        mn_iv_battery = (ImageView) inflate.findViewById(R.id.mn_iv_battery);
        mn_player_iv_play_center = (ImageView) inflate.findViewById(R.id.mn_player_iv_play_center);

        mn_seekBar.setOnSeekBarChangeListener(this);
        mn_iv_play_pause.setOnClickListener(this);
        mn_iv_fullScreen.setOnClickListener(this);
        mn_iv_back.setOnClickListener(this);
        mn_player_iv_lock.setOnClickListener(this);
        mn_player_ll_error.setOnClickListener(this);
        mn_player_ll_net.setOnClickListener(this);
        mn_player_iv_play_center.setOnClickListener(this);

        //初始化
        initViews();

        if (!isFirstPlay) {
            mn_player_iv_play_center.setVisibility(View.VISIBLE);
            mn_player_progressBar.setVisibility(View.GONE);
        }

        //初始化SurfaceView
        initSurfaceView();

        //初始化手势
        initGesture();

        //存储控件的位置信息
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayerX = getX();
                mediaPlayerY = getY();
                Log.i(TAG, "控件的位置---X：" + mediaPlayerX + "，Y：" + mediaPlayerY);
            }
        }, 1000);
    }

    private void initViews() {
        mn_tv_system_time.setText(PlayerUtils.getCurrentHHmmTime());
        mn_rl_bottom_menu.setVisibility(View.GONE);
        mn_rl_top_menu.setVisibility(View.GONE);
        mn_player_iv_lock.setVisibility(View.GONE);
        initLock();
        mn_player_rl_progress.setVisibility(View.VISIBLE);
        mn_player_progressBar.setVisibility(View.VISIBLE);
        mn_player_ll_error.setVisibility(View.GONE);
        mn_player_ll_net.setVisibility(View.GONE);
        mn_player_iv_play_center.setVisibility(View.GONE);
        initTopMenu();
    }

    private void initLock() {
        if (isFullscreen) {
            mn_player_iv_lock.setVisibility(View.VISIBLE);
        } else {
            mn_player_iv_lock.setVisibility(View.GONE);
        }
    }

    private void initSurfaceView() {
        Log.i(TAG, "initSurfaceView");
        // 得到SurfaceView容器，播放的内容就是显示在这个容器里面
        surfaceHolder = mn_palyer_surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        // SurfaceView的一个回调方法
        surfaceHolder.addCallback(this);
    }

    private void initTopMenu() {
        mn_tv_title.setText(videoTitle);
        if (isFullscreen) {
            mn_rl_top_menu.setVisibility(View.VISIBLE);
        } else {
            mn_rl_top_menu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.mn_iv_play_pause) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
                } else {
                    mediaPlayer.start();
                    mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
                }
            }
        } else if (i == R.id.mn_iv_fullScreen) {
            if (isFullscreen) {
                setProtrait();
            } else {
                setLandscape();
            }
        } else if (i == R.id.mn_iv_back) {
            setProtrait();
        } else if (i == R.id.mn_player_iv_lock) {
            if (isFullscreen) {
                if (isLockScreen) {
                    unLockScreen();
                    initBottomMenuState();
                } else {
                    lockScreen();
                    destroyControllerTask(true);
                }
            }
        } else if (i == R.id.mn_player_ll_error || i == R.id.mn_player_ll_net || i == R.id.mn_player_iv_play_center) {
            playVideo(videoPath, videoTitle, 0);
        }
    }

    //--------------------------------------------------------------------------------------
    // ######## 相关View的操作 ########
    //--------------------------------------------------------------------------------------

    private void unLockScreen() {
        isLockScreen = false;
        mn_player_iv_lock.setImageResource(R.drawable.mn_player_landscape_screen_lock_open);
    }

    private void lockScreen() {
        isLockScreen = true;
        mn_player_iv_lock.setImageResource(R.drawable.mn_player_landscape_screen_lock_close);
    }

    //下面菜单的显示和隐藏
    private void initBottomMenuState() {
        mn_tv_system_time.setText(PlayerUtils.getCurrentHHmmTime());
        if (mn_rl_bottom_menu.getVisibility() == View.GONE) {
            initControllerTask();
            mn_rl_bottom_menu.setVisibility(View.VISIBLE);
            if (isFullscreen) {
                mn_rl_top_menu.setVisibility(View.VISIBLE);
                mn_player_iv_lock.setVisibility(View.VISIBLE);
            }
        } else {
            destroyControllerTask(true);
        }
    }

    private void dismissControllerMenu() {
        if (isFullscreen && !isLockScreen) {
            mn_player_iv_lock.setVisibility(View.GONE);
        }
        mn_rl_top_menu.setVisibility(View.GONE);
        mn_rl_bottom_menu.setVisibility(View.GONE);
    }

    private void showErrorView() {
        mn_player_iv_play_center.setVisibility(View.GONE);
        mn_player_ll_net.setVisibility(View.GONE);
        mn_player_progressBar.setVisibility(View.GONE);
        mn_player_ll_error.setVisibility(View.VISIBLE);
    }

    private void showNoNetView() {
        mn_player_iv_play_center.setVisibility(View.GONE);
        mn_player_ll_net.setVisibility(View.VISIBLE);
        mn_player_progressBar.setVisibility(View.GONE);
        mn_player_ll_error.setVisibility(View.GONE);
    }

    private void setLandscape() {
        isFullscreen = true;
        //设置横屏
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mn_rl_bottom_menu.getVisibility() == View.VISIBLE) {
            mn_rl_top_menu.setVisibility(View.VISIBLE);
        }
        initLock();
    }

    private void setProtrait() {
        isFullscreen = false;
        //设置横屏
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mn_rl_top_menu.setVisibility(View.GONE);
        unLockScreen();
        initLock();
    }

    //--------------------------------------------------------------------------------------
    // ######## 计时器相关操作 ########
    //--------------------------------------------------------------------------------------

    private void initTimeTask() {
        timer_video_time = new Timer();
        task_video_timer = new TimerTask() {
            @Override
            public void run() {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer == null) {
                            return;
                        }
                        //设置时间
                        mn_tv_time.setText(String.valueOf(PlayerUtils.converLongTimeToStr(mediaPlayer.getCurrentPosition()) + " / " + PlayerUtils.converLongTimeToStr(mediaPlayer.getDuration())));
                        //进度条
                        int progress = mediaPlayer.getCurrentPosition();
                        mn_seekBar.setProgress(progress);
                    }
                });
            }
        };
        timer_video_time.schedule(task_video_timer, 0, 1000);
    }

    private void destroyTimeTask() {
        if (timer_video_time != null && task_video_timer != null) {
            timer_video_time.cancel();
            task_video_timer.cancel();
            timer_video_time = null;
            task_video_timer = null;
        }
    }

    private void initControllerTask() {
        // 设置计时器,控制器的影藏和显示
        timer_controller = new Timer();
        task_controller = new TimerTask() {
            @Override
            public void run() {
                destroyControllerTask(false);
            }
        };
        timer_controller.schedule(task_controller, 5000);
        initTimeTask();
    }

    private void destroyControllerTask(boolean isMainThread) {
        if (isMainThread) {
            dismissControllerMenu();
        } else {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismissControllerMenu();
                }
            });
        }
        if (timer_controller != null && task_controller != null) {
            timer_controller.cancel();
            task_controller.cancel();
            timer_controller = null;
            task_controller = null;
        }
        destroyTimeTask();
    }

    //--------------------------------------------------------------------------------------
    // ######## 接口方法实现 ########
    //--------------------------------------------------------------------------------------
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int maxCanSeekTo = seekBar.getMax() - 5 * 1000;
            if (seekBar.getProgress() < maxCanSeekTo) {
                mediaPlayer.seekTo(seekBar.getProgress());
            } else {
                //不能拖到最后
                mediaPlayer.seekTo(maxCanSeekTo);
            }
        }
    }

    //播放
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(holder); // 添加到容器中
        //播放完成的监听
        mediaPlayer.setOnCompletionListener(this);
        // 异步准备的一个监听函数，准备好了就调用里面的方法
        mediaPlayer.setOnPreparedListener(this);
        //播放错误的监听
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        //第一次初始化需不需要主动播放
        if (isFirstPlay) {
            //判断当前有没有网络（播放的是网络视频）
            if (!PlayerUtils.isNetworkConnected(context) && videoPath.startsWith("http")) {
                Toast.makeText(context, context.getString(R.string.mnPlayerNoNetHint), Toast.LENGTH_SHORT).show();
                showNoNetView();
            } else {
                //手机网络给提醒
                if (PlayerUtils.isMobileConnected(context)) {
                    Toast.makeText(context, context.getString(R.string.mnPlayerMobileNetHint), Toast.LENGTH_SHORT).show();
                }
                //添加播放路径
                try {
                    mediaPlayer.setDataSource(videoPath);
                    // 准备开始,异步准备，自动在子线程中
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        isFirstPlay = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //保存播放位置
        if (mediaPlayer != null) {
            video_position = mediaPlayer.getCurrentPosition();
        }
        destroyControllerTask(true);
        pauseVideo();
        Log.i(TAG, "surfaceDestroyed---video_position：" + video_position);
    }

    //MediaPlayer
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
        destroyControllerTask(true);
        video_position = 0;
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(mediaPlayer);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.i(TAG, "二级缓存onBufferingUpdate: " + percent);
        if (percent >= 0 && percent <= 100) {
            int secondProgress = mp.getDuration() * percent / 100;
            mn_seekBar.setSecondaryProgress(secondProgress);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "发生错误error:" + what);
        if (what != -38) {  //这个错误不管
            showErrorView();
        }
        return true;
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        mediaPlayer.start(); // 开始播放
        isPrepare = true;
        if (video_position > 0) {
            Log.i(TAG, "onPrepared---video_position:" + video_position);
            mediaPlayer.seekTo(video_position);
            video_position = 0;
        }
        // 把得到的总长度和进度条的匹配
        mn_seekBar.setMax(mediaPlayer.getDuration());
        mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
        mn_tv_time.setText(String.valueOf(PlayerUtils.converLongTimeToStr(mediaPlayer.getCurrentPosition()) + "/" + PlayerUtils.converLongTimeToStr(mediaPlayer.getDuration())));
        //延时：避免出现上一个视频的画面闪屏
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initBottomMenuState();
                mn_player_rl_progress.setVisibility(View.GONE);
            }
        }, 500);
    }

    //--------------------------------------------------------------------------------------
    // ######## 手势相关 ########
    //--------------------------------------------------------------------------------------
    private RelativeLayout gesture_volume_layout;// 音量控制布局
    private TextView geture_tv_volume_percentage;// 音量百分比
    private ImageView gesture_iv_player_volume;// 音量图标
    private RelativeLayout gesture_light_layout;// 亮度布局
    private TextView geture_tv_light_percentage;// 亮度百分比
    private RelativeLayout gesture_progress_layout;// 进度图标
    private TextView geture_tv_progress_time;// 播放时间进度
    private ImageView gesture_iv_progress;// 快进或快退标志
    private GestureDetector gestureDetector;
    private AudioManager audiomanager;
    private int maxVolume, currentVolume;
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_LIGHT = 2f;// 协调亮度滑动时的步长，避免每次滑动都改变，导致改变过快
    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHTNESS = 3;

    private void initGesture() {
        gesture_volume_layout = (RelativeLayout) findViewById(R.id.mn_gesture_volume_layout);
        geture_tv_volume_percentage = (TextView) findViewById(R.id.mn_gesture_tv_volume_percentage);
        gesture_iv_player_volume = (ImageView) findViewById(R.id.mn_gesture_iv_player_volume);

        gesture_progress_layout = (RelativeLayout) findViewById(R.id.mn_gesture_progress_layout);
        geture_tv_progress_time = (TextView) findViewById(R.id.mn_gesture_tv_progress_time);
        gesture_iv_progress = (ImageView) findViewById(R.id.mn_gesture_iv_progress);

        //亮度的布局
        gesture_light_layout = (RelativeLayout) findViewById(R.id.mn_gesture_light_layout);
        geture_tv_light_percentage = (TextView) findViewById(R.id.mn_geture_tv_light_percentage);

        gesture_volume_layout.setVisibility(View.GONE);
        gesture_progress_layout.setVisibility(View.GONE);
        gesture_light_layout.setVisibility(View.GONE);

        gestureDetector = new GestureDetector(getContext(), this);
        setLongClickable(true);
        gestureDetector.setIsLongpressEnabled(true);
        audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!isPrepare || isLockScreen) {
            return false;
        }
        initBottomMenuState();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        if (!isPrepare || isLockScreen) {
            return false;
        }

        int FLAG = 0;

        // 横向的距离变化大则调整进度，纵向的变化大则调整音量
        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                FLAG = GESTURE_MODIFY_PROGRESS;
            }
        } else {
            int intX = (int) e1.getX();
            int screenWidth = PlayerUtils.getScreenWidth((Activity) context);
            if (intX > screenWidth / 2) {
                FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                //左边是亮度
                FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }

        if (GESTURE_FLAG != 0 && GESTURE_FLAG != FLAG) {
            return false;
        }

        GESTURE_FLAG = FLAG;

        if (FLAG == GESTURE_MODIFY_PROGRESS) {
            //表示是横向滑动,可以添加快进
            // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.VISIBLE);
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                        if (distanceX >= PlayerUtils.dip2px(context, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                            gesture_iv_progress
                                    .setImageResource(R.drawable.mn_player_backward);
                            if (mediaPlayer.getCurrentPosition() > 3 * 1000) {// 避免为负
                                int cpos = mediaPlayer.getCurrentPosition();
                                mediaPlayer.seekTo(cpos - 3000);
                                mn_seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            } else {
                                //什么都不做
                                mediaPlayer.seekTo(3000);
                            }
                        } else if (distanceX <= -PlayerUtils.dip2px(context, STEP_PROGRESS)) {// 快进
                            gesture_iv_progress
                                    .setImageResource(R.drawable.mn_player_forward);
                            if (mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration() - 5 * 1000) {// 避免超过总时长
                                int cpos = mediaPlayer.getCurrentPosition();
                                mediaPlayer.seekTo(cpos + 3000);
                                // 把当前位置赋值给进度条
                                mn_seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                    }
                    String timeStr = PlayerUtils.converLongTimeToStr(mediaPlayer.getCurrentPosition()) + " / "
                            + PlayerUtils.converLongTimeToStr(mediaPlayer.getDuration());
                    geture_tv_progress_time.setText(timeStr);

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
        else if (FLAG == GESTURE_MODIFY_VOLUME) {
            //右边是音量
            gesture_volume_layout.setVisibility(View.VISIBLE);
            gesture_light_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.GONE);
            currentVolume = audiomanager
                    .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (currentVolume == 0) {// 静音，设定静音独有的图片
                    gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_close);
                }
                if (distanceY >= PlayerUtils.dip2px(context, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        currentVolume++;
                    }
                    gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_open);
                } else if (distanceY <= -PlayerUtils.dip2px(context, STEP_VOLUME)) {// 音量调小
                    if (currentVolume > 0) {
                        currentVolume--;
                        if (currentVolume == 0) {// 静音，设定静音独有的图片
                            gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_close);
                        }
                    }
                }
                int percentage = (currentVolume * 100) / maxVolume;
                geture_tv_volume_percentage.setText(String.valueOf(percentage + "%"));
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
        }
        //调节亮度
        else if (FLAG == GESTURE_MODIFY_BRIGHTNESS) {
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.VISIBLE);
            gesture_progress_layout.setVisibility(View.GONE);
            currentVolume = audiomanager
                    .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                // 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                int mLight = LightnessControl.GetLightness((Activity) context);
                if (mLight >= 0 && mLight <= 255) {
                    if (distanceY >= PlayerUtils.dip2px(context, STEP_LIGHT)) {
                        if (mLight > 245) {
                            LightnessControl.SetLightness((Activity) context, 255);
                        } else {
                            LightnessControl.SetLightness((Activity) context, mLight + 10);
                        }
                    } else if (distanceY <= -PlayerUtils.dip2px(context, STEP_LIGHT)) {// 亮度调小
                        if (mLight < 10) {
                            LightnessControl.SetLightness((Activity) context, 0);
                        } else {
                            LightnessControl.SetLightness((Activity) context, mLight - 10);
                        }
                    }
                } else if (mLight < 0) {
                    LightnessControl.SetLightness((Activity) context, 0);
                } else {
                    LightnessControl.SetLightness((Activity) context, 255);
                }
                //获取当前亮度
                int currentLight = LightnessControl.GetLightness((Activity) context);
                int percentage = (currentLight * 100) / 255;
                geture_tv_light_percentage.setText(String.valueOf(percentage + "%"));
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 手势里除了singleTapUp，没有其他检测up的方法
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.GONE);
        }
        return gestureDetector.onTouchEvent(event);
    }

    //--------------------------------------------------------------------------------------
    // ######## 对外提供的方法 ########
    //--------------------------------------------------------------------------------------

    /**
     * 设置视频信息
     *
     * @param url   视频地址
     * @param title 视频标题
     */
    public void setDataSource(String url, String title) {
        //赋值
        videoPath = url;
        videoTitle = title;
    }

    /**
     * 播放视频
     *
     * @param url   视频地址
     * @param title 视频标题
     */
    public void playVideo(String url, String title) {
        playVideo(url, title, video_position);
    }

    /**
     * 播放视频（支持上次播放位置）
     * 自己记录上一次播放的位置，然后传递position进来就可以了
     *
     * @param url      视频地址
     * @param title    视频标题
     * @param position 视频跳转的位置
     */
    public void playVideo(String url, String title, int position) {
        //地址判空处理
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(context, context.getString(R.string.mnPlayerUrlEmptyHint), Toast.LENGTH_SHORT).show();
            return;
        }
        //销毁ControllerView
        destroyControllerTask(true);

        //赋值
        videoPath = url;
        videoTitle = title;
        video_position = position;
        isPrepare = false;

        //判断当前有没有网络（播放的是网络视频）
        if (!PlayerUtils.isNetworkConnected(context) && url.startsWith("http")) {
            Toast.makeText(context, context.getString(R.string.mnPlayerNoNetHint), Toast.LENGTH_SHORT).show();
            showNoNetView();
            return;
        }
        //手机网络给提醒
        if (PlayerUtils.isMobileConnected(context)) {
            Toast.makeText(context, context.getString(R.string.mnPlayerMobileNetHint), Toast.LENGTH_SHORT).show();
        }

        //重置MediaPlayer
        resetMediaPlayer();

        //初始化View
        initViews();
        //判断广播相关
        if (isNeedBatteryListen) {
            registerBatteryReceiver();
        } else {
            unRegisterBatteryReceiver();
            mn_iv_battery.setVisibility(View.GONE);
        }
        //网络监听的广播
        if (isNeedNetChangeListen) {
            registerNetReceiver();
        } else {
            unregisterNetReceiver();
        }
    }

    private void resetMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.stop();
                }
                //重置mediaPlayer
                mediaPlayer.reset();
                //添加播放路径
                mediaPlayer.setDataSource(videoPath);
                // 准备开始,异步准备，自动在子线程中
                mediaPlayer.prepareAsync();
            } else {
                Toast.makeText(context, "播放器初始化失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频
     */
    public void startVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
        }
    }

    /**
     * 暂停视频
     */
    public void pauseVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
            video_position = mediaPlayer.getCurrentPosition();
        }
    }

    /**
     * 竖屏
     */
    public void setOrientationPortrait() {
        setProtrait();
    }

    /**
     * 横屏
     */
    public void setOrientationLandscape() {
        setLandscape();
    }

    /**
     * 设置是否需要电量监听
     */
    public void setIsNeedBatteryListen(boolean isNeedBatteryListen) {
        this.isNeedBatteryListen = isNeedBatteryListen;
    }

    /**
     * 设置是否需要网络变化监听
     */
    public void setIsNeedNetChangeListen(boolean isNeedNetChangeListen) {
        this.isNeedNetChangeListen = isNeedNetChangeListen;
    }

    /**
     * 判断是不是全屏状态
     *
     * @return
     */
    public boolean isFullScreen() {
        return isFullscreen;
    }

    /**
     * 获取当前播放的位置
     */
    public int getVideoCurrentPosition() {
        int position = 0;
        if (mediaPlayer != null) {
            position = mediaPlayer.getCurrentPosition();
        }
        return position;
    }

    /**
     * 获取视频总长度
     */
    public int getVideoTotalDuration() {
        int position = 0;
        if (mediaPlayer != null) {
            position = mediaPlayer.getDuration();
        }
        return position;
    }

    /**
     * 获取管理者
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 销毁资源
     */
    public void destroyVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();// 释放资源
            mediaPlayer = null;
        }
        surfaceHolder = null;
        mn_palyer_surfaceView = null;
        video_position = 0;
        unRegisterBatteryReceiver();
        unregisterNetReceiver();
        removeAllListener();
        destroyTimeTask();
        myHandler.removeCallbacksAndMessages(null);
    }


    //--------------------------------------------------------------------------------------
    // ######## 广播相关 ########
    //--------------------------------------------------------------------------------------

    /**
     * 电量广播接受者
     */
    class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //判断它是否是为电量变化的Broadcast Action
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);

                int battery = (level * 100) / scale;

                //把它转成百分比
                Log.i(TAG, "电池电量为" + battery + "%");

                mn_iv_battery.setVisibility(View.VISIBLE);
                if (battery > 0 && battery < 20) {
                    mn_iv_battery.setImageResource(R.drawable.mn_player_battery_01);
                } else if (battery >= 20 && battery < 40) {
                    mn_iv_battery.setImageResource(R.drawable.mn_player_battery_02);
                } else if (battery >= 40 && battery < 65) {
                    mn_iv_battery.setImageResource(R.drawable.mn_player_battery_03);
                } else if (battery >= 65 && battery < 90) {
                    mn_iv_battery.setImageResource(R.drawable.mn_player_battery_04);
                } else if (battery >= 90 && battery <= 100) {
                    mn_iv_battery.setImageResource(R.drawable.mn_player_battery_05);
                } else {
                    mn_iv_battery.setVisibility(View.GONE);
                }


            }
        }
    }

    private BatteryReceiver batteryReceiver;

    private void registerBatteryReceiver() {
        if (batteryReceiver == null) {
            //注册广播接受者
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            //创建广播接受者对象
            batteryReceiver = new BatteryReceiver();
            //注册receiver
            context.registerReceiver(batteryReceiver, intentFilter);
        }
    }

    private void unRegisterBatteryReceiver() {
        if (batteryReceiver != null) {
            context.unregisterReceiver(batteryReceiver);
        }
    }

    //-------------------------网络变化监听
    public class NetChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onNetChangeListener == null || !isNeedNetChangeListen) {
                return;
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isAvailable()) {
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) { //WiFi网络
                    onNetChangeListener.onWifi(mediaPlayer);
                } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {   //3g网络
                    onNetChangeListener.onMobile(mediaPlayer);
                } else {    //其他
                    Log.i(TAG, "其他网络");
                }
            } else {
                onNetChangeListener.onNoAvailable(mediaPlayer);
            }
        }
    }

    private NetChangeReceiver netChangeReceiver;

    private void registerNetReceiver() {
        if (netChangeReceiver == null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            netChangeReceiver = new NetChangeReceiver();
            context.registerReceiver(netChangeReceiver, filter);
        }
    }

    private void unregisterNetReceiver() {
        if (netChangeReceiver != null) {
            context.unregisterReceiver(netChangeReceiver);
        }
    }


    //--------------------------------------------------------------------------------------
    // ######## 自定义回调 ########
    //--------------------------------------------------------------------------------------

    private void removeAllListener() {
        if (onNetChangeListener != null) {
            onNetChangeListener = null;
        }
        if (onPlayerCreatedListener != null) {
            onPlayerCreatedListener = null;
        }
    }


    //网络监听回调
    private OnNetChangeListener onNetChangeListener;

    public void setOnNetChangeListener(OnNetChangeListener onNetChangeListener) {
        this.onNetChangeListener = onNetChangeListener;
    }

    public interface OnNetChangeListener {
        //wifi
        void onWifi(MediaPlayer mediaPlayer);

        //手机
        void onMobile(MediaPlayer mediaPlayer);

        //不可用
        void onNoAvailable(MediaPlayer mediaPlayer);
    }

    //SurfaceView初始化完成回调
    private OnPlayerCreatedListener onPlayerCreatedListener;

    public void setOnPlayerCreatedListener(OnPlayerCreatedListener onPlayerCreatedListener) {
        this.onPlayerCreatedListener = onPlayerCreatedListener;
    }

    public interface OnPlayerCreatedListener {
        //不可用
        void onPlayerCreated(String url, String title);
    }

    //-----------------------播放完回调
    private OnCompletionListener onCompletionListener;

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

}