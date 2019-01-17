package com.joydeep.solar.calculator.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.joydeep.solar.calculator.R;

public class UIUtils {

    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        editText.clearFocus();
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setupClearButtonWithAction(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int clearIcon = s.length() != 0 ? R.drawable.ic_close : 0;
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0);
            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundPaddingRight())) {
                        editText.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
