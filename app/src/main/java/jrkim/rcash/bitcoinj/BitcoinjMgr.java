package jrkim.rcash.bitcoinj;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.util.Date;
import java.io.File;
import java.util.List;

import jrkim.rcash.BuildConfig;
import jrkim.rcash.R;

public class BitcoinjMgr {
    private final static String TAG = "RCash_Bitcoinj";

    public final static String WALLET_NAME = "RWallet";

    private static BitcoinjMgr instance = null;
    private Context context = null;
    public WalletAppKit walletAppKit = null;
    public NetworkParameters parameters = null;

    private BitcoinjMgr(Context context) {
        this.context = context;
    }

    public static BitcoinjMgr getInstance(Context context) {
        if(instance == null) {
            synchronized (TAG) {
                if (instance == null) {
                    instance = new BitcoinjMgr(context);
                }
            }
        }
        return instance;
    }

    public void connect() {
        if(BuildConfig.MAINNET) {
            Log.i(TAG, "connect-MAINNET");
            parameters = MainNetParams.get();
        } else {
            Log.i(TAG, "connect-TESTNET");
            parameters = TestNet3Params.get();
        }

        File walletDir = context.getFilesDir();
        walletAppKit = new WalletAppKit(parameters, walletDir, WALLET_NAME) {
            /**
             * startAsync 가 호출되고 난후 실제 Peer에 접속해서 BlockChain을 다운로드 받기 전에
             * Wallet이 준비가 완료되면 호출된다.
             * 여기서 wallet의 초기 설정을 구성할 수 있다.
             */
            @Override
            protected void onSetupCompleted() {

                Log.i(TAG, "walletAppKit.onSetupCompleted");
                List<ECKey> importedKeys = wallet().getImportedKeys();

                Log.i(TAG, "importedKey Size :" + importedKeys.size());
                if(importedKeys.size() < 1) {
                    Log.i(TAG, "there is no ECkeys yet... stopAsync ");
                    //import new one
                    //wallet().importKey(new ECKey());
                    //Log.i(TAG, "MyAddress = " + wallet().freshReceiveAddress());
                    //walletAppKit.stopAsync();

                } else {
                    Log.i(TAG, "there is ECKey.... setupWalletListener...");
                    Log.i(TAG, "MyAddress = " + wallet().currentReceiveAddress());
                    Log.i(TAG, "allowSpendingUnconfirmedTransaction");
                    wallet().allowSpendingUnconfirmedTransactions();

                    Log.i(TAG, "setupWalletListener");
                    setupWalletListener(wallet());
                }
                //Log.i(TAG, "My Address = " + wallet().freshReceiveAddress());
            }
        };

        Log.i(TAG, "setDownloadListener");
        walletAppKit.setDownloadListener(new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                Log.i(TAG, "progress pct: " + pct + ", blocksSoFar:" + blocksSoFar + ", Date:" + date.toString());

                int percentage = (int)pct;
                Log.i(TAG, "DownloadPercentage:" + percentage);
            }
        });

        /**
         * true : 네트워크가 시작되고 피어연결이 설정 / 블록체인이 동기화 될때까지 서비스의 시작이 완료된 것으로 간주하지 않는다. 잠재적으로 오랜시간이 걸릴수 있다.
         * false : 네트워크가 시작되고 피어연결/ 블록체인동기화가 백그라운드에서 계속되면 바로 시작이 완료된 것으로 간주한다.
         */
        Log.i(TAG, "setBlockingStartup");
        walletAppKit.setBlockingStartup(false);

        /**
         * 변경사항이 있을때 자동으로 저장한다.
         */
        Log.i(TAG, "setAuthSave");
        walletAppKit.setAutoSave(true);

        /**
         * 앱의 이름과 버전을 입력
         */
        walletAppKit.setUserAgent(context.getString(R.string.app_name), BuildConfig.VERSION_NAME);

        Log.i(TAG, "startAsync");
        walletAppKit.startAsync();

    }

    private void setupWalletListener(Wallet wallet) {
        Log.i(TAG, "setupWalletListener");
        wallet.addCoinsReceivedEventListener(((wallet1, tx, prevBalance, newBalance) -> {
            Log.i(TAG, "ReceivedEventListener");
            Log.i(TAG, "My Ballance = " + wallet.getBalance().toFriendlyString());

            if(tx.getPurpose() == Transaction.Purpose.UNKNOWN) {
                Log.i(TAG, "Receive " + newBalance.minus(prevBalance).toFriendlyString());
            }
        }));

        wallet.addCoinsSentEventListener(((wallet1, tx, prevBalance, newBalance) -> {
            Log.i(TAG, "SentEventListener");
            Log.i(TAG, "My Ballance = " + wallet.getBalance().toFriendlyString());
            Toast.makeText(context, "Sent " + prevBalance.minus(newBalance).minus(tx.getFee()).toFriendlyString(), Toast.LENGTH_SHORT).show();
        }));
    }


    /**
     * 블록체인 접속 종료
     */
    private void unsubscribe() {
        //
    }

    /**
     * 새 받을 주소 얻기
     */
    private void refresh() {
        //String myAddress = walletAppKit.wallet().freshReceiveAddress().toCashAddress();
        //Log.i(TAG, "new Address : " + myAddress);
    }

    /**
     * 보낼 상대 선택
     */
    private void pickRecipient() {
        // 받을 사람 주소 삭제
        // startScanQR();
    }

    /**
     * 보내기
     */
    private void send() {
        String recipientAddress = null; // QR코드, 직접 입력 등등으로 확인된 상대방 주소
        String amount = null;             // 보낼 수량

        if(recipientAddress == null || recipientAddress.length() == 0) {
            Toast.makeText(context, "전송할 상대를 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(amount) || Double.parseDouble(amount) <= 0) {
            Toast.makeText(context, "전송할 수량을 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if(walletAppKit.wallet().getBalance().isLessThan(Coin.parseCoin(amount))) {
            Toast.makeText(context, "코인이 충분치 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


       // SendRequest request = SendRequest.to(Address.fromString(parameters, recipientAddress), Coin.parseCoin(amount));
//        try {
//            walletAppKit.wallet().completeTx(request);
//            walletAppKit.wallet().commitTx(request.tx);
//            walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
//        } catch (InsufficientMoneyException e) {
//            e.printStackTrace();
//            Log.e(TAG, "전송 예외 발생 : " + e.getMessage());
//        }
    }

    /**
     * 현재 지갑주소 얻기
     */
    private void getInfo() {
//        Log.i(TAG, walletAppKit.wallet().currentReceiveAddress().toCashAddress());
    }

}
