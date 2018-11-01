package jrkim.rcash.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.github.sumimakito.awesomeqr.GifPipeline;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.BIP38PrivateKey;
import org.bitcoinj.params.MainNetParams;

import java.io.File;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.RCashAddress;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_AMOUNT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_MESSAGE;
import static jrkim.rcash.consts.RCashConsts.EXTRA_PURPOSE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_PHOTOSELECTOR;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_READ_QRCODE_FROM_IMAGEFILE;
import static jrkim.rcash.ui.activities.DirectorySelectActivity.PURPOSE_READ_QR;

public class QRCodeActivity extends BaseActivity implements QRCodeReaderView.OnQRCodeReadListener {

    public final static int RESULT_BIP38 = 2;
    public final static int RESULT_WIF = 3;
    private final static String TAG = "RCash-QRCode";
    private boolean alreadyFound = false;

    private final static int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;

    @BindView(R.id.qrdecoderview) QRCodeReaderView qrCodeReaderView;
    @BindView(R.id.viewTop) View viewTop;
    @BindView(R.id.viewLeft) View viewLeft;
    @BindView(R.id.viewRight) View viewRight;
    @BindView(R.id.viewBottom) View viewBottom;
    @BindView(R.id.ivToolBarImage) ImageView ivToolBarImage;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodereader);
        ButterKnife.bind(this);

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);

        qrCodeReaderView.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(1000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);

        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();

        int holeWidth = (totalWidth / 3) * 2;
        int leftWidth = totalWidth - holeWidth;
        int leftHeight = totalHeight - holeWidth - Utils.dpToPixel(getApplicationContext(), 48); // ToolBar

        ViewGroup.LayoutParams lp;
        lp = viewTop.getLayoutParams();
        lp.height = (leftHeight / 2) - softKeyHeight;
        viewTop.setLayoutParams(lp);

        lp = viewBottom.getLayoutParams();
        lp.height = (leftHeight / 2) + softKeyHeight;
        viewBottom.setLayoutParams(lp);

        lp = viewLeft.getLayoutParams();
        lp.width = leftWidth / 2;
        viewLeft.setLayoutParams(lp);

        lp = viewRight.getLayoutParams();
        lp.width = leftWidth / 2;
        viewRight.setLayoutParams(lp);

        int cnt = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_READ_QRCODE_FROM_IMAGEFILE, 0);
        if(cnt <= 3) {
            cnt++;
            new SharedPreferenceMgr(this).put(SHAREDPREF_INT_READ_QRCODE_FROM_IMAGEFILE, cnt);
            TapTargetView.showFor(this,
                    TapTarget.forView(ivToolBarImage, getString(R.string.tabtarget_read_from_file_ttl), getString(R.string.tabtarget_read_from_file_desc))
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
    }

    @OnClick(R.id.ivToolBarImage)
    public void onToolBarImage() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent photoSelect = new Intent(this, DirectorySelectActivity.class);
            photoSelect.putExtra(EXTRA_PURPOSE, PURPOSE_READ_QR);
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
                    photoSelect.putExtra(EXTRA_PURPOSE, PURPOSE_READ_QR);
                    startActivityForResult(photoSelect, REQCODE_PHOTOSELECTOR);
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
                if(resultCode == RESULT_OK) {
                    String imgPath = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String temp = imgPath.toLowerCase();
                    if(temp.endsWith(".gif")) {
                        boolean found = false;
                        GifPipeline gifPipeline = new GifPipeline();
                        if(gifPipeline.init(new File(imgPath))) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                            gifPipeline.clippingRect = new RectF(0 , 0, bitmap.getWidth(), bitmap.getHeight());
                            Bitmap frame = gifPipeline.nextFrame();
                            if(frame != null) {
                                int[] intArray = new int[frame.getWidth() * frame.getHeight()];
                                QRCodeReader qrCodeReader = new QRCodeReader();
                                Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                                hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                                Result result = null;
                                while (frame != null) {
                                    frame.getPixels(intArray, 0, frame.getWidth(), 0, 0, frame.getWidth(), frame.getHeight());
                                    LuminanceSource source = new RGBLuminanceSource(frame.getWidth(), frame.getHeight(), intArray);
                                    HybridBinarizer binarizer = new HybridBinarizer(source);
                                    BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

                                    frame.recycle();

                                    try {
                                        result = qrCodeReader.decode(binaryBitmap, hints);
                                    } catch (NotFoundException e) {
                                        result = null;
                                    } catch (ChecksumException e) {
                                        result = null;
                                    } catch (FormatException e) {
                                        result = null;
                                    }

                                    qrCodeReader.reset();
                                    if(result == null) {
                                        try {
                                            result = qrCodeReader.decode(binaryBitmap);
                                        } catch (NotFoundException e) {
                                            result = null;
                                        } catch (ChecksumException e) {
                                            result = null;
                                        } catch (FormatException e) {
                                            result = null;
                                        }
                                    }
                                    qrCodeReader.reset();

                                    if (result != null) {

                                        String text = result.getText();
                                        //onQRCodeRead(text, null);
                                        found = checkAddress(text, false);
                                        break;
                                    }

                                    frame = gifPipeline.nextFrame();
                                }
                            }
                        }

                        if(!found) {
                            Toast.makeText(this, R.string.qrcode_no_address_in_qrcode, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);

                        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                        HybridBinarizer binarizer = new HybridBinarizer(source);
                        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

                        bitmap.recycle();

                        QRCodeReader reader = new QRCodeReader();
                        Result result = null;
                        try {

                            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                            result = reader.decode(binaryBitmap, hints);
                        } catch (FormatException e) {
                            e.printStackTrace();
                        } catch (ChecksumException e) {
                            e.printStackTrace();
                        } catch (NotFoundException e) {
                            e.printStackTrace();
                        }
                        reader.reset();

                        if(result == null) {
                            try {
                                result = reader.decode(binaryBitmap);
                            } catch (FormatException e) {
                                e.printStackTrace();
                            } catch (ChecksumException e) {
                                e.printStackTrace();
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        if (result != null) {
                            String text = result.getText();
                            onQRCodeRead(text, null);
                        } else {
                            Toast.makeText(this, R.string.qrcode_no_address_in_qrcode, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private boolean checkAddress(String text, boolean toast) {
        /**
         * 올바른 BitcoinCash 주소인지확인 (Legacy / BitcoinCash / CashAddr...)
         * 어떻게??!?!?!
         *
         * 1. 6p.. 로 시작하는 BIP38 지갑 개인키인지 확인
         * 2. BitcoinCash 주소 : bitcoincash: 로 시작
         * 3. Legacy 주소 : 1로 시작...35~42자???
         * 4. Bitpay 주소???
         */
        if(text.startsWith("6P")) {
            try {
                NetworkParameters networkParameters = MainNetParams.get();
                // Step 1 , BIP38 지갑인지 확인한다. 예외가 발생하지 않으면 BIP38 지갑으로 간주 가능
                BIP38PrivateKey.fromBase58(networkParameters, text);
                // Step 2. 암호를 입력받는다.
                launchBIP38Popup(text);
                return true;

            } catch (AddressFormatException e) {
                e.printStackTrace();
                if(toast)
                    Toast.makeText(this, R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if((text.startsWith("5") && text.length() == 51) ||
                ((text.startsWith("L") || (text.startsWith("K"))) && text.length() == 52)) {    // WIF 형식의 PrivateKey일수 있다.
            try {
                DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(MainNetParams.get(), text);
                ECKey ecKey = dumpedPrivateKey.getKey();
                if(text.equals(ecKey.getPrivateKeyAsWiF(MainNetParams.get()))) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_ADDRESS, text);
                    setResult(RESULT_WIF, intent);
                    finish();
                    return true;
                } else {
                    if(toast)
                        Toast.makeText(this, R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                    return false;
                }

            } catch(AddressFormatException e) {
                e.printStackTrace();
                if(toast)
                    Toast.makeText(this, R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            RCashAddress rCashAddress = Utils.parseAddress(text);
            if (rCashAddress != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ADDRESS, rCashAddress.cashAddress.toBase58());
                intent.putExtra(EXTRA_AMOUNT, rCashAddress.amount);
                intent.putExtra(EXTRA_MESSAGE, rCashAddress.message);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            } else {
                if(toast)
                    Toast.makeText(this, R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        if(text != null) {
            synchronized (TAG) {
                if (!alreadyFound) {
                    qrCodeReaderView.stopCamera();
                    alreadyFound = true;

                    if(checkAddress(text, true)) {

                    } else {
                        qrCodeReaderView.startCamera();
                        alreadyFound = false;
                    }
                }
            }
        }
    }

    private PopupWindow popupWindowBIP38 = null;

    private String bip38PriKey = null;
    private void launchBIP38Popup(String text) {
        this.bip38PriKey = text;
        if(popupWindowBIP38 == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_bip38, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_NOTO_LIGHT);
            popupWindowBIP38 = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindowBIP38.setElevation(16.f);
            popupWindowBIP38.setOutsideTouchable(false);
            popupWindowBIP38.setFocusable(true);

            /*
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.years_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner)popupView.findViewById(R.id.spYears)).setAdapter(adapter);

            adapter = ArrayAdapter.createFromResource(this,
                    R.array.months_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner)popupView.findViewById(R.id.spMonths)).setAdapter(adapter);*/

            /*
            popupView.findViewById(R.id.tvDate).setOnClickListener((v)->{
                DateTime dateTime = new DateTime();
                new DatePickerDialog(QRCodeActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        long selectedTime = getDate(year, month, dayOfMonth);
                        String date = DateUtils.formatDateTime(QRCodeActivity.this, selectedTime, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                        selectedTime /= 1000;
                        ((TextView)popupView.findViewById(R.id.tvDate)).setText(date);
                        popupView.findViewById(R.id.tvDate).setTag(selectedTime);

                        //((TextView) findViewById(R.id.tvSelect)).setText(date);
//        userDob = CommonUtils.getDate(year, monthOfYear, dayOfMonth);
//        String date = DateUtils.formatDateTime(this, userDob, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
//        ((TextView) findViewById(R.id.tvSelect)).setText(date);
//        int age = CommonUtils.getAge(userDob * 1000);
//        if(age < 18 || age > 70) {
//            CustomDialog.launchDialog(
//                    this,
//                    CustomDialog.DIALOG_TYPE_SINGLEBTN,
//                    getString(R.string.dialog_we_apologize_ttl),
//                    getString(R.string.complete_your_profile_error_desc),
//                    "",
//                    getString(R.string.common_ok),
//                    1);
//            findViewById(R.id.rlConfirm).setVisibility(View.GONE);
//        } else {
//            findViewById(R.id.rlConfirm).setVisibility(View.VISIBLE);
//        }
                    }
                }, dateTime.getYear(), dateTime.getMonthOfYear()-1, dateTime.getDayOfMonth()).show();
            });*/

            popupView.findViewById(R.id.btnCancel).setOnClickListener((v)-> {
                qrCodeReaderView.startCamera();
                alreadyFound = false;
                bip38PriKey = null;
                popupWindowBIP38.dismiss();
            });

            popupView.findViewById(R.id.btnOK).setOnClickListener((v)-> {
                String password = ((AppCompatEditText)popupView.findViewById(R.id.etPassword)).getText().toString();

//                long creationTime = (long)popupView.findViewById(R.id.tvDate).getTag();
//                long creationTime = -1;
//                DateTime dateTime  = new DateTime();
//
//                int selectedYear = ((Spinner)popupView.findViewById(R.id.spYears)).getSelectedItemPosition();
//                int selectedMonth = ((Spinner)popupView.findViewById(R.id.spMonths)).getSelectedItemPosition();
//
//                if(selectedYear != 0 || selectedMonth != 0) {
//                    if(selectedYear != 0)
//                        dateTime = dateTime.minusYears(selectedYear);
//                    if(selectedMonth != 0)
//                        dateTime = dateTime.minusMonths(selectedMonth + 1);
//
//                    creationTime = dateTime.getMillis() / 1000;
//                } else {
//                    creationTime = -1;
//                }
                popupWindowBIP38.dismiss();

                Intent intent = new Intent();
                intent.putExtra(EXTRA_ADDRESS, bip38PriKey);
                intent.putExtra(EXTRA_AMOUNT, password);
//                intent.putExtra(EXTRA_CREATION_TIME, creationTime);
                setResult(RESULT_BIP38, intent);
                finish();
            });
        }

        if(popupWindowBIP38.isShowing()) {
            popupWindowBIP38.dismiss();
        } else {

            View popupView = popupWindowBIP38.getContentView();
            //popupView.findViewById(R.id.tvDate).setTag(-1);
            //((TextView)popupView.findViewById(R.id.tvDate)).setText("");
//            ((Spinner)popupView.findViewById(R.id.spYears)).setSelection(0);
//            ((Spinner)popupView.findViewById(R.id.spMonths)).setSelection(0);
            ((AppCompatEditText)popupView.findViewById(R.id.etPassword)).setText("");
            popupWindowBIP38.showAtLocation(findViewById(R.id.qrdecoderview), Gravity.CENTER, 0, 0);
        }
    }

    private boolean resumed = false;

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        if(!alreadyFound) {
            ((Handler)handler).postDelayed(()-> {
                if(resumed) {
                    qrCodeReaderView.startCamera();
                }
            }, 1000);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
        qrCodeReaderView.stopCamera();
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    private long getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
