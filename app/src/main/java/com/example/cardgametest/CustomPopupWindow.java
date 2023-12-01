package com.example.cardgametest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CustomPopupWindow {
    private final PopupWindow popupWindow;
    private final TextView messageTextView;

    public CustomPopupWindow(Context context) {
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(context).inflate(R.layout.custom_popup, null);
        messageTextView = contentView.findViewById(R.id.messageTextView);

        // Create and configure the PopupWindow
        //popupWindow = new PopupWindow(contentView,222,202);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public void setMessage(String message) {
        messageTextView.setText(message);
    }

    public void showAtLocation(View anchor, int gravity, int x, int y) {
        popupWindow.showAtLocation(anchor, gravity, x, y);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }
}

