package com.hk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
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

// 👉 NATIVE AUTH IMPORTS
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";

    // HK-OPERATION: File Upload Variables
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    // 👉 HK-OPERATION: NATIVE AUTH VARIABLES
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. FULLSCREEN ALPHA UI SETUP ---
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505"));
        setContentView(layout);

        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        hkView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        hkView.setFocusable(true);
        hkView.setFocusableInTouchMode(true);

        layout.addView(hkView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // --- 2. ELITE SPLASH LOADER ---
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(-2, -2);
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        ImageView splashLogo = new ImageView(this);
        int logoId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (logoId != 0) splashLogo.setImageResource(logoId);
        splashLogo.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.7f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        splashLogo.startAnimation(pulse);
        loaderLayout.addView(splashLogo);

        ProgressBar spinner = new ProgressBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00f2fe")));
        }
        loaderLayout.addView(spinner);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe"));
        statusText.setTextSize(16);
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setPadding(0, 40, 0, 0);
        statusText.setGravity(Gravity.CENTER);
        statusText.setText("PLEASE WAIT");
        loaderLayout.addView(statusText);
        layout.addView(loaderLayout);

        // --- 3. 👉 NATIVE GOOGLE SIGN-IN ENGINE INITIALIZATION ---
        // ALERT: "YOUR_WEB_CLIENT_ID" ko apne Firebase project ke Web Client ID se replace karna
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID_HERE") 
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- 4. HYPER-SPEED REAL-TIME ENGINE ---
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        String chromeAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(chromeAgent);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);

        hkView.setWebChromeClient(new WebChromeClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) { uploadMessage.onReceiveValue(null); uploadMessage = null; }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try { startActivityForResult(intent, REQUEST_SELECT_FILE); } catch (Exception e) { return false; }
                return true;
            }
        });

        // --- 5. THE NATIVE INTERCEPTOR (WEBVIEW CLIENT) ---
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 👉 THE KILL SWITCH: Browser ki jagah Android ka native popup kholo
                if (url.contains("accounts.google.com") || url.contains("gsi/")) {
                    executeNativeGoogleLogin();
                    return true; // White screen block ho gayi!
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hkView.setVisibility(View.GONE);
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("SYNCING DATA...");
                statusText.setTextColor(Color.parseColor("#39FF14")); 
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                statusText.setTextColor(Color.parseColor("#00f2fe")); 
                CookieManager.getInstance().flush();
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
                            hkView.loadUrl(TARGET_URL);
                        }
                    });
                }
            });
        }
        hkView.loadUrl(TARGET_URL);
    }

    // 👉 HK-OPERATION: FIRE NATIVE LOGIN
    private void executeNativeGoogleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // --- HK-OPERATION: ALL SYSTEM RESULTS HANDLER ---
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 1. Handle Native Google Login Token
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                String email = account.getEmail();

                // 👉 TOKEN INJECTION: Token direct website ke javascript function ko pass kar diya
                Toast.makeText(this, "Secure Uplink: " + email, Toast.LENGTH_SHORT).show();
                hkView.evaluateJavascript("javascript:receiveAndroidToken('" + idToken + "');", null);

            } catch (ApiException e) {
                Toast.makeText(this, "Auth Intercept Failed", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // 2. Handle File Uploads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
    }

    // --- SECURE NO-INTERNET POPUP ---
    private void showNoInternetPopUp() {
        // ... (Same popup logic as previous) ...
        if (internetDialog != null && internetDialog.isShowing()) return;
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#004D00"));
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);
        TextView title = new TextView(this);
        title.setText("UPLINK DISCONNECTED");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dialogLayout.addView(title);
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" RETRY UPLINK ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000"));
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setPadding(30, 30, 30, 30);
        retryBtn.setOnClickListener(v -> {
            hkView.loadUrl(TARGET_URL);
            if (internetDialog != null) internetDialog.dismiss();
        });
        dialogLayout.addView(retryBtn);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        internetDialog = builder.create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) {
            hkView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
