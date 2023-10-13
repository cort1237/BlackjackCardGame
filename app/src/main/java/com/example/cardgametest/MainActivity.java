package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private CardHand playerHand = new CardHand();
    private LinearLayout playerLayout;

    private CardHand dealerHand = new CardHand();
    private LinearLayout dealerLayout;
    private Deck deck = new Deck();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerLayout = findViewById(R.id.main_player_hand);
        dealerLayout = findViewById(R.id.dealer_hand);

        // Money Text Field
        moneyTextView = findViewById(R.id.moneyTextView);
        currentHandText = findViewById(R.id.viewHand);
        updateMoneyText();

        //Example Button Implementation
        Button exampleButton = findViewById(R.id.buttonExample);
        exampleButton.setOnClickListener(view -> {

        });

        //button to open the options menu
        Button optionsButton = findViewById(R.id.optionButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), options.class);
                startActivity(intent);
            }
        });
    }

    //Adds a card to the player's Hand, and updates the Hand Layout.
    public void hit(View view){
        Card c = deck.retrieveTop();
        playerHand.addCard(c);
        addCardToHand(playerLayout, c);
        //System.out.println("Got Here.");
        //System.out.println(hand.retrieveFirstCard().getRank());
        Log.d("Button Test", "hit");
        updateCurrentHand();
    }

    //Adds a card to the specified Hand Layout on the screen
    private void addCardToHand(LinearLayout mainPlayerHand, Card c) {
        ImageView cardView = new ImageView(this);
        cardView.setImageResource(R.drawable.ace_of_diamond_test);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200,260);
        params.setMargins(8,8,8,8);
        cardView.setLayoutParams(params);
        mainPlayerHand.addView(cardView);
    }

    //when fold button is clicked, this function will run
    //Fold also currently crashes the app
    public void foldHand(View view){
        playerHand.foldHand();
        Log.d("Button Test", "fold");
        updateCurrentHand();
        playerLayout.removeAllViews();

    }

    //update the debug textview of current hand
    private void updateCurrentHand(){
        String currentHand = "";
        for(int i = 0; i < playerHand.size(); i++){
            System.out.println(playerHand.get(i).getRank());
            currentHand += " " + playerHand.get(i).getRank();
            Log.d("card debug", "card #" + i + "| value: " + playerHand.get(i).getRank());
        }
        currentHandText.setText(currentHand);
    }

    private void updateMoneyText() {
        moneyTextView.setText("Money: $" + playerMoney);
    }

    // Function to add money
    public void addMoney(int amount) {
        playerMoney += amount;
        updateMoneyText();
    }

    // Function to remove money
    public boolean removeMoney(int amount) {
        if (playerMoney >= amount) {
            playerMoney -= amount;
            updateMoneyText();
            return true; // Money successfully removed
        } else {
            return false; // Not enough money
        }
    }

    public boolean isMoneyNegative() {
        return playerMoney < 0;
    }


}