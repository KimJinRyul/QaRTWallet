package jrkim.rcash.data;

import android.util.Log;

import org.bitcoinj.core.Transaction;

public class DogerUTXO {
    public Double amount;
    public long height;
    public long satoshis;
    public String address;
    public String scriptPubKey;
    public String txid;
    public int confirmations;
    public int vout;
    public Transaction transaction = null;

    public void print(String tag) {
        Log.i(tag, "amount:" + amount);
        Log.i(tag, "height:" + height);
        Log.i(tag, "satoshis:" + satoshis);
        Log.i(tag, "address:" + address);
        Log.i(tag, "scriptPubKey:" + scriptPubKey);
        Log.i(tag, "txid:" + txid);
        Log.i(tag, "confirmations:" + confirmations);
        Log.i(tag, "vout:" + vout);
    }
}
