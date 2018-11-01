package jrkim.rcash.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;


public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private final static String TAG = "RCash_Fingerprint";
    public static boolean FINGERPRINT_USINGNOW = false;

    public interface FingerprintListener {
        void onAuthenticationError(int errId, CharSequence errString);
        void onAuthenticationHelp(int helpId, CharSequence helpString);
        void onAuthenticationFailed();
        void onAuthenticationSucceded(FingerprintManager.AuthenticationResult result);
    }

    private FingerprintListener listener;

    public FingerprintHandler(FingerprintListener listener) {
        this.listener = listener;
    }

    public void startAuth(FingerprintManager.CryptoObject cryptoObject, FingerprintManager manager, CancellationSignal cancellationSignal) {
        FINGERPRINT_USINGNOW = true;
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if(listener != null) listener.onAuthenticationError(errMsgId, errString);
        FINGERPRINT_USINGNOW = false;
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if(listener != null) listener.onAuthenticationHelp(helpMsgId, helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        if(listener != null) listener.onAuthenticationFailed();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if(listener != null) listener.onAuthenticationSucceded(result);
        FINGERPRINT_USINGNOW = false;
    }
}
