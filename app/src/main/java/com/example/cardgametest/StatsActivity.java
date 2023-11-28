package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        CardView winsCardView = findViewById(R.id.winsCardView);
        TextView winsTextView = winsCardView.findViewById(R.id.winsTextView);

        CardView lossesCardView = findViewById(R.id.lossesCardView);
        TextView lossesTextView = lossesCardView.findViewById(R.id.lossesTextView);

        CardView totalGamesCardView = findViewById(R.id.totalGamesCardView);
        TextView totalGamesTextView = totalGamesCardView.findViewById(R.id.totalGamesTextView);

        CardView pushesCardView = findViewById(R.id.pushesCardView);
        TextView pushesTextView = pushesCardView.findViewById(R.id.pushesTextView);

        Stats stats = new Stats(getApplicationContext());

        String[] statline = stats.getData();

        //statsView.setText(String.format("W: %s\nL: %s\nTotal Rounds: %s", statline[0], statline[1], statline[2]));
        winsTextView.setText(String.format("Wins: %s", statline[0]));
        lossesTextView.setText(String.format("Losses: %s", statline[1]));
        pushesTextView.setText(String.format("Pushes: %d" , Integer.parseInt(statline[2])-Integer.parseInt(statline[1])-Integer.parseInt(statline[0])));
        totalGamesTextView.setText(String.format("Total Games: %s", statline[2]));

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}