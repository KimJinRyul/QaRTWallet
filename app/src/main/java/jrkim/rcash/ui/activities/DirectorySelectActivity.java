package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.ui.adapters.DirectorySelectAdapter;
import jrkim.rcash.ui.adapters.PhotoSelectAdapter;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.EXTRA_PURPOSE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_PHOTOSELECTOR;
import static jrkim.rcash.consts.RCashConsts.RESULT_GIF;

public class DirectorySelectActivity extends BaseActivity implements DirectorySelectAdapter.DirectorySelectListener {

    public static int PURPOSE_MAKE_QR = 0;
    public static int PURPOSE_READ_QR = 1;

    @BindView(R.id.rvPhoto) RecyclerView rvPhoto;
    private int purpose = PURPOSE_MAKE_QR;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directoryselect);
        ButterKnife.bind(this);

        purpose = getIntent().getIntExtra(EXTRA_PURPOSE, PURPOSE_MAKE_QR);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        rvPhoto.setPadding(0, 0, 0, softKeyHeight);
        rvPhoto.setItemAnimator(new ScaleInAnimator());
        rvPhoto.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvPhoto.setLayoutManager(llm);

        DirectorySelectAdapter directorySelectAdapter = new DirectorySelectAdapter(getApplicationContext(), this);
        rvPhoto.setAdapter(directorySelectAdapter);
    }

    @Override
    public void onSelectDirectory(int idx, String key) {
        PhotoSelectAdapter.photoList = DirectorySelectAdapter.photoDirectories.get(key);
        Intent photoSelect = new Intent(this, PhotoSelectActivity.class);
        photoSelect.putExtra(EXTRA_PURPOSE, purpose);
        startActivityForResult(photoSelect, REQCODE_PHOTOSELECTOR);
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        onToolbarNavigator();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQCODE_PHOTOSELECTOR:
                if(resultCode == RESULT_OK || resultCode == RESULT_GIF) {
                    setResult(resultCode, intent);
                    finish();
                }
                break;
        }
    }
}
