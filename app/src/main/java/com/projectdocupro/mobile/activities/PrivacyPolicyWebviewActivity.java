package com.projectdocupro.mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.projectdocupro.mobile.R;



public class PrivacyPolicyWebviewActivity extends AppCompatActivity {


    private WebView webView;
    private TextView tv_accept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_webview);
        bindView();
        webView.loadUrl("https://www.projectdocu.net/assistant/pd_mobileapp_nutzungsbedingungen.html");
        tv_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void bindView() {
        webView = findViewById(R.id.web_view);
        tv_accept = findViewById(R.id.tv_accept);
    }
}
