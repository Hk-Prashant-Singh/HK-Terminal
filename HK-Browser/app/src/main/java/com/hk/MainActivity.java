package com.hk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface; 
import android.webkit.PermissionRequest;
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

public class MainActivity extends Activity {
    
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog; 
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    
    private static final int REQUEST_CODE_EMAIL = 1001;
    private String systemUserEmail = "UNKNOWN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // --- 1. ELITE UI SETUP (Pitch Black) ---
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(layout);
        
        hkView = new WebView(this);
        
        // HARDWARE ACCELERATION (Max GPU Usage for Speed)
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        hkView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); 
        
        // 👉 HK-OPERATION: DEVICE SCAN (AUTOFILL) & KEYBOARD FOCUS ENGINE 
        hkView.setFocusable(true);
        hkView.setFocusableInTouchMode(true);
        hkView.requestFocus(View.FOCUS_DOWN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hkView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        // DIRECT INJECTION
        layout.addView(hkView, new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // --- 2. SECURE LOADER SYSTEM WITH CUSTOM BRAND LOGO ---
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, 
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        // INJECTING FRONT LOGO
        ImageView splashLogo = new ImageView(this);
        int logoId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if(logoId != 0) {
            splashLogo.setImageResource(logoId);
        }
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(350, 350); 
        logoParams.setMargins(0, 0, 0, 40); 
        splashLogo.setLayoutParams(logoParams);
        loaderLayout.addView(splashLogo);

        ProgressBar spinner = new ProgressBar(this);
        loaderLayout.addView(spinner);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe")); 
        statusText.setTextSize(14);
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setPadding(0, 30, 0, 0);
        statusText.setGravity(Gravity.CENTER);
        loaderLayout.addView(statusText);

        layout.addView(loaderLayout);

        // --- 3. HK-OPERATION: HYPER-SPEED REAL-TIME ENGINE & OAUTH BYPASS ---
        WebSettings settings = hkView.getSettings();
        
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); 
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); 
        settings.setUseWideViewPort(true); 
        settings.setLoadWithOverviewMode(true); 
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH); 
        
        // Form Data Save Engine
        settings.setSaveFormData(true);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false); 
        }

        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        String chromeUserAgent = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(chromeUserAgent);
        
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(hkView, true);
        
        // --- 4. HK STEALTH BRIDGE & PERMISSIONS ---
        hkView.addJavascriptInterface(new HKStealthBridge(), "HK_TERMINAL");
        
        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    request.grant(request.getResources()); 
                });
            }
        });

        // --- 5. WEBVIEW CLIENT LOGIC ---
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; 
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hkView.setVisibility(View.GONE); 
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("ESTABLISHING CONNECTION...");
                statusText.setTextColor(Color.parseColor("#00f2fe"));
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                // HK-OPERATION: Force cookie sync for auto-login memory
                android.webkit.CookieManager.getInstance().flush();
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadUrl("about:blank"); 
                    showNoInternetPopUp(); 
                }
            }
        });

        // --- 6. AUTO-RECONNECT ENGINE ---
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null && internetDialog.isShowing()) {
                            internetDialog.dismiss(); 
                        }
                        statusText.setText("NETWORK DETECTED! RELOADING...");
                        statusText.setTextColor(Color.parseColor("#00ff00"));
                        hkView.loadUrl(TARGET_URL); 
                    });
                }
            });
        }

        hkView.loadUrl(TARGET_URL);
    }

    // --- HK-OPERATION STEALTH BRIDGE (NATIVE GMAIL POPUP TRIGGER) ---
    public class HKStealthBridge {
        @JavascriptInterface
        public void executeSystemLogin() {
            runOnUiThread(() -> {
                fetchSystemAccount();
            });
        }
    }

    private void fetchSystemAccount() {
        try {
            Intent intent = android.accounts.AccountManager.newChooseAccountIntent(
                null, null, new String[]{"com.google"}, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_EMAIL);
        } catch (Exception e) {
            Toast.makeText(this, "SYSTEM LINK FAILED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EMAIL) {
            if (resultCode == RESULT_OK && data != null) {
                systemUserEmail = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                Toast.makeText(this, "SYSTEM LINKED: " + systemUserEmail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "LOGIN BYPASSED.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- SECURE NO-INTERNET POPUP (GREEN PANEL / RED BUTTON) ---
    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#004D00")); 
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("NETWORK ERROR");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        dialogLayout.addView(title);

        TextView msg = new TextView(this);
        msg.setText("\nConnection Lost.\nCheck your uplink, Prashant bhai.\n");
        msg.setTextColor(Color.parseColor("#A3FFA3")); 
        msg.setTextSize(13);
        msg.setTypeface(Typeface.MONOSPACE);
        msg.setGravity(Gravity.CENTER);
        dialogLayout.addView(msg);

        TextView retryBtn = new TextView(this);
        retryBtn.setText(" RETRY ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); 
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setTextSize(16);
        retryBtn.setPadding(0, 35, 0, 35);
        retryBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        retryBtn.setGravity(Gravity.CENTER);
        
        retryBtn.setOnClickListener(v -> {
            hkView.loadUrl(TARGET_URL);
            if (internetDialog != null) internetDialog.dismiss();
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 50, 0, 0);
        retryBtn.setLayoutParams(btnParams);
        dialogLayout.addView(retryBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        
        internetDialog = builder.create();
        
        if (internetDialog.getWindow() != null) {
            internetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        internetDialog.show();
    }

    // --- CUSTOM SECURE EXIT & GESTURE LOGIC (INSTANT SILENT KILL) ---
    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) {
            hkView.goBack(); 
        } else {
            hkView.evaluateJavascript("javascript:(function() { " +
                    "if (window.history.length > 1 && document.location.hash !== '') { " +
                    "   window.history.back(); return 'JS_BACK_EXECUTED'; " +
                    "} else { return 'READY_TO_EXIT'; } " +
                    "})()", value -> {
                
                if (value != null && value.contains("READY_TO_EXIT")) {
                    super.onBackPressed(); 
                    finish();
                }
            });
        }
    }
}
