package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.ui.adapters.PhotoSelectAdapter;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.EXTRA_PURPOSE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_CROPIMAGE;
import static jrkim.rcash.consts.RCashConsts.RESULT_GIF;
import static jrkim.rcash.ui.activities.DirectorySelectActivity.PURPOSE_MAKE_QR;

public class PhotoSelectActivity extends BaseActivity implements PhotoSelectAdapter.PhotoSelectListener {

    @BindView(R.id.rvPhoto) RecyclerView rvPhoto;

    private int purpose = PURPOSE_MAKE_QR;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoselector);
        ButterKnife.bind(this);

        purpose = getIntent().getIntExtra(EXTRA_PURPOSE, PURPOSE_MAKE_QR);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        rvPhoto.setPadding(0, 0, 0, softKeyHeight);

        rvPhoto.setItemAnimator(new ScaleInAnimator());
        rvPhoto.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        rvPhoto.setLayoutManager(glm);
        rvPhoto.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int spacingInPixels = Utils.dpToPixel(view.getContext(), 2.f);
                outRect.top = spacingInPixels;
                outRect.left = spacingInPixels;
                outRect.right = spacingInPixels;
                outRect.bottom = spacingInPixels;
            }
        });

        PhotoSelectAdapter photoSelectAdapter = new PhotoSelectAdapter(getApplicationContext(), this);
        rvPhoto.setAdapter(photoSelectAdapter);
    }

    @Override
    public void onSelectPhoto(String path) {
        if(purpose == PURPOSE_MAKE_QR) {
            Log.i("RCash", "path: " + path);
            Intent intent = new Intent(this, CropImageActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, path);
            startActivityForResult(intent, REQCODE_CROPIMAGE);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_TEXT, path);
            setResult(RESULT_OK, intent);
            finish();
        }
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
            case REQCODE_CROPIMAGE:
                if(resultCode == RESULT_OK || resultCode == RESULT_GIF) {
                    File file = new File(Utils.getUserQRCodePath(this));
                    if(file.exists())
                        file.delete();
                    setResult(resultCode, intent);
                    finish();
                }
                break;
        }
    }
}
