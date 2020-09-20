package com.icche.aisdatabase;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
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
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blog.library.UpdateChecker;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebviewActivityFile extends AppCompatActivity {

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = WebviewActivityFile.class.getSimpleName();
    private WebSettings webSettings;
    private ValueCallback<Uri[]> mUploadMessage;
    private String mCameraPhotoPath = null;
    private long size = 0;
    private WebView webView = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context liContext = null;
    private FilePickerDialog dialog;
    private String LOG_TAG = "DREG";
    private Uri[] results;

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mUploadMessage == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        try {
            String file_path = mCameraPhotoPath.replace("file:","");
            File file = new File(file_path);
            size = file.length();

        }catch (Exception e){
            Log.e("Error!", "Error while opening image file" + e.getLocalizedMessage());
        }

        if (data != null || mCameraPhotoPath != null) {
            Integer count = 0; //fix fby https://github.com/nnian
            ClipData images = null;
            try {
                images = data.getClipData();
            }catch (Exception e) {
                Log.e("Error!", e.getLocalizedMessage());
            }

            if (images == null && data != null && data.getDataString() != null) {
                count = data.getDataString().length();
            } else if (images != null) {
                count = images.getItemCount();
            }
            Uri[] results = new Uri[count];
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (size != 0) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else if (data.getClipData() == null) {
                    results = new Uri[]{Uri.parse(data.getDataString())};
                } else {

                    for (int i = 0; i < images.getItemCount(); i++) {
                        results[i] = images.getItemAt(i).getUri();
                    }
                }
            }

            mUploadMessage.onReceiveValue(results);
            mUploadMessage = null;
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

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

            setContentView(R.layout.activity_webview);
            verifyStoragePermissions(this);
            liContext = this.getApplicationContext();
            FrameLayout frameLayout = findViewById(R.id.layout);
            final SwipeRefreshLayout swipeRefreshLayout = frameLayout.findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setRefreshing(true);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    UpdateChecker.checkForDialog(WebviewActivityFile.this);
                }
            }, 1);

            webView = (WebView) findViewById(R.id.webView);
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
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }

            });
            webView.setWebChromeClient(new PQChromeClient());
            webView.setWebChromeClient(new FileChromeClient());
            //if SDK version is greater of 19 then activate hardware acceleration otherwise activate software acceleration
            if (Build.VERSION.SDK_INT >= 19) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            //  // webView.loadUrl("http://aisru.cf");

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedError(WebView view, int errorCode, String description,
                                            String failingUrl) {
                    webView.loadUrl("about:blank");
                    if (!isNetworkStatusAvialable(getApplicationContext())) {
                        showError();
                    }
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }

                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    webView.loadUrl("about:blank");
                    if (!isNetworkStatusAvialable(getApplicationContext())) {
                        showError();
                    }
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivityFile.this);
                            builder.setTitle(getString(R.string.connect_net))
                                    .setMessage(color)
                                    /*   .setNegativeButton(getString(R.string.ok_btn), null)*/
                                    .setCancelable(true)
                                    .show();
                        }
                        return true;
                    } else if (url.startsWith("https://web.facebook.com") || url.startsWith("https://www.facebook.com") || url.startsWith("https://m.me") || url.startsWith("https://facebook.com") || url.startsWith("https://m.facebook.com") || url.startsWith("https://mobile.facebook.com") || url.startsWith("fb:")) {
                        String urlReplace = url;
                        String url_view;
                        urlReplace = urlReplace.replaceAll("https://facebook.com/|https://m.facebook.com/|https://www.facebook.com/|https://mobile.facebook.com/|https://www.facebook.com/profile.php?id=|https://m.facebook.com/profile.php?id=|https://web.facebook.com/profile.php?id=|https://web.facebook.com/|https://m.me/", "");

                        if (url.startsWith("https://m.me")) {
                            url_view = "https://m.me/" + urlReplace;
                        } else {
                            url_view = "fb://facewebmodal/f?href=https://www.facebook.com/" + urlReplace;
                        }

                        if (isNetworkStatusAvialable(getApplicationContext())) {
                            if (isAppInstalled(liContext, "com.facebook.lite") || isAppInstalled(liContext, "com.facebook.mlite") || isAppInstalled(liContext, "com.facebook.orca") || isAppInstalled(liContext, "com.facebook.katana")
                                    || isAppInstalled(liContext, "com.example.facebook") || isAppInstalled(liContext, "com.facebook.android")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_view));
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), (getString(R.string.facebook_view)), Toast.LENGTH_LONG).show();
                                // Toast.makeText(getApplicationContext(), (url_view),Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), (getString(R.string.not_fb)),
                                        Toast.LENGTH_LONG).show();
                            }

                        } else {
                            String titleText = getString(R.string.fb_pro);
                            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(140, 140, 140));
                            SpannableStringBuilder color = new SpannableStringBuilder(titleText);
                            color.setSpan(foregroundColorSpan, 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivityFile.this);
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
                    webView.loadUrl("javascript:(function() { " +
                            "document.getElementsByClassName('wrong_info_report_card')[0].style.display='none'; })()");
                    webView.loadUrl("javascript:(function() { " +
                            "document.getElementsByClassName('mobile_button_down_app')[0].style.display='none'; })()");

                }

                public void onPageFinished(WebView view, String url) {
                    swipeRefreshLayout.setRefreshing(false);
                    // hide element by class name
                    webView.loadUrl("javascript:(function() { " +
                            "document.getElementsByClassName('wrong_info_report_card')[0].style.display='none'; })()");
                    webView.loadUrl("javascript:(function() { " +
                            "document.getElementsByClassName('mobile_button_down_app')[0].style.display='none'; })()");
                    // hide element by id
                    //webView.loadUrl("javascript:(function() { " +
                            //"document.getElementById('your_id').style.display='none';})()");
                    super.onPageFinished(view, url);
                }
            });

            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            download(url, userAgent, contentDisposition, mimetype);

                        } else {

                            ActivityCompat.requestPermissions(WebviewActivityFile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    } else {

                        download(url, userAgent, contentDisposition, mimetype);

                    }
                }
            });
            manageIntent(getIntent());

        } else {
            showError();
        }

     /*
     // This script added below
     // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        */
    }

    private  void showError() {
        String titleText = getString(R.string.error_net);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(140, 140, 140));
        SpannableStringBuilder color = new SpannableStringBuilder(titleText);
        color.setSpan(foregroundColorSpan, 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivityFile.this);
        //builder.setTitle(getString(R.string.connect_net))
        builder.setMessage(color)
                //.setIcon(getResources().getDrawable(R.drawable.ic_wifi_off))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                       /*--------
                        onBackPressed();
                        /*-------
                        finish();
                        System.exit(0);
                        */
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
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

    private void openFileSelectionDialog() {

        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }

        //Create a DialogProperties object.
        final DialogProperties properties = new DialogProperties();

        //Instantiate FilePickerDialog with Context and DialogProperties.
        dialog = new FilePickerDialog(WebviewActivityFile.this, properties);
        dialog.setTitle("Choose a File");
        dialog.setPositiveBtnName("Choose");
        dialog.setNegativeBtnName("Cancel");
        //properties.selection_mode = DialogConfigs.MULTI_MODE; // for multiple files
        properties.selection_mode = DialogConfigs.SINGLE_MODE; // for single file
        properties.selection_type = DialogConfigs.FILE_SELECT;

        //Method handle selected files.
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                results = new Uri[files.length];
                for (int i = 0; i < files.length; i++) {
                    String filePath = new File(files[i]).getAbsolutePath();
                    if (!filePath.startsWith("file://")) {
                        filePath = "file://" + filePath;
                    }
                    results[i] = Uri.parse(filePath);
                    Log.d(LOG_TAG, "file path: " + filePath);
                    Log.d(LOG_TAG, "file uri: " + String.valueOf(results[i]));
                }
                mUploadMessage.onReceiveValue(results);
                mUploadMessage = null;
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (null != mUploadMessage) {
                    if (null != results && results.length >= 1) {
                        mUploadMessage.onReceiveValue(results);
                    } else {
                        mUploadMessage.onReceiveValue(null);
                    }
                }
                mUploadMessage = null;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (null != mUploadMessage) {
                    if (null != results && results.length >= 1) {
                        mUploadMessage.onReceiveValue(results);
                    } else {
                        mUploadMessage.onReceiveValue(null);
                    }
                }
                mUploadMessage = null;
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }
    public class FileChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            mUploadMessage = filePathCallback;

            openFileSelectionDialog();

            return true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (dialog != null) {
                        openFileSelectionDialog();
                    }
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(WebviewActivityFile.this, "Permission is Required for getting list of files", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, final View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    contextMenu.setHeaderTitle(webView.getTitle());
                    contextMenu.setHeaderIcon(getResources().getDrawable(R.drawable.ic_file_download));
                    contextMenu.add(0, 1, 0, R.string.save_img_title)
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    String DownloadImageURL = hitTestResult.getExtra();
                                    String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                    String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                                    String filename = "IMG" + "_" + todayDate + "_" + currentTime + ".jpg";
                                    if (URLUtil.isValidUrl(DownloadImageURL)) {
                                        String cookie = CookieManager.getInstance().getCookie(DownloadImageURL);
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                        request.addRequestHeader("Cookie", cookie);
                                        request.allowScanningByMediaScanner();
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        request.setDestinationInExternalPublicDir("/AIS Family/photos", filename);
                                        downloadManager.enqueue(request);
                                        final Snackbar snackbar = Snackbar.make(view, getString(R.string.save_images), Snackbar.LENGTH_LONG);
                                        snackbar.setAction(getString(R.string.dismiss_snackbar), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                        snackbar.show();

                                    } else {
                                        final Snackbar snackbar = Snackbar.make(view, getString(R.string.not_save_images), Snackbar.LENGTH_LONG);
                                        snackbar.setAction(getString(R.string.dismiss_snackbar), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                        snackbar.show();
                                    }
                                    return false;
                                }
                            });
                }

            } else {
                ActivityCompat.requestPermissions(WebviewActivityFile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                contextMenu.setHeaderTitle(webView.getTitle());
                contextMenu.setHeaderIcon(getResources().getDrawable(R.drawable.ic_file_download));
                contextMenu.add(0, 1, 0, R.string.save_img_title)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String DownloadImageURL = hitTestResult.getExtra();
                                String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                                String filename = "IMG" + "_" + todayDate + "_" + currentTime + ".jpg";
                                if (URLUtil.isValidUrl(DownloadImageURL)) {
                                    String cookie = CookieManager.getInstance().getCookie(DownloadImageURL);
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                    request.addRequestHeader("Cookie", cookie);
                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    request.setDestinationInExternalPublicDir("/AIS Family/photos", filename);
                                    downloadManager.enqueue(request);
                                    Snackbar snackbar = Snackbar.make(view, getString(R.string.save_images), Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                }
                                return false;
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(WebviewActivityFile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

        }
    }

    public void manageIntent(Intent intent) {
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = intent;
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (getIntent().getExtras() != null) {
            if (appLinkData == null){
                webView.loadUrl("http://aisru.cf");
            }else
                webView.loadUrl(String.valueOf(appLinkData));

        } else if (getIntent().getExtras() == null){
            webView.loadUrl("http://aisru.cf");

        }
    }

    // override to get the new intent when this activity has an instance already running
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // again call the same method here with the new intent received
        manageIntent(intent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    public class PQChromeClient extends WebChromeClient {

        // For Android 5.0+
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            mUploadMessage = filePath;
            Log.e("FileCooserParams => ", filePath.toString());

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
           contentSelectionIntent.setType("image/*|application/pdf|doc|ppt|xlxs|docx/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[2];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(Intent.createChooser(chooserIntent, "Select Images"), 1);

            return true;

        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
            return;
        }
        else {
            super.onBackPressed();
        }
    }

    public void download(final String url, final String userAgent, String contentDisposition, String mimetype) {


        final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

        AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivityFile.this);
       // builder.setIcon(getResources().getDrawable(R.drawable.ic_file_download));
        //builder.setTitle(getString(R.string.download_file_title));
        builder.setTitle(R.string.do_download);
        builder.setMessage(filename);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.download_file_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String cookie = CookieManager.getInstance().getCookie(url);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.addRequestHeader("Cookie", cookie);
                request.addRequestHeader("User-Agent", userAgent);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                request.setDestinationInExternalPublicDir("/AIS Family/files", filename);
                downloadManager.enqueue(request);

                String download = getString(R.string.download) + filename;
                Toast toast = Toast.makeText(getApplicationContext(), download, Toast.LENGTH_LONG);
                toast.show();
                webView.goBack();

            }
        });

        builder.setNegativeButton(getString(R.string.later_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                webView.goBack();
            }

        });
        builder.create().show();

    }

}