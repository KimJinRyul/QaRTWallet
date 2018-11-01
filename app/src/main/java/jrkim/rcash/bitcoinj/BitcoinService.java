package jrkim.rcash.bitcoinj;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.BIP38PrivateKey;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import jrkim.rcash.BuildConfig;
import jrkim.rcash.R;
import jrkim.rcash.data.DogerUTXO;
import jrkim.rcash.database.DataBaseConsts;
import jrkim.rcash.database.DataBaseMgr;
import jrkim.rcash.network.NetworkMgr;
import jrkim.rcash.ui.activities.BaseActivity;
import jrkim.rcash.ui.notification.NotificationMgr;

import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BIP38_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_CHECK;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_CONNECT_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_DISCONNECT_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_FRESH_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_SEND_RESULT;
import static jrkim.rcash.consts.RCashConsts.BROADCAST_BITCOINSERVICE_WALLET;
import static jrkim.rcash.consts.RCashConsts.EXTRA_ACTION;
import static jrkim.rcash.consts.RCashConsts.EXTRA_ADDRESS;
import static jrkim.rcash.consts.RCashConsts.EXTRA_BIP38PASSWORD;
import static jrkim.rcash.consts.RCashConsts.EXTRA_BIP38PRIKEY;
import static jrkim.rcash.consts.RCashConsts.EXTRA_REQCODE;
import static jrkim.rcash.consts.RCashConsts.EXTRA_SATOSHIS;
import static jrkim.rcash.consts.RCashConsts.REQCODE_CHECK;
import static jrkim.rcash.ui.activities.RestoreActivity.EXTRA_CREATION_TIME;
import static jrkim.rcash.ui.activities.RestoreActivity.EXTRA_SEED;


public class BitcoinService extends Service {

    private static final String TAG = "RCash_BitcoinS";

    private final static String WALLET_NAME = "RCash-Wallet";
    private static WalletAppKit walletAppKit = null;
    private static ECKey ecKeyBip38 = null;

    private static final int ACTION_NOTHING = 0;
    private static final int ACTION_CONNECT_TO_BLOCKCHAIN = 1;
    private static final int ACTION_DISCONNECT_TO_BLOCKCHAIN = 2;
    private static final int ACTION_SEND = 3;
    private static final int ACTION_CHECK_CONNECTED = 4;
    private static final int ACTION_CONNECT_TO_BLOCKCHAIN_WITH_NEW = 5;
    private static final int ACTION_CONNECT_TO_BLOCKCHAIN_WITH_RESTORE = 6;
    private static final int ACTION_GET_WALLET = 7;
    private static final int ACTION_FRESH_ADDRESS = 8;
    private static final int ACTION_GET_PROGRESS = 9;
    private static final int ACTION_BIP38 = 10;
    private static final int ACTION_SWEEP_BIP38 = 11;
    private static final int ACTION_WIF = 12;
    private static final int ACTION_SWEEP_WIF = 13;



    public static final int RESULT_UNKNOWN_ERROR = -5;
    public static final int RESULT_PASSWORD_ERROR = -4;
    public static final int RESULT_ADDRESS_FORMAT_ERROR = -3;
    public static final int RESULT_NOKEY = -2;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_SWEEP_SUCCESS = 1;

    public static final int RESULT_DISCONNECTED = -1;
    public static final int RESULT_CONNECTED = 0;

    private static final int CREATE_TYPE_NONE   = -1;
    private static final int CREATE_TYPE_NEW    = 1;
    private static final int CREATE_TYPE_RESTORE = 2;
    public static double pct = 0;
//    private static double downloadProgress = 0;

