package jrkim.rcash.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.WhereCash;
import jrkim.rcash.data.WhereCashFolder;
import jrkim.rcash.googlemap.MapStateListener;
import jrkim.rcash.googlemap.TouchableMapFragment;
import jrkim.rcash.kml.RCashKmlParser;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.BROADCAST_WHERECASH_COMPLETED;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_WHERECASH_FAILED;
import static jrkim.rcash.utils.Utils.DOWNLOAD_STATE_DOWNLOADING;

public class WhereCashActivity extends BaseActivity implements LocationListener {

    private static final String TAG = "RCash_WhereCash";

    private static final int PERMISSION_RQCODE_LOCATION = 10;

    private LocationManager locationManager;
    private Location curLocation;
    private TouchableMapFragment mapFragment;
    private GoogleMap googleMap = null;
    private RCashKmlParser kmlParser = null;

    private static final int MESSAGE_START_MAP = 1000;
    private static final int MESSAGE_START_MAP_NOLOCATION = 1001;
    private static final int MESSAGE_UPDATE_ADDRESS = 1002;

    @BindView(R.id.fabAdd) FloatingActionButton fabAdd;

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case BROADCAST_WHERECASH_COMPLETED:
            case BROADCAST_WHERECASH_FAILED:
                findViewById(R.id.tvDownloadingNow).setVisibility(View.GONE);
                initialize();
                break;

            case MESSAGE_START_MAP:
                Log.i(TAG, "handleMessage: START_MAP");
                ((Handler)handler).removeMessages(MESSAGE_START_MAP);
                ((Handler)handler).removeMessages(MESSAGE_START_MAP_NOLOCATION);
                locationManager.removeUpdates(this);
                updateCurrentLocation();
                break;
            case MESSAGE_START_MAP_NOLOCATION:
                Log.i(TAG, "handleMessage: START_MAP_NO_LOCATION");
                ((Handler)handler).removeMessages(MESSAGE_START_MAP);
                ((Handler)handler).removeMessages(MESSAGE_START_MAP_NOLOCATION);
                if(locationManager != null)
                    locationManager.removeUpdates(this);
                curLocation = null;
                updateCurrentLocation();
                //Toast.makeText(this, R.string.wherecash_cant_find_your_location, Toast.LENGTH_SHORT).show();
                //finish();
                break;

