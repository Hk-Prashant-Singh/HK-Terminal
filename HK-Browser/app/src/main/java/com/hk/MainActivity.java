package com.hk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

// HK-CORE: AUTH & GOOGLE SERVICES
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    // ⚡ HK-SYSTEM CONSTANTS
    private static final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SELECT_FILE = 100;

    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private GoogleSignInClient mGoogleSignInClient;
    public ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 🛡️ STEALTH UI: HARDWARE ACCELERATED & FULLSCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.parseColor("#050505"));
        setContentView(mainLayout);

        initializeWebView(mainLayout);
        setupEliteLoader(mainLayout);
        setupGoogleAuth();
        setupNetworkMonitoring();

        // 🛰️ FIRING TARGET
        hkView.loadUrl(TARGET_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView(RelativeLayout layout) {
        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        WebSettings s = hkView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setGeolocationEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // 🔗 THE BRIDGE: NATIVE TO WEB HANDSHAKE
        hkView.addJavascriptInterface(new HkWebBridge(), "AndroidInterface");

        // 🛡️ USER AGENT: PIXEL 8 STEALTH PRO
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);

        layout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));
        
        setupWebViewClients();
        setupDownloadManager();
    }

    private void setupGoogleAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupWebViewClients() {
        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> fpc, FileChooserParams fcp) {
                if (uploadMessage != null) uploadMessage.onReceiveValue(null);
                uploadMessage = fpc;
                startActivityForResult(fcp.createIntent(), REQUEST_SELECT_FILE);
                return true;
            }
        });

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 🛑 AUTH INTERCEPT: Catch Google login attempts
                if (url.contains("accounts.google.com") || url.contains("gsi/")) {
                    triggerNativeLogin();
                    return true;
                }
                if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                return false; 
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("HK-MALL: ENCRYPTING SESSION...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showNoInternetPopUp();
            }
        });
    }

    private void triggerNativeLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 🔑 AUTH SUCCESS: TRANSMIT TOKEN TO WEB-CORE
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    hkView.evaluateJavascript("javascript:if(window.receiveAndroidToken){ window.receiveAndroidToken('" + idToken + "'); }", null);
                    Toast.makeText(MainActivity.this, "TECH WIZARD: SYSTEM BYPASS GRANTED!", Toast.LENGTH_SHORT).show();
                }, 500);

            } catch (ApiException e) {
                Log.e("HK_ERROR", "Auth Failed Code: " + e.getStatusCode());
                Toast.makeText(this, "HK-SECURITY: Breach Detected! SHA-1 Error.", Toast.LENGTH_LONG).show();
            }
        }

        // 📂 FILE SELECTOR
        if (requestCode == REQUEST_SELECT_FILE && uploadMessage != null) {
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

    // --- HK-BRIDGE: ALPHA COMMUNICATION ---
    private class HkWebBridge {
        @JavascriptInterface
        public void startGoogleLogin() {
            runOnUiThread(() -> triggerNativeLogin());
        }
        
        @JavascriptInterface
        public void showHkToast(String msg) {
            Toast.makeText(MainActivity.this, "HK: " + msg, Toast.LENGTH_SHORT).show();
        }
    }

    // --- ELITE UI COMPONENTS ---
    private void setupEliteLoader(RelativeLayout layout) {
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        loaderLayout.setBackgroundColor(Color.parseColor("#050505"));
        
        ProgressBar pb = new ProgressBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00f2fe")));
        }

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe"));
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setText("HK-SYSTEM: INITIALIZING...");
        statusText.setPadding(0, 40, 0, 0);

        loaderLayout.addView(pb);
        loaderLayout.addView(statusText);
        layout.addView(loaderLayout, new RelativeLayout.LayoutParams(-1, -1));
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> { if (internetDialog != null) internetDialog.dismiss(); });
                }
            });
        }
    }

    private void showNoInternetPopUp() {
        runOnUiThread(() -> {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("⚠️ NETWORK BREACH").setMessage("System Offline. Check Connection.").setCancelable(false);
            internetDialog = b.show();
        });
    }

    private void setupDownloadManager() {
        hkView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "HK_MALL_FILE_" + System.currentTimeMillis());
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(r);
        });
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed();
    }
}
