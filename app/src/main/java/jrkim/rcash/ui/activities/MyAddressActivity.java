package jrkim.rcash.ui.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.RCashAddress;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.ADDRESS_FORMAT_CASHADDR;
import static jrkim.rcash.consts.RCashConsts.ADDRESS_FORMAT_LEGACY;
import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_AMOUNT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_BOTTOM;
import static jrkim.rcash.consts.RCashConsts.EXTRA_FILEPATH;
import static jrkim.rcash.consts.RCashConsts.EXTRA_LEFT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_MODE;
import static jrkim.rcash.consts.RCashConsts.EXTRA_PURPOSE;
import static jrkim.rcash.consts.RCashConsts.EXTRA_RIGHT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_TOP;
import static jrkim.rcash.consts.RCashConsts.REQCODE_PHOTOSELECTOR;
import static jrkim.rcash.consts.RCashConsts.REQCODE_SEND;
import static jrkim.rcash.consts.RCashConsts.RESULT_GIF;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_ADDRESS_FORMAT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_BOTTOM;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_LEFT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_RIGHT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_CROP_TOP;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_EDIT_QRCODE;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_QRCODE_DOT_SIZE;
import static jrkim.rcash.ui.activities.DirectorySelectActivity.PURPOSE_MAKE_QR;
import static jrkim.rcash.utils.Utils.createGIFQRCode;
import static jrkim.rcash.utils.Utils.createQRCode;
import static jrkim.rcash.utils.Utils.deleteAllQRImages;
import static jrkim.rcash.utils.Utils.getUserCustomGIFQRBGPath;
import static jrkim.rcash.utils.Utils.getUserCustomQRBGPath;
import static jrkim.rcash.utils.Utils.getUserGIFQRCodePath;
import static jrkim.rcash.utils.Utils.getUserQRCodePath;

public class MyAddressActivity extends BaseActivity {

    private final static String TAG = "RCash-MyAddr";
    public static String address = null;

    private final static int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;

    private final static int MESSAGE_GIF_COMPLETED = 100;
    private final static int MESSAGE_GIF_COMPLETED_ADDRESS_FORMAT = 101;

    @BindView(R.id.ivQRCode) ImageView ivQRCode;
    @BindView(R.id.ivToolbarEdit) ImageView ivToolbarEdit;
    @BindView(R.id.seekBar) AppCompatSeekBar seekBar;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    @BindView(R.id.tvProgressMsg) TextView tvProgressMsg;

    @OnClick(R.id.ivDelete)
    public void onDelete() {
        deleteAllQRImages(this, true);

        int dotSize = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);
        File fQRcode = Utils.createQRCode(getApplicationContext(), address, dotSize);
        if(fQRcode != null) {
            Glide.with(this)
                    .load(fQRcode)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .crossFade()
                    .into(ivQRCode);
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_GIF_COMPLETED_ADDRESS_FORMAT:
                ((TextView) findViewById(R.id.tvAddress)).setText(address);
                if(msg.arg1 == ADDRESS_FORMAT_CASHADDR)
                    Toast.makeText(this, R.string.myaddress_addr_format_cash, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.myaddress_addr_format_legacy, Toast.LENGTH_SHORT).show();

                // NO BREAK HERE..... pass through

            case MESSAGE_GIF_COMPLETED:
                File oFile = (File)msg.obj;
                rlProgress.setVisibility(View.GONE);
                tvProgressMsg.setVisibility(View.GONE);
                if(oFile != null && oFile.exists()) {
                    Glide.with(this)
                            .load(oFile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .crossFade()
                            .into(ivQRCode);
                } else {
                    Toast.makeText(this, "Something Wrong...", Toast.LENGTH_SHORT).show();
                }

                break;

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myaddress);
        ButterKnife.bind(this);

        int dotSize = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);

