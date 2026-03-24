package com.hk;

import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
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

// 👉 HK-CORE: ALPHA NATIVE AUTH IMPORTS
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    // --- HK-MALL: SYSTEM CONSTANTS ---
    private static final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SELECT_FILE = 100;

    // --- UI & ENGINE VARIABLES ---
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private GoogleSignInClient mGoogleSignInClient;
    public ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🛡️ FULLSCREEN STEALTH UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.parseColor("#050505"));
        setContentView(mainLayout);

        // 🎯 WEBVIEW INITIALIZATION
        initializeWebView(mainLayout);

        // ⚡ SPLASH LOADER SETUP
        setupEliteLoader(mainLayout);

        // 🔑 NATIVE GOOGLE ENGINE CONFIG
        setupGoogleAuth();

        // 🛰️ AUTO-RECONNECT PROTOCOL
        setupNetworkMonitoring();

        // START SYSTEM
        hkView.loadUrl(TARGET_URL);
    }

    private void initializeWebView(RelativeLayout layout) {
        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        hkView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        hkView.setFocusable(true);
        hkView.setFocusableInTouchMode(true);
        hkView.requestFocus(View.FOCUS_DOWN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hkView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        // HYPER SETTINGS
        WebSettings s = hkView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setGeolocationEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setRenderPriority(WebSettings.RenderPriority.HIGH);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // PIXEL 8 STEALTH AGENT
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);

        layout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));
        
        setupWebViewClients();
        setupDownloadManager();
    }

    private void setupEliteLoader(RelativeLayout layout) {
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, -1);
        
        ImageView logo = new ImageView(this);
        int resId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (resId != 0) logo.setImageResource(resId);
        logo.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
        
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.5f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(pulse);

        ProgressBar pb = new ProgressBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00f2fe")));
        }

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe"));
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 40, 0, 0);
        statusText.setText("HK-SYSTEM: INITIALIZING...");

        loaderLayout.addView(logo);
        loaderLayout.addView(pb);
        loaderLayout.addView(statusText);
        layout.addView(loaderLayout, lp);
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
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

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
                // 👉 NATIVE BYPASS: Intercept Google Sign-In URLs
                if (url.contains("accounts.google.com") || url.contains("gsi/")) {
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                    return true;
                }
                if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaderLayout.setVisibility(View.VISIBLE);
                hkView.setVisibility(View.GONE);
                statusText.setText("HK-MALL: SYNCING DATA...");
                statusText.setTextColor(Color.parseColor("#39FF14"));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadUrl("about:blank");
                    showNoInternetPopUp();
                }
            }
        });
    }

    private void setupDownloadManager() {
        hkView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setMimeType(mimetype);
            r.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(r);
                Toast.makeText(this, "HK-ENCRYPTED DOWNLOAD STARTED...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 🛡️ GOOGLE LOGIN SUCCESS HANDLER
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String token = account.getIdToken();
                Toast.makeText(this, "HK-MALL: Access Granted. Tech Wizard Live!", Toast.LENGTH_LONG).show();
                // INJECT TOKEN TO WEB APP
                hkView.evaluateJavascript("javascript:receiveAndroidToken('" + token + "');", null);
            } catch (ApiException e) {
                Log.e("HK_AUTH", "Error: " + e.getStatusCode());
                Toast.makeText(this, "HK-MALL: Auth Intercept Failed! Check SHA-1.", Toast.LENGTH_LONG).show();
            }
        }

        // 📂 FILE UPLOAD HANDLER
        if (requestCode == REQUEST_SELECT_FILE && uploadMessage != null) {
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null && internetDialog.isShowing()) {
                            internetDialog.dismiss();
                            hkView.loadUrl(TARGET_URL);
                        }
                    });
                }
            });
        }
    }

    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(Color.parseColor("#0a0a0a"));
        l.setPadding(50, 50, 50, 50);
        l.setGravity(Gravity.CENTER);
        
        TextView t = new TextView(this);
        t.setText("NETWORK BREACH DETECTED");
        t.setTextColor(Color.RED);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        
        TextView btn = new TextView(this);
        btn.setText(" FORCE RECONNECT ");
        btn.setPadding(20, 20, 20, 20);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#222222"));
        btn.setOnClickListener(v -> { hkView.loadUrl(TARGET_URL); internetDialog.dismiss(); });

        l.addView(t);
        l.addView(btn);
        b.setView(l).setCancelable(false);
        internetDialog = b.create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed();
    }
}
