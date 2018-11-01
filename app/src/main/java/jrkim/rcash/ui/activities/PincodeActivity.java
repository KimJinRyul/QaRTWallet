package jrkim.rcash.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.fingerprint.FingerprintHandler;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.BROADCAST_PINCODE_WILL_APPEAR;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_FINGERPRINT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_SECUREMODE;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_STRING_SECURE_PASSWORD;

public class PincodeActivity extends BaseActivity implements FingerprintHandler.FingerprintListener {

    private static final String TAG = "RCash_PincodeActivity";

    public static final String EXTRA_TYPE = "extra_type";
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_REMOVE_PASSWORD = 1;
    public static final int TYPE_SET_PASSWORD = 2;

    public static final int PINCODE_RESULT_CANCELD = -2;
    public static final int PINCODE_RESULT_PASS = 1;
    public static final int PINCODE_RESULT_REMOVE_PASS = 2;
    public static final int PINCODE_RESULT_SETTED = 3;

    private static final int MESSAGE_CLEAR_PASSWORD = 1000;
    private static final int MESSAGE_CHECK_SUCCESS = 1001;
    private static final int PINCODE_LENGTH = 6;

    private String pincodeInput[] = new String[PINCODE_LENGTH];
    private View views[] = new View[PINCODE_LENGTH];
    private int currentLength = 0;
    private int type = TYPE_DEFAULT;
    private int step = 1;
    private String firstPassword = null;
    private CancellationSignal cancellationSignal = null;
    private SharedPreferenceMgr sharedPreferenceMgr = null;

    public static int launchedCount = 0;
    private boolean checkingNow = false;

    @BindView(R.id.view01) View view01;
    @BindView(R.id.view02) View view02;
    @BindView(R.id.view03) View view03;
    @BindView(R.id.view04) View view04;
    @BindView(R.id.view05) View view05;
    @BindView(R.id.view06) View view06;
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindColor(R.color.colorAccent) int colorAccent;
    @BindView(R.id.ivFingerprint) ImageView ivFingerprintl;

