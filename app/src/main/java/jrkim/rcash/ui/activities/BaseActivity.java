package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.application.RCashApplication;

public abstract class BaseActivity extends AppCompatActivity {
    private static class BaseHandler extends Handler {
        private final WeakReference<BaseActivity> activity;
        public BaseHandler(BaseActivity activity) {
            this.activity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            BaseActivity baseActivity = activity.get();
            if(baseActivity != null) {
                baseActivity.handleMessage(msg);
            }
        }
    }

    public BaseHandler handler = null;
    public static ArrayList<BaseHandler> handlers = new ArrayList<>();

    public static int statusBarHeight = 0;
    public static int softKeyHeight = 0;
    public static int totalHeight = 0;
    public static int totalWidth = 0;

    protected abstract void handleMessage(Message msg);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 스크린 캡쳐 방지, 혹시 의미 없거나 스크린캡쳐 허용이 필요한 경우 이후에 풀어준다.
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        handler = new BaseHandler(this);
        if(!handlers.contains(handler)) {
            handlers.add(handler);
        }

        if(statusBarHeight == 0) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            totalHeight = displayMetrics.heightPixels;
            totalWidth = displayMetrics.widthPixels;

            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if(resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            int realHeight = displayMetrics.heightPixels;
            if(realHeight > totalHeight) {
                softKeyHeight = realHeight - totalHeight;
            }
        }
    }

    @Override
    public void onDestroy() {
        handlers.remove(handler);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        RCashApplication application = (RCashApplication)getApplication();
        if(new SharedPreferenceMgr(getApplicationContext()).get(RCashConsts.SHAREDPREF_BOOL_SECUREMODE, false)) {
            if(application.wasInBackground) {
                // 혹시 이미 Pincode가 떠있는 경우에는 PincodeActivity에게 보안용 Pincode가 떠야 함을 알려준다.
                // REMOVE모드인 경우에 스스로를 DEFAULT 모드로 변경시킴
                broadcastMessage(RCashConsts.BROADCAST_PINCODE_WILL_APPEAR);
                if(PincodeActivity.launchedCount == 0) {
                    startActivityForResult(new Intent(this, PincodeActivity.class), RCashConsts.REQCODE_PINCODE);
                }
            }
        }
        application.stopActivityTransitionTimer();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);

    }

    @Override
    public void startActivityForResult(Intent intent, int reqeustCode) {
        super.startActivityForResult(intent, reqeustCode);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((RCashApplication)getApplication()).startActivityTransitionTimer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case RCashConsts.REQCODE_PINCODE:
                break;
        }
    }

    public static void sendEmptyMessage(BaseHandler handler, int message) {
        handler.sendEmptyMessage(message);
    }

    public static void sendEmptyMessageDelayed(BaseHandler handler, int what, long delayMillis) {
        handler.sendEmptyMessageDelayed(what, delayMillis);
    }

    public static void sendMessage(BaseHandler handler, int what, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, obj));
    }

    public static void sendMessage(BaseHandler handler, int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    public static void sendMessageDelayed(BaseHandler handler, int what, int arg1, int arg2, Object obj, long delayMillis) {
        handler.sendMessageDelayed(handler.obtainMessage(what, arg1, arg2, obj), delayMillis);
    }


    public static void broadcastMessage(int message) {
        broadcastMessage(message, 0, 0, null);
    }

    public static void broadcastMessage(int message, int arg1, int arg2, Object obj) {
        for(BaseHandler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(message, arg1, arg2, obj));
        }
    }

    public static void broadcastMessageDelayed(int message, int arg1, int arg2, Object obj, long delayed) {
        for(BaseHandler handler : handlers) {
            handler.sendMessageDelayed(handler.obtainMessage(message, arg1, arg2, obj), delayed);
        }
    }
}
