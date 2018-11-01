package jrkim.rcash.ui.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.Wallet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.bitcoinj.BitcoinService;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.ADDRESS_FORMAT_CASHADDR;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_FRESH_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_WALLET;
import static jrkim.rcash.consts.RCashConsts.REQCODE_CHECK;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_ADDRESS_FORMAT;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_WALLET_COLOR;
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

public class EditWalletActivity extends BaseActivity {

    private static String TAG ="RCash_EditWallet";

    @BindView(R.id.ivRed) ImageView ivRed;
    @BindView(R.id.ivOrange) ImageView ivOrange;
    @BindView(R.id.ivPink) ImageView ivPink;
    @BindView(R.id.ivPurple) ImageView ivPurple;
    @BindView(R.id.ivIndigo) ImageView ivIndigo;
    @BindView(R.id.ivCyon) ImageView ivCyon;
    @BindView(R.id.ivBrown) ImageView ivBrown;
    @BindView(R.id.ivBluegrey) ImageView ivBluegrey;
    @BindView(R.id.ivGreen) ImageView ivGreen;
    @BindView(R.id.etAlias) EditText etAlias;

    @Override
    protected void handleMessage(Message msg) {
        int addrFormat = ADDRESS_FORMAT_CASHADDR;
        switch (msg.what) {
            case BROADCAST_BITCOINSERVICE_FRESH_ADDRESS:
                new SharedPreferenceMgr(this).put(SHAREDPREF_STRING_LAST_ADDRESS, ((Address)msg.obj).toBase58());
                addrFormat = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_ADDRESS_FORMAT, ADDRESS_FORMAT_CASHADDR);
                if(msg.obj != null) {
                    if(addrFormat == ADDRESS_FORMAT_CASHADDR)
                        ((TextView)findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(((Address)msg.obj).toBase58()));
                    else
                        ((TextView)findViewById(R.id.tvAddress)).setText(((Address)msg.obj).toBase58());
                }
                Utils.deleteAllQRImages(this, false);

                break;
            case BROADCAST_BITCOINSERVICE_WALLET:
                if(msg.obj != null) {
                    MainActivity.wallet = (Wallet)msg.obj;

                    String lastAddress = new SharedPreferenceMgr(this).get(SHAREDPREF_STRING_LAST_ADDRESS, null);
                    addrFormat = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_ADDRESS_FORMAT, ADDRESS_FORMAT_CASHADDR);
                    if(lastAddress == null) {
                        if(addrFormat == ADDRESS_FORMAT_CASHADDR)
                            ((TextView)findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(MainActivity.wallet));
                        else
                            ((TextView)findViewById(R.id.tvAddress)).setText(MainActivity.wallet.currentReceiveAddress().toBase58());
                    } else {
                        if(addrFormat == ADDRESS_FORMAT_CASHADDR)
                            ((TextView) findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(lastAddress));
                        else
                            ((TextView) findViewById(R.id.tvAddress)).setText(lastAddress);
                    }
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wallet);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);


        String lastAddress = new SharedPreferenceMgr(this).get(SHAREDPREF_STRING_LAST_ADDRESS, null);
        int addrFormat = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_ADDRESS_FORMAT, ADDRESS_FORMAT_CASHADDR);
        if(lastAddress == null && MainActivity.wallet != null) {
            if(addrFormat == ADDRESS_FORMAT_CASHADDR)
                ((TextView) findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(MainActivity.wallet));
            else
                ((TextView) findViewById(R.id.tvAddress)).setText(MainActivity.wallet.currentReceiveAddress().toBase58());
        } else if(lastAddress != null) {
            if(addrFormat == ADDRESS_FORMAT_CASHADDR)
                ((TextView) findViewById(R.id.tvAddress)).setText(Utils.getReceiveCashAddress(lastAddress));
            else
                ((TextView) findViewById(R.id.tvAddress)).setText(lastAddress);
        } else {
            BitcoinService.GetWallet(this, REQCODE_CHECK);
            ((TextView) findViewById(R.id.tvAddress)).setText("");
        }

        String alias = new SharedPreferenceMgr(this).get(SHAREDPREF_STRING_WALLET_ALIAS, getString(R.string.main_wallet_default_alias));
        etAlias.setText(alias);

        updateWalletColorSelector();
    }

    @OnClick(R.id.ivRefreshAddress)
    public void onRefreshAddress() {
        BitcoinService.FreshAddress(this);
    }

    @OnClick({R.id.viewRed, R.id.viewOrange, R.id.viewPink, R.id.viewPurple, R.id.viewIndigo, R.id.viewCyon, R.id.viewBrown, R.id.viewBluegrey, R.id.viewGreen})
    public void onSelectColor(View view) {
        switch (view.getId()) {
            case R.id.viewRed:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_RED);
                break;
            case R.id.viewOrange:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_ORANGE);
                break;
            case R.id.viewPink:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_PINK);
                break;
            case R.id.viewPurple:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_PURPLE);
                break;
            case R.id.viewIndigo:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_INDIGO);
                break;
            case R.id.viewCyon:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_CYON);
                break;
            case R.id.viewBrown:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_BROWN);
                break;
            case R.id.viewBluegrey:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_BLUEGREY);
                break;
            case R.id.viewGreen:
                new SharedPreferenceMgr(this).put(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_GREEN);
                break;
        }
        updateWalletColorSelector();
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        new SharedPreferenceMgr(this).put(SHAREDPREF_STRING_WALLET_ALIAS, etAlias.getText().toString());
        finish();
    }

    @Override
    public void onBackPressed() {
        onToolbarNavigator();
    }

    private void updateWalletColorSelector() {
        ivRed.setVisibility(View.GONE);
        ivPink.setVisibility(View.GONE);
        ivOrange.setVisibility(View.GONE);
        ivPurple.setVisibility(View.GONE);
        ivIndigo.setVisibility(View.GONE);
        ivCyon.setVisibility(View.GONE);
        ivGreen.setVisibility(View.GONE);
        ivBrown.setVisibility(View.GONE);
        ivBluegrey.setVisibility(View.GONE);

        int walletColor = new SharedPreferenceMgr(this).get(SHAREDPREF_INT_WALLET_COLOR, WALLET_COLOR_RED);
        switch(walletColor) {
            case WALLET_COLOR_RED:
                ivRed.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_PINK:
                ivPink.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_ORANGE:
                ivOrange.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_PURPLE:
                ivPurple.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_INDIGO:
                ivIndigo.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_CYON:
                ivCyon.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_GREEN:
                ivGreen.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_BROWN:
                ivBrown.setVisibility(View.VISIBLE);
                break;
            case WALLET_COLOR_BLUEGREY:
                ivBluegrey.setVisibility(View.VISIBLE);
                break;
        }
    }
}
