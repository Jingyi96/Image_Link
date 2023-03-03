package com.coen268.recommendapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProductsDialog extends BottomSheetDialog {

    public ProductsDialog(@NonNull Context context) {
        super(context);
    }

    public ProductsDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected ProductsDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
