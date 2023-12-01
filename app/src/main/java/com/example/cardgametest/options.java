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

import com.google.android.material.slider.LabelFormatter;
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
        Button exitButton = findViewById(R.id.exitButton);
        Bundle extras = getIntent().getExtras();

        if(extras != null){
            int exit = extras.getInt("exit");

            if(exit == 0){
                exitButton.setVisibility(View.GONE);
            }
            else{
                exitButton.setVisibility(View.VISIBLE);
                exitButton.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), TitleScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            }
        }
        prefs =  getSharedPreferences("options", Context.MODE_PRIVATE);

        if(!prefs.contains("volume")) {

            SharedPreferences.Editor e = prefs.edit();
            e.putFloat("volume", volumeValue);
            e.apply();
        }
        volumeValue = prefs.getFloat("volume",0);

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor e = prefs.edit();
                e.putFloat("volume", volumeValue);
                e.apply();
                finish();
            }
        });





        Slider volumeSlider = findViewById(R.id.volume_slider);
        volumeSlider.setValue(volumeValue);
        volumeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        volumeSlider.addOnChangeListener((slider, value, fromUser) -> volumeValue = value);
    }
}