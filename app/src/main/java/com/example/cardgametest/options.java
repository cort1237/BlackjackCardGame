package com.example.cardgametest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.material.slider.Slider;
public class options extends AppCompatActivity {

    private SharedPreferences prefs;
    private float volumeValue ;

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor e = prefs.edit();
        e.putFloat("volume", volumeValue);
        e.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        prefs = getPreferences(Context.MODE_PRIVATE);

        if(!prefs.contains("volume")) {

            SharedPreferences.Editor e = prefs.edit();
            e.putFloat("volume", volumeValue);
            e.apply();
        }
        volumeValue = prefs.getFloat("volume",0);
        Log.d("yeet", String.valueOf(volumeValue));

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Slider volumeSlider = findViewById(R.id.volume_slider);
        volumeSlider.setValue(volumeValue);
        volumeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {

                volumeValue = value;
            }
        });
    }
}