    private static BitcoinServiceHandler handler = null;
    private static class BitcoinServiceHandler extends Handler {
        private final WeakReference<BitcoinService> service;
        BitcoinServiceHandler(BitcoinService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BitcoinService bitcoinService = service.get();
            if(bitcoinService != null) {
                bitcoinService.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
    }

    public static void ConnectToBlockChain(Context context) {
        Log.i(TAG, "ConnectToBlockChain");
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(getDefaultIntentWithAction(context, ACTION_CONNECT_TO_BLOCKCHAIN));
            } else {
                context.startService(getDefaultIntentWithAction(context, ACTION_CONNECT_TO_BLOCKCHAIN));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void ConnectToBlockChainWithRestoreWallet(Context context, String seed, long creationTime) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_CONNECT_TO_BLOCKCHAIN_WITH_RESTORE);
        intent.putExtra(EXTRA_SEED, seed);
        intent.putExtra(EXTRA_CREATION_TIME, creationTime);
        context.startService(intent);
    }

    public static void ConnectToBlockChainWithNewWallet(Context context) {
        context.startService(getDefaultIntentWithAction(context, ACTION_CONNECT_TO_BLOCKCHAIN_WITH_NEW));
    }

    public static void GetWallet(Context context, int reqCode) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_GET_WALLET);
        intent.putExtra(EXTRA_REQCODE, reqCode);
        context.startService(intent);
    }

    public static void FreshAddress(Context context) {
        context.startService(getDefaultIntentWithAction(context, ACTION_FRESH_ADDRESS));
    }

    public static void GetProgress(Context context ) {
        context.startService(getDefaultIntentWithAction(context, ACTION_GET_PROGRESS));
    }

    public static void Send(Context context, String address, long satoshis) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_SEND);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_SATOSHIS, satoshis);
        context.startService(intent);
    }

    public static void GetFromWIF(Context context, String wif) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_WIF);
        intent.putExtra(EXTRA_BIP38PRIKEY, wif);
        context.startService(intent);
    }

    public static void GetFromBIP38Key(Context context, String bip38PriKey, String password) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_BIP38);
        intent.putExtra(EXTRA_BIP38PRIKEY, bip38PriKey);
        intent.putExtra(EXTRA_BIP38PASSWORD, password);
        context.startService(intent);
    }

    public static void SweepBip38(Context context) {
        Intent intent = getDefaultIntentWithAction(context, ACTION_SWEEP_BIP38);
        context.startService(intent);
    }

    private static Intent getDefaultIntentWithAction(Context context, int action) {
        Intent intent = new Intent(context, BitcoinService.class);
        intent.putExtra(EXTRA_ACTION, action);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return mMessenger.getBinder();
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new BitcoinServiceHandler(this);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int falg, int startId) {

        if(intent != null) {
            int action = intent.getIntExtra(EXTRA_ACTION, ACTION_NOTHING);
            switch (action) {
                case ACTION_CONNECT_TO_BLOCKCHAIN_WITH_NEW:
                    connect(CREATE_TYPE_NEW);
                    break;
                case ACTION_CONNECT_TO_BLOCKCHAIN:
                    connect(CREATE_TYPE_NONE);
                    break;
                case ACTION_CONNECT_TO_BLOCKCHAIN_WITH_RESTORE:
                    connect(CREATE_TYPE_RESTORE, intent.getStringExtra(EXTRA_SEED), intent.getLongExtra(EXTRA_CREATION_TIME, -1));
                    break;
                case ACTION_GET_WALLET:
                    if(walletAppKit != null) {
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_WALLET, intent.getIntExtra(EXTRA_REQCODE, -1), 0, walletAppKit.wallet());
                    } else {
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_WALLET, intent.getIntExtra(EXTRA_REQCODE, -1), 0, null);
                    }
                    break;
                case ACTION_FRESH_ADDRESS:
                    if(walletAppKit != null) {
                        Address freshAddress = walletAppKit.wallet().freshReceiveAddress();
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_FRESH_ADDRESS, 0, 0, freshAddress);
                    } else {
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_FRESH_ADDRESS, 0, 0, null);
                    }
                    break;
                case ACTION_GET_PROGRESS:
                    if(walletAppKit != null) {
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS, (int)BitcoinService.pct, 0, null);
                    } else {
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS, 0, 0, null);
                    }
                    break;
//                case ACTION_DISCONNECT_TO_BLOCKCHAIN:
//                    disconnect();
//                    break;
                case ACTION_SEND:
                        send(intent.getStringExtra(EXTRA_ADDRESS), intent.getLongExtra(EXTRA_SATOSHIS, 0));
                    break;
