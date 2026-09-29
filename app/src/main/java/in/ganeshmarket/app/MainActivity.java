package in.ganeshmarket.app;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Ganesh Bhaji Market - Customer App
 * Full-screen WebView wrapper for https://ganeshmarket.in/app/login
 */
public class MainActivity extends AppCompatActivity {

    private static final String START_URL = "https://ganeshmarket.in/app/login";
    private static final String HOST = "ganeshmarket.in";
    private static final int FILE_CHOOSER_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 1002;

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout offlineView;
    private View splashView;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable splashTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            hideSplash();
        }
    };

    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraPhotoUri;
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Must run before super.onCreate() so the system splash theme applies.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        offlineView = findViewById(R.id.offlineView);
        splashView = findViewById(R.id.splashView);
        Button btnRetry = findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(v -> loadSite());

        swipeRefresh.setOnRefreshListener(() -> {
            if (isOffline) {
                loadSite();
            } else {
                webView.reload();
            }
        });

        setupWebView();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            webView.reload(); // re-render restored page; fires onPageFinished
        } else {
            loadSite();
        }

        // Never leave customers stuck on the splash: fade out by 12 s at the latest.
        mainHandler.postDelayed(splashTimeoutRunnable, 12000);
    }

    /**
     * Hides the branded splash overlay with a fade once the site is loaded
     * (or an error/timeout occurred).
     */
    private void hideSplash() {
        mainHandler.removeCallbacks(splashTimeoutRunnable);
        if (splashView == null || splashView.getVisibility() == View.GONE) {
            return;
        }
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(400);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        splashView.startAnimation(fadeOut);
    }

    private void loadSite() {
        if (hasInternet()) {
            isOffline = false;
            offlineView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            swipeRefresh.setEnabled(true);
            webView.loadUrl(START_URL);
        } else {
            showOffline();
        }
    }

    private void showOffline() {
        isOffline = true;
        swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        swipeRefresh.setEnabled(true); // allow pull to retry
        offlineView.setVisibility(View.VISIBLE);
        hideSplash();
    }

    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();

                // Keep our site inside the app
                if (host.equals(HOST) || host.endsWith("." + HOST)) {
                    return false;
                }

                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                // WhatsApp / tel / mailto links open in their own apps
                if (scheme.equals("whatsapp") || scheme.equals("tel")
                        || scheme.equals("mailto") || scheme.equals("sms")
                        || scheme.equals("intent") || scheme.equals("market")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "No app found to open this link", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                // Other websites open in the browser
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                // First successful page render: reveal the app.
                hideSplash();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) {
                    showOffline();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            // Camera (QR scan / profile photo) support
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (hasCameraPermission()) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.CAMERA},
                                CAMERA_PERMISSION_CODE);
                    }
                });
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Let the website use location if permission granted at OS level
                callback.invoke(origin, true, false);
            }

            // File upload support (e.g. product images)
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_CODE);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "Cannot open file picker", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        // Let the site's download links work (e.g. bills, reports)
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                try {
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm != null) dm.enqueue(req);
                    Toast.makeText(MainActivity.this, "Downloading…", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    private boolean hasCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && webView != null) {
            webView.evaluateJavascript("document.dispatchEvent(new Event('cameraPermission'))", null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_CODE && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Hardware back button goes back in web history first
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()
                && !isOffline) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(splashTimeoutRunnable);
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
