package jrkim.rcash.ui.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_BACKUP;

;

public class BackupActivity extends BaseActivity {

    @BindView(R.id.btnAccept) Button btnAccept;
    @BindView(R.id.rlBottom) RelativeLayout rlBottom;
    @BindView(R.id.rlCover) RelativeLayout rlCover;

    public static String phrases = null;
    private boolean confirmed = false;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 백업 화면은 스크린 캡쳐 방지
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_backup);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvPhrases)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_SANSATION_BOLD));
        ((TextView)findViewById(R.id.tvPhrases)).setText(phrases);

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        rlBottom.setPadding(0, 0, 0, softKeyHeight);

        btnAccept.setEnabled(false);
        btnAccept.setAlpha(0.4f);
    }

    @OnCheckedChanged(R.id.cbAccept)
    public void onCheckChanged(CheckBox checkBox) {
        if(checkBox.isChecked()) {
            btnAccept.setEnabled(true);
            btnAccept.setAlpha(1.f);
        } else {
            btnAccept.setEnabled(false);
            btnAccept.setAlpha(0.4f);
        }
    }

    @OnClick(R.id.btnAccept)
    public void onAccept() {
        confirmed = true;
        rlBottom.animate().translationY(rlBottom.getHeight()).setDuration(500).setInterpolator(new AccelerateInterpolator()).start();
        rlCover.animate().translationY(rlCover.getHeight() * -2).setStartDelay(200).setDuration(500).setInterpolator(new AccelerateInterpolator()).start();
        new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_BACKUP, true);
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        if(!confirmed) {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                    .setTitle(R.string.backup_finish_without_confirm)
                    .setMessage(R.string.backup_finish_without_confirm_desc)
                    .setPositiveButton(R.string.common_cancel, null)
                    .setNegativeButton(R.string.common_ok, (dialog, which) -> {
                                finish();
                            }
                    ).create();
            alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
            alertDialog.show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        onToolbarNavigator();
    }
}
