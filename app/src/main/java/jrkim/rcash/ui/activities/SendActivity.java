package jrkim.rcash.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.wallet.Wallet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.SharedPreferenceMgr;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_AMOUNT;
import static jrkim.rcash.consts.RCashConsts.EXTRA_MODE;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_CHY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_EU;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_JPY;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_KRW;
import static jrkim.rcash.consts.RCashConsts.LOCAL_CURRENCY_USD;
import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_LOCAL_CURRENCY;

public class SendActivity extends BaseActivity {

    private final static String TAG = "RCash_Send";

    private String address = "";
    private String input = "";
    private boolean dot = false;
    private int cntAfterDot = 0;
    private int mode = MODE_SEND;
    public static Wallet wallet = null;
    private boolean bchMode = true;
    @BindView(R.id.tvBalance) TextView tvBalance;
    @BindView(R.id.tvSubBalance) TextView tvSubBalance;

    public static final int MODE_SEND = 0;
    public static final int MODE_REQUEST = 1;

    @Override
    protected void handleMessage(Message msg) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(wallet == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            setContentView(R.layout.activity_send);
            ButterKnife.bind(this);

            Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_SANSATION_BOLD);


            int margin2 = getResources().getDimensionPixelOffset(R.dimen.default_margin2);
            findViewById(R.id.rlTop).setPadding(0, statusBarHeight, 0, 0);
            findViewById(R.id.rlBottom).setPadding(margin2, 0, margin2, softKeyHeight);

            address = getIntent().getStringExtra(EXTRA_ADDRESS);
            mode = getIntent().getIntExtra(EXTRA_MODE, MODE_SEND);

            if(mode == MODE_REQUEST) {
                ((TextView)findViewById(R.id.tvTitle)).setText(R.string.request_ttl);
                ((Button)findViewById(R.id.btnSend)).setText(R.string.request_btn);
            }

            updateInput();

