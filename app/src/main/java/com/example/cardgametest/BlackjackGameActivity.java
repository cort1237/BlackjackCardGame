package com.example.cardgametest;

import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import org.w3c.dom.Text;

public class BlackjackGameActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private TextView splitHandText;
    private CardHand playerHand = new CardHand();
    private int handValue = 0;
    private LinearLayout playerLayout;
    private LinearLayout splitLayout;
    private CardHand dealerHand = new CardHand();
    private LinearLayout dealerLayout;
    private Deck deck = new Deck();
    private TableLayout tabLayout;

    private Stats stats;

    private CustomPopupWindow roundEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerLayout = findViewById(R.id.main_player_hand);
        splitLayout = findViewById(R.id.split_player_hand);
        dealerLayout = findViewById(R.id.dealer_hand);
        tabLayout = findViewById(R.id.expandTab);
        // Money Text Field
        moneyTextView = findViewById(R.id.moneyTextView);
        currentHandText = findViewById(R.id.viewHand);
        splitHandText = findViewById(R.id.viewSplit);
        updateMoneyText();

        //Example Button Implementation
        /*
        Button exampleButton = findViewById(R.id.buttonExample);
        exampleButton.setOnClickListener(view -> {

        });
        */

        //button to open the options menu
        /*@SuppressLint("WrongViewCast")*/ ImageButton optionsButton = findViewById(R.id.optionButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), options.class);
                startActivity(intent);
            }
        });


        //expand tab and button
        tabLayout.bringToFront();
        findViewById(R.id.expandButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(tabLayout.getVisibility() == View.VISIBLE){
                    tabLayout.setVisibility(View.GONE);
                }
                else{
                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        roundEnd = new CustomPopupWindow(this);

        stats = new Stats(getApplicationContext());
        setup();
    }
    // generate the hands for each row in the side bar
    protected void generateHand(){
        int NUM_PLAYERS = 2;
        CardHand handList[] = new CardHand[NUM_PLAYERS];
        handList[0] = playerHand;
        handList[1] = dealerHand;
        TableRow.LayoutParams params = new TableRow.LayoutParams(150,180);
        params.setMargins(4,8,4,8);
        TableRow tabLayout1 = findViewById(R.id.row1);
        TableRow tabLayout2 = findViewById(R.id.row2);
        TableRow t[] = new TableRow[2];
        t[0] = tabLayout1;
        t[1] = tabLayout2;
        for(int x = 0; x < NUM_PLAYERS; x++) {
            t[x].removeAllViews();
            for (int i = 0; i < handList[x].size(); i++) {
                ImageView cardView = new ImageView(this);
                cardView.setImageResource(R.drawable.default_jack_of_hearts);
                cardView.setLayoutParams(params);
                t[x].addView(cardView);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Resumed", "Returned from settings");

    }

    //Adds a card to the player's Hand, and updates the Hand Layout.
    public void hit(View view){
        hitHelper();
    }

    public void resetGame(View view) {
        //for each player
        playerHand.clearHand();
        playerLayout.removeAllViews();
        dealerHand.clearHand();
        dealerLayout.removeAllViews();
        if(playerHand.isSplit()){
            splitLayout.removeAllViews();
        }
        roundEnd.dismiss();
        setup();
    }


    private void hitHelper() {
        if(!playerHand.isSplit()) {
            dealCard(playerHand, playerLayout);
            //System.out.println("Got Here.");
            //System.out.println(hand.retrieveFirstCard().getRank());
            Log.d("Hit Button Test", "hit");
            updateCurrentHand();

            if (playerHand.getTotalValue() >= 21)
                dealerTurn();
        } else if(playerHand.getTotalValue(0) < 21) {
            dealCard(playerHand, playerLayout);
            Log.d("Hit Button Test", "hit");
            updateCurrentHand();
        } else {
            dealCard(playerHand, splitLayout, 1);
            updateCurrentHand();
            if(playerHand.getTotalValue(1) >= 21){
                dealerTurn();
            }
        }

    }


    //Adds a card to the specified Hand Layout on the screen
    private void addCardToHand(LinearLayout mainPlayerHand, Card c, int margin) {
        Log.d("AddCardToHand", c.getSuit() + " | " + c.getRank());
        if(mainPlayerHand.getChildCount() == 0)
            margin = 0;
        if(margin < -130)
            margin = -130;
        ImageView cardView = new ImageView(this);
        cardView.setImageResource(c.getCardImage());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200,260);
        Log.d("Margin", Integer.toString(margin));
        params.setMargins(margin,8,0,8);
        cardView.setLayoutParams(params);
        mainPlayerHand.addView(cardView);
        generateHand();
    }

    //when fold button is clicked, this function will run
    //Fold also currently crashes the app
    public void foldHand(View view){
        foldHandHelper();
        Log.d("Fold Button Test", "fold");
    }
    private void foldHandHelper() {
        dealerTurn();
    }

    //update the debug textview of current hand
    /*private void updateCurrentHand(){
        String currentHand = "";
        for(int i = 0; i < playerHand.size(); i++){
            System.out.println(playerHand.get(i).getRank());
            currentHand += " " + playerHand.get(i).getRank();
            Log.d("card debug", "card #" + i + "| value: " + playerHand.get(i).getRank());
        }

        currentHand += " (" + playerHand.getTotalValue() + ")";
        currentHandText.setText(currentHand);
    }*/

    //new updateCurrentHand to handle split hands
    private void updateCurrentHand(){
        if(!playerHand.isSplit()){
            String currentHand = "";
            for(int i = 0; i < playerHand.size(); i++){
                currentHand += " " + playerHand.get(i).getRank();
                Log.d("card debug", "card #" + i + "| value: " + playerHand.get(i).getRank());
            }

            currentHand += " (" + playerHand.getTotalValue() + ")";
            currentHandText.setText(currentHand);
        }
        else{
            String hand1 = "";
            String hand2 = "";
            for(int i = 0; i < playerHand.retrieveHand(0).size(); i++){
                hand1 += " " + playerHand.retrieveHand(0).get(i).getRank();
                Log.d("hand 1 debug", "card #" + i + "| value: " + playerHand.retrieveHand(0).get(i).getRank());
            }
            for(int i = 0; i < playerHand.retrieveHand(1).size(); i++){
                hand2 += " " + playerHand.retrieveHand(1).get(i).getRank();
                Log.d("hand 2 debug", "card #" + i + "| value: " + playerHand.retrieveHand(1).get(i).getRank());
            }


            currentHandText.setText(hand1);
            splitHandText.setText(hand2);

        }
    }

    //when split button is pushed, split hand and make button invisible
    public void splitButton(View view){
        if(!playerHand.splitHand()){
            Log.d("split debug", "Player cannot split hand");
        }
        else{
            Log.d("split debug", "Player successfully split");
            ((Button) findViewById(R.id.splitButton)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.viewSplit)).setVisibility(View.VISIBLE);
            updateCurrentHand();
        }
    }


    private void updateMoneyText() {
        moneyTextView.setText("" + playerMoney);
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

    //Will hand out two cards to each entity at the table
    private void setup() {
        playerHand.clearHand();
        playerLayout.removeAllViews();
        dealerHand.clearHand();
        dealerLayout.removeAllViews();

        for (int i=0; i<2; i++)  {
            //for (Player player : playerList) {
            hitHelper();
            //}
            dealCard(dealerHand, dealerLayout);
        }

        Log.d("dealer.getTotalValue", "" + dealerHand.getTotalValue());
        Log.d("playerHand.getTotalValue", "" + playerHand.getTotalValue());

        // Early version of standard Blackjack peek procedure (still needs to be tied to actual animation)
        // Animation should play if dealer's visible card is an "Ace" or "Face" card, regardless if getTotalValue()==21
        if(dealerHand.getTotalValue() == 21) {
            dealerTurn();
        }
        else {
            //Enable Player Controls
            ((Button) findViewById(R.id.hitButton)).setEnabled(true);
            ((Button) findViewById(R.id.foldButton)).setEnabled(true);
            ((Button) findViewById(R.id.betButton)).setEnabled(true);
            ((Button) findViewById(R.id.restart)).setEnabled(false);
            if(playerHand.isPair()){
                ((Button) findViewById(R.id.splitButton)).setVisibility(View.VISIBLE);
            }
            else{
                ((Button) findViewById(R.id.splitButton)).setVisibility(View.INVISIBLE);
            }
            ((Button) findViewById(R.id.restart)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.viewSplit)).setVisibility(View.INVISIBLE);

            updateCurrentHand();
        }
    }

    private Card dealCard(CardHand entity, LinearLayout handLayout, int split) {
        int margin;
        Card topCard = deck.retrieveTop();
        entity.addCard(topCard, split);
        int cardCount = handLayout.getChildCount();
        if(cardCount > 2)
            margin = 8 - ((cardCount-1) * 40);
        else
            margin = 8;
        handLayout.removeAllViews();
        entity.retrieveHand(0).forEach((card -> addCardToHand(handLayout, card, margin)));
        Log.d("dealCard Test", "Dealer Card: " + topCard.getRank());
        return topCard;
    }
    private Card dealCard(CardHand entity, LinearLayout handLayout) {
        return dealCard(entity, handLayout, 0);
    }

    private void dealerTurn() {
        int dealerTotal = dealerHand.getTotalValue(), playerTotal = playerHand.getTotalValue();
        //dealerTotal = 16;

        //Disable Player controls
        ((Button) findViewById(R.id.hitButton)).setEnabled(false);
        ((Button) findViewById(R.id.foldButton)).setEnabled(false);
        ((Button) findViewById(R.id.betButton)).setEnabled(false);
        ((Button) findViewById(R.id.restart)).setEnabled(true);
        ((Button) findViewById(R.id.restart)).setVisibility(View.VISIBLE);

        //Insert win view below

        while(dealerTotal<17) {
            Card card = dealCard(dealerHand, dealerLayout);
            dealerTotal+= card.getValue();
            Log.d("dealerTurn Test", "Current Dealer Total: " + dealerTotal);
        }

        View rootView = findViewById(android.R.id.content);

        if (dealerTotal>21) {
            Log.d("dealerTurn Test", "Final Dealer Total: " + dealerTotal + " BUST");
            if (playerTotal<=21) {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " WIN");
                stats.recordWin();
                roundEnd.setMessage("PLAYER WINS");
            }
            else {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " BUST");
                stats.recordLoss();
                roundEnd.setMessage("PLAYER BUSTS");
            }
            roundEnd.showAtLocation(rootView, Gravity.CENTER, 0, -200);
        }
        else {
            Log.d("dealerTurn Test", "Final Dealer Total: " + dealerTotal);
            if (playerTotal > dealerTotal && playerTotal<=21) {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " WIN");
                stats.recordWin();
                roundEnd.setMessage("PLAYER WINS");
            }
            else if (playerTotal == dealerTotal) {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " PUSH");
                stats.recordPush();
                roundEnd.setMessage("PUSH");
            } //should stats of a draw be recorded?
            else {
                stats.recordLoss();
                if (playerTotal<21) {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " LOSS");
                    roundEnd.setMessage("PLAYER LOSES");
                }
                else {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " BUST");
                    roundEnd.setMessage("PLAYER BUST");
                }
            }
            roundEnd.showAtLocation(rootView, Gravity.CENTER, 0, -200);
        }

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                roundEnd.dismiss();
//            }
//        }, 2500);

        Log.d("End Stats", "Wins: " + stats.getStats()[0]);
        Log.d("End Stats", "Losses: " + stats.getStats()[1]);
        Log.d("End Stats", "Total Games: " + stats.getStats()[2]);
    }


}