        File qrCode;
        boolean creatingGIFNow = false;
        // 우선 PNG QR이 있는지 확인....
        qrCode = new File(getUserQRCodePath(this));
        if(!qrCode.exists()) {
            // 없네.. 그럼 PNG BG가 있는지 확인해서 만든다.
            File bg = new File(getUserCustomQRBGPath(this));
            if(bg.exists()) {
               // 있으면 이걸로 만든다.
                qrCode = Utils.createQRCode(getApplicationContext(), address, dotSize, true);
            } else {
                // 혹시 GIF 파일이 있는지 확인한다.
                qrCode = new File(getUserGIFQRCodePath(this));
                if(!qrCode.exists()) {
                    // GIF BG가 있는지 화인해서 만든다.
                    bg = new File(getUserCustomGIFQRBGPath(this));
                    if(bg.exists()) {
                        creatingGIFNow = true;
                        rlProgress.setVisibility(View.VISIBLE);
                        tvProgressMsg.setVisibility(View.VISIBLE);
                        new Thread(()->{
                            SharedPreferenceMgr sharedPref = new SharedPreferenceMgr(getApplicationContext());
                            File oFile = createGIFQRCode(getApplicationContext(),
                                    getUserCustomGIFQRBGPath(getApplicationContext()),
                                    sharedPref.get(SHAREDPREF_INT_CROP_LEFT, 0),
                                    sharedPref.get(SHAREDPREF_INT_CROP_TOP, 0),
                                    sharedPref.get(SHAREDPREF_INT_CROP_RIGHT, 100),
                                    sharedPref.get(SHAREDPREF_INT_CROP_BOTTOM, 100),
                                    address,
                                    dotSize,
                                    true);
                            sendMessage(handler, MESSAGE_GIF_COMPLETED, oFile);
                        }).start();
                    } else {
                        qrCode = Utils.createQRCode(getApplicationContext(), address, dotSize, true);
                    }
                }
            }
        }