    private void addNewPass(String newPass) {
        if(currentLength < PINCODE_LENGTH)
            pincodeInput[currentLength++] = newPass;
    }
    @OnClick({R.id.tv0, R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6, R.id.tv7, R.id.tv8, R.id.tv9, R.id.tvBack})
    public void onNumPad(View view) {
        if(checkingNow)
            return;
        switch (view.getId()) {
            case R.id.tv0:  addNewPass("0"); break;
            case R.id.tv1:  addNewPass("1"); break;
            case R.id.tv2:  addNewPass("2"); break;
            case R.id.tv3:  addNewPass("3"); break;
            case R.id.tv4:  addNewPass("4"); break;
            case R.id.tv5:  addNewPass("5"); break;
            case R.id.tv6:  addNewPass("6"); break;
            case R.id.tv7:  addNewPass("7"); break;
            case R.id.tv8:  addNewPass("8"); break;
            case R.id.tv9:  addNewPass("9"); break;
            case R.id.tvBack:
                if(currentLength > 0) {
                    currentLength -= 1;
                } else {
                    if(type == TYPE_SET_PASSWORD || type == TYPE_REMOVE_PASSWORD) {
                        new AlertDialog.Builder(this, R.style.AlertDialog)
                                .setTitle(R.string.pincode_cancel_set)
                                .setMessage(R.string.pincode_cancel_set_desc)
                                .setPositiveButton(R.string.pincode_conitnue, (dialog, whitch) -> {
                                    clearPincode();
                                })
                                .setNegativeButton(R.string.pincode_nexttime, (dialog, which) -> {
                                    setResult(PINCODE_RESULT_CANCELD);
                                    finish();
                                }).show();
                    }
                }
                break;
        }
        updatePinCode();

        if(currentLength == PINCODE_LENGTH) {
            if(type == TYPE_SET_PASSWORD) {
                String input = "";
                for(int i = 0; i < PINCODE_LENGTH; i++) {
                    input += pincodeInput[i];
                }

                if(step == 1) {
                    firstPassword = String.valueOf(input.hashCode());
                    step = 2;
                    tvTitle.setText(R.string.pincode_onemoretime);
                    clearPincode();
                } else if(step == 2) {
                    if(firstPassword.equals(String.valueOf(input.hashCode()))) {
                        sharedPreferenceMgr.put(SHAREDPREF_BOOL_SECUREMODE, true);
                        sharedPreferenceMgr.put(SHAREDPREF_STRING_SECURE_PASSWORD, firstPassword);

                        PincodeActivity.sendEmptyMessageDelayed(handler, MESSAGE_CHECK_SUCCESS, 200);
                    } else {
                        Toast.makeText(this, R.string.pincode_notmatched, Toast.LENGTH_SHORT).show();
                        step = 1;
                        firstPassword = null;
                        clearPincode();
                        tvTitle.setText(R.string.pincode_desc_set);
                    }
                }
            } else {
                checkingNow = true;
                String password = sharedPreferenceMgr.get(SHAREDPREF_STRING_SECURE_PASSWORD, null);
                new Thread(() -> {
                    String input = "";
                    for(int i = 0; i < PINCODE_LENGTH; i++) {
                        input += pincodeInput[i];
                    }

                    if(password != null) {
                        if (password.equals(String.valueOf(input.hashCode()))) {
                            PincodeActivity.sendEmptyMessageDelayed(handler, MESSAGE_CHECK_SUCCESS, 500);
                        } else {
                            checkingNow = false;
                            PincodeActivity.sendEmptyMessageDelayed(handler, MESSAGE_CLEAR_PASSWORD, 200);
                        }
                    } else {
                        sharedPreferenceMgr.put(SHAREDPREF_BOOL_SECUREMODE, false);
                        finish();
                    }
                }).start();
            }
        }
    }

    private void checkSuccessAndFinish() {
        if(type == TYPE_REMOVE_PASSWORD) {
            sharedPreferenceMgr.put(SHAREDPREF_BOOL_SECUREMODE, false);
            sharedPreferenceMgr.put(SHAREDPREF_STRING_SECURE_PASSWORD, null);
            sharedPreferenceMgr.put(SHAREDPREF_BOOL_FINGERPRINT, false);
            setResult(PINCODE_RESULT_REMOVE_PASS);
        } else if(type == TYPE_SET_PASSWORD) {
            setResult(PINCODE_RESULT_SETTED);
            finish();
        } else {
            setResult(PINCODE_RESULT_PASS);
        }
        finish();
    }

    private void clearPincode() {
        currentLength = 0;
        updatePinCode();
    }