            findViewById(R.id.ivChange).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipping();
                }
            });
        }
    }

    private void flipping() {
        if(input.length() > 0) {

            String subBalacne = tvSubBalance.getText().toString();
            subBalacne = subBalacne.substring(subBalacne.indexOf(" ") + 1);
            input = "";

            dot = false;
            cntAfterDot = 0;
            for (int i = 0; i < subBalacne.length(); i++) {
                addNewNum("" + subBalacne.charAt(i));
            }
        }

        bchMode = !bchMode;
        updateInput();
    }

    private void addNewNum(String newNum) {
        if(((input.length() == 0 || dot) && newNum.equals(".")) || cntAfterDot == 8) {
            return;
        }
        input += newNum;
        if(dot)
            cntAfterDot++;

        if(newNum.equals("."))
            dot = true;

        if(input.length() == 1 && input.equals("0")) {
            input += ".";
            dot = true;
        }
    }

    private void updateInput() {
        int localCurrency = new SharedPreferenceMgr(getApplicationContext()).get(SHAREDPREF_INT_LOCAL_CURRENCY, LOCAL_CURRENCY_USD);

        if(bchMode) {
            if (input.length() == 0) {
                tvBalance.setText("BCH 0");
                switch (localCurrency) {
                    case LOCAL_CURRENCY_KRW:
                        tvSubBalance.setText("₩ 0");
                        break;
                    case LOCAL_CURRENCY_USD:
                        tvSubBalance.setText("$ 0");
                        break;
                    case LOCAL_CURRENCY_EU:
                        tvSubBalance.setText("€ 0");
                        break;
                    case LOCAL_CURRENCY_JPY:
                        tvSubBalance.setText("¥ 0");
                        break;
                    case LOCAL_CURRENCY_CHY:
                        tvSubBalance.setText("元 0");
                        break;
                }
            } else {
                String temp = "BCH " + input;
                tvBalance.setText(temp);

                double localValue = 0.0;
                try {
                    localValue = MainActivity.BCHUSD * Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                String subBalance = String.format("$ %.2f", localValue);
                switch (localCurrency) {
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
                tvSubBalance.setText(subBalance);
            }
        } else {
            if (input.length() == 0) {
                tvSubBalance.setText("BCH 0");
                switch (localCurrency) {
                    case LOCAL_CURRENCY_KRW:
                        tvBalance.setText("₩ 0");
                        break;
                    case LOCAL_CURRENCY_USD:
                        tvBalance.setText("$ 0");
                        break;
                    case LOCAL_CURRENCY_EU:
                        tvBalance.setText("€ 0");
                        break;
                    case LOCAL_CURRENCY_JPY:
                        tvBalance.setText("¥ 0");
                        break;
                    case LOCAL_CURRENCY_CHY:
                        tvBalance.setText("元 0");
                        break;
                }
            } else {
                String temp = "$ " + input;
                double bchValue = 0.0;
                try {
                    bchValue = Double.parseDouble(input) / MainActivity.BCHUSD;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                switch (localCurrency) {
                    case LOCAL_CURRENCY_KRW:
                        bchValue /= MainActivity.USDKRW;
                        temp = "₩ " + input;
                        break;
                    case LOCAL_CURRENCY_EU:
                        bchValue /= MainActivity.USDEUR;
                        temp = "€ " + input;
                        break;
                    case LOCAL_CURRENCY_JPY:
                        bchValue /= MainActivity.USDJPY;
                        temp = "¥ " + input;
                        break;
                    case LOCAL_CURRENCY_CHY:
                        bchValue /= MainActivity.USDCNY;
                        temp = "元 " + input;
                        break;
                }
                tvBalance.setText(temp);
                tvSubBalance.setText(String.format("BCH %.8f", bchValue));
            }
        }

        long satoshis = getAmount();
        if(satoshis > 0) {
            Coin sendCoin = Coin.valueOf(satoshis);
            if (mode == MODE_SEND && wallet.getBalance().isLessThan(sendCoin)) {
                boolean originMode = bchMode;
                if(input.length() > 0) {
                    String maxBalance = wallet.getBalance().toPlainString();
                    input = "";

                    dot = false;
                    cntAfterDot = 0;
                    for (int i = 0; i < maxBalance.length(); i++) {
                        addNewNum("" + maxBalance.charAt(i));
                    }
                }
                bchMode = true;
                updateInput();

                if(!originMode) {
                    flipping();
                }

                Toast.makeText(getApplicationContext(), R.string.send_full_amount, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick({R.id.tv0, R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6, R.id.tv7, R.id.tv8, R.id.tv9, R.id.tvBack, R.id.tvDot})
    public void onNumPad(View view) {
        switch (view.getId()) {
            case R.id.tv0:  addNewNum("0"); break;
            case R.id.tv1:  addNewNum("1"); break;
            case R.id.tv2:  addNewNum("2"); break;
            case R.id.tv3:  addNewNum("3"); break;
            case R.id.tv4:  addNewNum("4"); break;
            case R.id.tv5:  addNewNum("5"); break;
            case R.id.tv6:  addNewNum("6"); break;
            case R.id.tv7:  addNewNum("7"); break;
            case R.id.tv8:  addNewNum("8"); break;
            case R.id.tv9:  addNewNum("9"); break;
            case R.id.tvDot: addNewNum("."); break;
            case R.id.tvBack:
                if(input.length() > 0) {
                    String removedCh = input.substring(input.length() - 1);
                    input = input.substring(0, input.length() -1);
                    if(removedCh.equals(".")) {
                        dot = false;
                        cntAfterDot = 0;
                    }

                    if(dot)
                        cntAfterDot--;

                    if(input.length() == 1 && input.equals("0")) {
                        dot = false;
                        input = "";
                    }
                }
                break;
        }
        updateInput();
    }

    private long getAmount() {
        double amount = 0;
        String bchInput = bchMode ? tvBalance.getText().toString() : tvSubBalance.getText().toString();
        bchInput = bchInput.substring(bchInput.indexOf(" "));

        if(bchInput.length() > 0) {
            String temp = bchInput;
            String lastCh = bchInput.substring(bchInput.length() - 1);
            if(lastCh.equals(".")) {
                temp += "0";
            }

            amount = Double.parseDouble(temp);
        }

        long satoshis = (long)(amount * 100000000L);

        return satoshis;
    }

    @OnClick(R.id.btnSend)
    public void onSend() {

        long satoshis = getAmount();
        if(satoshis > 0) {
            Coin sendCoin = Coin.valueOf(satoshis);
            if(mode == MODE_SEND && wallet.getBalance().isLessThan(sendCoin)) {
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.send_cant_zero_ttl)
                        .setMessage(getString(R.string.send_cant_less_desc))
                        .setPositiveButton(R.string.common_ok, null).create();
                alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                alertDialog.show();
            } else{
                Intent intent = new Intent();
                intent.putExtra(EXTRA_AMOUNT, satoshis);
                intent.putExtra(EXTRA_ADDRESS, address);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            if(mode == MODE_SEND) {
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.send_cant_zero_ttl)
                        .setMessage(getString(R.string.send_cant_zero_desc))
                        .setPositiveButton(R.string.common_ok, null).create();
                alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                alertDialog.show();
            } else if(mode == MODE_REQUEST) {
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.request_cant_zero_ttl)
                        .setMessage(getString(R.string.request_cant_zero_desc))
                        .setPositiveButton(R.string.common_ok, null).create();
                alertDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                alertDialog.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(input.length() > 0) {
            onNumPad(findViewById(R.id.tvBack));
        } else {
            if(mode == MODE_SEND) {
                new AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.send_cancel_ttl)
                        .setMessage(R.string.send_cancel_desc)
                        .setPositiveButton(R.string.pincode_conitnue, null)
                        .setNegativeButton(R.string.pincode_nexttime, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        }).show();
            } else if(mode == MODE_REQUEST) {
                new AlertDialog.Builder(this, R.style.AlertDialog)
                        .setTitle(R.string.request_cancel_ttl)
                        .setMessage(R.string.request_cancel_desc)
                        .setPositiveButton(R.string.pincode_conitnue, null)
                        .setNegativeButton(R.string.pincode_nexttime, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        }).show();
            }
        }
    }
}
