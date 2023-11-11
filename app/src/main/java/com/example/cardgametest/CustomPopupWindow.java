package com.example.cardgametest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CustomPopupWindow {
    private PopupWindow popupWindow;
    private TextView messageTextView;
    private View contentView;

    public CustomPopupWindow(Context context) {
        contentView = LayoutInflater.from(context).inflate(R.layout.custom_popup, null);
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

