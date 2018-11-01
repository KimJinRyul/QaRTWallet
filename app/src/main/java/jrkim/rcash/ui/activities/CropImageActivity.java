package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.utils.SharedPreferenceMgr;

import static jrkim.rcash.consts.RCashConsts.EXTRA_BOTTOM;
import static jrkim.rcash.consts.RCashConsts.EXTRA_FILEPATH;
import static jrkim.rcash.consts.RCashConsts.EXTRA_LEFT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_RIGHT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_TOP;
import static jrkim.rcash.consts.RCashConsts.RESULT_GIF;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_BOTTOM;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_LEFT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_RIGHT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_TOP;
import static jrkim.rcash.utils.Utils.getUserCustomGIFQRBGPath;
import static jrkim.rcash.utils.Utils.getUserCustomQRBGPath;
import static jrkim.rcash.utils.Utils.getUserQRCodePath;

public class CropImageActivity extends BaseActivity {

    private final static String TAG = "RCash_CropImg";
    private String originalPath = null;;
    private Uri originalUri = null;

    @BindView(R.id.cropImageView) CropImageView cropImageView;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropimage);
        ButterKnife.bind(this);

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.rlBottom).setPadding(0,0, 0, softKeyHeight);

        originalPath = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        originalUri = Uri.fromFile(new File(originalPath));

        cropImageView.setFixedAspectRatio(true);
        cropImageView.setImageUriAsync(originalUri);
    }

    @OnClick(R.id.ivRotateLeft)
    public void onRotateLeft() {
        cropImageView.rotateImage(-90);
    }

    @OnClick(R.id.ivRotateRight)
    public void onRotateRight() {
        cropImageView.rotateImage(90);
    }

    @OnClick(R.id.ivFlipV)
    public void onFlipV() {
        cropImageView.flipImageVertically();
    }

    @OnClick(R.id.ivFlipH)
    public void onFlipH() {
        cropImageView.flipImageHorizontally();
    }

    @OnClick(R.id.btnConfirm)
    public void onConfirm() {
        String ext = originalPath.substring(originalPath.lastIndexOf(".") + 1).toLowerCase();

        // delete prev qr images
        String qrbgPath = getUserCustomQRBGPath(this);
        File fQrbg = new File(qrbgPath);
        if(fQrbg.exists()) {
            fQrbg.delete();
        }

        String userqrPath = getUserQRCodePath(this);
        File fUserQR = new File(userqrPath);
        if(fUserQR.exists()) {
            fUserQR.delete();
        }

        String userGifQRBGPath = getUserCustomGIFQRBGPath(this);
        File fUserGIFQRBG = new File(userGifQRBGPath);
        if(fUserGIFQRBG.exists()) {
            fUserGIFQRBG.delete();
        }

        if(ext.equals("gif")) {
            Rect rect = cropImageView.getCropRect();

            String gifPath = getUserCustomGIFQRBGPath(this, originalPath);
            if(gifPath != null) {

                SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
                sharedPreferenceMgr.put(SHAREDPREF_INT_CROP_LEFT, rect.left);
                sharedPreferenceMgr.put(SHAREDPREF_INT_CROP_TOP, rect.top);
                sharedPreferenceMgr.put(SHAREDPREF_INT_CROP_RIGHT, rect.right);
                sharedPreferenceMgr.put(SHAREDPREF_INT_CROP_BOTTOM, rect.bottom);

                Intent intent = new Intent();
                intent.putExtra(EXTRA_LEFT, rect.left);
                intent.putExtra(EXTRA_TOP, rect.top);
                intent.putExtra(EXTRA_RIGHT, rect.right);
                intent.putExtra(EXTRA_BOTTOM, rect.bottom);
                intent.putExtra(EXTRA_FILEPATH, gifPath);
                setResult(RESULT_GIF, intent);
                finish();

            } else {
                Toast.makeText(this, "Something wrong...", Toast.LENGTH_SHORT).show();
            }
        } else {
            findViewById(R.id.rlProgress).setVisibility(View.VISIBLE);

            Bitmap bitmap = cropImageView.getCroppedImage();

            new Thread(()-> {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                    fQrbg.createNewFile();
                    FileOutputStream fos = new FileOutputStream(fQrbg);
                    fos.write(baos.toByteArray());
                    fos.close();

                    setResult(RESULT_OK);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CropImageActivity.this, "SAVE ERROR", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }

            }).start();
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
}
