package jrkim.rcash.utils;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.github.sumimakito.awesomeqr.AwesomeQRRender;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.background.GifBackground;
import com.github.sumimakito.awesomeqr.option.background.StillBackground;
import com.github.sumimakito.awesomeqr.option.color.Color;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.CashAddress;
import org.bitcoinj.core.CashAddressFactory;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import jrkim.rcash.BuildConfig;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.RCashAddress;
import jrkim.rcash.ui.activities.BaseActivity;

import static jrkim.rcash.consts.RCashConsts.BROADCAST_WHERECASH_COMPLETED;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_WHERECASH_FAILED;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_LONG_LAST_WHERECASH_DOWNLOAD;

public class Utils {
    private static final String TAG = "RCash.utils";
    /**
     * 네트워크 연결 여부를 확인
     * @param context
     * @return true : 연결/연결중,   false:연결안됨
     */
    public static int checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null) {
            return RCashConsts.NETWORK_NOTCONNECTED;
        } else {
            if(networkInfo.isConnectedOrConnecting()) {
                if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return RCashConsts.NETWORK_MOBILE;
                } else {
                    return RCashConsts.NETWORK_WIFI;
                }
            } else {
                return RCashConsts.NETWORK_NOTCONNECTED;
            }
        }
    }

    /**
     * dp 단위를 pixel 단위로 환산
     * @param context   applicationContext
     * @param dp        dp
     * @return pixel
     */
    public static int dpToPixel(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setFont(Activity activity, int id, String assetFontName) {
        View view = activity.findViewById(id);
        if(view != null && view instanceof TextView) {
            ((TextView)view).setTypeface(Typeface.createFromAsset(view.getContext().getAssets(), assetFontName));
        }
    }

    /**
     * 전체 layout에 동일한 Font 적용
     * @param view              parent
     * @param assetFontName     font path in assets
     */
    public static void setGlobalFont(View view, String assetFontName) {
        setGlobalFont(view, Typeface.createFromAsset(view.getContext().getAssets(), assetFontName));
    }

    /**
     * 전체 layout에 동일한 Typeface 적용
     * @param view          view
     * @param typeface      typeface
     */
    public static void setGlobalFont(View view, Typeface typeface) {
        if(view != null) {
            if(view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup)view;
                int vgCount = viewGroup.getChildCount();
                for(int i = 0; i < vgCount; i++) {
                    try {
                        View v = viewGroup.getChildAt(i);
                        if(v != null) {
                            if(v instanceof TextView) {
                                ((TextView)v).setTypeface(typeface);
                            } else {
                                setGlobalFont(v, typeface);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 권한 체크 결과 권한이 모두 수락 되었는지 여부를 확인한다.
     * @param grantResult      results
     * @return                  true / false
     */
    public static boolean checkPermissions(int[] grantResult) {
        if(grantResult != null && grantResult.length > 0) {
            for(int i = 0; i < grantResult.length; i++) {
                if(grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

//    public static String getMyPhoneNumber(Activity activity, Context context){
//        TelephonyManager mTelephonyMgr;
//        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            return "";
//        }
//        mTelephonyMgr = (TelephonyManager)
//                context.getSystemService(Context.TELEPHONY_SERVICE);
//        return mTelephonyMgr.getLine1Number();
//    }

    /**
     * URL Encode
     * @param input         input string
     * @return              output string
     */
    public static String urlEncode(String input) {
        String output;
        try {
            output = URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            output = input;
        }
        return output;
    }

    /**
     * 단말의 로컬 IP 주소를 획득한다.
     * @return          IP address
     */
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface nif = en.nextElement();
                for(Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddr = inetAddress.getHostAddress();
                        return ipAddr;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getLastVersionFromPlayStore(final Context context) {
        new Thread() {
            @Override
            public void run() {
                URL url;
                HttpsURLConnection httpsURLConnection;
                try {
                    String strURL = "https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&hl=en";
                    url = new URL(strURL);

                    httpsURLConnection = (HttpsURLConnection)url.openConnection();
                    httpsURLConnection.setConnectTimeout(5000);
                    httpsURLConnection.setRequestProperty("User-Agent", "anything");
                    httpsURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    httpsURLConnection.setRequestMethod("GET");
                    httpsURLConnection.connect();

                    String retValue = null;
                    int response = httpsURLConnection.getResponseCode();

                    if(response == HttpsURLConnection.HTTP_OK) {
                        InputStream is = httpsURLConnection.getInputStream();
                        if(is != null) {
                            if("gzip".equals(httpsURLConnection.getContentEncoding())) {
                                is = new GZIPInputStream(is);
                            }

                            BufferedReader br = new BufferedReader(new InputStreamReader(is));
                            String line;
                            String found = "itemprop=\"softwareVersion\">";
                            while((line = br.readLine()) != null) {
                                int idx = line.indexOf(found);
                                if(idx > 0) {
                                    line = line.substring(idx + found.length());
                                    idx = line.indexOf("</div>");
                                    if(idx > 0) {
                                        line = line.substring(0, idx).trim();
                                        retValue = line;
                                        break;
                                    }
                                }
                            }
                            br.close();
                        }
                    }
                    httpsURLConnection.disconnect();
                    BaseActivity.broadcastMessage(RCashConsts.BROADCAST_PLAYSTORE_VERSION, 0, 0, retValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    BaseActivity.broadcastMessage(RCashConsts.BROADCAST_PLAYSTORE_VERSION, 0, 0, null);
                }
            }
        }.start();
    }

    /**
     * 공유 날리기
     * @param context
     * @param address
     * @param title
     */
    public static void launchShare(Context context, String address, String title) {
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_TEXT, address);

        Intent chooserIntent = Intent.createChooser(shareIntent, title);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chooserIntent);
    }

    public static void launchShareImage(Context context, Uri imageUri, String title) {
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        Intent chooserIntent = Intent.createChooser(shareIntent, title);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chooserIntent);
    }

    public static boolean checkBiometric(Context context) {
        if(checkSecure(context)) {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
                FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
                if(fingerprintManager != null) {
                    if (fingerprintManager.isHardwareDetected()) {
                        if(fingerprintManager.hasEnrolledFingerprints()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if(keyguardManager != null) {
            return keyguardManager.isDeviceSecure();
        }
        return false;
    }

    /**
     * 사용자가 저장한 CustomQRCode 배경 저장
     * @param context
     * @return
     */
    public static String getUserCustomQRBGPath(Context context) {
        return context.getFilesDir().getAbsoluteFile() + File.separator + "userqrbg.png";
    }

    /**
     * gif 파일을 복사하지 않고 경로만 돌려줌
     * @param context
     * @return
     */
    public static String getUserCustomGIFQRBGPath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "usergifqrbg.gif";
    }

    /**
     * gi f파일을 복사하고 경로를 돌려줌
     * @param context
     * @param originPath
     * @return
     */
    public static String getUserCustomGIFQRBGPath(Context context, String originPath) {
        String destPath = getUserCustomGIFQRBGPath(context);
        try {
            FileUtils.copyFile(new File(originPath), new File(destPath));
        } catch (IOException e) {
            e.printStackTrace();
            destPath = null;
        }
        return destPath;

    }

    public static String getUserQRCodePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "images" + File.separator + "qrcode.png";
    }

    public static String getUserGIFQRCodePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "images" + File.separator + "qrcode.gif";
    }

    public static String getCertainQRCodePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "images" + File.separator + "certain.png";
    }

    public static File checkQRCodeGenerated(Context context) {
        File fileQRcode = new File(Utils.getUserQRCodePath(context));
        if(fileQRcode.exists()) {
            return fileQRcode;
        } else {
            fileQRcode = new File(getUserGIFQRCodePath(context));
            if(fileQRcode.exists()) {
                return fileQRcode;
            }
        }
        return null;
    }

    public static String getWhereCashPath(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "wherecash";
    }

    public static void copyFileFromAsset(Context context, String output, String assetFile) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetFile);
            File outFile = new File(output);
            out = new FileOutputStream(outFile);

            byte[] buffer = new byte[2048];
            int read;
            while((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File createQRCode(Context context, String address, int dotSize) {
        return createQRCode(context, address, dotSize, false, false);
    }

    public static File createQRCode(Context context, String address, int dotSize, boolean createNew) {

        return createQRCode(context, address, dotSize, createNew, false);
    }

    private static final int QRWIDTH = 480;

    public static File createQRCode(Context context, String address, int dotSize, boolean createNew, boolean certain) {
        File fileQRcode = new File(certain ? Utils.getCertainQRCodePath(context) : Utils.getUserQRCodePath(context));
        if(!createNew && fileQRcode.exists() && fileQRcode.length() > 0) {
            return fileQRcode;
//            //return BitmapFactory.decodeFile(fileQRcode.getAbsolutePath());
        } else {
            RenderOption renderOption = new RenderOption();
            renderOption.content = address;
            renderOption.size = QRWIDTH;
            renderOption.borderWidth = QRWIDTH / 20;
            renderOption.ecl = ErrorCorrectionLevel.H;
            renderOption.patternScale = dotSize / 100f;
            renderOption.roundedPatterns = true;
            renderOption.clearBorder = true;

            Color color = new Color();
            color.auto = false;
            color.light = 0xFFFFFFFF;
            color.dark = context.getResources().getColor(R.color.bitcoinBlack, context.getTheme());
            color.background = 0xFFFFFFFF;
            renderOption.color = color;

//        Logo logo = new Logo();
//        logo.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.qart_logo);
//        logo.clippingRect = new RectF(0.f, 0.f, logo.bitmap.getWidth(), logo.bitmap.getHeight());
//        renderOption.setLogo(logo);
            String bgPath = getUserCustomQRBGPath(context);
            File file = new File(bgPath);
            Bitmap bg ;
            if(file.exists() && file.length() > 0) {
                bg = BitmapFactory.decodeFile(bgPath);
            } else {
                bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.bitcoin_cash_square_crop_medium);
            }

            StillBackground stillBackground = new StillBackground();
            stillBackground.bitmap = bg;
            stillBackground.clippingRect = new Rect(0, 0, stillBackground.bitmap.getWidth(), stillBackground.bitmap.getHeight());
            stillBackground.alpha = 0.7f;
            renderOption.setBackground(stillBackground);

            RenderResult renderResult = AwesomeQRRender.render(renderOption);

            if(renderResult.bitmap != null) {

                fileQRcode.getParentFile().mkdirs();
                try {
                    FileOutputStream fos = new FileOutputStream(fileQRcode);
                    renderResult.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return fileQRcode;
            }
            return null;
        }



//        float fDot = dotSize / 100.f;
//
//        File fileQRcode = new File(certain ? Utils.getCertainQRCodePath(context) : Utils.getUserQRCodePath(context));
//        if(!createNew && fileQRcode.exists() && fileQRcode.length() > 0) {
//            return fileQRcode;
//            //return BitmapFactory.decodeFile(fileQRcode.getAbsolutePath());
//        } else {
//            String path = getUserCustomQRBGPath(context);
//            File file = new File(path);
//            Bitmap bg;
//            if (file.exists() && file.length() > 0) {
//                bg = BitmapFactory.decodeFile(path);
//            } else {
//                bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.bitcoin_cash_square_crop_medium);
//            }
//            Bitmap qrcode = AwesomeQRCode.create(address, totalWidth / 2, totalWidth / 40, fDot, context.getColor(android.R.color.black), context.getColor(android.R.color.white), bg, true);
//
//            fileQRcode.getParentFile().mkdirs();
//            try {
//                FileOutputStream fos = new FileOutputStream(fileQRcode);
//                qrcode.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return fileQRcode;
//        }
    }

    public static void deleteAllQRImages(Context context, boolean withBGImg) {
        File file;

        file = new File(getUserQRCodePath(context));
        if(file.exists()) {
            file.delete();
        }

        file = new File(getUserGIFQRCodePath(context));
        if(file.exists()) {
            file.delete();
        }

        if(withBGImg) {
            // delete prev qr images
            file = new File(getUserCustomQRBGPath(context));
            if(file.exists()) {
                file.delete();
            }

            file = new File(getUserCustomGIFQRBGPath(context));
            if(file.exists()) {
                file.delete();
            }
        }


    }

    public static File createGIFQRCode(Context context, String gifPath, int left, int top, int right, int bottom, String address, int dotSize, boolean createNew) {

        RenderOption renderOption = new RenderOption();
        renderOption.content = address;
        renderOption.size = QRWIDTH;
        renderOption.borderWidth = QRWIDTH / 20;
        renderOption.ecl = ErrorCorrectionLevel.H;
        renderOption.patternScale = dotSize / 100f;

        Log.i(TAG, "dotSize: " + dotSize + ", patternScale:" + renderOption.patternScale);

        renderOption.roundedPatterns = true;
        renderOption.clearBorder = true;        // 배경이 보더에는 안그려짐

        Color color = new Color();
        color.auto = false;
        color.light = 0xFFFFFFFF;
        color.dark = context.getResources().getColor(R.color.bitcoinBlack, context.getTheme());
        color.background = 0xFFFFFFFF;
        renderOption.color = color;

//        Logo logo = new Logo();
//        logo.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qart_logo);
//        logo.clippingRect = new RectF(0.f, 0.f, logo.bitmap.getWidth(), logo.bitmap.getHeight());
//        renderOption.setLogo(logo);

        GifBackground gifBackground = new GifBackground();
        gifBackground.oFile = new File(Utils.getUserGIFQRCodePath(context));
        gifBackground.iFile = new File(gifPath);
        gifBackground.clippingRect = new Rect(left, top, right, bottom);
        gifBackground.alpha = 0.7f;
        renderOption.setBackground(gifBackground);

        RenderResult renderResult = AwesomeQRRender.render(renderOption);

        if(renderResult.type == RenderResult.OutputType.GIF) {
            Log.i(TAG, "OuTPUTTYPE GIF");
            if(gifBackground.oFile.exists()) {
                Log.i(TAG, "FILE EXIST");
                return gifBackground.oFile;
            }
        } else {
            Log.i(TAG, "SOMETHING WRONG!");
        }
        return null;
    }

    public static String readTxtFromAssets(Context context, String path) {
        String txt = "";
        try {
            InputStream is = context.getAssets().open(path);
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(is));
            String line;
            do {
                line = bufferedReader.readLine();
                if(line != null)
                    txt += line + "\n";
            } while(line != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txt;
    }

    public static String getFormattedTime(long time, Context context, boolean withSeconds) {
        String ret = "";
        if(time > 0) {
            Period period = new Period(new DateTime(time), new DateTime());

            int years = period.getYears();
            int months = period.getMonths();
            int weeks = period.getWeeks();
            int days = period.getDays();
            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            if(years > 0) {
                if(years == 1) {
                    ret = context.getString(R.string.time_year_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_years_ago), years);
                }
            } else if (months > 0) {
                if(months == 1) {
                    ret = context.getString(R.string.time_month_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_months_ago), months);
                }
            } else if(weeks > 0) {
                if(weeks == 1) {
                    ret = context.getString(R.string.time_week_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_weeks_ago), weeks);
                }
            } else if(days > 0) {
                if(days == 1) {
                    ret = context.getString(R.string.time_day_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_days_ago), days);
                }
            } else if(hours > 0) {
                if(hours == 1) {
                    ret = context.getString(R.string.time_hour_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_hours_ago), hours);
                }
            } else if(minutes > 0) {
                if(minutes == 1) {
                    ret = context.getString(R.string.time_min_ago);
                } else {
                    ret = String.format(context.getString(R.string.time_mins_ago), minutes);
                }
            } else if(seconds > 0) {
                if(withSeconds) {
                    if(seconds == 1) {
                        ret = context.getString(R.string.time_sec_ago);
                    } else {
                        ret = String.format(context.getString(R.string.time_secs_ago), seconds);
                    }
                } else {
                    ret = context.getString(R.string.time_now);
                }
            } else {
                ret = context.getString(R.string.time_now);
            }
        } else {
            ret = context.getString(R.string.time_now);
        }
        return ret;
    }

    public static int DOWNLOAD_STATE_COPLETED = 0;
    public static int DOWNLOAD_STATE_DOWNLOADING = 1;
    public static int downlaodKmlState = DOWNLOAD_STATE_COPLETED;

    //http://www.google.com/maps/d/kml?forcekml=1&mid=1FL9Xo2z51LWlW9Yj8S52oeJb1N13wRQw]
    public static void downloadFile(final Context context, final String strUrl, final String fileName) {
        new Thread(()-> {
            downlaodKmlState = DOWNLOAD_STATE_DOWNLOADING;
            InputStream is = null;
            FileOutputStream fos = null;
            URL url = null;
            HttpURLConnection urlConnection = null;
            boolean success = false;
            try {
                url = new URL(strUrl);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("User-Agent", "anything");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || resCode == HttpURLConnection.HTTP_MOVED_PERM
                        || resCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    // get redirect url from "location" header field
                    String newUrl = urlConnection.getHeaderField("Location");

                    // get the cookie if need, for login
                    String cookies = urlConnection.getHeaderField("Set-Cookie");

                    // open the new connnection again
                    urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                    urlConnection.setRequestProperty("Cookie", cookies);
                    urlConnection.addRequestProperty("User-Agent", "anything");
                }

                if(resCode == HttpURLConnection.HTTP_OK ||
                        resCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || resCode == HttpURLConnection.HTTP_MOVED_PERM
                        || resCode == HttpURLConnection.HTTP_SEE_OTHER) {

                    is = urlConnection.getInputStream();
                    if("gzip".equals(urlConnection.getContentEncoding())) {
                        is = new GZIPInputStream(is);
                    }

                    fos = new FileOutputStream(fileName);
                    int byteRead = -1;
                    byte [] buffer = new byte[2048];
                    while((byteRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteRead);
                    }

                    new SharedPreferenceMgr(context).put(SHAREDPREF_LONG_LAST_WHERECASH_DOWNLOAD, System.currentTimeMillis());
                    success = true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if(is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if(fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            downlaodKmlState = DOWNLOAD_STATE_COPLETED;
            if(success) {
                BaseActivity.broadcastMessage(BROADCAST_WHERECASH_COMPLETED);
            } else {
                BaseActivity.broadcastMessage(BROADCAST_WHERECASH_FAILED);
            }
        }).start();
    }

    public static RCashAddress parseAddress(String address) {
        RCashAddress rCashAddress = new RCashAddress();
        NetworkParameters params;
        if(BuildConfig.MAINNET) {
            params = MainNetParams.get();
        } else {
            params = TestNet3Params.get();
        }

        try {

            int idx;
            while((idx = address.lastIndexOf("?")) >= 0) {
                String param = address.substring(idx);
                address = address.substring(0, idx);
                if(param.startsWith("?amount=")) {
                    param = param.substring(8);
                    rCashAddress.amount = (long)(Double.parseDouble(param)  * 100000000L);
                } else if(param.startsWith("?message=")) {
                    param = param.substring(9);
                    rCashAddress.message = param;
                }
            }

            if (address.startsWith("bitcoincash:")) {
                if (address.charAt(12) == '1') {
                    String base58Addr = address.substring(12);
                    rCashAddress.cashAddress = CashAddressFactory.create().getFromBase58(params, base58Addr);
                } else {
                    rCashAddress.cashAddress = CashAddressFactory.create().getFromFormattedAddress(params, address);
                }

            } else if (address.startsWith("1") && address.length() >= 26 && address.length() <= 35) {
                rCashAddress.cashAddress = CashAddressFactory.create().getFromBase58(params, address);
            } else if (address.startsWith("q")) {
                rCashAddress.cashAddress = CashAddressFactory.create().getFromFormattedAddress(params, address);
            } else {
                rCashAddress = null;
            }
        } catch (Exception e) {
            rCashAddress = null;
        }

        return rCashAddress;
    }

    /**
     * BitcoinCash 주소로 변환
     */
    public static String getReceiveCashAddress(Wallet wallet) {
        CashAddress cashAddress = null;
        try {
            cashAddress = CashAddressFactory.create().getFromBase58(wallet.getNetworkParameters(), wallet.currentReceiveAddress().toBase58());
        } catch (AddressFormatException e) {
            e.printStackTrace();
            cashAddress = null;
        }

        if(cashAddress != null) {
            return cashAddress.toString();
        } else {
            return wallet.currentReceiveAddress().toBase58();
        }
    }

    public static String getReceiveCashAddress(String base85Address) {
        CashAddress cashAddress = null;
        try {
            NetworkParameters params;
            if(BuildConfig.MAINNET)
                params = new MainNetParams();
            else
                params = new TestNet3Params();

            cashAddress = CashAddressFactory.create().getFromBase58(params, base85Address);
        } catch (AddressFormatException e) {
            e.printStackTrace();
            cashAddress = null;
        }

        if(cashAddress != null) {
            return cashAddress.toString();
        } else {
            return base85Address;
        }
    }

    public static String getReceiveLegacyAddress(String cashAddr) {
        CashAddress cashAddress = null;
        try {
            NetworkParameters params;
            if(BuildConfig.MAINNET)
                params = new MainNetParams();
            else
                params = new TestNet3Params();

            cashAddress = CashAddressFactory.create().getFromFormattedAddress(params, cashAddr);
        } catch (AddressFormatException e) {
            e.printStackTrace();
            cashAddress = null;
        }

        if(cashAddress != null) {
            return cashAddress.toBase58();
        } else {
            return cashAddr;
        }
    }

    public static void sendSuggestion(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"sinrichi@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"[QaRT Wallet] ");
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"));

    }

    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(view == null) {
            view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
