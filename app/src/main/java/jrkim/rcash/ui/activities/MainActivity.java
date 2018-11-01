package jrkim.rcash.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ConfigurationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.CashAddressFactory;
import org.bitcoinj.core.Coin;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.bitcoinj.BitcoinService;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.DogerUTXO;
import jrkim.rcash.data.RCashAddress;
import jrkim.rcash.data.Restore;
import jrkim.rcash.network.NetworkMgr;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static jrkim.rcash.bitcoinj.BitcoinService.RESULT_NOKEY;
import static jrkim.rcash.bitcoinj.BitcoinService.RESULT_PASSWORD_ERROR;
import static jrkim.rcash.bitcoinj.BitcoinService.RESULT_SUCCESS;
import static jrkim.rcash.bitcoinj.BitcoinService.RESULT_SWEEP_SUCCESS;
import static jrkim.rcash.bitcoinj.BitcoinService.RESULT_UNKNOWN_ERROR;
import static jrkim.rcash.consts.RCashConsts.ADDRESS_FORMAT_CASHADDR;
import static jrkim.rcash.consts.RCashConsts.BCH_FORMAT_BCH;
import static jrkim.rcash.consts.RCashConsts.BCH_FORMAT_BITS;
import static jrkim.rcash.consts.RCashConsts.BCH_FORMAT_SATOSHIS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BIP38WALLET;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BIP38_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS_BIP38;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_CONNECT_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_FRESH_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_RECEIVED;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_SEND_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_WALLET;
import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_AMOUNT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_MESSAGE;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_CHY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_EU;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_JPY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_KRW;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_USD;
import static jrkim.rcash.consts.RCashConsts.NETWORK_MOBILE;
import static jrkim.rcash.consts.RCashConsts.NETWORK_NOTCONNECTED;
import static jrkim.rcash.consts.RCashConsts.NETWORK_WIFI;
import static jrkim.rcash.consts.RCashConsts.PERMISSION_RQCODE_CAMERA;
import static jrkim.rcash.consts.RCashConsts.REQCODE_BACKUP;
import static jrkim.rcash.consts.RCashConsts.REQCODE_CHECK;
import static jrkim.rcash.consts.RCashConsts.REQCODE_EDITWALLET;
import static jrkim.rcash.consts.RCashConsts.REQCODE_EULA;
import static jrkim.rcash.consts.RCashConsts.REQCODE_MYADDRESS;
import static jrkim.rcash.consts.RCashConsts.REQCODE_PINCODE_REMOVE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_PINCODE_SET;
import static jrkim.rcash.consts.RCashConsts.REQCODE_QRCODE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_RESTORE;
import static jrkim.rcash.consts.RCashConsts.REQCODE_SEND;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_BACKUP;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_EULA_ACCEPTED;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_SECUREMODE;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_SYNC_COMPLETED;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_BOOL_WALLET_CREATED;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_ADDRESS_FORMAT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_BCH_FORMAT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_LOCAL_CURRENCY;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_WALLET_COLOR;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_LONG_LAST_WHERECASH_DOWNLOAD;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_STRING_LAST_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_STRING_WALLET_ALIAS;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_BLUEGREY;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_BROWN;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_CYON;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_GREEN;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_INDIGO;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_ORANGE;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_PINK;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_PURPLE;
import static jrkim.rcash.consts.RCashConsts.WALLET_COLOR_RED;
import static jrkim.rcash.network.NetworkMgr.METHOD_GET;
import static jrkim.rcash.ui.activities.QRCodeActivity.RESULT_BIP38;
import static jrkim.rcash.ui.activities.QRCodeActivity.RESULT_WIF;
import static jrkim.rcash.ui.activities.RestoreActivity.EXTRA_CREATION_TIME;
import static jrkim.rcash.ui.activities.RestoreActivity.EXTRA_SEED;

public class MainActivity extends BaseActivity {

    private final static String TAG = "RCash_Main";

