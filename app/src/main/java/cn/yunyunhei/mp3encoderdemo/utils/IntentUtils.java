package cn.yunyunhei.mp3encoderdemo.utils;

import android.app.Activity;
import android.content.Intent;

import cn.yunyunhei.mp3encoderdemo.AudioRecordActivity;
import cn.yunyunhei.mp3encoderdemo.MainActivity;

/**
 * Created by wuhang on 2018/9/13.
 *
 * @author wuhang
 */

public class IntentUtils {

    public static void jumpToAudioRecord(Activity activity) {
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);

    }

    public static void jumpToAudioRecord2(Activity activity) {
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        Intent intent = new Intent(activity, AudioRecordActivity.class);
        activity.startActivity(intent);

    }
}
