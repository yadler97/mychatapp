package com.yannick.mychatapp;

import android.text.Editable;

import com.google.android.material.textfield.TextInputLayout;

public class TextWatcher implements android.text.TextWatcher {

    private final TextInputLayout textInputLayout;

    public TextWatcher(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() != 0) {
            textInputLayout.setError(null);
        }
    }
}