//                case ACTION_CHECK_CONNECTED:
//                    check();
//                    break;

                case ACTION_BIP38:
                    loadBalanceFromBip38(intent.getStringExtra(EXTRA_BIP38PRIKEY), intent.getStringExtra(EXTRA_BIP38PASSWORD));
                    break;

                case ACTION_WIF:
                    loadBalanceFromWIF(intent.getStringExtra(EXTRA_BIP38PRIKEY));
                    break;

                case ACTION_SWEEP_BIP38:
                    doSweep();
                    break;

                default:
                    break;
            }
        }
        return START_STICKY;
    }

    private void connect(int createType) {
        connect(createType, null, -1);
    }
    /**
     * 블록체인 접속 시작
     * TODO
     * Network 미연결 상태에서는 이부분이 어떻게 처리되는지 확인 필요
     * 혹시 연결이 되면 그때 부터 Download되고 지갑자체는 정상적으로 진행 되는 것인지?
     * RCash 앱은 온라인 상태일때에만 허용 할것인지 정책 필요... Offline시 동작에 대해
     * Research 해보지 않으면 모른다.
     */
    @SuppressLint("StringFormatInvalid")
    private void connect(final int createType, final String mnemonicCode, final long creationTime) {
        Threading.USER_THREAD = handler::post;

        NetworkParameters parameters = null;

        if(walletAppKit == null) {
            DeterministicSeed seed = null;
            if(createType == CREATE_TYPE_RESTORE) {

                if(mnemonicCode != null) {
                    try {
                        seed = new DeterministicSeed(mnemonicCode, null, "", creationTime > 0 ? creationTime : MnemonicCode.BIP39_STANDARDISATION_TIME_SECS);
                    } catch (UnreadableWalletException e) {
                        e.printStackTrace();
                        seed = null;
                    }
                }
                if(seed == null) {
                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_CONNECT_RESULT, RESULT_FAIL, 0, null);
                    return;
                }
            }

            if(BuildConfig.MAINNET)
                parameters = MainNetParams.get();
            else
                parameters = TestNet3Params.get();

            File walletDir = getFilesDir();
            walletAppKit = new WalletAppKit(parameters, walletDir, WALLET_NAME) {
                @Override
                protected void onSetupCompleted() {
                    wallet().allowSpendingUnconfirmedTransactions();
                    wallet().addCoinsReceivedEventListener(((wallet1, tx, prevBalance, newBalance) -> {
//                        Log.i(TAG,"ReceivedEvent");
//                        Log.i(TAG, "\tMy Balance = " + wallet1.getBalance().toFriendlyString());
//                        Log.i(TAG, "\tPrev Balance = " + prevBalance.toFriendlyString());
//                        Log.i(TAG, "\tNew Balance = " + newBalance.toFriendlyString());
//                        getInfosFromTx(tx, wallet1);

                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_WALLET, REQCODE_CHECK, 0, wallet());
                        if(tx.getPurpose() == Transaction.Purpose.UNKNOWN) {

                            Coin amount = newBalance.minus(prevBalance);
                            if(amount.value > 0) {

                                String base58Addr = null;
                                List<TransactionInput> listIns = tx.getInputs();
                                for (TransactionInput input : listIns) {
                                    try {
                                        base58Addr = (input.getFromAddress() != null ? input.getFromAddress().toBase58() : null);
                                        if (base58Addr != null) {
                                            base58Addr = base58Addr.trim();
                                            break;
                                        }
                                    } catch (ScriptException e) {
                                    }
                                }

                                // 10분 이내 발생 건에 대해서만 Notification 발생
                                if (System.currentTimeMillis() - tx.getUpdateTime().getTime() <= 1000 * 60 * 10) {
                                    NotificationMgr.showNotification(getApplicationContext(), (int) System.currentTimeMillis(), getString(R.string.notification_coin_received_ttl), String.format(getString(R.string.notification_coin_received_desc), amount.toFriendlyString()));
                                }

                                // DB에 쓰기
                                DataBaseMgr dbMgr = DataBaseMgr.getInstance(getApplicationContext());
                                dbMgr.insertHistory(DataBaseConsts.TRANSACTIONTYPE_RECEIVE, tx.getHashAsString(), base58Addr, amount.getValue(), tx.getUpdateTime().getTime());
                            }
                        }
                    }));

                    wallet().addCoinsSentEventListener(((wallet1, tx, prevBalance, newBalance) -> {
//                        Log.i(TAG,"SentEvent");
//                        Log.i(TAG, "\tMy Balance = " + wallet1.getBalance().toFriendlyString());
//                        Log.i(TAG, "\tPrev Balance = " + prevBalance.toFriendlyString());
//                        Log.i(TAG, "\tNew Balance = " + newBalance.toFriendlyString());
//                        Log.i(TAG, "\tSent : " + prevBalance.minus(newBalance).minus(tx.getFee()).toFriendlyString());
//                        getInfosFromTx(tx, wallet1);

                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_WALLET, REQCODE_CHECK, 0, wallet());

                        Coin amount = prevBalance.minus(newBalance).minus(tx.getFee());
                        if(amount.value > 0) {
                            String base58Addr = null;
                            List<TransactionOutput> listOuts = tx.getOutputs();
                            for(TransactionOutput output : listOuts) {
                                try {
                                    if(output != null) {
                                        Address address = output.getAddressFromP2PKHScript(wallet1.getNetworkParameters());
                                        if(address != null) {
                                            base58Addr = address.toBase58();
                                        }

                                        if(base58Addr == null) {
                                            address = output.getAddressFromP2SH(wallet1.getNetworkParameters());
                                            if(address != null) {
                                                base58Addr = address.toBase58();
                                            }
                                        }
                                    }
                                    if(base58Addr != null)
                                        break;
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (System.currentTimeMillis() - tx.getUpdateTime().getTime() <= 1000 * 60 * 10) {
                                NotificationMgr.showNotification(getApplicationContext(), (int) System.currentTimeMillis(), getString(R.string.notification_coin_sent_ttl), String.format(getString(R.string.notification_coin_sent_desc), amount.toFriendlyString()));
                            }

                            // DB에 쓰기
                            DataBaseMgr dbMgr = DataBaseMgr.getInstance(getApplicationContext());
                            dbMgr.insertHistory(DataBaseConsts.TRANSACTIONTYPE_SEND, tx.getHashAsString(), base58Addr, amount.getValue(), tx.getUpdateTime().getTime());
                        }
                    }));

                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_CONNECT_RESULT, RESULT_SUCCESS,  0, wallet());
                }
            };

            walletAppKit.setDownloadListener(new DownloadProgressTracker() {
                @Override
                protected void progress(double pct, int blockSoFar, Date date) {
                    super.progress(pct, blockSoFar, date);
                    BitcoinService.pct = pct;
                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS, (int)pct, blockSoFar, null);
                }
            });

            if(seed != null)
                walletAppKit.restoreWalletFromSeed(seed);
            walletAppKit.setBlockingStartup(false);
            walletAppKit.setAutoSave(true);
            walletAppKit.setUserAgent(getString(R.string.app_name), BuildConfig.VERSION_NAME);
            walletAppKit.startAsync();
        } else {
            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_CONNECT_RESULT, RESULT_SUCCESS, 0, walletAppKit.wallet());
        }
    }

    private boolean doSweep = false;
    private void doSweep() {
        if (ecKeyBip38 != null && arrUTXO.size() > 0) {
            Wallet wallet = new Wallet(MainNetParams.get());
            wallet.importKey(ecKeyBip38);
            wallet.allowSpendingUnconfirmedTransactions();

            Address destination = walletAppKit.wallet().currentReceiveAddress();

            long satoshis = 0;
            for(DogerUTXO utxo : arrUTXO) {
                satoshis += utxo.satoshis;
                //if(utxo.scriptPubKey.equals("null"))
                    wallet.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.UNSPENT ,utxo.transaction));
                //else
                //    wallet.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.PENDING, utxo.transaction));
            }

            Coin coinToSend = Coin.valueOf(satoshis);
            boolean foundAmount = false;
            SendRequest request = null;
            while(!foundAmount) {
                try {
                    request = SendRequest.to(destination, coinToSend);
                    wallet.completeTx(request);

                    // 여기로 오면 일단 전송가능... 잔액이 남는지 한번더 확인하자
                    long fee = request.tx.getFee().value;
                    long coin = coinToSend.value;

                    if(fee + coin == satoshis) {
                        foundAmount = true;
                    } else {
                        coinToSend = Coin.valueOf(coinToSend.value + 1);
                    }
                } catch(InsufficientMoneyException e) {
                    e.printStackTrace();
                    String eMsg = e.getMessage().trim();
                    eMsg = eMsg.substring(eMsg.indexOf("missing") + 7).trim();
                    eMsg = eMsg.substring(0, eMsg.lastIndexOf(" ")).trim();

                    long insufficientMoney = (long)(Double.parseDouble(eMsg) * 100000000L);

                    if(insufficientMoney < coinToSend.value)
                        coinToSend = Coin.valueOf(coinToSend.value - insufficientMoney);
                    else
                        break;
                }
            }

            if(foundAmount) {
                doSweep = true;
                wallet.commitTx(request.tx);
                // 같은 앱내에서 이동이라서 그런지 Purpose.USER_PAYMENT 로 지정되는데 이경우 제대로 스윕되지 않고 돈이 날라간다???!? 강제로 UNKNOWN으로 지정
                request.tx.setPurpose(Transaction.Purpose.UNKNOWN);

                walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();


                doSweep = false;
                ecKeyBip38 = null;
                arrUTXO.clear();
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_SWEEP_SUCCESS, 0, null);
            } else {
                doSweep = false;
                ecKeyBip38 = null;
                arrUTXO.clear();
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_UNKNOWN_ERROR, 0, null);
            }

        } else {
            // There is no wallet or UTXOs
            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_UNKNOWN_ERROR, 0, null);
        }
    }

    private ArrayList<DogerUTXO> arrUTXO = new ArrayList<>();

    private void getAllBalances(String base58Addr) {
        arrUTXO.clear();
        String checkBalance = "https://blockdozer.com/api/addr/" + base58Addr +"/utxo";
        Log.i(TAG, "checkBal:" + checkBalance);
        new NetworkMgr().execute(getApplicationContext(), checkBalance, NetworkMgr.METHOD_GET, null, null, new NetworkMgr.NetworkListener() {
            @Override
            public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
                if(resCode == HttpsURLConnection.HTTP_OK) {
                    try {
                        Log.i(TAG, "retValue:" + retValue);
                        JSONArray jsonArray = new JSONArray(retValue);
                        Log.i(TAG,"lenght:" + jsonArray.length());
                        if(jsonArray.length() == 0) {
                            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_SUCCESS, 0, arrUTXO);
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObj = jsonArray.getJSONObject(i);

                                DogerUTXO dogerUTXO = new DogerUTXO();

                                dogerUTXO.amount = jsonObj.getDouble("amount");
                                dogerUTXO.height = jsonObj.getLong("height");
                                dogerUTXO.satoshis = jsonObj.getLong("satoshis");
                                dogerUTXO.address = jsonObj.getString("address");
                                dogerUTXO.scriptPubKey = jsonObj.getString("scriptPubKey");
                                dogerUTXO.txid = jsonObj.getString("txid");
                                dogerUTXO.confirmations = jsonObj.getInt("confirmations");
                                dogerUTXO.vout = jsonObj.getInt("vout");

                                dogerUTXO.print(TAG);

                                /**
                                 * TODO
                                 * scriptPubKey 가 이미 존재하는 경우에
                                 * 지갑이 잔액으로 인식을 못함.
                                 * 이부분 처리 방법을 알기 전까지 해당 트랜잭션은 무효로...
                                 */
//                                if(dogerUTXO.scriptPubKey.equals("null")) {
                                    arrUTXO.add(dogerUTXO);

                                    String url = "https://blockdozer.com/api/rawtx/" + dogerUTXO.txid;
                                    Log.i(TAG, "rawTx:" + url);
                                    new NetworkMgr().execute(getApplicationContext(),
                                            url,
                                            NetworkMgr.METHOD_GET,
                                            null,
                                            null,
                                            new NetworkMgr.NetworkListener() {
                                                @Override
                                                public void onNetworkComplete(int resCode, String retValue, Map<String, List<String>> headerField) {
                                                    if (resCode == HttpsURLConnection.HTTP_OK) {
                                                        try {
                                                            Log.i(TAG, "tx:" + retValue);
                                                            JSONObject obj = new JSONObject(retValue);
                                                            String rawTx = obj.getString("rawtx");
                                                            Transaction tx = new Transaction(MainNetParams.get(), Utils.HEX.decode(rawTx));

                                                            List<TransactionInput> listInputs = tx.getInputs();
                                                            List<TransactionOutput> listOutputs = tx.getOutputs();

                                                            for(TransactionInput input : listInputs) {
                                                                input.getScriptSig();
                                                            }
                                                            for(TransactionOutput output : listOutputs) {
                                                                output.getScriptPubKey();
                                                            }

                                                            // find matched utxo
                                                            for (DogerUTXO utxo : arrUTXO) {
                                                                if (utxo.txid.equals(tx.getHashAsString())) {
                                                                    utxo.transaction = tx;
                                                                    getInfosFromTx(utxo.transaction);
                                                                }
                                                            }

                                                            // check completed
                                                            for (int idx = 0; idx < arrUTXO.size(); idx++) {
                                                                if (arrUTXO.get(idx).transaction == null)
                                                                    return;
                                                            }

                                                            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_SUCCESS, 0, arrUTXO);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_PASSWORD_ERROR, 0, null);
                                                        }
                                                    }
                                                }
                                            });
//                                }
                            }
                        }

                        // check completed
                        if(arrUTXO.size() == 0) {
                            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_UNKNOWN_ERROR, 0, arrUTXO);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_PASSWORD_ERROR, 0, null);
                    }
                } else {
                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_UNKNOWN_ERROR, 0, arrUTXO);
                }
            }
        });
    }

    private void loadBalanceFromWIF(String wif) {
        doSweep = false;
        new Thread(()->{
            try {
                NetworkParameters parameters = MainNetParams.get();
                DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(parameters, wif);
                ecKeyBip38 = dumpedPrivateKey.getKey();
                getAllBalances(ecKeyBip38.toAddress(parameters).toBase58());
            } catch(Exception e) {
                e.printStackTrace();
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_ADDRESS_FORMAT_ERROR, 0, null);
            }

        }).start();
    }

    private void loadBalanceFromBip38(String bip38prikey, String password) {
        doSweep = false;
        new Thread(()->{
            NetworkParameters params = MainNetParams.get();
            try {
                BIP38PrivateKey bip38PrivateKey = BIP38PrivateKey.fromBase58(params, bip38prikey);
                ecKeyBip38 = bip38PrivateKey.decrypt(password);
                getAllBalances(ecKeyBip38.toAddress(params).toBase58());
            } catch (AddressFormatException e) {
                e.printStackTrace();
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_ADDRESS_FORMAT_ERROR, 0, null);
            } catch (BIP38PrivateKey.BadPassphraseException e) {
                e.printStackTrace();
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_BIP38_RESULT, RESULT_PASSWORD_ERROR, 0, null);
            }
        }).start();
    }

    private void getInfosFromTx(Transaction tx) {
        Log.i(TAG, "\tupdatedTime: " + tx.getUpdateTime().getTime() + "=>" + tx.getUpdateTime().toString());
        Log.i(TAG, "\tPurpose:" + tx.getPurpose());
        Log.i(TAG, "\tFee:" + (tx.getFee() != null ? tx.getFee().toString() : "null"));
        Log.i(TAG, "\tMemo:" + (tx.getMemo() != null ? tx.getMemo().toString()  : "null"));
        Log.i(TAG, "\tHash:" + tx.getHashAsString());

        List<TransactionInput> listInputs = tx.getInputs();
        List<TransactionOutput> listOutputs = tx.getOutputs();

        for(int i = 0; i < listInputs.size(); i++) {
            try {
                Log.i(TAG, "\t\tInput addr" + i + ":" + listInputs.get(i).getFromAddress());
            } catch (ScriptException e) {
                Log.i(TAG, "\t\tInput " + i + ": no address... " + e.getMessage());
            }
        }


        for(int i = 0; i < listOutputs.size(); i++) {
            try {
                Log.i(TAG, "\t\tOutput addrP2PKH" + i + ":" + listOutputs.get(i).getAddressFromP2PKHScript(MainNetParams.get()));
                Log.i(TAG, "\t\tOutput addrP2SH" + i + ":" + listOutputs.get(i).getAddressFromP2SH(MainNetParams.get()));
            } catch (ScriptException e) {
                Log.i(TAG, "\t\tInput " + i + ": no address... " + e.getMessage());
            }
            Log.i(TAG, "\t\tOutput parentHash" + i + ":" + (listOutputs.get(i).getParentTransactionHash() != null ? listOutputs.get(i).getParentTransactionHash().toString() : "null"));
        }
    }

    private void send(String address, long satoshis) {
        if(walletAppKit != null) {
            if(walletAppKit.wallet().getBalance().isLessThan(Coin.valueOf(satoshis))){
                BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_SEND_RESULT, RESULT_FAIL, 0, null);
            } else {

                Coin coinToSend = Coin.valueOf(satoshis);
                boolean foundAmont = false;
                boolean sendAll = false;
                SendRequest request = null;
                while(!foundAmont) {
                    try {
                        request = SendRequest.to(Address.fromBase58(walletAppKit.wallet().getNetworkParameters(), address), coinToSend);
                        walletAppKit.wallet().completeTx(request);

                        if(sendAll) {
                            long fee = request.tx.getFee().value;
                            long coin = coinToSend.value;
                            if (fee + coin == walletAppKit.wallet().getBalance().value) {
                                foundAmont = true;
                            } else {
                                coinToSend = Coin.valueOf(coinToSend.value + 1);
                            }
                        } else {
                            foundAmont = true;
                        }

                    } catch (InsufficientMoneyException e) {
                        e.printStackTrace();
                        sendAll = true;
                        String eMsg = e.getMessage().trim();
                        eMsg = eMsg.substring(eMsg.indexOf("missing") + 7).trim();
                        eMsg = eMsg.substring(0, eMsg.lastIndexOf(" ")).trim();

                        long insufficientMoney = (long)(Double.parseDouble(eMsg) * 100000000L);

                        if(insufficientMoney < coinToSend.value) {
                            coinToSend = Coin.valueOf(coinToSend.value - insufficientMoney);
                        } else {
                            break;
                        }
                    }
                }

                if(foundAmont) {
                    walletAppKit.wallet().commitTx(request.tx);
                    walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_SEND_RESULT, RESULT_SUCCESS, 0, walletAppKit.wallet());
                } else {
                    BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_SEND_RESULT, RESULT_FAIL, 0, null);
                }
            }
        }
    }

    /**
     * 블록체인 접속 종료
     */
    private void disconnect() {
        synchronized (TAG) {
            if (walletAppKit != null) {
                walletAppKit.stopAsync();
                walletAppKit = null;
            }
        }
        BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_DISCONNECT_RESULT, RESULT_SUCCESS, 0, null);
    }

    /**
     * 현재 상황 체크
     */
    private void check() {
        if(walletAppKit != null && walletAppKit.isRunning()) {
            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_CHECK, RESULT_CONNECTED, 0, null);
        } else {
            BaseActivity.broadcastMessage(BROADCAST_BITCOINSERVICE_CHECK, RESULT_DISCONNECTED, 0, null);
        }
    }
}
