package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView statsView = (TextView) findViewById(R.id.statsView);

        Stats stats = new Stats(getApplicationContext());

        String[] statline = stats.getStats();

        statsView.setText(String.format("W: %s\nL: %s\nTotal Games: %s\n\nReward Currency: %s", statline[0], statline[1], statline[2], statline[3]));

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}