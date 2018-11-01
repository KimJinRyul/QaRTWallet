package jrkim.rcash.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import jrkim.rcash.bitcoinj.BitcoinService;

/**
 * 단말이 재부팅 되었을때 다시 BlockChain 과 자동으로 연결함.
 */
public class BootReceiver extends BroadcastReceiver {
    private final static String TAG = "RCash_Boot";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BOOT_COMPLETED");
        BitcoinService.ConnectToBlockChain(context);
//        Intent newIntent = new Intent(context.getApplicationContext(), BitcoinService.class);
//        newIntent.putExtra(BitcoinService.EXTRA_ACTION, BitcoinService.ACTION_CONNECT_TO_BLOCKCHAIN);
//        context.startService(intent);
    }
}
