package cn.bluemobi.dylan.step.activity;


import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.bluemobi.dylan.step.R;


public class WeatherActivity extends AppCompatActivity {

    private WebView mWVmhtml;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWVmhtml=(WebView) findViewById(R.id.WV_Id);
        mWVmhtml.getSettings().setJavaScriptEnabled(true);
        mWVmhtml.loadUrl("https://www.zran.xyz/weather/");
        mWVmhtml.setWebViewClient(new MyWebViewClient());

        mWVmhtml.setWebChromeClient(new MyWebChromeClient());

    }
    class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("WebView","Start visit the webpage");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("WebView","End visit the webpage");
        }
    }
    class MyWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setTitle(title);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK&&mWVmhtml.canGoBack()){
            mWVmhtml.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}