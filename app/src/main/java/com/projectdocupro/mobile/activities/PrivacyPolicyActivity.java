package com.projectdocupro.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;



public class PrivacyPolicyActivity extends AppCompatActivity {


    private TextView tv_privacy_policy;
    private TextView tv_accept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        bindView();

        Spannable wordtoSpan = new SpannableString("Durch Verwendung dieser App stimmen Sie den Nutzungsbedingungen der projectdocu GmbH zu.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(PrivacyPolicyActivity.this, PrivacyPolicyWebviewActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.privacy_policy_text_color));
            }
        };

        wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.privacy_policy_text_color)), 44, 63, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(clickableSpan, 44, 63, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_privacy_policy.setText(wordtoSpan);
        tv_privacy_policy.setMovementMethod(LinkMovementMethod.getInstance());
        tv_privacy_policy.setText(wordtoSpan);

        tv_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(PrivacyPolicyActivity.this);
                sharedPrefsManager.setBooleanValue(AppConstantsManager.USER_PRIVACY_POLICY, false);
                Intent intent = new Intent(PrivacyPolicyActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void bindView() {
        tv_privacy_policy = findViewById(R.id.tv_privacy_policy);
        tv_accept = findViewById(R.id.tv_accept);
    }
}
