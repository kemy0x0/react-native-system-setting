package com.ninty.system.setting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by ninty on 2017/5/29.
 */

public class SystemSetting extends ReactContextBaseJavaModule implements ActivityEventListener {

    private String TAG = SystemSetting.class.getSimpleName();

    private static final String VOL_VOICE_CALL = "call";
    private static final String VOL_SYSTEM = "system";
    private static final String VOL_RING = "ring";
    private static final String VOL_MUSIC = "music";
    private static final String VOL_ALARM = "alarm";
    private static final String VOL_NOTIFICATION = "notification";

    private ReactApplicationContext mContext;
    private AudioManager am;
    private BroadcastReceiver volumeBR;
    private IntentFilter filter;

    public SystemSetting(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        am = (AudioManager) getReactApplicationContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        listenVolume(reactContext);
    }

    private void listenVolume(final ReactApplicationContext reactContext) {
        volumeBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    WritableMap para = Arguments.createMap();
                    para.putDouble("value", getNormalizationVolume(VOL_MUSIC));
                    para.putDouble(VOL_VOICE_CALL, getNormalizationVolume(VOL_VOICE_CALL));
                    para.putDouble(VOL_SYSTEM, getNormalizationVolume(VOL_SYSTEM));
                    para.putDouble(VOL_RING, getNormalizationVolume(VOL_RING));
                    para.putDouble(VOL_MUSIC, getNormalizationVolume(VOL_MUSIC));
                    para.putDouble(VOL_ALARM, getNormalizationVolume(VOL_ALARM));
                    para.putDouble(VOL_NOTIFICATION, getNormalizationVolume(VOL_NOTIFICATION));
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("EventVolume", para);
                }
            }
        };
        filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");

        reactContext.registerReceiver(volumeBR, filter);
    }

    @Override
    public String getName() {
        return SystemSetting.class.getSimpleName();
    }

    @ReactMethod
    public void setVolume(float val, String type) {
        mContext.unregisterReceiver(volumeBR);
        int volType = getVolType(type);
        am.setStreamVolume(volType, (int) (val * am.getStreamMaxVolume(volType)), AudioManager.FLAG_PLAY_SOUND);
        mContext.registerReceiver(volumeBR, filter);
    }

    @ReactMethod
    public void getVolume(String type, Promise promise) {
        promise.resolve(getNormalizationVolume(type));
    }

    private void checkAndSet(String name, int value, Promise promise) {
        boolean reject = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mContext)) {
            reject = true;
        } else {
            try {
                Settings.System.putInt(getReactApplicationContext().getContentResolver(), name, value);
                int newVal = Settings.System.getInt(getReactApplicationContext().getContentResolver(), name);
                if (newVal != value) {
                    reject = true;
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                //ignore
            } catch (SecurityException e) {
                e.printStackTrace();
                reject = true;
            }
        }
        if (reject) {
            promise.reject("-1", "write_settings premission is blocked by system");
        } else {
            promise.resolve(true);
        }
    }

    private float getNormalizationVolume(String type) {
        int volType = getVolType(type);
        return am.getStreamVolume(volType) * 1.0f / am.getStreamMaxVolume(volType);
    }

    private int getVolType(String type) {
        switch (type) {
            case VOL_VOICE_CALL:
                return AudioManager.STREAM_VOICE_CALL;
            case VOL_SYSTEM:
                return AudioManager.STREAM_SYSTEM;
            case VOL_RING:
                return AudioManager.STREAM_RING;
            case VOL_ALARM:
                return AudioManager.STREAM_ALARM;
            case VOL_NOTIFICATION:
                return AudioManager.STREAM_NOTIFICATION;
            default:
                return AudioManager.STREAM_MUSIC;
        }
    }

    private void switchSetting(SysSettings setting) {
        if (mContext.getCurrentActivity() != null) {
            mContext.addActivityEventListener(this);
            Intent intent = new Intent(setting.action);
            mContext.getCurrentActivity().startActivityForResult(intent, setting.requestCode);
        } else {
            Log.w(TAG, "getCurrentActivity() return null, switch will be ignore");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        SysSettings setting = SysSettings.get(requestCode);
        if (setting != SysSettings.UNKNOW) {
            mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(setting.event, null);
            mContext.removeActivityEventListener(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
