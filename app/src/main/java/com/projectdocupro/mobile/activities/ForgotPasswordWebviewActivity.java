package com.projectdocupro.mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.projectdocupro.mobile.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class ForgotPasswordWebviewActivity extends AppCompatActivity {


    private Toolbar toolbar;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manual);
        bindView();

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getString(R.string.text_forgot_password_heading));
        webView.loadUrl("https://www.projectdocu.net/projects/token.php");


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void bindView() {
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.web_view);
    }
}
