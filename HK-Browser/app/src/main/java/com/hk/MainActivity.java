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

// 👉 HK-OPERATION: NATIVE GOOGLE AUTH IMPORTS
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    // --- HK-OPERATION: CORE SYSTEM VARIABLES ---
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";

    // --- HK-OPERATION: FILE UPLOAD MATRIX ---
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    // --- HK-OPERATION: NATIVE AUTH VARIABLES ---
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    // ASLI WEB CLIENT ID INJECTED
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";

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
        hkView.requestFocus(View.FOCUS_DOWN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hkView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        layout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));

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
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.6f);
        pulse.setDuration(900);
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
        statusText.setText("SYSTEM INITIALIZING...");
        loaderLayout.addView(statusText);
        layout.addView(loaderLayout);

        // --- 3. 👉 NATIVE GOOGLE SIGN-IN INITIALIZATION ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID) 
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- 4. HYPER-SPEED REAL-TIME ENGINE ---
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); 
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setSaveFormData(true);
        settings.setAllowFileAccess(true);
        settings.setGeolocationEnabled(true); 
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false); 

        String chromeAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(chromeAgent);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);

        // --- 5. HK-OPERATION: DOWNLOAD MANAGER ---
        hkView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading encrypted file...");
                request.setTitle(url.substring(url.lastIndexOf("/") + 1));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
                
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) { dm.enqueue(request); Toast.makeText(getApplicationContext(), "Download Sequence Initiated...", Toast.LENGTH_SHORT).show(); }
            }
        });

        // --- 6. PERMISSIONS & FILE UPLOADS ---
        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
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

        // --- 7. THE NATIVE INTERCEPTOR (DEVICE ACCOUNT POP-UP TRIGGER) ---
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent://") || url.startsWith("whatsapp://") || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; } catch (Exception e) { return false; }
                }
                
                // 👉 THE MAGIC: Account login trigger
                if (url.contains("accounts.google.com") || url.contains("gsi/")) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    return true; 
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
                if (request.isForMainFrame()) { view.loadUrl("about:blank"); showNoInternetPopUp(); }
            }
        });

        // --- 8. AUTO-RECONNECT ENGINE ---
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

    // --- 9. UPLOAD & NATIVE AUTH RESULT HANDLER ---
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 👉 HK-OPERATION: HANDLE NATIVE GOOGLE LOGIN SELECTION
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken(); 
                
                // HK-MALL BRANDING LINE
                Toast.makeText(this, "HK-MALL: Access Granted. Tech Wizard Live!", Toast.LENGTH_LONG).show();
                
                // INJECT TOKEN INTO WEBSITE
                hkView.evaluateJavascript("javascript:receiveAndroidToken('" + idToken + "');", null);

            } catch (ApiException e) {
                Toast.makeText(this, "HK-MALL: Auth Intercept Failed! Check SHA-1.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // Handle File Uploads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
    }

    // --- 10. SECURE NO-INTERNET POPUP ---
    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#002200")); 
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);
        TextView title = new TextView(this);
        title.setText("NETWORK BREACH");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dialogLayout.addView(title);
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" FORCE RECONNECT ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); 
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setPadding(40, 35, 40, 35);
        retryBtn.setOnClickListener(v -> { hkView.loadUrl(TARGET_URL); if (internetDialog != null) internetDialog.dismiss(); });
        dialogLayout.addView(retryBtn);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        internetDialog = builder.create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed();
    }
}
            
