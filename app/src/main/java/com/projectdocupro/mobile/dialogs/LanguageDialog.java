package com.projectdocupro.mobile.dialogs;
/*

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.LoginDialogCallback;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LanguageDialog extends Dialog {

    Unbinder    unbinder;
    LoginDialogCallback  listener;

    public LanguageDialog(@NonNull Context context, LoginDialogCallback  loginDialogCallback) {
        super(context);
        this.listener=loginDialogCallback;
        initDialog();
    }

    public LanguageDialog(@NonNull Context context, int themeResId, LoginDialogCallback loginDialogCallback) {
        super(context, themeResId);
        this.listener=loginDialogCallback;
        initDialog();
    }

    protected LanguageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initDialog();
    }

    private void initDialog(){
        setContentView(R.layout.language_dialog);
        setCancelable(false);
        bindView();
    }

    private RadioGroup  languageGroup;
    private View mSelectLanguage;

    private void selectLanguageClick(){
        SharedPrefsManager  sharedPrefsManager=new SharedPrefsManager(getContext());
        switch (languageGroup.getCheckedRadioButtonId()){
            case    R.id.english_language:
                setLocale("en");
                sharedPrefsManager.setStringValue(AppConstantsManager.APP_LANGUAGE,"en");
                break;
            case    R.id.german_language:
                setLocale("de");
                sharedPrefsManager.setStringValue(AppConstantsManager.APP_LANGUAGE,"de");
                break;
        }
        listener.onLoginDialogResponse();
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        dismiss();
    }

    private void bindView() {
        languageGroup = findViewById(R.id.language_group);
        mSelectLanguage = findViewById(R.id.select_language);
        languageGroup.setOnClickListener(v -> {
            selectLanguageClick();
        });

        mSelectLanguage.setOnClickListener(v -> {
            selectLanguageClick();
        });
    }
}
*/
