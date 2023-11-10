package com.example.cardgametest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShopActivity extends AppCompatActivity {

    private ShopItem[] items;
    private static final String TAG = "ShopActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Stats stats = new Stats(getApplicationContext());

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

        items = new ShopItem[]{
                new ShopItem("Item 1", 10),
                new ShopItem("Item 2", 20),
                new ShopItem("Item 3", 15),
                new ShopItem("Item 4", 25),
                new ShopItem("Item 5", 40),
                new ShopItem("Item 6", 30),
                new ShopItem("Odd # of Items Test", 120)
                // Add more items as needed
        };

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        int margin = 50;
        for (int i=0; i<items.length; i++) {
            // Loop through the items array to dynamically create CardViews
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 0; // Set width to 0dp to enable layout_columnWeight
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Set height to WRAP_CONTENT
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // 1f is layout_columnWeight
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
//          layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
//          layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
//          layoutParams.rowSpec = GridLayout.spec(i / 2); // Assuming 2 columns
//          layoutParams.columnSpec = GridLayout.spec(i % 2);
            layoutParams.setMargins( margin, margin, margin,margin);
            cardView.setLayoutParams(layoutParams);

            View view = getLayoutInflater().inflate(R.layout.cardview_item, null);
            TextView itemNameTextView = view.findViewById(R.id.itemNameTextView);
            TextView itemCostTextView = view.findViewById(R.id.itemCostTextView);

            itemNameTextView.setText(items[i].getName());
            itemCostTextView.setText("Cost: " + items[i].getCost());

            cardView.addView(view);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = gridLayout.indexOfChild(v);
                    Log.d(TAG, "Item " + (position + 1) + " clicked");
                    showAlertDialog(v, items[position].getName());
                }
            });

            gridLayout.addView(cardView);
        }

/*
        CardView item6CardView = findViewById(R.id.item6CardView);
        item6CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item 6 clicked");
                showAlertDialog(v, "Item 6");
            }
        });

 */
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

class ShopItem {

    private String name;
    private int cost;

    public ShopItem(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}