package com.maning.mnvideoplayerlibrary.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.nfc.Tag;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 调节屏幕亮度的类
 */
public class LightnessControl {

    private static final String TAG = "MNVideoPlayer";

    // 判断是否开启了自动亮度调节
    public static boolean isAutoBrightness(Activity act) {
        boolean automicBrightness = false;
        ContentResolver aContentResolver = act.getContentResolver();
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception e) {
            Log.e(TAG, "判断是否开启了自动亮度调节失败:" + e.toString());
        }
        return automicBrightness;
    }

    // 改变亮度
    public static void SetLightness(Activity act, int value) {
        try {
            if (isAutoBrightness(act)) {
                stopAutoBrightness(act);
            }
            Settings.System.putInt(act.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
            WindowManager.LayoutParams lp = act.getWindow().getAttributes();
            lp.screenBrightness = (value <= 0 ? 1 : value) / 255f;
            act.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.e(TAG, "无法改变屏幕亮度:" + e.toString());
        }
    }

    // 获取亮度0~255
    public static int GetLightness(Activity act) {
        return Settings.System.getInt(act.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
    }

    // 停止自动亮度调节
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    // 开启亮度自动调节
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

}
