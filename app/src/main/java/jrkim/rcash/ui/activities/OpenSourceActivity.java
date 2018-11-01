package jrkim.rcash.ui.activities;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.utils.Utils;

public class OpenSourceActivity extends BaseActivity {

    @BindView(R.id.tvCredit) TextView tvCredit;
    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensource);
        ButterKnife.bind(this);

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);


        //tvCredit.setText(Html.fromHtml(Utils.readTxtFromAssets(this,"opensource/opensource.txt")));
        tvCredit.setText(Utils.readTxtFromAssets(this,"opensource/opensource.txt"));
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onBack() {
        onBackPressed();
    }
}