        if((!creatingGIFNow && !qrCode.exists()) || address == null) {
            finish();
        }


        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        ((TextView)findViewById(R.id.tvAddress)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_SANSATION_LIGHT));

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.rlBottom).setPadding(Utils.dpToPixel(this, 32), 0, Utils.dpToPixel(this, 32), softKeyHeight);

        if(qrCode != null) {
            Glide.with(this)
                    .load(qrCode)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .crossFade()
                    .into(ivQRCode);
        }

        ((TextView)findViewById(R.id.tvAddress)).setText(address);

        seekBar.setProgress(dotSize);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int dotSize = seekBar.getProgress() + 10;
                rlProgress.setVisibility(View.VISIBLE);
                new SharedPreferenceMgr(getApplicationContext()).put(SHAREDPREF_INT_QRCODE_DOT_SIZE, dotSize);
                new Handler().post(()-> {

                    File bgFile = new File(getUserCustomQRBGPath(getApplicationContext()));
                    if(bgFile.exists()) {
                        File newQRcode = Utils.createQRCode(getApplicationContext(), address, dotSize, true);
                        if (newQRcode != null && newQRcode.exists()) {
                            Glide.with(getApplicationContext())
                                    .load(newQRcode)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .centerCrop()
                                    .crossFade()
                                    .into(ivQRCode);
                            rlProgress.setVisibility(View.GONE);
                        }
                    } else {
                        bgFile = new File(getUserCustomGIFQRBGPath(getApplicationContext()));
                        if(bgFile.exists()) {
                            rlProgress.setVisibility(View.VISIBLE);
                            tvProgressMsg.setVisibility(View.VISIBLE);
                            new Thread(()->{
                                SharedPreferenceMgr sharedPref = new SharedPreferenceMgr(getApplicationContext());
                                File oFile = createGIFQRCode(getApplicationContext(),
                                        getUserCustomGIFQRBGPath(getApplicationContext()),
                                        sharedPref.get(SHAREDPREF_INT_CROP_LEFT, 0),
                                        sharedPref.get(SHAREDPREF_INT_CROP_TOP, 0),
                                        sharedPref.get(SHAREDPREF_INT_CROP_RIGHT, 100),
                                        sharedPref.get(SHAREDPREF_INT_CROP_BOTTOM, 100),
                                        address,
                                        dotSize,
                                        true);
                                sendMessage(handler, MESSAGE_GIF_COMPLETED, oFile);
                            }).start();
                        }
                    }
                });
            }
        });

        int cnt = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_EDIT_QRCODE, 0);
        if(cnt <= 3) {
            cnt++;
            new SharedPreferenceMgr(this).put(SHAREDPREF_INT_EDIT_QRCODE, cnt);
            TapTargetView.showFor(this,
                    TapTarget.forView(ivToolbarEdit, getString(R.string.tabtarget_delete_ttl), getString(R.string.tabtarget_delete_desc))
                            .outerCircleColor(R.color.bitcoinGreen)
                            .outerCircleAlpha(0.96f)
                            .targetCircleColor(R.color.colorWhite)
                            .textColor(R.color.colorTextLight)
                            .titleTextSize(28)
                            .descriptionTextSize(16)
                            .textTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NANUM_SQUARE_R))
                            .dimColor(R.color.colorBgBlack)
                            .drawShadow(true)
                            .cancelable(true)
                            .tintTarget(true)
                            .transparentTarget(true)
                            .targetRadius(52),
                    null);
        }

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
    }

    @OnClick(R.id.tvRequestCertainAmount)
    public void onRequestCertainAmount() {
        SendActivity.wallet = MainActivity.wallet;
        Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra(EXTRA_MODE, SendActivity.MODE_REQUEST);
        startActivityForResult(intent, REQCODE_SEND);
    }

    @OnLongClick(R.id.ivQRCode)
    public boolean onLongClickQRCode() {
        Log.i(TAG, "onLongClickQRCode");

        RCashAddress rCashAddress = Utils.parseAddress(address);

        if(rCashAddress != null && rCashAddress.cashAddress != null) {

            SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
            int curFormat = sharedPreferenceMgr.get(SHAREDPREF_INT_ADDRESS_FORMAT, ADDRESS_FORMAT_CASHADDR);
            if (curFormat == ADDRESS_FORMAT_CASHADDR)
                curFormat = ADDRESS_FORMAT_LEGACY;
            else
                curFormat = ADDRESS_FORMAT_CASHADDR;

            String newAddress = null;
            switch (curFormat) {
                case ADDRESS_FORMAT_LEGACY:
                    newAddress = rCashAddress.cashAddress.toBase58();
                    break;
                case ADDRESS_FORMAT_CASHADDR:
                    newAddress = rCashAddress.cashAddress.toString();
                    break;
            }

            if(newAddress != null) {
                int dotSize = sharedPreferenceMgr.get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);

                File file = new File(getUserQRCodePath(this));
                if(file != null && file.exists()) {
                    File newFile = createQRCode(this, newAddress, dotSize, true);
                    if(newFile != null && newFile.exists()) {
                        address = newAddress;
                        ((TextView) findViewById(R.id.tvAddress)).setText(address);
                        sharedPreferenceMgr.put(SHAREDPREF_INT_ADDRESS_FORMAT, curFormat);

                        if(curFormat == ADDRESS_FORMAT_CASHADDR)
                            Toast.makeText(this, R.string.myaddress_addr_format_cash, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(this, R.string.myaddress_addr_format_legacy, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    file = new File(getUserGIFQRCodePath(this));
                    if(file != null && file.exists()) {
                        rlProgress.setVisibility(View.VISIBLE);
                        tvProgressMsg.setVisibility(View.VISIBLE);
                        final String addressForThread = newAddress;
                        final int formatForThread = curFormat;
                        new Thread(()->{
//                            SharedPreferenceMgr sharedPreferenceMgr1 = new SharedPreferenceMgr(this);
                            File oFile = createGIFQRCode(this,
                                    getUserCustomGIFQRBGPath(this),
                                    sharedPreferenceMgr.get(SHAREDPREF_INT_CROP_LEFT, 0),
                                    sharedPreferenceMgr.get(SHAREDPREF_INT_CROP_TOP, 0),
                                    sharedPreferenceMgr.get(SHAREDPREF_INT_CROP_RIGHT, 100),
                                    sharedPreferenceMgr.get(SHAREDPREF_INT_CROP_BOTTOM, 100),
                                    addressForThread,
                                    dotSize,
                                    true);

                            if(oFile != null && oFile.exists()) {
                                address = addressForThread;
                                sharedPreferenceMgr.put(SHAREDPREF_INT_ADDRESS_FORMAT, formatForThread);

                            }
                            sendMessage(handler, MESSAGE_GIF_COMPLETED_ADDRESS_FORMAT, formatForThread, 0, oFile);
                        }).start();

                    }
                }
            }
        }
        return false;
    }

    @OnLongClick(R.id.tvAddress)
    public boolean onLongClickAddress() {
        Log.i(TAG, "onLongClickAddress");
        if(address != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("bitcoincash address", address);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.myaddress_toast_copy_address, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @OnClick(R.id.btnShareAddress)
    public void onCopyAddreess() {
        Log.i(TAG, "onShareAddress");
        if(address != null) {
            Utils.launchShare(getApplicationContext(), address, getString(R.string.app_name));
        }
    }

    @OnClick(R.id.btnShareQRcode)
    public void onShareAddress() {
        Log.i(TAG, "onShareQRCode");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = getLocalBitmapUri();
            if (uri != null) {
                Log.i(TAG, "onShareAddress != null");
                Utils.launchShareImage(getApplicationContext(), uri, getString(R.string.app_name));
            } else {
                Log.i(TAG, "onShareAddress - null");
            }
        } else {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public Uri getLocalBitmapUri() {
        // Extract Bitmap from ImageView drawable
        // Store image to default external storage directory
        File file = new File(getUserQRCodePath(this));
        if(file == null || !file.exists()) {
            file = new File(getUserGIFQRCodePath(this));
        }

        if(file != null && file.exists()) {
            return  FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
        }
        return null;
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        finish();
    }

    @OnClick(R.id.ivToolbarEdit)
    public void onToolbarEdit() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent photoSelect = new Intent(this, DirectorySelectActivity.class);
            photoSelect.putExtra(EXTRA_PURPOSE, PURPOSE_MAKE_QR);
            startActivityForResult(photoSelect, REQCODE_PHOTOSELECTOR);
        } else {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoSelect = new Intent(this, DirectorySelectActivity.class);
                    photoSelect.putExtra(EXTRA_PURPOSE, PURPOSE_MAKE_QR);
                    startActivityForResult(photoSelect, REQCODE_PHOTOSELECTOR);
                } else {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, R.string.main_permission_storage_ttl, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.main_permission_storage_desc, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    onShareAddress();
                } else {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, R.string.main_permission_storage_ttl, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.main_permission_storage_desc, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQCODE_PHOTOSELECTOR:
                int dotSize = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);

                if(resultCode == RESULT_OK) {

                    File qrcode = Utils.createQRCode(getApplicationContext(), address, dotSize);
                    if(qrcode != null && qrcode.exists()) {
                        Glide.with(this)
                                .load(qrcode)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .centerCrop()
                                .crossFade()
                                .into(ivQRCode);
                    }
                } else if(resultCode == RESULT_GIF) {

                    Log.i(TAG, "RESULT_GIF");
                    rlProgress.setVisibility(View.VISIBLE);
                    tvProgressMsg.setVisibility(View.VISIBLE);
                    new Handler().postAtTime(()->{
                        Log.i(TAG, "postAtTime...");
                        new Thread(()->{
                            Log.i(TAG, "Create GIF Start");
                            File oFile = createGIFQRCode(this,
                                    intent.getStringExtra(EXTRA_FILEPATH),
                                    intent.getIntExtra(EXTRA_LEFT, 0),
                                    intent.getIntExtra(EXTRA_TOP, 0),
                                    intent.getIntExtra(EXTRA_RIGHT, 100),
                                    intent.getIntExtra(EXTRA_BOTTOM, 100),
                                    address,
                                    dotSize,
                                    true);

                            Log.i(TAG, "Create GIF FIN");

                            sendMessage(handler, MESSAGE_GIF_COMPLETED, oFile);

                        }).start();
                    }, 100);

                }
                break;
            case REQCODE_SEND:
                if(resultCode == RESULT_OK) {
                    long satoshis = intent.getLongExtra(EXTRA_AMOUNT, 0);
                    Intent newIntent = new Intent(this, RequestActivity.class);
                    newIntent.putExtra(EXTRA_ADDRESS, address);
                    newIntent.putExtra(EXTRA_AMOUNT, satoshis);
                    startActivity(newIntent);
                }
                break;
        }
    }
}
