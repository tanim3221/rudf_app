package com.icche.aisdatabase;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blog.library.UpdateChecker;

public class WebviewActivity extends AppCompatActivity {

    Toolbar toolbar = null;
    String db_url = "http://aisru.cf";
    String fbapp = "fb://group/49880688703";
    String fburl = "https://www.facebook.com/groups/bfdf.ru/";
    String pageApp = "fb://page/169680089735915";
    String pageurl = "https://www.facebook.com/rubfdf/";
    private WebView webView = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context liContext = null;

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                return netInfos.isConnected();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNetworkStatusAvialable(getApplicationContext())) {

            if (Build.VERSION.SDK_INT < 22) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            setContentView(R.layout.activity_webview);
            String url = db_url;
            liContext = this.getApplicationContext();
            FrameLayout frameLayout = findViewById(R.id.layout);
            final WebView webView = frameLayout.findViewById(R.id.webView);
            /*  final ProgressBar progress = frameLayout.findViewById(R.id.progress);*/
            final SwipeRefreshLayout swipeRefreshLayout = frameLayout.findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setRefreshing(true);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                }
            });

       /* //progressbar tinting color
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Drawable wrapDrawable = DrawableCompat.wrap(progress.getIndeterminateDrawable());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getApplicationContext(),
                    R.color.colorPrimary));
            progress.setIndeterminateDrawable(DrawableCompat.unwrap(wrapDrawable));

        } else {
            progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
        //progressbar tinting color*/
           new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UpdateChecker.checkForDialog(WebviewActivity.this);
                    }
                }, 1);

            WebSettings webSettings = webView.getSettings();
            webView.getSettings().setSupportZoom(false);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAppCacheMaxSize(10 * 1024 * 1024);
            webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setAppCacheEnabled(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.setScrollbarFadingEnabled(true);
            if (isNetworkStatusAvialable(getApplicationContext())) {
                webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            }
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(url);
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }

            });
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedError(WebView view, int errorCode, String description,
                                            String failingUrl) {
                    webView.loadUrl("about:blank");
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }

                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    webView.loadUrl("about:blank");
                    super.onReceivedError(view, request, error);
                }

                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (url.startsWith("tel:")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), (getString(R.string.call_toast)),
                                Toast.LENGTH_LONG).show();

                        return true;

                    } else if (url.startsWith("mailto:")) {
                        if (isNetworkStatusAvialable(getApplicationContext())) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), (getString(R.string.email_toast)),
                                    Toast.LENGTH_LONG).show();
                        } else {

                            String titleText = getString(R.string.email_dialog);
                            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(140, 140, 140));
                            SpannableStringBuilder color = new SpannableStringBuilder(titleText);
                            color.setSpan(foregroundColorSpan, 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivity.this);
                            builder.setTitle(getString(R.string.connect_net))
                                    .setMessage(color)
                                    /*   .setNegativeButton(getString(R.string.ok_btn), null)*/
                                    .setCancelable(true)
                                    .show();
                        }
                        return true;
                    } else if (url.startsWith("fb:")) {
                        if (isNetworkStatusAvialable(getApplicationContext())) {
                            if (isAppInstalled(liContext, "com.facebook.orca") || isAppInstalled(liContext, "com.facebook.katana")
                                    || isAppInstalled(liContext, "com.example.facebook") || isAppInstalled(liContext, "com.facebook.android")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), (getString(R.string.facebook_view)),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), (getString(R.string.not_fb)),
                                        Toast.LENGTH_LONG).show();
                            }

                        } else {
                            String titleText = getString(R.string.fb_pro);
                            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(140, 140, 140));
                            SpannableStringBuilder color = new SpannableStringBuilder(titleText);
                            color.setSpan(foregroundColorSpan, 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivity.this);
                            builder.setTitle(getString(R.string.connect_net))
                                    .setMessage(color)
                                    /*  .setNegativeButton(getString(R.string.ok_btn), null)*/
                                    .setCancelable(true)
                                    .show();
                        }
                        return true;
                    } else {
                        view.loadUrl(url);
                        return true;
                    }

                }

                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    swipeRefreshLayout.setRefreshing(true);
                }

                public void onPageFinished(WebView view, String url) {
                    swipeRefreshLayout.setRefreshing(false);
                    super.onPageFinished(view, url);
                }
            });
        } else {
            String titleText = getString(R.string.error_net);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(140, 140, 140));
            SpannableStringBuilder color = new SpannableStringBuilder(titleText);
            color.setSpan(foregroundColorSpan, 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivity.this);
            builder.setTitle(getString(R.string.connect_net))
                    .setMessage(color)
                    .setIcon(getResources().getDrawable(R.drawable.ic_wifi_off))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            onBackPressed();
                        }
                    })
                    .setPositiveButton(getString(R.string.retry_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

}
