package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Example Button Implementation
        Button exampleButton = findViewById(R.id.buttonexample);
        exampleButton.setOnClickListener(view -> {
            return;
        });


        }

}