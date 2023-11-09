package com.example.cardgametest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShopActivity extends AppCompatActivity {

    private static final String TAG = "ShopActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Stats stats = new Stats(getApplicationContext());


        Log.d(TAG, stats.getData()[0]);
        Log.d(TAG, stats.getData()[1]);
        Log.d(TAG, stats.getData()[2]);
        Log.d(TAG, stats.getData()[3]);
        String rewardCurrency = stats.getData()[3];

        TextView rewardCurrencyTextView = findViewById(R.id.rewardCurrencyTextView);
        rewardCurrencyTextView.setText(String.format("Currency: %s", rewardCurrency));

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
        create button for each item. open an alert dialog if button has been pressed. add item to player's inventory if confirmed
         */
        CardView item1CardView = findViewById(R.id.item1CardView);
        item1CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 1 clicked");
                showAlertDialog(v, "Item 1");
            }
        });

        CardView item2CardView = findViewById(R.id.item2CardView);
        item2CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 2 clicked");
                showAlertDialog(v, "Item 2");
            }
        });

        CardView item3CardView = findViewById(R.id.item3CardView);
        item3CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 3 clicked");
                showAlertDialog(v, "Item 3");
            }
        });

        CardView item4CardView = findViewById(R.id.item4CardView);
        item4CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 4 clicked");
                showAlertDialog(v, "Item 4");
            }
        });

        CardView item5CardView = findViewById(R.id.item5CardView);
        item5CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 5 clicked");
                showAlertDialog(v, "Item 5");
            }
        });

        CardView item6CardView = findViewById(R.id.item6CardView);
        item6CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 6 clicked");
                showAlertDialog(v, "Item 6");
            }
        });
    }

    public void showAlertDialog(View v, String item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(item);
        alertDialog.setMessage("Do you wish to purchase?");
        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShopActivity.this, "Purchased", Toast.LENGTH_SHORT).show();
                Log.d(TAG + " showAlertDialog", "Confirmed " + item + " purchase");
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShopActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                Log.d(TAG + " showAlertDialog", "Canceled " + item + " purchase");
            }
        });

        alertDialog.create().show();
    }
}