            case MESSAGE_UPDATE_ADDRESS:
                //tvAddress.setText((String)msg.obj);
                break;
        }
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wherecash);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        //fabShare.setPadding(0, 0, 0, softKeyHeight);

        fabAdd.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabAdd.getViewTreeObserver().removeOnPreDrawListener(this);

                int fabShareHeight = fabAdd.getHeight();
                int marginHeight = Utils.dpToPixel(getApplicationContext(), 24.f);
                int fabTotalMove = fabShareHeight + marginHeight + softKeyHeight;
                fabAdd.setTranslationY(fabTotalMove);

                return false;
            }
        });

        fabAdd.setOnClickListener((v) -> {
            Log.i(TAG, "Floating Action Button !!1");

//            String url = "https://wherecash.page.link/submit";
            String url = "https://docs.google.com/forms/d/e/1FAIpQLSdNLKnBmUbg-YGCFMzdXhzZApflUasKYJUrMOJMo1XGwNaSSA/viewform";
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.wherecash_ttl));
            intent.putExtra(Intent.EXTRA_TEXT, url);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, WebViewActivity.TYPE_WHERECASH);
            startActivity(intent);
        });

        if(Utils.downlaodKmlState == DOWNLOAD_STATE_DOWNLOADING) {
            Log.i(TAG, "DOWNLOAD_STATE_DOWNLOADING...");
            findViewById(R.id.tvDownloadingNow).setVisibility(View.VISIBLE);
        } else {
            initialize();
        }
    }

    private void checkGPSProvider() {
        Log.i(TAG, "checkGPSProvider...");
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, R.string.wherecash_plz_turnon_gps, Toast.LENGTH_SHORT).show();
        }
        startLocationCheck();
    }

    @SuppressLint("MissingPermission")
    private void startLocationCheck() {
        Log.i(TAG, "startLocationCheck...");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 10, this);

        curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);



        if(curLocation == null) {
            curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        Log.i(TAG, "lastKnownLocation...");


        if(curLocation != null) {
            sendEmptyMessageDelayed(handler, MESSAGE_START_MAP, 3000);
        } else {
            sendEmptyMessageDelayed(handler, MESSAGE_START_MAP_NOLOCATION, 5000);
        }
    }

    private void initialize() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGPSProvider();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_RQCODE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResult) {
        switch (requestCode) {
            case PERMISSION_RQCODE_LOCATION:
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSProvider();
                } else {
                    sendEmptyMessageDelayed(handler, MESSAGE_START_MAP_NOLOCATION, 500);
//                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(this, R.string.wherecash_permission_denied2, Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(this, R.string.wherecash_permission_denied, Toast.LENGTH_SHORT).show();
//                    }
//                    finish();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        curLocation = location;
        if(curLocation != null) {
            ((Handler)handler).removeMessages(MESSAGE_START_MAP);
            ((Handler)handler).removeMessages(MESSAGE_START_MAP_NOLOCATION);
            locationManager.removeUpdates(this);
            updateCurrentLocation();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void showUI(int delay) {
        fabAdd.animate().translationY(-1 * softKeyHeight).setInterpolator(new DecelerateInterpolator()).setDuration(500).start();
    }

    private void updateCurrentLocation() {
        Log.i(TAG, "updateCurrentLocation");

        //double latitude = curLocation.getLatitude();
        //double longitude = curLocation.getLongitude();
//        String address = getCompleteAddressString(latitude, longitude);
//        sendMessage(handler, MESSAGE_UPDATE_ADDRESS, address);

        showUI(800);
        mapFragment = (TouchableMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment == null) {
            findViewById(R.id.rlProgress).setVisibility(View.GONE);
            finish();
            return;
        }

        //mapFragment.

        Log.i(TAG, "getMapAsync!");
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                Log.i(TAG, "onMapReady!");
                findViewById(R.id.rlProgress).setVisibility(View.GONE);
                WhereCashActivity.this.googleMap = googleMap;
                googleMap.setPadding(0, softKeyHeight, 0, softKeyHeight);
                googleMap.clear();

                try {
                    // step 1 로컬에 파일을 복사했는지 확인 / 없으면 복사
                    String whereCashPath = Utils.getWhereCashPath(getApplicationContext());
                    Log.i(TAG, "wherecashPath:" + whereCashPath);
                    File fileWhereCash = new File(whereCashPath);
                    Log.i(TAG, "check file");
                    if(fileWhereCash.exists()) {
                        Log.i(TAG, "check EXIST!");
                        // step 2 로컬에 있는 파일로 부터 KmlLayer 생성
                        InputStream is = new FileInputStream(fileWhereCash);

                        // step 3 KML 파일 파싱
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser parser = factory.newPullParser();
                        parser.setInput(is, (String)null);


                        kmlParser = new RCashKmlParser(parser);
                        kmlParser.parseKml();
                        is.close();
                    } else {
                        Log.i(TAG, "check NOT exist T_T");
                    }

                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BitmapDescriptor bd;
                bd = BitmapDescriptorFactory.fromResource(R.drawable.wherecash_small);
                for(WhereCashFolder cashFolder : kmlParser.arrFolders) {
                    for(WhereCash whereCash : cashFolder.list) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(whereCash.latLng)
                                .anchor(.1f, .5f)
                                .icon(bd)
                                .title(whereCash.name)
                                .snippet(whereCash.description));
                    }
                }

                googleMap.setOnMarkerClickListener((marker) -> {
                    String title = marker.getTitle();
                    String description = marker.getSnippet();

                    int pos = description.lastIndexOf("<br>");
                    String imgPath = description.substring(0, pos);
                    imgPath = imgPath.substring(10);
                    imgPath = imgPath.substring(0, imgPath.indexOf("\""));
                    description = description.substring(pos + 4);

                    launchWhereCashPopup(title, imgPath, description);
                    return true;        // dont' show info window...
                });

                if(curLocation != null) {
                    LatLng latlng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12));
                }

                new MapStateListener(googleMap, mapFragment, WhereCashActivity.this) {

                    @Override
                    public void onMapTouched() {
                    }

                    @Override
                    public void onMapReleased() {
                    }

                    @Override
                    public void onMapUnsettled() {
                    }

                    @Override
                    public void onMapSettled() {
                    }
                };
            }
        });
    }

    private PopupWindow popupWindowWhereCash = null;

    private void launchWhereCashPopup(String title, String imgPath, String description) {
        if(popupWindowWhereCash == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_wherecash, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_NOTO_LIGHT);
            popupWindowWhereCash = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindowWhereCash.setElevation(16.f);
            popupWindowWhereCash.setOutsideTouchable(true);
            popupWindowWhereCash.setFocusable(true);

            popupView.findViewById(R.id.ivClose).setOnClickListener((v) -> {
                popupWindowWhereCash.dismiss();
            });
        }

        if(popupWindowWhereCash.isShowing()) {
            popupWindowWhereCash.dismiss();
        } else {
            View popupView = popupWindowWhereCash.getContentView();
            ((TextView)popupView.findViewById(R.id.tvTitle)).setText(title);
            ((TextView)popupView.findViewById(R.id.tvDescription)).setText(description);

            Glide.with(this)
                    .load(imgPath)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .crossFade()
                    .into((ImageView)popupView.findViewById(R.id.ivImage));

            popupWindowWhereCash.showAtLocation(findViewById(R.id.map), Gravity.CENTER, 0, 0);
        }
    }

    private String getCompleteAddressString(double latitude, double longitude) {
        String strAddr = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses != null) {
                if(addresses.size() > 0) {
                    for(int i = 0; addresses.get(0).getMaxAddressLineIndex() >= i; i++) {
                        strAddr += addresses.get(0).getAddressLine(i) + " ";
                    }
                    strAddr = strAddr.trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            strAddr = "";
        }
        return strAddr;
    }
}
