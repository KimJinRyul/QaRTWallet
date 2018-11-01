package jrkim.rcash.network;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkMgr implements Runnable {

    private final static String TAG = "RCash_Network";
    public interface NetworkListener {
        void onNetworkComplete(int resCode, String retValue, Map<String ,List<String>> headerField);
    }

    private final static int TIMEOUT = 6000;
    public final static String METHOD_GET = "GET";          //GET
    public final static String METHOD_POST = "POST";        //POST

    private Context context;
    private String strUrl;
    private String method;
    private String request;
    private Map<String, List<String>> cookies;
    private NetworkListener listener;

    private final static String USERAGENT = "Mozilla/5.0 ( compatible ) ";
    private final static String ACCEPT = "*/*";

    private static SSLContext sslContext = null;
    private static SSLEngine sslEngine = null;
    private static SSLSocketFactory sslSocketFactory = null;
    public static KeyManager[] kms = null;

    private static X509TrustManager x509TrustManager = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {;}
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {;}
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static TrustManager[] localTrustManagers = new TrustManager[] { x509TrustManager };

    private final static HostnameVerifier DONT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public void execute(Context context,
                        String url,
                        String method,
                        String request,
                        Map<String, List<String>> cookies,
                        NetworkListener listener) {
        this.context = context;
        this.strUrl = url;
        this.method = method;
        this.request = request;
        this.cookies = cookies;
        this.listener = listener;

        new Thread(this).start();
    }

    private void clearSSL() {
        sslContext = null;
        sslSocketFactory = null;
        sslEngine = null;
    }

    private String makeCookieString(Map<String, List<String>> header) {
        StringBuffer cookie = new StringBuffer();
        Iterator<String> it = header.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key != null && key.equals("Set-Cookie")) {
                List<String> values = header.get(key);
                if(values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        String[] param = values.get(i).split(";");
                        if(param != null) {
                            cookie.append(param[0] + "; ");
                        }
                    }
                }
            }
        }
        return cookie.toString();
    }

    @Override
    public void run() {
        int resCode = -1;
        String retValue = "";
        Map<String, List<String>> headerField = null;

        KeyManagerFactory keyManagerFactory = null;
        KeyStore keyStore = null;

        HttpsURLConnection httpsConn = null;
        HttpURLConnection httpConn = null;

        try {
            if(strUrl.startsWith("https")) {
                httpsConn = (HttpsURLConnection)new URL(strUrl).openConnection();

                String temp = strUrl.substring(8);
                int idx = temp.indexOf("/");
                if(idx > 0) {
                    temp = temp.substring(0, idx);
                }
                Pattern pattern = Pattern.compile(":[0-9]+");
                Matcher matcher = pattern.matcher(temp);
                int port = 443;
                String hostName = "";
                if(matcher.find()) {
                    port = Integer.parseInt(matcher.group(0).substring(1));
                    hostName = "https://" + temp.substring(0, temp.indexOf(":"));
                } else {
                    hostName = "https://" + temp;
                }

                synchronized (TAG) {
                    if(sslContext == null || sslEngine == null || sslSocketFactory == null ||
                            !sslEngine.getPeerHost().equalsIgnoreCase(hostName) ||
                            sslEngine.getPeerPort() != port) {
                        clearSSL();


                        sslContext = SSLContext.getInstance("TLSv1.2");
                        sslContext.init(kms, localTrustManagers, new SecureRandom());

                        sslEngine = sslContext.createSSLEngine(hostName, port);

                        sslEngine.setUseClientMode(true);
                        sslEngine.setEnableSessionCreation(true);
                        sslSocketFactory = sslContext.getSocketFactory();
                    }
                    httpsConn.setSSLSocketFactory(sslSocketFactory);
                }

                httpsConn.setRequestMethod(method);
                httpsConn.setDoInput(true);
                httpsConn.setConnectTimeout(TIMEOUT);
                httpsConn.setReadTimeout(TIMEOUT);
                httpsConn.setHostnameVerifier(DONT_VERIFY);
                httpsConn.setRequestProperty("Accept-Charset", "UTF-8");
                httpsConn.addRequestProperty("Connection", "keep-alive");
                if(cookies != null) {
                    httpsConn.setRequestProperty("Cookie", makeCookieString(cookies));
                }

                if(method.equals(METHOD_POST) && request != null) {
                    httpsConn.setDoOutput(true);
                    httpsConn.setRequestProperty("Content-length", String.valueOf(request.getBytes().length));
                    httpsConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpsConn.setRequestProperty("User-Agent", USERAGENT);
                    httpsConn.setRequestProperty("Accept", ACCEPT);

                    OutputStream os = httpsConn.getOutputStream();
                    if(os != null) {
                        os.write(request.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                    }
                }
                httpsConn.connect();
                resCode = httpsConn.getResponseCode();
                if(resCode == HttpsURLConnection.HTTP_OK) {
                    headerField = httpsConn.getHeaderFields();
                    try {
                        InputStream is = httpsConn.getInputStream();
                        if (is != null) {
                            if ("gzip".equals(httpsConn.getContentEncoding())) {
                                is = new GZIPInputStream(is);
                            }
                        }

                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line = br.readLine()) != null) {
                            retValue += line + "\n";
                        }
                        retValue = retValue.trim();
                        br.close();

                    } catch (IOException e) {
                        httpsConn.getErrorStream();
                        e.printStackTrace();
                    }
                }
                httpsConn.disconnect();

            } else {
                httpConn = (HttpURLConnection) new URL(strUrl).openConnection();

                httpConn.setRequestMethod(method);
                httpConn.setDoInput(true);
                httpConn.setConnectTimeout(TIMEOUT);
                httpConn.setReadTimeout(TIMEOUT);
                httpConn.setRequestProperty("Accept-Charset", "UTF-8");

                if (cookies != null) {
                    httpConn.setRequestProperty("Cookie", makeCookieString(cookies));
                }

                if (method.equals(METHOD_POST) && request != null) {
                    httpConn.setDoOutput(true);
                    httpConn.setRequestProperty("Content-length", String.valueOf(request.getBytes().length));
                    httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpConn.setRequestProperty("User-Agent", USERAGENT);
                    httpConn.setRequestProperty("Accept", ACCEPT);

                    OutputStream os = httpConn.getOutputStream();
                    if (os != null) {
                        os.write(request.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                    }
                }

                httpConn.connect();
                resCode = httpConn.getResponseCode();
                if (resCode == HttpsURLConnection.HTTP_OK) {
                    headerField = httpConn.getHeaderFields();

                    InputStream is = httpConn.getInputStream();
                    if (is != null) {
                        if ("gzip".equals(httpConn.getContentEncoding())) {
                            is = new GZIPInputStream(is);
                        }
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        retValue += line + "\n";
                    }
                    retValue = retValue.trim();
                    br.close();
                }
                httpConn.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        if(listener != null) {
            listener.onNetworkComplete(resCode, retValue, headerField);
        }
    }
}
