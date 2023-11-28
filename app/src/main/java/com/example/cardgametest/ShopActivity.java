package com.example.cardgametest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {

    private ShopItem[] items;
    ArrayList<ShopItem> unpurchasedItemsList = new ArrayList<>();
    private static final String TAG = "ShopActivity";

    private String rewardCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Stats stats = new Stats(getApplicationContext());

        Log.d(TAG, stats.getData()[3]);
        rewardCurrency = stats.getData()[3];

        TextView rewardCurrencyTextView = findViewById(R.id.rewardCurrencyTextView);
        rewardCurrencyTextView.setText(String.format("Currency: %s", rewardCurrency));

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        items = new ShopItem[]{
                new ShopItem("Card_Skin_1", 10),
                new ShopItem("Card_Skin_2", 20),
                new ShopItem("Background_1", 15),
                new ShopItem("Background_2", 25)
                // Add more items as needed
        };

        //clearPrefs();

        //Check which items have already been purchased
        SharedPreferences prefs = getSharedPreferences("PurchasedItems", MODE_PRIVATE);
        for (ShopItem item : items) {
            boolean isPurchased = prefs.getBoolean(item.getItemName(), false);
            Log.d(TAG + " Purchased Items", item.getItemName() + " " + isPurchased);
            if (!isPurchased) {
                unpurchasedItemsList.add(item);
            }
        }

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        /*
        create onClick for each item. open an alert dialog if button has been pressed. add item to player's inventory if confirmed
         */
        int margin = 50;
        for (int i=0; i<items.length; i++) {
            // Loop through the items array to dynamically create CardViews
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 0;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.setMargins( margin, margin, margin, margin);
            cardView.setContentPadding(16, 16, 16, 16);
            cardView.setLayoutParams(layoutParams);

            View view = getLayoutInflater().inflate(R.layout.cardview_item, null);
            TextView itemNameTextView = view.findViewById(R.id.itemNameTextView);
            TextView itemCostTextView = view.findViewById(R.id.itemCostTextView);

            itemNameTextView.setText(items[i].getItemName().replace("_", " "));
            if (unpurchasedItemsList.contains(items[i]))
                itemCostTextView.setText("Cost: " + items[i].getItemCost());
            else
                itemCostTextView.setText("Purchased");

            cardView.addView(view);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = gridLayout.indexOfChild(v);
                    Log.d(TAG + " cardView.OnClick", "Item " + (position + 1) + " clicked");

                    if (unpurchasedItemsList.contains(items[position])) {
                        showPurchaseAlertDialog(v, items[position]);
                    } else {
                        showEquipAlertDialog(v, items[position]);
                    }
                }
            });

            gridLayout.addView(cardView);
        }

/*
// Before attempting to dynamically create items i was manually doing this for each item:

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

    private void showEquipAlertDialog(View v, ShopItem item) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(item.getItemName().replace("_", " "));
        alertDialogBuilder.setMessage("Do you wish to equip this item ?");

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Equip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (item.getItemName().contains("Card")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("EquippedCard", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (int i=0; i<items.length; i++) {
                        if (items[i].getItemName().contains("Card")) {

                            if (items[i].getItemName() == item.getItemName())
                                editor.putBoolean(items[i].getItemName(), true);
                            else
                                editor.putBoolean(items[i].getItemName(), false);
                        }
                    }
                    editor.apply();
                    Log.d(TAG + " showAlertDialog", item.getItemName() + " EquippedCard");

                    Toast.makeText(ShopActivity.this, "Equipped", Toast.LENGTH_SHORT).show();
                    Log.d(TAG + " showAlertDialog", "Confirmed " + item.getItemName() + " equipped");

                } else if (item.getItemName().contains("Background")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("EquippedBackground", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (int i=0; i<items.length; i++) {
                        if (items[i].getItemName().contains("Background")) {

                            if (items[i].getItemName() == item.getItemName())
                                editor.putBoolean(items[i].getItemName(), true);
                            else
                                editor.putBoolean(items[i].getItemName(), false);
                        }
                    }
                    editor.putBoolean(item.getItemName(), true);
                    editor.apply();
                    Log.d(TAG + " showAlertDialog", item.getItemName() + " EquippedBackground");

                    Toast.makeText(ShopActivity.this, "Equipped", Toast.LENGTH_SHORT).show();
                    Log.d(TAG + " showAlertDialog", "Confirmed " + item.getItemName() + " equipped");
                }
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShopActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                Log.d(TAG + " showAlertDialog", "Canceled " + item.getItemName() + " purchase");
            }
        });

        alertDialog.show();
    }

    private void showPurchaseAlertDialog(View v, ShopItem item) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(item.getItemName().replace("_", " "));
        alertDialogBuilder.setMessage("Do you want to purchase this item for " + item.getItemCost() + "?");

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Check if there's enough money to cover the cost
                if (Integer.parseInt(rewardCurrency) >= item.getItemCost()) {
                    // User has enough money, proceed with the purchase

                    // Need to deduct rewardCurrency and rewrite it in the stats.txt file
                    rewardCurrency = String.valueOf(Integer.parseInt(rewardCurrency) - item.getItemCost());
                    Log.d(TAG + " showAlertDialog", "Updated Currency: " + rewardCurrency);

                    TextView rewardCurrencyTextView = findViewById(R.id.rewardCurrencyTextView);
                    rewardCurrencyTextView.setText(String.format("Currency: %s", rewardCurrency));

                    Stats stats = new Stats(getApplicationContext());
                    stats.recordCurrency((int)(0-(item.getItemCost()/.1)));

                    SharedPreferences sharedPreferences = getSharedPreferences("PurchasedItems", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(item.getItemName(), true);
                    editor.apply();
                    Log.d(TAG + " showAlertDialog", item.getItemName() + " PurchasedItems");

                    //unpurchasedItemsList.remove(item);
                    //Log.d(TAG + " showAlertDialog", String.valueOf(purchasedItems.size()));
                    Toast.makeText(ShopActivity.this, "Purchased", Toast.LENGTH_SHORT).show();
                    Log.d(TAG + " showAlertDialog", "Confirmed " + item.getItemName() + " purchase");
                    recreate();
                } else {
                    // User does not have enough money
                    Toast.makeText(ShopActivity.this, "Insufficient funds", Toast.LENGTH_SHORT).show();
                    Log.d(TAG + " showAlertDialog", "Insufficient funds for " + item.getItemName() + " purchase");
                }
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShopActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                Log.d(TAG + " showAlertDialog", "Canceled " + item.getItemName() + " purchase");
            }
        });

        alertDialog.show();
    }

    private void clearPrefs() {
        SharedPreferences prefs = getSharedPreferences("PurchasedItems", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}

// abstract class ShopItem {
class ShopItem {

    private String itemName;
    private int itemCost;

    public ShopItem(String itemName, int itemCost) {
        this.itemName = itemName;
        this.itemCost = itemCost;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemCost() {
        return itemCost;
    }
}

/* Set items to purchased
SharedPreferences.Editor editor = getSharedPreferences("PurchasedItems", MODE_PRIVATE).edit();
editor.putBoolean(itemName, true);
editor.apply();
 */

/* Check if item has been purchased
SharedPreferences prefs = getSharedPreferences("PurchasedItems", MODE_PRIVATE);
boolean isPurchased = prefs.getBoolean(itemName, false);
 */

/*
Add Items For Shop
    - 2 Alternate Card Skins
    - 2 Alternate Backgrounds
 */

