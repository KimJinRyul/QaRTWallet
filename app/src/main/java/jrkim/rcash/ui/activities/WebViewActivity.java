package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.Utils;


public class WebViewActivity extends BaseActivity {

    private static final String TAG = "RCash_Webview";
    @BindView(R.id.webview) WebView webView;
    @BindView(R.id.fabShare) FloatingActionButton fabShare;

    public static int TYPE_TRANSACTION = 0;
    public static int TYPE_WHERECASH = 1;

    @Override
    protected void handleMessage(Message msg) {
    }

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.rlWebview).setPadding(0, 0, 0, softKeyHeight);

        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        int type = getIntent().getIntExtra(Intent.EXTRA_MIME_TYPES, TYPE_TRANSACTION);

        ((TextView)findViewById(R.id.tvToolbarTitle)).setText(title);

        if(type == TYPE_TRANSACTION) {
            fabShare.setTranslationY(-1 * softKeyHeight);
            fabShare.setOnClickListener((v) -> {
                Utils.launchShare(getApplicationContext(), url, getString(R.string.app_name));
            });
        } else if(type == TYPE_WHERECASH) {
            //fabShare.setTranslationY(Utils.dpToPixel(this, 100.f));
            fabShare.setVisibility(View.GONE);
        }

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onBackOnToolbar() {
        finish();
    }

    @Override
    public void onBackPressed() {
        onBackOnToolbar();
    }
}
