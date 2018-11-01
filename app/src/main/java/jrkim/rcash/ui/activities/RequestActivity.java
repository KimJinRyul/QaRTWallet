package jrkim.rcash.ui.activities;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.bitcoinj.core.Coin;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_AMOUNT;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_CHY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_EU;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_JPY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_KRW;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_USD;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_LOCAL_CURRENCY;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_QRCODE_DOT_SIZE;

public class RequestActivity extends BaseActivity {

    private static String TAG = "RCash_Request";
    public static Bitmap qrcode = null;
    public static String address = null;
    public static long amount = 0L;
    private String content = "";

    @BindView(R.id.ivQRCode) ImageView ivQRCode;
    @BindView(R.id.seekBar) AppCompatSeekBar seekBar;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    @BindView(R.id.tvBCH) TextView tvBCH;
    @BindView(R.id.tvLocalCurrency) TextView tvLocalCurrency;

    @Override
    protected void handleMessage(Message msg) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);

        address = getIntent().getStringExtra(EXTRA_ADDRESS);
        amount = getIntent().getLongExtra(EXTRA_AMOUNT, 0);

        content = String.format(Locale.getDefault(), "%s?amount=%.8f", address, (amount / 100000000.f));
        Log.i(TAG, "content:" + content);

        int dotSize = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);
        seekBar.setProgress(dotSize);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Don't save dotSize on RequestActivity
                RefreshQR(seekBar.getProgress() + 10);
            }
        });

        /**
         * 화면 넓이에 맞춰서 정사각형으로 만들어준다.
         * Width 기반으로 height도 Width랑 똑같이 맞춰줌
         * layout_width="match_parent"
         * layout_height="wrap_context"
         */
        ivQRCode.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivQRCode.getViewTreeObserver().removeOnPreDrawListener(this);
                int width = ivQRCode.getWidth();
                ViewGroup.LayoutParams lp = ivQRCode.getLayoutParams();
                lp.height = width;
                ivQRCode.setLayoutParams(lp);
                return false;
            }
        });

        RefreshQR(dotSize);

        showBalanceIfReady();
    }

    private void showBalanceIfReady() {
        boolean readyToDisplay = false;
        int localCurrency = new SharedPreferenceMgr(getApplicationContext()).get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD);
        switch(localCurrency) {
            case LOCAL_CURRENCY_KRW:
                if(MainActivity.USDKRW > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_USD:
                readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_EU:
                if(MainActivity.USDEUR > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_JPY:
                if(MainActivity.USDJPY > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_CHY:
                if(MainActivity.USDCNY > 0) readyToDisplay = true;
                break;
        }

        if(readyToDisplay) {
            ((Handler)handler).post(() -> {
                long satoshis = amount;
                double balance = satoshis / 100000000.f;
                double localValue = MainActivity.BCHUSD * balance;

                String subBalance = String.format("$ %.2f", localValue);

                switch(localCurrency) {
                    case LOCAL_CURRENCY_KRW:
                        localValue *= MainActivity.USDKRW;
                        subBalance = String.format("₩ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_EU:
                        localValue *= MainActivity.USDEUR;
                        subBalance = String.format("€ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_JPY:
                        localValue *= MainActivity.USDJPY;
                        subBalance = String.format("¥ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_CHY:
                        localValue *= MainActivity.USDCNY;
                        subBalance = String.format("元 %.2f", localValue);
                        break;
                }

                tvBCH.setText(Coin.valueOf(amount).toPlainString());
                tvLocalCurrency.setText(subBalance);

            });
        }
    }

    private void RefreshQR(int dotSize) {
        rlProgress.setVisibility(View.VISIBLE);
        new Handler().post(()->{
            /**
             * 만약 GIF인 경우는 그냥 Default로 생성하는 뭐시기가 필요할듯....
             */
           File oFile = Utils.createQRCode(getApplicationContext(), content, dotSize, true, true);
           if(oFile != null && oFile.exists()) {
               Glide.with(this)
                       .load(oFile)
                       .skipMemoryCache(true)
                       .diskCacheStrategy(DiskCacheStrategy.NONE)
                       .centerCrop()
                       .crossFade()
                       .into(ivQRCode);
           }
           rlProgress.setVisibility(View.GONE);
        });
    }
}
