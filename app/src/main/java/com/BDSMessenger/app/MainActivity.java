package com.BDSMessenger.app;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;





public class MainActivity extends Activity {

    private WebView mWebView;
    ////////toni
    private final static int FCR = 1;
    /////////toni
    int notificationID = 1;
    /** File upload callback for platform versions prior to Android 5.0 */
    protected ValueCallback<Uri> mFileUploadCallbackFirst;
    /** File upload callback for Android 5.0+ */
    protected ValueCallback<Uri[]> mFileUploadCallbackSecond;
    protected static final int REQUEST_CODE_FILE_PICKER = 1;
    private static final int PERMISO_MICROFONO = 1;

    ////////toni
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE_FILE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(intent.getData());
                        mFileUploadCallbackFirst = null;
                    }
                    else if (mFileUploadCallbackSecond != null) {
                        Uri[] dataUris;
                        try {
                            dataUris = new Uri[] { Uri.parse(intent.getDataString()) };
                        }
                        catch (Exception e) {
                            dataUris = null;
                        }
                        mFileUploadCallbackSecond.onReceiveValue(dataUris);
                        mFileUploadCallbackSecond = null;
                    }
                }
            }
            else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst.onReceiveValue(null);
                    mFileUploadCallbackFirst = null;
                }
                else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond.onReceiveValue(null);
                    mFileUploadCallbackSecond = null;
                }
            }
        }
    }

    ////////toni
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        //mejorar rendimiento
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        //mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        mWebView.getSettings().setUseWideViewPort(true);
        ////
        //webSettings.setGeolocationEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(0);
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        ///toni///
        assert mWebView != null;
        ///toni///
        /////toni///
        mWebView.setWebViewClient(new Callback());
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        // toni
        // Use remote resource
       // mWebView.setWebViewClient(new MyAppWebViewClient());
        // Force links and redirects to open in the WebView instead of in a browser
        //

       mWebView.setWebChromeClient(new WebChromeClient()
                                            ////toni
                                   {
                                       @Override
                                       public void onPermissionRequest(final PermissionRequest request) {
                                           Log.d("Permiso", "onPermissionRequest");
                                           MainActivity.this.runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                       request.grant(request.getResources());
                                                   }
                                               }
                                           });
                                       }

                                       public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                                           new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).show();
                                           result.confirm();
                                           displayNotification(message);
                                           return true;

                                       }


                                       // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
                                       @SuppressWarnings("unused")
                                       public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                           openFileChooser(uploadMsg, null);
                                      }

                                       // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
                                       public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                                           openFileChooser(uploadMsg, acceptType, null);
                                       }

                                       // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
                                       @SuppressWarnings("unused")
                                       public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                                           openFileInput(uploadMsg, null);
                                       }

                                       // file upload callback (Android 5.0 (API level 21) -- current) (public method)
                                       //For Android 5.0+
                                       @SuppressWarnings("all")
                                       public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                                           openFileInput(null, filePathCallback);
                                           return true;
                                       }
                                   }

                /////toni
        );
        mWebView.setWebViewClient(new MyAppWebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mWebView.loadUrl("file:///android_asset/myerrorpage.html");

                                      }
                                  }

        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISO_MICROFONO);
        } else {
            cargarUrl();
        }

       //mWebView.loadUrl("https://dominacionworld.com/arrowchat/modarrowchat/modaudiorecorder/clyp2/");
        // Stop local links and redirects from opening in browser instead of WebView
        // Use local resource
        // mWebView.loadUrl("file:///android_asset/www/index.html");
        this.mWebView.getSettings().setUserAgentString(
                this.mWebView.getSettings().getUserAgentString()
                        + " "
                        + getString(R.string.user_agent_suffix)
        );
        // disabltodoelscroll
        //mWebView.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        return (event.getAction() == MotionEvent.ACTION_MOVE);
        //    }
        //});
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISO_MICROFONO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cargarUrl();
                } else {
                    Toast.makeText(this, "Permiso no dado", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void cargarUrl() {
        mWebView.loadUrl("https://dominacionworld.com/viewFriendss.php");
    }

    @SuppressLint("NewApi")
    protected void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst,
                                 final ValueCallback<Uri[]> fileUploadCallbackSecond) {
        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst.onReceiveValue(null);
        }
        mFileUploadCallbackFirst = fileUploadCallbackFirst;
        if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond.onReceiveValue(null);
        }
        mFileUploadCallbackSecond = fileUploadCallbackSecond;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Choose a file"),
                REQUEST_CODE_FILE_PICKER);
    }

    protected void displayNotification(String message) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("notificationID", notificationID);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        CharSequence ticker = message;
        CharSequence contentTitle = "BDSMessenger";
        CharSequence contentText = message;
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification noti = new Notification.Builder(this)
                .setContentIntent(pendingIntent)
                .setTicker(ticker)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(R.drawable.ic_launcher, ticker, pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[]{100, 250, 100, 500})
                .setSound(defaultSound)
                .setLights(0xff493C7C, 1000, 1000)
                .build();
        nm.notify(notificationID, noti);
    }


    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        // insert here your instructions
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /////////////toni
    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }


    // Create an image file
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        // This function can be called in our JS script now
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }
    }
}
