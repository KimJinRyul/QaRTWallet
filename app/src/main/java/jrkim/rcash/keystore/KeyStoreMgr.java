package jrkim.rcash.keystore;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;


@SuppressWarnings("TryWithIdenticalCatches")
public class KeyStoreMgr {
    private static final String TAG = "RCash_KeyStore";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String TAG_KEY_SECRET = "rcash_secret";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String UTF8 = "UTF-8";

    private static KeyStoreMgr instance = null;
    private KeyStore keyStore = null;

    private KeyStoreMgr() {
    }

    public static KeyStoreMgr getInstance() {
        if(instance == null) {
            synchronized (TAG) {
                if(instance == null) {
                    instance = new KeyStoreMgr();
                }
            }
        }
        return instance;
    }

    private KeyStore getKeyStore() {
        if(keyStore == null) {
            synchronized (TAG) {
                if(keyStore == null) {
                    try {
                        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                        keyStore.load(null);
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return keyStore;
    }

    public KeyPair createSecreteKey(Context context) {
        try {
            if(!isKeyExist(TAG_KEY_SECRET)) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
                kpg.initialize(new KeyGenParameterSpec.Builder(TAG_KEY_SECRET, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .build());

                return kpg.generateKeyPair();
            } else {
                return getKeyPair(TAG_KEY_SECRET);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA 텍스트 암호화
     * @param context
     * @param plain
     * @return Base64 인코딩된 암호문
     */
    public String encryptText(Context context, final String plain) {
        try {
            KeyPair keyPair = createSecreteKey(context);
            if(keyPair != null) {
                final Cipher cipher = Cipher.getInstance(TRANSFORMATION);

                cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                CipherOutputStream cos = new CipherOutputStream(baos, cipher);
                cos.write(plain.getBytes(UTF8));
                cos.close();
                return Base64.encodeToString(baos.toByteArray(), Base64.URL_SAFE | Base64.NO_WRAP);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA 복호화
     * @param context
     * @param encrypted
     * @return 복호화된 문자열
     */
    public String decryptText(Context context, final String encrypted) {
        try {
            KeyPair keyPair = getKeyPair(TAG_KEY_SECRET);
            if(keyPair != null) {
                final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

                ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(encrypted, Base64.URL_SAFE | Base64.NO_WRAP));
                CipherInputStream cis = new CipherInputStream(bais, cipher);
                ArrayList<Byte> output = new ArrayList<>();
                int nextByte;
                while((nextByte = cis.read()) != -1) {
                    output.add((byte)nextByte);
                }

                byte[] decrypted = new byte[output.size()];
                for(int i = 0; i < output.size(); i++) {
                    decrypted[i] = output.get(i);
                }
                return new String(decrypted, 0, decrypted.length, UTF8);
            }
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean isKeyExist(String alias) {
        try {
            KeyStore keystore = getKeyStore();
            if(keystore != null && keystore.containsAlias(alias)) {
                return true;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    private KeyPair getKeyPair(final String alias) {
        try {
            KeyStore keyStore = getKeyStore();
            if(isKeyExist(alias)) {
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
                if(keyEntry != null) {
                    return new KeyPair(keyEntry.getCertificate().getPublicKey(), keyEntry.getPrivateKey());
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteKey(String alias) {
        try {
            KeyStore keyStore = getKeyStore();
            if(isKeyExist(alias)) {
                keyStore.deleteEntry(alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}
