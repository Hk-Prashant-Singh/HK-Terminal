package com.hk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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

    // --- HK-MALL CORE TARGETS ---
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

    // ⚡ [HK-OPERATION] GHOST VOICE ENGINE (BACKGROUND)
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechIntent;

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
            if (mGoogleSignInClient != null) mGoogleSignInClient.signOut(); 
            hkElitePrefs.edit().clear().apply(); 
            ((Activity)mContext).runOnUiThread(() -> {
                Toast.makeText(mContext, "HK-SYSTEM: Native Session Destroyed", Toast.LENGTH_SHORT).show();
            });
        }

        // ⚡ BACKGROUND SILENT TRIGGER (No Google Dialog Popup)
        @JavascriptInterface
        public void triggerVoiceSearch() {
            ((Activity)mContext).runOnUiThread(() -> {
                if (mSpeechRecognizer != null) {
                    mSpeechRecognizer.startListening(mSpeechIntent);
                } else {
                    Toast.makeText(mContext, "Voice Engine Not Initialized!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⚡ RUNTIME MIC PERMISSION CHECK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
            }
        }

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
        setupGhostVoiceEngine(); // ⚡ INIT GHOST ENGINE

        hkView.loadUrl(TARGET_URL);
    }

    // ⚡ [HK-OPERATION] SILENT SPEECH RECOGNIZER LOGIC
    private void setupGhostVoiceEngine() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                hkView.evaluateJavascript("javascript:if(window.onNativeVoiceReady) window.onNativeVoiceReady();", null);
            }
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {
                hkView.evaluateJavascript("javascript:if(window.onNativeVoiceError) window.onNativeVoiceError(" + error + ");", null);
            }
            @Override
            public void onResults(Bundle results) {
                java.util.ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0).replace("'", "\\'"); 
                    String js = "javascript:(function() { " +
                                "var searchBar = document.getElementById('hk-search-bar');" +
                                "if(searchBar) {" +
                                "    searchBar.value = '" + spokenText + "';" +
                                "    searchBar.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "}" +
                                "if(window.stopEliteVoiceSearch) window.stopEliteVoiceSearch();" +
                                "})()";
                    hkView.evaluateJavascript(js, null);
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
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

        loaderLayout.setPadding(0, 0, 0, 250); 

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
        dotParams.topMargin = 40; 
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

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = 20; 
        stableText.setLayoutParams(textParams);

        loaderLayout.addView(logo);
        loaderLayout.addView(hkDot);
        loaderLayout.addView(stableText); 
        
        layout.addView(loaderLayout, new RelativeLayout.LayoutParams(-1, -1));
    }

    private void setupGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile() 
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupWebSettings() {
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        hkView.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);
    }

    private void setupHandlers() {
        hkView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
            Toast.makeText(this, "RIDDHI SIDDHI: Encrypted Download Started", Toast.LENGTH_SHORT).show();
        });

        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;
                startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE);
                return true;
            }
        });

        hkView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleEliteUrlOverrides(view, request.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleEliteUrlOverrides(view, url);
            }

            private boolean handleEliteUrlOverrides(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Dialer missing!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                if (url.startsWith("whatsapp:") || url.startsWith("mailto:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "App not found!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                if (url.startsWith("intent://")) { return true; } 
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaderLayout.setVisibility(View.VISIBLE);
                hkView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                CookieManager.getInstance().flush();
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String url = request.getUrl().toString();
                    if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("mailto:")) {
                        return; 
                    }
                }
                if (request.isForMainFrame()) { view.loadUrl("about:blank"); showNoInternetPopUp(); }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                String userEmail = account.getEmail();

                hkElitePrefs.edit().putString("SECURE_EMAIL", userEmail).putString("SECURE_TOKEN", idToken).apply();
                Toast.makeText(this, "RIDDHI SIDDHI: Decrypting Alpha Token...", Toast.LENGTH_SHORT).show();

                String js = "javascript:(function() { " +
                            "if(window.handleAndroidLogin) { window.handleAndroidLogin('" + idToken + "'); }" +
                            "else { alert('System Breach: Master Receiver Not Found'); }" +
                            "})()";
                hkView.evaluateJavascript(js, null);

                new Handler().postDelayed(() -> {
                    Toast.makeText(MainActivity.this, "Tech Wizard System Refreshing...", Toast.LENGTH_SHORT).show();
                    hkView.reload();
                }, 2500);

            } catch (Exception e) {
                Toast.makeText(this, "Auth Intercept Failed! Check SHA-1 Key.", Toast.LENGTH_LONG).show();
            }
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
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null) internetDialog.dismiss();
                        hkView.reload();
                    });
                }
            });
        }
    }

    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        LinearLayout dLayout = new LinearLayout(this);
        dLayout.setOrientation(LinearLayout.VERTICAL);
        dLayout.setBackgroundColor(Color.parseColor("#002200")); 
        dLayout.setPadding(60, 80, 60, 80);
        dLayout.setGravity(Gravity.CENTER);
        
        TextView title = new TextView(this);
        title.setText("NETWORK BREACH");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" FORCE RECONNECT ");
        retryBtn.setPadding(40, 35, 40, 35);
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); 
        retryBtn.setTextColor(Color.WHITE);
        
        retryBtn.setOnClickListener(v -> { hkView.loadUrl(TARGET_URL); if (internetDialog != null) internetDialog.dismiss(); });
        
        dLayout.addView(title);
        dLayout.addView(retryBtn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dLayout).setCancelable(false);
        internetDialog = builder.create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed();
    }
}