    private void updatePinCode() {
        for(int i = 0; i < PINCODE_LENGTH; i++) {
            if(i < currentLength) {
                views[i].setBackgroundResource(R.drawable.shape_round_pincode_full);
            } else {
                views[i].setBackgroundResource(R.drawable.shape_round_pincode_empty);
            }
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_CLEAR_PASSWORD:
                checkingNow = false;
                tvTitle.setText(R.string.pincode_notmatched);
                clearPincode();
                Toast.makeText(this, R.string.pincode_notmatched, Toast.LENGTH_SHORT).show();
                break;

            case BROADCAST_PINCODE_WILL_APPEAR:
                if(type == TYPE_REMOVE_PASSWORD) {
                    type = TYPE_DEFAULT;

//                if(sharedPreferenceMgr.get(SHAREDPREF_BOOL_FINGERPRINT, false)) {
//                     tvTitle.setText(R.string.pincode_desc_with_fingerprint);
//                        ivFingerprintl.setVisibility(View.VISIBLE);
//                    } else {
                    tvTitle.setText(R.string.pincode_desc);
                    ivFingerprintl.setVisibility(View.GONE);
//                    }
                    clearPincode();
                }



                break;
            case MESSAGE_CHECK_SUCCESS:
                checkSuccessAndFinish();
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchedCount++;

        synchronized (TAG) {
            if(launchedCount > 1) {
                finish();
            } else {
                sharedPreferenceMgr = new SharedPreferenceMgr(this);

                type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_DEFAULT);
                setContentView(R.layout.activity_pincode);
                ButterKnife.bind(this);

                Utils.setGlobalFont(getWindow().getDecorView(), Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
                tvTitle.setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_REGULAR));

                findViewById(R.id.llDesc).setPadding(0, statusBarHeight, 0, 0);
                findViewById(R.id.llNumPad).setPadding(0, 0, 0, softKeyHeight);

                views[0] = view01;
                views[1] = view02;
                views[2] = view03;
                views[3] = view04;
                views[4] = view05;
                views[5] = view06;

                updatePinCode();

                if(type == TYPE_SET_PASSWORD) {
                    step = 1;
                    tvTitle.setText(R.string.pincode_desc_set);
                    ivFingerprintl.setVisibility(View.GONE);
                } else if(type == TYPE_REMOVE_PASSWORD) {
                    tvTitle.setText(R.string.pincode_desc_remove);
                    ivFingerprintl.setVisibility(View.GONE);
                } else {
//                    if(sharedPreferenceMgr.get(SHAREDPREF_BOOL_FINGERPRINT, false)) {
//                        if(KeyStoreMgr.getInstance().isKeyExist(KeyStoreMgr.TAG_KEY_BIOMETRIC)) {
//                            cancellationSignal = new CancellationSignal();
//                            KeyStoreMgr.getInstance().unlockAuthenticate(this, this, cancellationSignal);
//                            tvTitle.setText(R.string.pincode_desc_with_fingerprint);
//                            ivFingerprintl.setVisibility(View.VISIBLE);
//                        } else {
//                            sharedPreferenceMgr.put(SHAREDPREF_BOOL_FINGERPRINT, false);
//                            tvTitle.setText(R.string.pincode_desc);
//                            ivFingerprintl.setAlpha(0.f);
//                        }
//                    } else {
                        tvTitle.setText(R.string.pincode_desc);
                        ivFingerprintl.setVisibility(View.GONE);
//                    }
                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        launchedCount--;
    }

    @Override
    public void onBackPressed() {
        if(currentLength > 0) {
            currentLength -= 1;
            updatePinCode();
        } else {
            if (type == TYPE_REMOVE_PASSWORD || type == TYPE_SET_PASSWORD) {
                new AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.pincode_cancel_set)
                        .setMessage(R.string.pincode_cancel_set_desc)
                        .setPositiveButton(R.string.pincode_conitnue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentLength = 0;
                                updatePinCode();
                            }
                        })
                        .setNegativeButton(R.string.pincode_nexttime, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(PINCODE_RESULT_CANCELD);
                                finish();
                            }
                        }).show();
            } else {
                moveTaskToBack(true);
            }
        }
    }

    @Override
    public void onAuthenticationError(int errId, CharSequence errString) {

        switch (errId) {
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                Toast.makeText(this, errString, Toast.LENGTH_SHORT).show();
                break;

            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT:
                Toast.makeText(this, errString, Toast.LENGTH_SHORT).show();
                break;

            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                break;

            default:
                break;
        }
        cancellationSignal.cancel();
        cancellationSignal = null;
    }

    @Override
    public void onAuthenticationHelp(int helpId, CharSequence helpString) {
        Toast.makeText(this, helpString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    @Override
    public void onAuthenticationSucceded(FingerprintManager.AuthenticationResult result) {
        currentLength = PINCODE_LENGTH;
        updatePinCode();
        sendEmptyMessageDelayed(handler, MESSAGE_CHECK_SUCCESS, 500);
    }
}
