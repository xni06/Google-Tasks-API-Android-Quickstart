package com.example.quickstart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

class MyProgressDialog extends ProgressDialog {
    MyProgressDialog(Context context, final Contract.Presenter presenter, String msg) {
        super(context);
        setMessage(msg);
        setCanceledOnTouchOutside(false);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                presenter.cancelTask();
            }
        });
    }
}
