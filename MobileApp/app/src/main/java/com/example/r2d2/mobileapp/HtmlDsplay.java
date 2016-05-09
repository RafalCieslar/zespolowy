package com.example.r2d2.mobileapp;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.bluetoothexample.R;

public class HtmlDsplay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_htmli_dsplay);
        WebView localWebView = (WebView)findViewById(R.id.webView);

        String deviceName = getIntent().getStringExtra("device-name");
        localWebView.getSettings().setJavaScriptEnabled(true);
        Toast.makeText(getApplicationContext(),deviceName ,
                Toast.LENGTH_LONG).show();
        localWebView.loadUrl("file:///android_asset/"+deviceName+".html"); //new.html is html file name.
    }

}
