package com.erp.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Base64;
import android.util.Log;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends Activity {

    private static final String TAG = "ERPBilling";
    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow network on main thread for simplicity (printer is local LAN)
        StrictMode.ThreadPolicy policy =
            new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);          // localStorage
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Inject the printer bridge — JS can call window.AndroidPrinter.printBase64(...)
        webView.addJavascriptInterface(new PrinterBridge(), "AndroidPrinter");

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/erp-billing.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  PRINTER BRIDGE  —  called from JavaScript
    //  Static inner class to prevent Activity memory leak
    // ─────────────────────────────────────────────────────────────
    static class PrinterBridge {

        /**
         * Called from JS:  AndroidPrinter.printBase64(ip, port, base64Data)
         * Returns "OK" on success, or an error message string.
         *
         * The JS side converts the ESC/POS string to base64 before calling this,
         * so special characters survive the JS→Java bridge safely.
         */
        @JavascriptInterface
        public String printBase64(String ip, int port, String base64Data) {
            try {
                // Decode base64 back to raw ESC/POS bytes
                byte[] data = Base64.decode(base64Data, Base64.DEFAULT);

                Log.d(TAG, "Connecting to " + ip + ":" + port + " (" + data.length + " bytes)");

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000); // 5s timeout
                socket.setSoTimeout(5000);

                OutputStream out = socket.getOutputStream();
                out.write(data);
                out.flush();

                // Small delay to let printer process before closing
                Thread.sleep(300);

                out.close();
                socket.close();

                Log.d(TAG, "Print sent successfully");
                return "OK";

            } catch (Exception e) {
                Log.e(TAG, "Print failed: " + e.getMessage(), e);
                return e.getMessage() != null ? e.getMessage() : "Unknown error";
            }
        }

        /**
         * Called from JS: AndroidPrinter.testConnection(ip, port)
         * Returns "OK" or error message.
         */
        @JavascriptInterface
        public String testConnection(String ip, int port) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 4000);
                socket.close();
                return "OK";
            } catch (Exception e) {
                return e.getMessage() != null ? e.getMessage() : "Cannot connect";
            }
        }
    }
}
