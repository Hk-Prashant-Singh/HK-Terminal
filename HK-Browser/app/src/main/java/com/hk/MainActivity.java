package com.hk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.*;
import android.widget.*;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    private static final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SELECT_FILE = 100;

    private WebView hkView;
    private LinearLayout loaderLayout;
    private View hkDot; 
    private AlertDialog internetDialog;
    private GoogleSignInClient mGoogleSignInClient;
    public ValueCallback<Uri[]> uploadMessage;
    private SharedPreferences hkElitePrefs;

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public void triggerGoogleLogin() {
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut(); 
            }
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            ((Activity)mContext).startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        @JavascriptInterface
        public void triggerGoogleLogout() {
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut(); 
            }
            hkElitePrefs.edit().clear().apply(); 
            ((Activity)mContext).runOnUiThread(() -> {
                Toast.makeText(mContext, "HK-SYSTEM: Native Session Destroyed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hkElitePrefs = getSharedPreferences("HK_ELITE_VAULT", MODE_PRIVATE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(mainLayout);

        hkView = new WebView(this);
        mainLayout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));

        setupLoader(mainLayout);
        setupGoogle();
        setupWebSettings();
        setupHandlers();
        setupNetwork();
        hkView.loadUrl(TARGET_URL);
    }

    private void setupLoader(RelativeLayout layout) {
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);

        int bgResId = getResources().getIdentifier("hk_background", "drawable", getPackageName());
        if (bgResId != 0) {
            loaderLayout.setBackgroundResource(bgResId);
        } else {
            loaderLayout.setBackgroundColor(Color.parseColor("#050505")); 
        }

        // ⚡ ALPHA SHIFT: Logo ko 75% bottom height par lane ke liye padding badha di hai
        loaderLayout.setPadding(0, 0, 0, 800); 

        ImageView logo = new ImageView(this);
        int resId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (resId != 0) logo.setImageResource(resId);
        logo.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
        
        AlphaAnimation logoPulse = new AlphaAnimation(1.0f, 0.4f);
        logoPulse.setDuration(800);
        logoPulse.setRepeatMode(Animation.REVERSE);
        logoPulse.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(logoPulse);

        hkDot = new View(this);
        GradientDrawable dotShape = new GradientDrawable();
        dotShape.setShape(GradientDrawable.OVAL);
        dotShape.setColor(Color.parseColor("#FF8C00")); 
        hkDot.setBackground(dotShape);

        int dotSize = 40; 
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.topMargin = 15; // Orange dot ko logo ke paas rakha hai
        hkDot.setLayoutParams(dotParams);

        AlphaAnimation dotPulse = new AlphaAnimation(1.0f, 0.3f); 
        dotPulse.setDuration(600); 
        dotPulse.setRepeatMode(Animation.REVERSE); 
        dotPulse.setRepeatCount(Animation.INFINITE); 
        hkDot.startAnimation(dotPulse);

        TextView stableText = new TextView(this);
        stableText.setText("Rs Mall"); 
        stableText.setTextColor(Color.WHITE); 
        stableText.setGravity(Gravity.CENTER);
        stableText.setTextSize(14f); 
        stableText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); 

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-2, -2);
        textParams.topMargin = 10; 
        stableText.setLayoutParams(textParams);

        loaderLayout.addView(logo);
        loaderLayout.addView(hkDot);
        loaderLayout.addView(stableText); 
        layout.addView(loaderLayout, new RelativeLayout.LayoutParams(-1, -1));
    }

    private void setupGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID).requestEmail().requestProfile().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupWebSettings() {
        WebSettings s = hkView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setGeolocationEnabled(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");
        hkView.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);
    }

    private void setupHandlers() {
        hkView.setDownloadListener((url, ua, cd, mime, cl) -> {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
        });

        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb, FileChooserParams fcp) {
                uploadMessage = cb; startActivityForResult(fcp.createIntent(), REQUEST_SELECT_FILE); return true;
            }
        });

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView v, String u, Bitmap f) { loaderLayout.setVisibility(View.VISIBLE); hkView.setVisibility(View.GONE); }
            @Override
            public void onPageFinished(WebView v, String u) { loaderLayout.setVisibility(View.GONE); hkView.setVisibility(View.VISIBLE); CookieManager.getInstance().flush(); }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) { if (r.isForMainFrame()) { v.loadUrl("about:blank"); showNoInternetPopUp(); } }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                hkElitePrefs.edit().putString("SECURE_EMAIL", acc.getEmail()).putString("SECURE_TOKEN", acc.getIdToken()).apply();
                // ⚡ GHOST AUTH: Silent Injection
                hkView.evaluateJavascript("javascript:(function() { if(window.handleAndroidLogin) { window.handleAndroidLogin('" + acc.getIdToken() + "'); } })()", null);
                new Handler().postDelayed(() -> hkView.reload(), 2500);
            } catch (Exception e) {}
        }
        if (requestCode == REQUEST_SELECT_FILE && uploadMessage != null) {
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

    private void setupNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= 24) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network n) { runOnUiThread(() -> { if (internetDialog != null) internetDialog.dismiss(); hkView.reload(); }); }
            });
        }
    }

    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        LinearLayout d = new LinearLayout(this); d.setOrientation(LinearLayout.VERTICAL);
        d.setBackgroundColor(Color.parseColor("#002200")); d.setPadding(60, 80, 60, 80); d.setGravity(Gravity.CENTER);
        TextView t = new TextView(this); t.setText("NETWORK BREACH"); t.setTextColor(Color.WHITE);
        TextView b = new TextView(this); b.setText(" FORCE RECONNECT "); b.setBackgroundColor(Color.RED); b.setTextColor(Color.WHITE);
        b.setOnClickListener(v -> { hkView.loadUrl(TARGET_URL); if (internetDialog != null) internetDialog.dismiss(); });
        d.addView(t); d.addView(b);
        internetDialog = new AlertDialog.Builder(this).setView(d).setCancelable(false).create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() { if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed(); }
}
