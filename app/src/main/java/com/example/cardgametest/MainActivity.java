package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private CardHand hand = new CardHand();
    private int sizeOfHand = hand.size();
    private Deck deck = new Deck();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //Hit Button
        Button hitButton = findViewById(R.id.hitButton);
        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button Test", "hit");
            }
        });

        //Fold Button
        Button foldButton = findViewById(R.id.foldButton);
        foldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button Test", "fold");
            }
        });


    }

    //when hit button is clicked, this function will run
    //Hit button currently crashes the app
    public void hit(View view){
        hand.hit(deck.retrieveTop());
        //System.out.println("Got Here.");
        //System.out.println(hand.retrieveFirstCard().getRank());
        updateCurrentHand();
    }

    //when fold button is clicked, this function will run
    //Fold also currently crashes the app
    public void foldHand(View view){
        hand.foldHand();
        updateCurrentHand();
    }

    //update the debug textview of current hand
    private void updateCurrentHand(){
        String currentHand = "";
        for(int i = 0; i < sizeOfHand; i++){
            System.out.println(hand.get(i).getRank());
            currentHand += " " + hand.get(i).getRank();
        }
        System.out.println(currentHand);
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