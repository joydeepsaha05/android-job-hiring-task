package com.joydeep.solar.calculator.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
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

    public static String formatTime(double timeInHours) {
        if (timeInHours == Double.POSITIVE_INFINITY) {
            return "-";
        }
        if (timeInHours >= 24) {
            timeInHours -= 24;
        } else if (timeInHours < 0) {
            timeInHours += 24;
        }
        String time = "";
        String postfix;
        int hours = (int) timeInHours;
        int minutes = (int) ((timeInHours - hours) * 60);
        if (hours >= 12) {
            if (hours != 12) {
                hours -= 12;
            }
            postfix = " PM";
        } else {
            postfix = " AM";
        }
        time += hours + ":";
        time += minutes < 10 ? "0" + minutes : minutes;
        return time + postfix;
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

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundPaddingRight())) {
                    editText.setText("");
                    return true;
                }
            }
            return false;
        });
    }
}
