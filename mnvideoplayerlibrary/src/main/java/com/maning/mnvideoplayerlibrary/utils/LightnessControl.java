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

    // 改变亮度
    public static void SetLightness(Activity act, int value) {
        try {
            WindowManager.LayoutParams lp = act.getWindow().getAttributes();
            Log.e(">>>>>>>>>", "lp.screenBrightness:" + lp.screenBrightness);
            if (lp.screenBrightness == -1.0) {
                lp.screenBrightness = 0.5f;
            }
            lp.screenBrightness = lp.screenBrightness + value / 255.0f;
            if (lp.screenBrightness > 1) {
                lp.screenBrightness = 1;
            } else if (lp.screenBrightness < 0) {
                lp.screenBrightness = 0f;
            }
            act.getWindow().setAttributes(lp);
        } catch (Exception e) {
        }
    }

    // 获取亮度0~255
    public static int GetLightness(Activity act) {
        WindowManager.LayoutParams lp = act.getWindow().getAttributes();
        return (int) (lp.screenBrightness * 255f);
    }
}
