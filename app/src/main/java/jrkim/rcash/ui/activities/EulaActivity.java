package jrkim.rcash.ui.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.Utils;

public class EulaActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "RCash_Eula";

    @BindView(R.id.btnAccept) Button btnAccept;
    @BindView(R.id.cbEula01) CheckBox cbEula01;
    @BindView(R.id.cbEula02) CheckBox cbEula02;
    @BindView(R.id.cbEula03) CheckBox cbEula03;
    @BindView(R.id.cbEulaAll) CheckBox cbEulaAll;

    @Override
    protected void handleMessage(Message msg) {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_eula);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.rlBottom).setPadding(0, 0, 0, softKeyHeight);

        cbEula01.setOnCheckedChangeListener(this);
        cbEula02.setOnCheckedChangeListener(this);
        cbEula03.setOnCheckedChangeListener(this);
        cbEulaAll.setOnCheckedChangeListener(this);

        setBtnEnabled(false);
    }

    private void setBtnEnabled(boolean enabled) {
        btnAccept.setEnabled(enabled);
        if(enabled)
            btnAccept.setAlpha(1.f);
        else
            btnAccept.setAlpha(0.4f);
    }

    private boolean isCheckedAll() {
        return cbEula01.isChecked() && cbEula02.isChecked() && cbEula03.isChecked();
    }

    @OnClick({R.id.rlEula01, R.id.rlEula02, R.id.rlEula03})
    public void onCheck(RelativeLayout view) {
        switch (view.getId()) {
            case R.id.rlEula01:
                cbEula01.setChecked(!cbEula01.isChecked());
                break;
            case R.id.rlEula02:
                cbEula02.setChecked(!cbEula02.isChecked());
                break;
            case R.id.rlEula03:
                cbEula03.setChecked(!cbEula03.isChecked());
                break;
        }
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onBackOnToolbar() {
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(R.string.eula_finish_without_agree_ttl)
                .setMessage(R.string.eula_finish_without_agree_desc)
                .setPositiveButton(R.string.common_cancel, null)
                .setNegativeButton(R.string.common_ok, (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                ).create();
        alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        alertDialog.show();
    }

    @OnClick(R.id.btnAccept)
    public void onBtnAccept() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        onBackOnToolbar();
    }

    private void slienceCheck(CheckBox checkBox, boolean checked) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cbEula01:
            case R.id.cbEula02:
            case R.id.cbEula03:
                if(isCheckedAll()) {
                    slienceCheck(cbEulaAll, true);
                    setBtnEnabled(true);
                } else {
                    slienceCheck(cbEulaAll, false);
                    setBtnEnabled(false);
                }
                break;
            case R.id.cbEulaAll:
                if(buttonView.isChecked()) {
                    slienceCheck(cbEula01, true);
                    slienceCheck(cbEula02, true);
                    slienceCheck(cbEula03, true);
                    setBtnEnabled(true);
                } else {
                    slienceCheck(cbEula01, false);
                    slienceCheck(cbEula02, false);
                    slienceCheck(cbEula03, false);
                    setBtnEnabled(false);
                }
                break;
        }
    }
}
