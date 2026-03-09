package com.projectdocupro.mobile.dialogs;

/*
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.LoginDialogCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MessageDialog extends Dialog {

    Unbinder    unbinder;
    LoginDialogCallback  listener;


    public MessageDialog(@NonNull Context context, LoginDialogCallback  loginDialogCallback) {
        super(context);
        this.listener=loginDialogCallback;
        initDialog();
    }

    public MessageDialog(@NonNull Context context, int themeResId, LoginDialogCallback loginDialogCallback) {
        super(context, themeResId);
        this.listener=loginDialogCallback;
        initDialog();
    }

    protected MessageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initDialog();
    }

    private void initDialog(){
        setContentView(R.layout.message_dialog);
        setCancelable(false);
        unbinder    = ButterKnife.bind(this);
    }

    @BindView(R.id.title)
    TextView  titleTV;

    @BindView(R.id.message)
    TextView  messageTV;

    @BindView(R.id.cancel)
    Button  cancelButton;

    @OnClick(R.id.ok)
    public void selectOKClick(){
        dismiss();
        listener.onLoginDialogResponse();
    }

    @OnClick(R.id.cancel)
    public void selectCancelClick(){
        dismiss();
        listener.onLoginDialogResponse();
    }

    public void setText(String title, String message){
        titleTV.setText(title);
        messageTV.setText(message);
    }

    public void hideCancel(){
        cancelButton.setVisibility(View.GONE);
    }

}
*/