    @BindView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.rlSticky) RelativeLayout rlSticky;
    @BindView(R.id.rlStickyProgress) RelativeLayout rlStickyProgress;
    @BindView(R.id.tvSticky) TextView tvSticky;
    @BindView(R.id.rlWallet) RelativeLayout rlWallet;
    @BindView(R.id.rlBip38Wallet) RelativeLayout rlBip38Wallet;
    @BindView(R.id.rlEmptyWallet) RelativeLayout rlEmptyWallet;
    @BindView(R.id.swiperefreshTimeline) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tvBchFormat) TextView tvBchFormat;
    @BindView(R.id.tvBCH) TextView tvBCH;
    @BindView(R.id.tvBip38BCH) TextView tvBip38BCH;
    @BindView(R.id.tvBip38Balance) TextView tvBip38Balance;

    @BindView(R.id.flSideMenu) FrameLayout flSideMenu;
    @BindView(R.id.tvSubBalance) TextView tvSubBalance;
    @BindView(R.id.tvSubBalanceChange) TextView tvSubBalanceChange;
    @BindView(R.id.ivLocalCurrency) ImageView ivLocalCurrency;

    private int stickyState = STICKY_NO_WALLET;
    private final static int STICKY_HIDE = 0x01;            //강제로 숨기기
    private final static int STICKY_NO_INTERNET = 0x02;     //인터넷연결이 없음
    private final static int STICKY_NO_WIFI = 0x04;         //WIFI연결이 없음 (모바일)
    private final static int STICKY_NO_GEOLOCATION = 0x08;  //현재위치를 알수 없음
    private final static int STICKY_NO_GPS = 0x10;          //GPS신호를 집을수 없음
    private final static int STICKY_NO_CONNECTION = 0x20;   //블록체인에 연결되지 않음
    private final static int STICKY_NO_WALLET = 0x40;       //생성된 지갑이 없음
    private final static int STICKY_DOWNLOADING = 0x80;     //블록체인 다운로드중
    private final static int STICKY_NOT_BACKUPPED = 0x100;  // 지갑 백업되지 않음
    private final static int STICKY_DOWNLOADING_BIP38 = 0x200; //블록체인 다운로드 BIP38

    private STICKY_STATE curStickyState = STICKY_STATE.NOTHING;
    enum STICKY_STATE {
        NOTHING,
        HIDE,
        NO_INTERNET,
        NO_GEOLOCATION,
        NO_GPS,
        NO_CONNECTION,
        NO_WALLET,
        NORMAL
    }

    private final static int MESSAGE_CHECK_NETWORK_STATE = 1000;
    private final static int MESSAGE_START_ANIMATION_EMTPY = 1001;
    private final static int MESSAGE_START_ANIMATION_WALLET = 1002;
    private final static int MESSAGE_HIDE_PROGRESS = 1003;
    private final static int MESSAGE_START_SHARE = 1004;
    private final static int MESSAGE_START_RESTORE_WALLET = 1005;
    private final static int MESSAGE_REFRESH_WALLET = 1006;
    private final static int MESSAGE_SHOW_BALANCE_IF_READY = 1007;
    private final static int MESSAGE_SHOW_SUBBALANCE_UNKNOWN = 1008;
    private final static int MESSAGE_GET_HANDCASH_HANDLE = 1009;
    private final static int MESSAGE_SHOW_TOAST = 1010;

    private PopupWindow popupWindow4BCHForamt = null;
    private PopupWindow popupWindow4LocalCurrency = null;
    private PopupWindow popupWindow4Send = null;

    private PopupWindow popupWindow4Addr = null;
    private boolean popup4Addr = false;

    private int curBlockchainProgress = 0;
    private int curBlockchainProgressBip38 = 0;
    private boolean isActiveNow = false;
    private boolean connectedToBlockChain = false;
    private boolean requestingNow = false;

    private String addressToSend = null;
    private String messageToSend = null;
    private long satoshiToSend = 0;

    public static Wallet wallet = null;
    public static ArrayList<DogerUTXO> arrUtxo;

    private long lastUpdatedBCHUSD = 0;
    public static double BCHUSD = -1;
    private double BCHUSDCHANGED = -1;

    private long lastUpdatedLocalCurrency = 0;
    public static double USDKRW = -1;
    public static double USDEUR = -1;
    public static double USDJPY = -1;
    public static double USDCNY = -1;

    private void updateBip38WalletInfo(ArrayList<DogerUTXO> arrUtxo) {

        SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        String balance = "";
        int bchFormat = sharedPreferenceMgr.get(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BCH);

        switch (bchFormat) {
            case BCH_FORMAT_BCH:
                tvBip38BCH.setText(R.string.bch_format_bch);
                break;
            case BCH_FORMAT_BITS:
                tvBip38BCH.setText(R.string.bch_format_bits);
                break;
            case BCH_FORMAT_SATOSHIS:
                tvBip38BCH.setText(R.string.bch_format_satoshis);
                break;
        }

        double totalBalance = 0.0;
        long totalBalanceStatoshis = 0;
        if(arrUtxo != null) {
            for (DogerUTXO utxo : arrUtxo) {
                totalBalance += utxo.amount;
                totalBalanceStatoshis += utxo.satoshis;
            }
        }
        /**
         * TODO 처리 완료 해야 함....
         * 우선 ECKey / Wallet / Transaction 만으로 코인 전송이 가능한지 확인하고..
         */

        if (wallet != null) {
            switch (bchFormat) {
                case BCH_FORMAT_BCH:
                    balance = String.format(Locale.getDefault(), "%.8f", totalBalance);
                    break;
                case BCH_FORMAT_BITS:
                    balance = String.format(Locale.getDefault(), "%.3f", (totalBalanceStatoshis / 1000.f));
                    break;
                case BCH_FORMAT_SATOSHIS:
                    balance = Long.toString(totalBalanceStatoshis);
                    break;
            }
        } else {
            balance = "0";
        }

        tvBip38Balance.setText(balance);
    }

    private void updateWalletInfo(Wallet wallet) {
        if(wallet != null) {
            SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
            String balance = "";
            int bchFormat = sharedPreferenceMgr.get(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BCH);
            long satoshis = wallet.getBalance().value;
            switch (bchFormat) {
                case BCH_FORMAT_BCH:
                    balance = wallet.getBalance().toPlainString();
                    tvBCH.setText(R.string.bch_format_bch);
                    break;
                case BCH_FORMAT_BITS:
                    balance = String.format(Locale.getDefault(), "%.3f", (satoshis / 1000.f));
                    tvBCH.setText(R.string.bch_format_bits);
                    break;
                case BCH_FORMAT_SATOSHIS:
                    balance = Long.toString(satoshis);
                    tvBCH.setText(R.string.bch_format_satoshis);
                    break;
            }
            ((TextView) findViewById(R.id.tvBalance)).setText(balance);


            String lastAddress = sharedPreferenceMgr.get(SHAREDPREF_STRING_LAST_ADDRESS, null);
            if(lastAddress == null) {
                lastAddress = wallet.currentReceiveAddress().toBase58();
                sharedPreferenceMgr.put(SHAREDPREF_STRING_LAST_ADDRESS, lastAddress);
            }

            int addFormat = sharedPreferenceMgr.get(SHAREDPREF_INT_ADDRESS_FORMAT, ADDRESS_FORMAT_CASHADDR);
            if(addFormat == ADDRESS_FORMAT_CASHADDR) {
                ((TextView) findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(lastAddress));
            } else {
                ((TextView) findViewById(R.id.tvAddress)).setText(lastAddress);
            }

        }

        /**
         * blockchain.info 에서 각 가격 획득
         * 이건 BTC만 나오네 -_-
         *
         * https://blockchain.info/ticker
         * https://github.com/bitpay/insight-api    <= bitpay 블록 / 트랜잭션 정보
         * https://bch.btc.com/api-doc              <= btc.com
         *
         * https://api.coinmarketcap.com/v1/ticker/bitcoin-cash/ <= CoinMarketCap
         */

        synchronized (TAG) {
            boolean requested = false;
            if (System.currentTimeMillis() - lastUpdatedBCHUSD >= 1000 * 60 * 10 && !requestingNow) { // 10분에 1회정도만 새로 호출
                requested = true;
                requestingNow = true;

                new NetworkMgr().execute(this, "https://api.coinmarketcap.com/v1/ticker/bitcoin-cash/", METHOD_GET, null, null, new NetworkMgr.NetworkListener() {
                    @Override
                    public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
                        requestingNow = false;
                        if (resCode == HttpsURLConnection.HTTP_OK) {
                            if (retValue != null && retValue.length() > 0) {
                                try {
                                    JSONObject json = (JSONObject) new JSONArray(retValue).get(0);

                                    String price_usd = json.getString("price_usd");
                                    String percent_change_24h = json.getString("percent_change_24h");
                                    BCHUSD = Double.parseDouble(price_usd);
                                    BCHUSDCHANGED = Double.parseDouble(percent_change_24h);

                                    lastUpdatedBCHUSD = System.currentTimeMillis();
                                    ((Handler) handler).sendEmptyMessage(MESSAGE_SHOW_BALANCE_IF_READY);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            ((Handler) handler).sendEmptyMessage(MESSAGE_SHOW_SUBBALANCE_UNKNOWN);
                        }
                    }
                });
            }
//                new NetworkMgr().execute(this, "https://poloniex.com/public?command=returnTicker", NetworkMgr.METHOD_GET, null, null, new NetworkMgr.NetworkListener() {
//                    @Override
//                    public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
//                        requestingNow = false;
//                        if (resCode == HttpsURLConnection.HTTP_OK) {
//                            if (retValue != null && retValue.length() > 0) {
////                                Log.i(TAG, retValue);
//                                try {
//                                    JSONObject json = new JSONObject(retValue);
//                                    JSONObject jsonUSDT_BCH = json.getJSONObject("USDT_BCH");
//
//                                    // exam : "USDT_BCH":{"id":191,"last":"450.12232652","lowestAsk":"449.10519815","highestBid":"448.98537281","percentChange":"0.01029300","baseVolume":"1199311.83277278","quoteVolume":"2620.11549352","isFrozen":"0","high24hr":"476.34412325","low24hr":"442.99999999"}
//                                    String last = jsonUSDT_BCH.getString("last");
////                            String lowestAsk = jsonUSDT_BCH.getString("lowestAsk");
////                            String highestBid = jsonUSDT_BCH.getString("highestBid");
//                                    String percentChange = jsonUSDT_BCH.getString("percentChange");
////                            String baseVolume = jsonUSDT_BCH.getString("baseVolume");
////                            String quoteVolume = jsonUSDT_BCH.getString("quoteVolume");
//                                    String high24hr = jsonUSDT_BCH.getString("high24hr");
//                                    String low24hr = jsonUSDT_BCH.getString("low24hr");
//
//                                    BCHUSD = (Double.parseDouble(last) + Double.parseDouble(high24hr) + Double.parseDouble(low24hr)) / 3;
//                                    BCHUSDCHANGED = Double.parseDouble(percentChange);
//
//                                    lastUpdatedBCHUSD = System.currentTimeMillis();
//
//                                    ((Handler)handler).sendEmptyMessage(MESSAGE_SHOW_BALANCE_IF_READY);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        } else {
//                            Log.i(TAG, " network error : " + resCode + ", " + retValue);
//                            ((Handler)handler).sendEmptyMessage(MESSAGE_SHOW_SUBBALANCE_UNKNOWN);
//                        }
//                    }
//                });
//            }

            if (System.currentTimeMillis() - lastUpdatedLocalCurrency >= 1000 * 60 * 60 * 24) { // 하루 1번 정도만 호출되도록...
                requested = true;
                requestingNow = true;
                // https://shin-jaeheon.github.io/article/2018/02/01/free-exchange-api/
                //
                new NetworkMgr().execute(this, "http://earthquake.kr/exchange", METHOD_GET, null, null, new NetworkMgr.NetworkListener() {
                    @Override
                    public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
                        requestingNow = false;
                        if (resCode == HttpsURLConnection.HTTP_OK) {
                            if (retValue != null && retValue.length() > 0) {
//                                Log.i(TAG, retValue);
                                try {
                                    JSONObject json = new JSONObject(retValue);
                                    JSONArray jsonUSDKRW = json.getJSONArray("USDKRW");
                                    JSONArray jsonUSDEUR = json.getJSONArray("USDEUR");
                                    JSONArray jsonUSDJPY = json.getJSONArray("USDJPY");
                                    JSONArray jsonUSDCNY = json.getJSONArray("USDCNY");

                                    USDKRW = jsonUSDKRW.getDouble(0);
                                    USDEUR = jsonUSDEUR.getDouble(0);
                                    USDJPY = jsonUSDJPY.getDouble(0);
                                    USDCNY = jsonUSDCNY.getDouble(0);

                                    lastUpdatedLocalCurrency = System.currentTimeMillis();

                                    ((Handler)handler).sendEmptyMessage(MESSAGE_SHOW_BALANCE_IF_READY);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            ((Handler)handler).sendEmptyMessage(MESSAGE_SHOW_SUBBALANCE_UNKNOWN);
                        }
                    }
                });
            }

            if (!requested) {
                showBalanceIfReady();
            }
        }
    }

    private void showBalanceIfReady() {
        boolean readyToDisplay = false;
        int localCurrency = new SharedPreferenceMgr(getApplicationContext()).get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD);
        switch(localCurrency) {
            case LOCAL_CURRENCY_KRW:
                if(USDKRW > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_USD:
                readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_EU:
                if(USDEUR > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_JPY:
                if(USDJPY > 0) readyToDisplay = true;
                break;
            case LOCAL_CURRENCY_CHY:
                if(USDCNY > 0) readyToDisplay = true;
                break;
        }

        if(readyToDisplay && wallet != null) {
            ((Handler)handler).post(() -> {
                String subBalanceChange = String.format("(%.2f%%)", BCHUSDCHANGED);
                long satoshis = wallet.getBalance().value;
                double balance = satoshis / 100000000.f;
                double localValue = BCHUSD * balance;

                String subBalance = String.format("$ %.2f", localValue);

                switch(localCurrency) {
                    case LOCAL_CURRENCY_KRW:
                        localValue *= USDKRW;
                        subBalance = String.format("₩ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_EU:
                        localValue *= USDEUR;
                        subBalance = String.format("€ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_JPY:
                        localValue *= USDJPY;
                        subBalance = String.format("¥ %.2f", localValue);
                        break;
                    case LOCAL_CURRENCY_CHY:
                        localValue *= USDCNY;
                        subBalance = String.format("元 %.2f", localValue);
                        break;
                }

                tvSubBalance.setText(subBalance);
                tvSubBalanceChange.setText(subBalanceChange);
            });
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        Intent newIntent = null;
        switch (msg.what) {
            case BROADCAST_BITCOINSERVICE_CONNECT_RESULT:
                SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
                showProgress(false);
                switch (msg.arg1) {
                    case BitcoinService.RESULT_SUCCESS:

                        sharedPreferenceMgr.put(SHAREDPREF_BOOL_WALLET_CREATED, true);

                        connectedToBlockChain = true;

                        wallet = (Wallet)msg.obj;
                        updateWalletInfo(wallet);

                        stickyState &= ~STICKY_NO_CONNECTION;
                        stickyState &= ~STICKY_NO_WALLET;

                        if(sharedPreferenceMgr.get(SHAREDPREF_BOOL_BACKUP, false)) {
                            stickyState &= ~STICKY_NOT_BACKUPPED;
                        } else {
                            stickyState |= STICKY_NOT_BACKUPPED;
                        }

                        if(sharedPreferenceMgr.get(SHAREDPREF_BOOL_SYNC_COMPLETED, false)) {
                            stickyState &= ~STICKY_DOWNLOADING;
                        } else {
                            stickyState |= STICKY_DOWNLOADING;
                            BitcoinService.GetProgress(this);

                            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                                    .setTitle(R.string.main_sync_not_completed_ttl)
                                    .setMessage(R.string.main_sync_not_completed_desc)
                                    .setPositiveButton(R.string.common_ok, null).create();
                            alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                            alertDialog.show();
                        }
                        updateSticky();
                        sendEmptyMessageDelayed(handler, MESSAGE_START_ANIMATION_WALLET, 500);
                        break;

                    case RESULT_NOKEY:
                        sharedPreferenceMgr.put(SHAREDPREF_BOOL_WALLET_CREATED, false);
                        stickyState |= STICKY_NO_CONNECTION;
                        stickyState |= STICKY_NO_WALLET;
                        updateSticky();
                        sendEmptyMessageDelayed(handler, MESSAGE_START_ANIMATION_EMTPY, 500);
                        break;
                }
                break;

            case BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS:
                curBlockchainProgress = msg.arg1;
                if(curBlockchainProgress < 0) curBlockchainProgress = 0;
                if(curBlockchainProgress > 100) curBlockchainProgress = 100;

                if(curBlockchainProgress == 100) {
                    new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_SYNC_COMPLETED, true);
                }

                if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_SYNC_COMPLETED, false) && curBlockchainProgress == 0) {
                    curBlockchainProgress = 100;
                }

                if(msg.arg1 >= 0 && msg.arg1 < 100) {
                    stickyState |= STICKY_DOWNLOADING;
                    new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_SYNC_COMPLETED, false);
                } else {
                    stickyState &= ~STICKY_DOWNLOADING;
                }
                updateSticky();
                break;

            case BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS_BIP38:
                curBlockchainProgressBip38 = msg.arg1;
                if(curBlockchainProgressBip38 < 0) curBlockchainProgressBip38 = 0;
                if(curBlockchainProgressBip38 >= 100) {
                    curBlockchainProgressBip38 = 100;
                    //BitcoinService.SweepBip38(this);
                }

                //btnBip38.setProgress(curBlockchainProgressBip38);
//                if(msg.arg1 >= 1 && msg.arg1 < 100) {
//                    stickyState |= STICKY_DOWNLOADING_BIP38;
//                } else {
//                    stickyState &= ~STICKY_DOWNLOADING_BIP38;
//                }
//                updateSticky();
                break;

            case BROADCAST_BITCOINSERVICE_BIP38WALLET:
                Log.i(TAG, "BROADCAST_BITCOINSERVICE_BIP38WALLET");
                /* 지갑 방식에서 api 방식으로 변경 하며 필요 없어짐. 원래는 Receive Sent 이벤트 처리용
                if(msg.obj != null) {
                    if(msg.arg1 == REQCODE_CHECK) {
                        Log.i(TAG, "CHECK");
                        if(msg.obj != null ) {
                            Wallet bip38Wallet = (Wallet)msg.obj;
                            if(btnBip38.getProgress() == 100 && bip38Wallet.getBalance().getValue() == 0) {
                                updateBip38WalletInfo(null);
                                // 발란스가 없음을 알리고 삭제
                            } else {
                                updateBip38WalletInfo(bip38Wallet);
                            }
                        }

                    } else if(msg.arg1 == REQCODE_SEND) {
                        Log.i(TAG, "SENT");
                        updateBip38WalletInfo(null);
                        rlBip38Wallet.animate().translationY(Utils.dpToPixel(this, 100.f)).setDuration(400).setInterpolator(new AccelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                rlBip38Wallet.setVisibility(GONE);
                            }
                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }
                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        }).start();
                    }
                }*/
                break;

            case BROADCAST_BITCOINSERVICE_WALLET:
                if(msg.obj != null) {
                    wallet = (Wallet)msg.obj;
                    if(msg.arg1 == REQCODE_BACKUP) {
                        List<String> seeds = wallet.getKeyChainSeed().getMnemonicCode();
                        BackupActivity.phrases = "";
                        for(String seed : seeds) {
                            BackupActivity.phrases += seed + " ";
                        }
                        BackupActivity.phrases = BackupActivity.phrases.trim();
                        startActivity(new Intent(this, BackupActivity.class));
                    } else if(msg.arg1 == REQCODE_CHECK) {
                        swipeRefreshLayout.setRefreshing(false);
                        updateWalletInfo(wallet);
                        updateBip38WalletInfo(arrUtxo);
                    } else if(msg.arg1 == REQCODE_SEND) {
                        SendActivity.wallet = wallet;
                        newIntent = new Intent(this, SendActivity.class);
                        newIntent.putExtra(EXTRA_ADDRESS, addressToSend);
                        newIntent.putExtra(EXTRA_MESSAGE, messageToSend);
                        startActivityForResult(newIntent, REQCODE_SEND);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    if(connectedToBlockChain)
                        Toast.makeText(this, R.string.main_no_wallet, Toast.LENGTH_SHORT).show();
                }
                break;

            case BROADCAST_BITCOINSERVICE_RECEIVED:
                if(msg.obj != null) {
                    updateWalletInfo((Wallet)msg.obj);
                }
                break;

            case BROADCAST_BITCOINSERVICE_FRESH_ADDRESS:
                if(msg.obj != null) {
                    new SharedPreferenceMgr(this).put(SHAREDPREF_STRING_LAST_ADDRESS, ((Address)msg.obj).toBase58());
                    updateWalletInfo(wallet);
                }
                break;

            case BROADCAST_BITCOINSERVICE_SEND_RESULT:
                if(msg.arg1 == RESULT_SUCCESS) {
                    if(msg.obj != null) {
                        updateWalletInfo((Wallet)msg.obj);
                    }
                } else {
                    Log.e(TAG, "SEND FAIL");
                }
                showProgress(false);
                break;

            case BROADCAST_BITCOINSERVICE_BIP38_RESULT:
                switch (msg.arg1) {
                    case RESULT_SUCCESS:
                        arrUtxo = (ArrayList<DogerUTXO>)msg.obj;
                        updateBip38WalletInfo(arrUtxo);

                        rlBip38Wallet.setTranslationY(-1 * Utils.dpToPixel(this, 200.f));
                        rlBip38Wallet.setVisibility(VISIBLE);
                        rlBip38Wallet.animate().translationY(0).setDuration(400).setInterpolator(new DecelerateInterpolator()).setListener(null).start();
                        break;
                    case RESULT_PASSWORD_ERROR:
                        Toast.makeText(getApplicationContext(), R.string.main_bip38_error_password_desc, Toast.LENGTH_SHORT).show();
                        onBip38Cancel();
                        break;
                    case RESULT_UNKNOWN_ERROR:
                        Toast.makeText(getApplicationContext(), R.string.main_bip38_unknownerror_desc, Toast.LENGTH_SHORT).show();
                        onBip38Cancel();
                        break;
                    case RESULT_SWEEP_SUCCESS:
                        sendEmptyMessage(handler, MESSAGE_HIDE_PROGRESS);
                        Toast.makeText(getApplicationContext(), R.string.main_bip38_sweep_success, Toast.LENGTH_SHORT).show();
                        arrUtxo = null;
                        onBip38Cancel();
                        break;
                }
                showProgress(false);
                break;

            case MESSAGE_CHECK_NETWORK_STATE:
                switch(Utils.checkNetworkConnection(this)) {
                    case NETWORK_NOTCONNECTED:
                        stickyState |= STICKY_NO_INTERNET;
                        if(isActiveNow)
                            sendEmptyMessage(handler, MESSAGE_CHECK_NETWORK_STATE);
                        break;
                    case NETWORK_MOBILE:
                    case NETWORK_WIFI:
                        stickyState &= ~STICKY_NO_INTERNET;
                        break;
                }
                updateSticky();
                break;

            case MESSAGE_START_ANIMATION_EMTPY:
                readyWithAnimation(false);
                rlEmptyWallet.setVisibility(VISIBLE);
                rlEmptyWallet.animate().translationY(0).setDuration(600).setInterpolator(new DecelerateInterpolator()).start();
                break;

            case MESSAGE_START_ANIMATION_WALLET:
                readyWithAnimation(false);
                rlWallet.setVisibility(VISIBLE);
                rlWallet.animate().translationY(0).setDuration(400).setInterpolator(new DecelerateInterpolator()).start();
                break;

            case MESSAGE_HIDE_PROGRESS:
                showProgress(false);
                break;

            case MESSAGE_START_SHARE:
                startActivityForResult(new Intent(this, MyAddressActivity.class), REQCODE_MYADDRESS);
                break;

            case MESSAGE_START_RESTORE_WALLET:
                BitcoinService.ConnectToBlockChainWithRestoreWallet(this, ((Restore)msg.obj).seed, ((Restore)msg.obj).creationTime);
                break;

            case MESSAGE_REFRESH_WALLET:
                BitcoinService.GetWallet(this, REQCODE_CHECK);
                break;

            case MESSAGE_SHOW_BALANCE_IF_READY:
                showBalanceIfReady();
                break;

            case MESSAGE_SHOW_SUBBALANCE_UNKNOWN:
                tvSubBalance.setText(R.string.main_wallet_subbalance_unknown);
                tvSubBalanceChange.setText("-");
                break;

            case MESSAGE_GET_HANDCASH_HANDLE:
                String handle = addressToSend.replace("$", "");
                String handleApi = "https://api.handcash.io/api/receivingAddress/" + handle;
                findViewById(R.id.rlProgress).setVisibility(VISIBLE);
                new NetworkMgr().execute(getApplicationContext(),
                        handleApi,
                        METHOD_GET,
                        null,
                        null,
                        new NetworkMgr.NetworkListener() {
                            @Override
                            public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
                                boolean success = false;
                                if(resCode == HttpsURLConnection.HTTP_OK && retValue != null && retValue.length() > 0) {
                                    try {
                                        JSONObject json = new JSONObject(retValue);
                                        addressToSend = json.getString("receivingAddress");
                                        BitcoinService.GetWallet(MainActivity.this, REQCODE_SEND);
                                        success = true;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                sendEmptyMessage(handler, MESSAGE_HIDE_PROGRESS);
                                if(!success) {
                                    sendMessage(handler, MESSAGE_SHOW_TOAST, R.string.main_wrong_handle, 0, null);
                                }
                            }
                        });
                break;

            case MESSAGE_SHOW_TOAST:
                Toast.makeText(this, msg.arg1, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.ivBip38Cancel)
    public void onBip38Cancel() {
        arrUtxo = null;
        updateBip38WalletInfo(null);
        rlBip38Wallet.animate().translationY(-1 * Utils.dpToPixel(this, 200.f))
                .setDuration(400)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                rlBip38Wallet.setVisibility(GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).start();
    }

    @OnClick(R.id.ivBip38Receive)
    public void onBip38Receive() {
        BitcoinService.SweepBip38(this);
        showProgress(true, R.string.main_progress_msg_bip38_sweep);
    }

    private void launchSendPopup(String address, long satoshis, String message) {
        addressToSend = address;
        satoshiToSend = satoshis;
        messageToSend = message;

        final SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        if(popupWindow4Send == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_sendconfirm, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_NOTO_LIGHT);
            popupWindow4Send = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow4Send.setElevation(16.f);
            popupWindow4Send.setOutsideTouchable(true);
            popupWindow4Send.setFocusable(true);

            popupView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
                popupWindow4Send.dismiss();
            });

            popupView.findViewById(R.id.btnConfirm).setOnClickListener((v)->{
                BitcoinService.Send(this, addressToSend, satoshiToSend);
                showProgress(true, R.string.main_progress_msg_sending);
                popupWindow4Send.dismiss();
            });
        }

        if(popupWindow4Send.isShowing()) {
            popupWindow4Send.dismiss();
        } else {
            View popupView = popupWindow4Send.getContentView();

            ((TextView)popupView.findViewById(R.id.tvBalance)).setText(Coin.valueOf(satoshiToSend).toFriendlyString());
            double localValue = BCHUSD * (satoshiToSend / 100000000.0f);
            String subBalance = String.format("$ %.2f", localValue);
            switch (sharedPreferenceMgr.get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD)) {
                case LOCAL_CURRENCY_KRW:
                    localValue *= MainActivity.USDKRW;
                    subBalance = String.format("₩ %.2f", localValue);
                    break;
                case LOCAL_CURRENCY_EU:
                    localValue *= MainActivity.USDEUR;
                    subBalance = String.format("€ %.2f", localValue);
                    break;
                case LOCAL_CURRENCY_JPY:
                    localValue *= MainActivity.USDJPY;
                    subBalance = String.format("¥ %.2f", localValue);
                    break;
                case LOCAL_CURRENCY_CHY:
                    localValue *= MainActivity.USDCNY;
                    subBalance = String.format("元 %.2f", localValue);
                    break;
            }
            ((TextView)popupView.findViewById(R.id.tvSubBalance)).setText(subBalance);
            String cashAddr = CashAddressFactory.create().getFromBase58(wallet.getNetworkParameters(), addressToSend).toString();
            if(cashAddr != null)
                ((TextView)popupView.findViewById(R.id.tvAddress)).setText(cashAddr);
            else
                ((TextView)popupView.findViewById(R.id.tvAddress)).setText(addressToSend);

            popupWindow4Send.showAtLocation(findViewById(R.id.swiperefreshTimeline), Gravity.CENTER, 0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case REQCODE_EULA:
                if(resultCode == RESULT_OK) {
                    new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_EULA_ACCEPTED, true);
                    if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_WALLET_CREATED, false)) {
                        BitcoinService.ConnectToBlockChain(this);
                    } else {
                        sendMessageDelayed(handler, BROADCAST_BITCOINSERVICE_CONNECT_RESULT, RESULT_NOKEY, 0, null, 100);
                    }
                } else {
                    finish();
                }
                break;
            case REQCODE_QRCODE:
                if(resultCode == RESULT_OK) {
                    String address = intent.getStringExtra(EXTRA_ADDRESS);
                    long satoshis = intent.getLongExtra(EXTRA_AMOUNT, 0);
                    String message = intent.getStringExtra(EXTRA_MESSAGE);
                    if(satoshis > 0 ) {
                        launchSendPopup(address, satoshis, message);
                    } else {
                        addressToSend = address;
                        messageToSend = message;
                        BitcoinService.GetWallet(this, REQCODE_SEND);
                    }
                } else if(resultCode == RESULT_BIP38) {
                    if(arrUtxo != null) {
                        Toast.makeText(this, R.string.main_bip38_already_opened, Toast.LENGTH_SHORT).show();
                    } else {
                        showProgress(true, R.string.main_progress_msg_bip38);
                        ((Handler) handler).post(() -> {
                            String address = intent.getStringExtra(EXTRA_ADDRESS);
                            String password = intent.getStringExtra(EXTRA_AMOUNT);
                            BitcoinService.GetFromBIP38Key(this, address, password);
                        });
                    }
                } else if(resultCode == RESULT_WIF) {
                    if(arrUtxo != null) {
                        Toast.makeText(this, R.string.main_bip38_already_opened, Toast.LENGTH_SHORT).show();
                    } else {
                        showProgress(true, R.string.main_progress_msg_bip38);
                        ((Handler) handler).post(() -> {
                            String wif = intent.getStringExtra(EXTRA_ADDRESS);
                            BitcoinService.GetFromWIF(this, wif);
                        });
                    }
                }
                break;
            case REQCODE_SEND:
                if(resultCode == RESULT_OK) {
                    String address = intent.getStringExtra(EXTRA_ADDRESS);
                    long satoshis = intent.getLongExtra(EXTRA_AMOUNT, 0);

                    if(satoshis > 0 ) {
                        launchSendPopup(address, satoshis, null);
                    }
                }
                break;
            case REQCODE_RESTORE:
                if(resultCode == RESULT_OK) {
                    String seed = intent.getStringExtra(EXTRA_SEED);
                    long creationTime = intent.getLongExtra(EXTRA_CREATION_TIME, -1);
                    showProgress(true, R.string.main_progress_msg_create_new_wallet);

                    Restore restore = new Restore(seed, creationTime);
                    sendMessageDelayed(handler, MESSAGE_START_RESTORE_WALLET, 0, 0, restore, 500);
                }
                break;
            case REQCODE_PINCODE_SET:
                switch (resultCode) {
                    case PincodeActivity.PINCODE_RESULT_CANCELD:
                        ((SwitchCompat)findViewById(R.id.switchPin)).setChecked(false);
                        break;
                }
                break;
            case REQCODE_PINCODE_REMOVE:
                switch (resultCode) {
                    case PincodeActivity.PINCODE_RESULT_REMOVE_PASS:
                        ((SwitchCompat)findViewById(R.id.switchFinger)).setChecked(false);
                        break;
                    case PincodeActivity.PINCODE_RESULT_PASS:
                    case PincodeActivity.PINCODE_RESULT_CANCELD:
                        ((SwitchCompat)findViewById(R.id.switchPin)).setChecked(true);
                        break;
                }
                break;

            case REQCODE_EDITWALLET:
                updateWalletBG();
                break;

            case REQCODE_MYADDRESS:
                updateWalletInfo(wallet);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isActiveNow = true;
        sendEmptyMessage(handler, MESSAGE_CHECK_NETWORK_STATE);
        BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
    }

    @Override
    public void onPause() {
        super.onPause();
        isActiveNow = false;
    }

    @OnClick(R.id.rlCreate)
    public void onCreateNewWallet() {
        showProgress(true, R.string.main_progress_msg_create_new_wallet);
        BitcoinService.ConnectToBlockChainWithNewWallet(this);
    }

    @OnClick(R.id.rlRestore)
    public void onRestoreWallet() {
        startActivityForResult(new Intent(this, RestoreActivity.class), REQCODE_RESTORE);
    }

    @OnClick(R.id.ivQRCode)
    public void onQRCode() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(this, QRCodeActivity.class), REQCODE_QRCODE);
        } else {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.CAMERA}, PERMISSION_RQCODE_CAMERA);
        }
    }

    @OnClick(R.id.ivShareAddress)
    public void onShareAddress() {
        String address = ((TextView) findViewById(R.id.tvAddress)).getText().toString();
        launchShareAddress(address);
//        int dotSize = new SharedPreferenceMgr(MainActivity.this).get(SHAREDPREF_INT_QRCODE_DOT_SIZE, 20);
//        File qrCode = Utils.checkQRCodeGenerated(this);
//        if(qrCode != null) {
//            launchShareAddress(qrCode, address, dotSize);
//        } else {
//            showProgress(true);
//            new Thread() {
//                @Override
//                public void run() {
//                    launchShareAddress(null, address, dotSize);
//                    sendEmptyMessage(handler, MESSAGE_HIDE_PROGRESS);
//                }
//            }.start();
//        }
    }

    @OnClick(R.id.ivEditWallet)
    public void onEditWallet() {
        startActivityForResult(new Intent(this, EditWalletActivity.class), REQCODE_EDITWALLET);
    }

    private void launchShareAddress(String address /*File qrcodeFile, String address, int dotSize*/) {
        MyAddressActivity.address = address;
        sendEmptyMessage(handler, MESSAGE_START_SHARE);

//        if(qrcodeFile == null) {
//            qrcodeFile = Utils.createQRCode(getApplicationContext(), address, totalWidth, dotSize);
//        }
//        if (qrcodeFile != null && qrcodeFile.exists()) {

//        } else {
//            Toast.makeText(MainActivity.this, R.string.main_cannot_create_qrcode, Toast.LENGTH_SHORT).show();
//        }
    }

    @OnClick(R.id.ivSendByAddress)
    public void onSendByAddress() {
        if(popupWindow4Addr == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_inputaddr, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_NOTO_LIGHT);
            popupWindow4Addr = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow4Addr.setElevation(16.f);
            popupWindow4Addr.setOutsideTouchable(true);
            popupWindow4Addr.setFocusable(true);

            popupView.findViewById(R.id.etAddress).setOnLongClickListener((v) -> {
                Utils.hideKeyboard(MainActivity.this, v);
                ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if(clipboard != null) {
                    if(clipboard.hasPrimaryClip()) {
                        ClipData clipData = clipboard.getPrimaryClip();
                        if (clipData != null) {
                            if (clipData.getItemCount() > 0) {
                                ((AppCompatEditText) v).setText(clipboard.getPrimaryClip().getItemAt(0).getText());
                            }
                        }
                    } else if(clipboard.hasText()) {
                        String text = clipboard.getText().toString();
                        ((AppCompatEditText) v).setText(text);
                    }

                }
                return true;
            });

            popupView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
                popup4Addr = false;
                popupWindow4Addr.dismiss();
            });

            popupView.findViewById(R.id.btnOK).setOnClickListener((v)->{
                if(popup4Addr) {
                    Utils.hideKeyboard(MainActivity.this, v);
                    popup4Addr = false;
                    addressToSend = ((AppCompatEditText) popupView.findViewById(R.id.etAddress)).getText().toString().trim();
                    RCashAddress rCashAddress = Utils.parseAddress(addressToSend);
                    if (rCashAddress != null) {
                        addressToSend = rCashAddress.cashAddress.toBase58();
                        messageToSend = rCashAddress.message;
                        BitcoinService.GetWallet(MainActivity.this, REQCODE_SEND);
                        popupWindow4Addr.dismiss();
                    } else /*if(addressToSend.startsWith("$"))*/ {
                        sendEmptyMessage(handler, MESSAGE_GET_HANDCASH_HANDLE);
                        popupWindow4Addr.dismiss();
                    } /*else {
                        addressToSend = null;
                        messageToSend = null;
                        satoshiToSend = 0;
                        ((AppCompatEditText) popupView.findViewById(R.id.etAddress)).setText("");
                        Toast.makeText(getApplicationContext(), R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                        popup4Addr = true;
                    } */
                }
            });
        }

        if(popupWindow4Addr.isShowing()) {
            popupWindow4Addr.dismiss();
            popup4Addr = false;
        } else {
            View popupView = popupWindow4Addr.getContentView();
            ((AppCompatEditText)popupView.findViewById(R.id.etAddress)).setText("");
            popupWindow4Addr.showAtLocation(findViewById(R.id.swiperefreshTimeline), Gravity.CENTER, 0, 0);
            popup4Addr = true;
        }
    }

    @OnClick(R.id.rlSideMenuPhrase)
    public void onSideMenuPhrase() {
        BitcoinService.GetWallet(this, REQCODE_BACKUP);
    }

    private void updateLocalCurrency() {
        SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        switch (sharedPreferenceMgr.get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD)) {
            case LOCAL_CURRENCY_KRW:
                ivLocalCurrency.setImageResource(R.drawable.flag_korean);
                break;
            case LOCAL_CURRENCY_USD:
                ivLocalCurrency.setImageResource(R.drawable.flag_us);
                break;
            case LOCAL_CURRENCY_EU:
                ivLocalCurrency.setImageResource(R.drawable.flag_eu);
                break;
            case LOCAL_CURRENCY_JPY:
                ivLocalCurrency.setImageResource(R.drawable.flag_japanese);
                break;
            case LOCAL_CURRENCY_CHY:
                ivLocalCurrency.setImageResource(R.drawable.flag_chinese);
                break;
        }
    }

    @OnClick(R.id.rlSideMenuLocalCurrency)
    public void onSideMenuLocalCurrency() {
        final SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        if(popupWindow4LocalCurrency == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_localcurrency, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_UBUNTU_LIGHT);
            popupWindow4LocalCurrency = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow4LocalCurrency.setElevation(16.f);
            popupWindow4LocalCurrency.setOutsideTouchable(true);
            popupWindow4LocalCurrency.setFocusable(true);
            popupView.findViewById(R.id.rlKRW).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_KRW);
                ivLocalCurrency.setImageResource(R.drawable.flag_korean);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4LocalCurrency.dismiss();
            } );
            popupView.findViewById(R.id.rlUSD).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD);
                ivLocalCurrency.setImageResource(R.drawable.flag_us);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4LocalCurrency.dismiss();
            } );
            popupView.findViewById(R.id.rlEU).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_EU);
                ivLocalCurrency.setImageResource(R.drawable.flag_eu);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4LocalCurrency.dismiss();
            } );
            popupView.findViewById(R.id.rlJPY).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_JPY);
                ivLocalCurrency.setImageResource(R.drawable.flag_japanese);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4LocalCurrency.dismiss();
            } );
            popupView.findViewById(R.id.rlCHY).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_CHY);
                ivLocalCurrency.setImageResource(R.drawable.flag_chinese);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4LocalCurrency.dismiss();
            } );
        }

        if(popupWindow4LocalCurrency.isShowing()) {
            popupWindow4LocalCurrency.dismiss();
        } else {
            View popupView = popupWindow4LocalCurrency.getContentView();
            popupView.findViewById(R.id.ivKRW).setVisibility(GONE);
            popupView.findViewById(R.id.ivUSD).setVisibility(GONE);
            popupView.findViewById(R.id.ivEU).setVisibility(GONE);
            popupView.findViewById(R.id.ivJPY).setVisibility(GONE);
            popupView.findViewById(R.id.ivCHY).setVisibility(GONE);

            switch (sharedPreferenceMgr.get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD)) {
                case LOCAL_CURRENCY_KRW:
                    popupView.findViewById(R.id.ivKRW).setVisibility(VISIBLE);
                    break;
                case LOCAL_CURRENCY_USD:
                    popupView.findViewById(R.id.ivUSD).setVisibility(VISIBLE);
                    break;
                case LOCAL_CURRENCY_EU:
                    popupView.findViewById(R.id.ivEU).setVisibility(VISIBLE);
                    break;
                case LOCAL_CURRENCY_JPY:
                    popupView.findViewById(R.id.ivJPY).setVisibility(VISIBLE);
                    break;
                case LOCAL_CURRENCY_CHY:
                    popupView.findViewById(R.id.ivCHY).setVisibility(VISIBLE);
                    break;
            }
            popupWindow4LocalCurrency.showAsDropDown(findViewById(R.id.rlSideMenuLocalCurrency));
        }
    }

    private void updateBchFormat() {
        SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        switch (sharedPreferenceMgr.get(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BCH)) {
            case BCH_FORMAT_BCH:
                tvBchFormat.setText(R.string.bch_format_bch);
                break;
            case BCH_FORMAT_BITS:
                tvBchFormat.setText(R.string.bch_format_bits);
                break;
            case BCH_FORMAT_SATOSHIS:
                tvBchFormat.setText(R.string.bch_format_satoshis);
                break;
        }
    }

    @OnClick(R.id.rlSideMenuBCHFormat)
    public void onSideMenuBCHFormat() {
        final SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        if(popupWindow4BCHForamt == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_bchformat, null);
            Utils.setGlobalFont(popupView, RCashConsts.FONT_UBUNTU_LIGHT);
            popupWindow4BCHForamt = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow4BCHForamt.setElevation(16.f);
            popupWindow4BCHForamt.setOutsideTouchable(true);
            popupWindow4BCHForamt.setFocusable(true);
            popupView.findViewById(R.id.rlBCH).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BCH);
                tvBchFormat.setText(R.string.bch_format_bch);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4BCHForamt.dismiss();
            } );
            popupView.findViewById(R.id.rlBits).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BITS);
                tvBchFormat.setText(R.string.bch_format_bits);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4BCHForamt.dismiss();
            } );
            popupView.findViewById(R.id.rlSatoshis).setOnClickListener((v) -> {
                sharedPreferenceMgr.put(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_SATOSHIS);
                tvBchFormat.setText(R.string.bch_format_satoshis);
                BitcoinService.GetWallet(getApplicationContext(), REQCODE_CHECK);
                popupWindow4BCHForamt.dismiss();
            } );
        }

        if(popupWindow4BCHForamt.isShowing()) {
            popupWindow4BCHForamt.dismiss();
        } else {
            View popupView = popupWindow4BCHForamt.getContentView();
            popupView.findViewById(R.id.ivBCH).setVisibility(GONE);
            popupView.findViewById(R.id.ivBits).setVisibility(GONE);
            popupView.findViewById(R.id.ivSatoshis).setVisibility(GONE);

            switch (sharedPreferenceMgr.get(SHAREDPREF_INT_BCH_FORMAT, BCH_FORMAT_BCH)) {
                case BCH_FORMAT_BCH:
                    popupView.findViewById(R.id.ivBCH).setVisibility(VISIBLE);
                    break;
                case BCH_FORMAT_BITS:
                    popupView.findViewById(R.id.ivBits).setVisibility(VISIBLE);
                    break;
                case BCH_FORMAT_SATOSHIS:
                    popupView.findViewById(R.id.ivSatoshis).setVisibility(VISIBLE);
                    break;
            }
            popupWindow4BCHForamt.showAsDropDown(findViewById(R.id.rlSideMenuBCHFormat));
        }
    }

    @OnClick(R.id.rlSideMenuOpenSource)
    public void onSideMenuOpenSource() {
        startActivity(new Intent(this, OpenSourceActivity.class));
    }

    @OnClick(R.id.rlSideMenuContactUs)
    public void onSideMenuContactUs() {
        Utils.sendSuggestion(this);
    }

    @OnClick(R.id.rlSideMenuClearAll)
    public void onSideMenuClearAll() {
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(R.string.main_alert_clearall_ttl)
                .setMessage(R.string.main_alert_clearall_desc)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    am.clearApplicationUserData();
                }).create();
        alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        alertDialog.show();
    }

    /*
    @OnClick(R.id.rlSideMenuAboutRcash)
    public void onSideMenuAboutRcash() {
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(R.string.donate_ttl)
                .setMessage(R.string.donate_desc)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    addressToSend = getString(R.string.developer_address);
                    RCashAddress rCashAddress = Utils.parseAddress(addressToSend);
                    if(rCashAddress != null) {
                        addressToSend = rCashAddress.cashAddress.toBase58();
                        BitcoinService.GetWallet(MainActivity.this, REQCODE_SEND);
                    } else {
                        addressToSend = null;
                    }
                }).create();
        alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        alertDialog.show();
    }*/

    @OnCheckedChanged(R.id.switchPin)
    public void onCheckChangedPin(SwitchCompat view) {
        if(view.isChecked()) {
            if(!new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_SECUREMODE, false)) {
                Intent intent = new Intent(this, PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_TYPE, PincodeActivity.TYPE_SET_PASSWORD);
                startActivityForResult(intent, REQCODE_PINCODE_SET);
            }
        } else {
            if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_SECUREMODE, false)) {
                Intent intent = new Intent(this, PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_TYPE, PincodeActivity.TYPE_REMOVE_PASSWORD);
                startActivityForResult(intent, REQCODE_PINCODE_REMOVE);
            }
        }
    }

    @OnCheckedChanged(R.id.switchFinger)
    public void onCheckChangedFinger(SwitchCompat view) {
        view.setChecked(false);
        Toast.makeText(this, R.string.common_todo, Toast.LENGTH_SHORT).show();
//        Log.i(TAG, "onCheckChangedfinger");
//        boolean isChecked = false;
//        if(view.isChecked()) {
//            Log.i(TAG, "isChecked");
//            if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_SECUREMODE, false)) {
//                Log.i(TAG, "SECUREMODE : true");
//                if (Utils.checkBiometric(this)) {
//                    Log.i(TAG, "checkBiometric : true");
//                    KeyStoreMgr keyStoreMgr = KeyStoreMgr.getInstance();
//                    if(keyStoreMgr.createBiometricKey(this) != null) {
//                        Log.i(TAG, "createSecretKey success");
//                        isChecked = true;
//                        new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_FINGERPRINT, true);
//                    }
//                }
//            }
//        }
//
//        Log.i(TAG, "isChecked : " + isChecked);
//        if(!isChecked) {
//
//            new SharedPreferenceMgr(this).put(SHAREDPREF_BOOL_FINGERPRINT, false);
//            KeyStoreMgr.getInstance().deleteKey(TAG_KEY_SECRET);
//            view.setChecked(false);
//        }
    }

    private void updateWalletBG() {
        SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(this);
        int walletColor = sharedPreferenceMgr.get(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_RED);
        switch(walletColor) {
            case WALLET_COLOR_RED:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_red);
                break;
            case WALLET_COLOR_PINK:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_pink);
                break;
            case WALLET_COLOR_ORANGE:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_orange);
                break;
            case WALLET_COLOR_PURPLE:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_purple);
                break;
            case WALLET_COLOR_INDIGO:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_indigo);
                break;
            case WALLET_COLOR_CYON:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_cyon);
                break;
            case WALLET_COLOR_GREEN:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_green);
                break;
            case WALLET_COLOR_BROWN:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_brown);
                break;
            case WALLET_COLOR_BLUEGREY:
                rlWallet.setBackgroundResource(R.drawable.shape_rect_wallet_bluegrey);
                break;
        }
        ((TextView) findViewById(R.id.tvWalletAlias)).setText(sharedPreferenceMgr.get(SHAREDPREF_STRING_WALLET_ALIAS, getString(R.string.main_wallet_default_alias)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 지역통화가 설정 안된 케이스에 몇몇 나라는 기본 통화로 자기 나라를 지정해준다.
         * 한국 / 일본 / 중국 ....
         */
        if(new SharedPreferenceMgr(this).get(SHAREDPREF_INT_LOCAL_CURRENCY, -1) == -1) {
            Locale current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
            if(current.getLanguage().equals(Locale.KOREA.getLanguage())) {
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_KRW);
            }
        }

        /**
         * Download Where.Cash KML file
         */
        long lastDownloadTime = new SharedPreferenceMgr(this).get(SHAREDPREF_LONG_LAST_WHERECASH_DOWNLOAD, 0L);
        if(System.currentTimeMillis() - lastDownloadTime > 1000 * 60 * 60 * 24) {
            Utils.downloadFile(getApplicationContext(),"http://www.google.com/maps/d/kml?forcekml=1&mid=1FL9Xo2z51LWlW9Yj8S52oeJb1N13wRQw", Utils.getWhereCashPath(this));
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);

        ((TextView)findViewById(R.id.tvWalletAlias)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        tvBCH.setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_BOLD));
        ((TextView)findViewById(R.id.tvBip38BCH)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_BOLD));
        ((TextView)findViewById(R.id.tvBalance)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        ((TextView)findViewById(R.id.tvBip38Balance)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        tvSubBalance.setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        tvSubBalanceChange.setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        ((TextView)findViewById(R.id.tvAddress)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));
        ((TextView)findViewById(R.id.tvEmptyDesc)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_REGULAR));
        ((TextView)findViewById(R.id.tvPoweredByWhereCash)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_UBUNTU_LIGHT));

        ((TextView)findViewById(R.id.tvProfileName)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        ((TextView)findViewById(R.id.tvSideMenuSecurity)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        ((TextView)findViewById(R.id.tvSideMenuBackup)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        ((TextView)findViewById(R.id.tvSideMenuSetting)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        ((TextView)findViewById(R.id.tvSideMenuAbout)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));

        rlBip38Wallet.setVisibility(GONE);

        updateWalletBG();

        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.llSideMenu).setPadding(0, statusBarHeight, 0, softKeyHeight);
        findViewById(R.id.swiperefreshTimeline).setPadding(0, 0, 0, softKeyHeight);

        updateBchFormat();
        updateLocalCurrency();

        swipeRefreshLayout.setOnRefreshListener(() -> {
                sendEmptyMessage(handler, MESSAGE_REFRESH_WALLET);
        });
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorPrimary));

        showProgress(true, R.string.main_progress_msg_init);
        readyWithAnimation(true);

        if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_SECUREMODE, false)) {
            ((SwitchCompat)findViewById(R.id.switchPin)).setChecked(true);
        } else {
            ((SwitchCompat)findViewById(R.id.switchPin)).setChecked(false);
        }

        if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_EULA_ACCEPTED, false)) {
            if(new SharedPreferenceMgr(this).get(SHAREDPREF_BOOL_WALLET_CREATED, false)) {
                BitcoinService.ConnectToBlockChain(this);
            } else {
                sendMessageDelayed(handler, BROADCAST_BITCOINSERVICE_CONNECT_RESULT, RESULT_NOKEY, 0, null, 100);
            }
        } else {
            Intent newIntent = new Intent(this, EulaActivity.class);
            startActivityForResult(newIntent, REQCODE_EULA);
        }

        flSideMenu.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                flSideMenu.getViewTreeObserver().removeOnPreDrawListener(this);
                ViewGroup.LayoutParams lp = flSideMenu.getLayoutParams();
                lp.width = totalWidth - Utils.dpToPixel(MainActivity.this, 96.f);
                flSideMenu.setLayoutParams(lp);
                return false;
            }
        });
    }

    private void readyWithAnimation(boolean firstTime) {
        if(firstTime) {
            rlWallet.setTranslationY(totalHeight);
            rlEmptyWallet.setTranslationY(totalHeight);
            rlWallet.setVisibility(GONE);
            rlEmptyWallet.setVisibility(GONE);
            rlStickyProgress.setTranslationX(-totalWidth);
        } else {
            if(rlWallet.getVisibility() == VISIBLE) {
                rlWallet.animate().alpha(0.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        rlWallet.setAlpha(1.f);
                        rlWallet.setVisibility(GONE);
                        rlWallet.setTranslationY(totalHeight);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                }).start();
            } else if(rlEmptyWallet.getVisibility() == VISIBLE) {
                rlEmptyWallet.animate().alpha(0.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        rlEmptyWallet.setAlpha(1.f);
                        rlEmptyWallet.setVisibility(GONE);
                        rlEmptyWallet.setTranslationY(totalHeight);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                }).start();
            }
        }
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onMenu() {
        if(drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @OnClick(R.id.ivToolbarHistory)
    public void onHistory() {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void showProgress(boolean visible) {
        showProgress(visible, -1);
    }

    private void showProgress(boolean visible, int messageId) {
        if(visible) {
            findViewById(R.id.rlProgress).setVisibility(VISIBLE);
            if(messageId != -1)
                ((TextView) findViewById(R.id.tvProgressMsg)).setText(messageId);
            else
                ((TextView) findViewById(R.id.tvProgressMsg)).setText("");
        }
        else {
            findViewById(R.id.rlProgress).setVisibility(GONE);
            ((TextView) findViewById(R.id.tvProgressMsg)).setText("");
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateSticky() {
        if(rlSticky != null) {
            rlSticky.clearAnimation();

            if((stickyState & STICKY_HIDE) != 0) {
                if(curStickyState != STICKY_STATE.HIDE) {
                    curStickyState = STICKY_STATE.HIDE;
                    rlSticky.animate()
                            .translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setAlpha(0.f);
                                    rlStickyProgress.setVisibility(GONE);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                }
            } else if((stickyState & STICKY_NO_INTERNET) != 0) {
                if(curStickyState != STICKY_STATE.NO_INTERNET) {
                    curStickyState = STICKY_STATE.NO_INTERNET;
                    rlSticky.animate().translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setBackgroundColor(getResources().getColor(R.color.colorStickyRed, getTheme()));
                                    rlSticky.setAlpha(0.69f);
                                    tvSticky.setText(R.string.sticky_no_internet);
                                    rlSticky.animate().translationY(0).setDuration(300).setListener(null).start();
                                    rlStickyProgress.setVisibility(GONE);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                }
            }  else if((stickyState & STICKY_NO_WALLET) != 0) {
                if(curStickyState != STICKY_STATE.NO_WALLET) {
                    curStickyState = STICKY_STATE.NO_WALLET;
                    rlSticky.animate().translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setBackgroundColor(getResources().getColor(R.color.colorStickyRed, getTheme()));
                                    rlSticky.setAlpha(0.69f);
                                    tvSticky.setText(R.string.sticky_no_wallet);
                                    rlSticky.animate().translationY(0).setDuration(300).setListener(null).start();
                                    rlStickyProgress.setVisibility(GONE);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                }
            } else if((stickyState & STICKY_NO_CONNECTION) != 0) {
                if(curStickyState != STICKY_STATE.NO_CONNECTION) {
                    curStickyState = STICKY_STATE.NO_CONNECTION;
                    rlSticky.animate().translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setBackgroundColor(getResources().getColor(R.color.colorStickyRed, getTheme()));
                                    rlSticky.setAlpha(0.69f);
                                    tvSticky.setText(R.string.sticky_no_connection);
                                    rlSticky.animate().translationY(0).setDuration(300).setListener(null).start();
                                    rlStickyProgress.setVisibility(GONE);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                }
            } else if((stickyState & STICKY_DOWNLOADING) != 0) {
                if (curStickyState != STICKY_STATE.NO_WALLET) {
                    curStickyState = STICKY_STATE.NO_WALLET;
                    rlSticky.animate().translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setBackgroundColor(getResources().getColor(R.color.colorStickyOrange, getTheme()));
                                    rlSticky.setAlpha(0.69f);
                                    updateStickyBlockchainSync(false);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                } else {
                    updateStickyBlockchainSync(false);
                }
            } else if((stickyState & STICKY_DOWNLOADING_BIP38) != 0) {
                if (curStickyState != STICKY_STATE.NO_WALLET) {
                    curStickyState = STICKY_STATE.NO_WALLET;
                    rlSticky.animate().translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setBackgroundColor(getResources().getColor(R.color.colorStickyOrange, getTheme()));
                                    rlSticky.setAlpha(0.69f);
                                    updateStickyBlockchainSync(true);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                } else {
                    updateStickyBlockchainSync(true);
                }
            } else {
                if(curStickyState != STICKY_STATE.NORMAL) {
                    curStickyState = STICKY_STATE.NORMAL;
                    rlSticky.animate()
                            .translationY(-rlSticky.getHeight())
                            .setDuration(100)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) { }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rlSticky.setAlpha(0.f);
                                    rlStickyProgress.setVisibility(GONE);
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) { }
                                @Override
                                public void onAnimationRepeat(Animator animation) { }
                            }).start();
                }
            }
        }
    }

    @OnClick(R.id.ivBCHAH)
    public void onBitcoinCashAcceptedHere() {
        startActivity(new Intent(this, WhereCashActivity.class));
    }

    private void updateStickyBlockchainSync(boolean bip38) {
        String format = getString(R.string.sticky_downloading);
        String downloading = String.format(format + " (%d%%)",(bip38 ? curBlockchainProgressBip38 : curBlockchainProgress));
        tvSticky.setText(downloading);
        rlSticky.animate().translationY(0).setDuration(300).setListener(null).start();

        // 100 : totalWidth = curBlockhainProgress : x
        int x = (totalWidth * (bip38 ? curBlockchainProgressBip38 : curBlockchainProgress)) / 100;
        rlStickyProgress.animate().translationX(-(totalWidth - x)).setDuration(100).start();
        rlStickyProgress.setVisibility(VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        switch (requestCode) {
            case PERMISSION_RQCODE_CAMERA:
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(this, QRCodeActivity.class), REQCODE_QRCODE);
                } else {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        Toast.makeText(this, R.string.main_permission_camera_ttl, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.main_permission_camera_desc, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
