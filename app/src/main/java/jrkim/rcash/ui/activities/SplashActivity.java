package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.TextView;

import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;

/**
 * Splash 에서는 PincodeActivity가 안뜨도록 BaseActivity로 부터 상속받지 않음.
 */
public class SplashActivity extends AppCompatActivity {
    private final static String TAG = "RCash_Splash";
    private final static int SPLASH_DELAY_TIME = 600;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ((TextView)findViewById(R.id.tvBch)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_BOLD));
        ((TextView)findViewById(R.id.tvForEveryone)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        ((TextView)findViewById(R.id.tvPoweredBy)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_REGULAR));


        int statusBarHeight = 0;
        int totalHeight = 0;
        int softKeyHeight = 0;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        totalHeight = displayMetrics.heightPixels;

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int realHeight = displayMetrics.heightPixels;
        if(realHeight > totalHeight) {
            softKeyHeight = realHeight - totalHeight;
        }

        findViewById(R.id.llBottom).setPadding(0, 0, 0, softKeyHeight);

        new Handler().postDelayed(() -> {
            Intent newIntent = new Intent(SplashActivity.this, MainActivity.class);
            SplashActivity.this.startActivity(newIntent);
            overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
            SplashActivity.this.finish();
        }, SPLASH_DELAY_TIME);
    }
}
