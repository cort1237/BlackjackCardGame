package com.example.cardgametest;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.divider.MaterialDivider;

import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Objects;


public class BlackjackGameActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private TextView splitHandText;
    private ImageView card_image;
    private final Deck deck = new Deck();
    private LinearLayout tabLayout;
    private NetworkHandler netHandle;
    private Stats stats;
    private CustomPopupWindow roundEnd;
    private boolean MP_FLAG;
    private boolean HOST_FLAG;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private final ArrayList<Player> players = new ArrayList<>(); //Players 0 - Dealer; Player 1 - Host
    private int messageNum = 0;
    private TableLayout logTable;
    private ScrollView logScroll;
    private int playerID;
    private int currentTurn = 1;
    private int playerNum;
    private int minBet = 100;

    private float volumeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Will rework once there is a second background option
        ImageView imageview = findViewById(R.id.backgroundImageView);
        SharedPreferences background = getSharedPreferences("EquippedBackground", MODE_PRIVATE);
        boolean equippedBackground = background.getBoolean("Background 1", false);
        Log.d("Background 1 Equipped?" , " " + equippedBackground);
        if (equippedBackground) {
            imageview.setImageResource(R.drawable.background_1);
        }
        else {
            imageview.setImageResource(R.color.white);
        }
        imageview.setVisibility(View.VISIBLE);

        LinearLayout playerLayout = findViewById(R.id.main_player_hand);
        LinearLayout dealerLayout = findViewById(R.id.dealer_hand);
        tabLayout = findViewById(R.id.expandTab);
        logTable = findViewById(R.id.logTable);
        logScroll = findViewById(R.id.logTableView);
        moneyTextView = findViewById(R.id.moneyTextView);
        currentHandText = findViewById(R.id.viewHand);
        splitHandText = findViewById(R.id.viewSplit);
        card_image = findViewById(R.id.card_image);
        Button logButton = findViewById(R.id.logViewButton);

        players.add(new Player(0, dealerLayout, this));

        float scale = this.getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (scale * 50), (int) (scale * 70));
        card_image.setLayoutParams(params);


        MP_FLAG = Objects.equals(getIntent().getStringExtra("type"), "MP");

        //If in multiplayer, setup socket threads for each connection
        if(MP_FLAG) {
            HOST_FLAG = Objects.equals(getIntent().getStringExtra("host"), "HOST");
            netHandle = ((GameApplication) getApplication()).getNetworkHandler();
            minBet = getIntent().getIntExtra("min_bet", 100);
            playerMoney = getIntent().getIntExtra("start_money", 1000);
            Log.d("Net Handle ID", Integer.toString(netHandle.id));
            logButton.setVisibility(View.VISIBLE);

            //Initialize Players
            playerNum = getIntent().getIntExtra("players", 2);
            Log.d("playerNum", "" + playerNum);
            switch(playerNum) {
                case 4:
                    players.add(1, new Player(playerMoney,findViewById(R.id.Trow3 ) , findViewById(R.id.Rrow3),this));
                case 3:
                    players.add(1, new Player(playerMoney,findViewById(R.id.Trow2), findViewById(R.id.Rrow2),this));
                    tabLayout.removeView(findViewById(R.id.row3));
                case 2:
                    players.add(1, new Player(playerMoney,findViewById(R.id.Trow1), findViewById(R.id.Rrow1),this));
                    tabLayout.removeView(findViewById(R.id.row2));
                    tabLayout.removeView(findViewById(R.id.row3));
                default:
                    players.add(netHandle.id+1, new Player(playerMoney, playerLayout,this));
            }


            playerID = netHandle.id+1;
            String name = getIntent().getStringExtra("my_name");
            players.get(playerID).setNickname(name);
            sendAllMessage("PLAYER_NAME", name);

            for (Socket socket : netHandle.getClientSockets()) {
                Thread t = new Thread(()-> handleClientConnection(socket));
                t.start();
            }

        }
        else {
            players.add(new Player(playerMoney, playerLayout, this));
            playerID = 1;
        }

        int id_counter = 0;
        for (Player p : players) {
            p.setId(id_counter);
            id_counter++;
        }

        updateMoneyText();

        ImageButton optionsButton = findViewById(R.id.optionButton);
        optionsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), options.class);
            intent.putExtra("exit", 1);
            startActivity(intent);
        });

        //get prefereneces which contains values stored from options
        SharedPreferences prefs = getSharedPreferences("options", Context.MODE_PRIVATE);

        if(!prefs.contains("volume")) {

            SharedPreferences.Editor e = prefs.edit();
            e.putFloat("volume", volumeValue);
            e.apply();
        }
        volumeValue = prefs.getFloat("volume",0);

        //expand tab and button
        //tabLayout.bringToFront();
        Button tabButton = findViewById(R.id.expandButton);
        if(!MP_FLAG){
            tabButton.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }

        findViewById(R.id.expandButton).setOnClickListener(v -> {
            if(tabLayout.getVisibility() == View.VISIBLE){
                tabLayout.setVisibility(View.GONE);
                //roundEnd.showAtLocation(v, Gravity.CENTER, 0, -200);
            }
            else{
                tabLayout.setVisibility(View.VISIBLE);
                //roundEnd.dismiss();
            }
        });

        logButton.setOnClickListener(v -> {
            if(logScroll.getVisibility() == View.VISIBLE){
                logScroll.setVisibility(View.GONE);
                Log.d("Message Log", "Message log is hidden");
            } else {
                logScroll.setVisibility(View.VISIBLE);
                Log.d("Message Log", "Message log is visible");
            }
        });


        //Bet Add & Sub
        findViewById(R.id.betButtonAdd).setOnClickListener(v -> changeBetAmount(10));
        findViewById(R.id.betButtonSub).setOnClickListener(v -> changeBetAmount(-10));

        roundEnd = new CustomPopupWindow(this);

        stats = new Stats(getApplicationContext());
        findViewById(R.id.restart).setVisibility(View.INVISIBLE);
        findViewById(R.id.hitButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.foldButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
        resetGame();
    }

    @Override
    protected void onResume() {
        //get prefereneces which contains values stored from options
        SharedPreferences prefs = getSharedPreferences("options", Context.MODE_PRIVATE);

        if(!prefs.contains("volume")) {

            SharedPreferences.Editor e = prefs.edit();
            e.putFloat("volume", volumeValue);
            e.apply();
        }
        volumeValue = prefs.getFloat("volume",0);
        super.onResume();
        Log.d("Resumed", "Returned from settings");

    }

    //Adds a card to the player's Hand, and updates the Hand Layout.
    public void hit(View view){
        findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
        hitHelper();
        mediaPlayer.release();
        mediaPlayer=MediaPlayer.create(this,R.raw.drawn);
        mediaPlayer.setVolume(volumeValue,volumeValue);
        mediaPlayer.start();

        playHitAnimation(playerID);
    }

    private void playHitAnimation(int id){
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_animation);
        Player p = players.get(id);

        //players.get(0).getVisualHand().getX(); Example: Returns X of Dealer's Hand
        //new TranslateAnimation(0, 0, 0, 0); -- Example: Create an animation programmatically

        //Create an animation to move the card from the top right to the specified player
        slide = new TranslateAnimation(0, (p.getVisualHand().getX() + ((p.getVisualHand().getChildCount()-1) * card_image.getWidth())) - card_image.getX(),
                0, p.getVisualHand().getY() - card_image.getY());
        slide.setDuration(500);


        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                card_image.setVisibility(View.VISIBLE);
                card_image.setImageResource(R.drawable.cardbackblack);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                card_image.setVisibility(View.GONE);
                card_image.setImageResource(R.drawable.cardbackblack);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        card_image.startAnimation(slide);
    }

    private void resetGame() {
        //for each player
        for (Player p: players) {
            p.clearHand();
            p.stand = false;
            p.setBet(0);
        }
        currentTurn = 1;
        roundEnd.dismiss();
        clearLog();

        //Hide Play Buttons
        findViewById(R.id.hitButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.foldButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.restart).setVisibility(View.INVISIBLE);

        //Show Bet Buttons
        findViewById(R.id.betButton).setVisibility(View.VISIBLE);
        mediaPlayer=MediaPlayer.create(this,R.raw.register);
        mediaPlayer.setVolume(volumeValue, volumeValue);
        findViewById(R.id.betButtonAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.betButtonSub).setVisibility(View.VISIBLE);
        players.get(0).setDealerTurn(false);
    }

    public void resetGame(View view) {
        resetGame();
        if(MP_FLAG)
            sendAllMessage("RESET", " ");
    }

    private void hitHelper() {
        Player p = players.get(playerID);

        //Single Player
        if(!MP_FLAG) {
            if (!(p.getHand().isSplit())) {
                dealCard(p);
                Log.d("Hit Button Test", "hit");
                updateCurrentHand();
                if (p.getMainTotal() >= 21)
                    dealerTurn();
            } else if (p.getHand().getTotalValue(0) < 21) {
                dealCard(p, 0);
                Log.d("Hit Button Test", "hit");
                updateCurrentHand();
            } else {
                dealCard(p, 1);
                updateCurrentHand();
                if (p.getHand().getTotalValue(1) >= 21) {
                    dealerTurn();
                }
            }
        }
        else {
            int s;
            if (!(p.getHand().isSplit())) {
                sendAllMessage("Hit", "0");
                s = 0;
            } else if (p.getHand().getTotalValue(0) < 21) {
                sendAllMessage("Hit", "0");
                s = 0;
            } else {
                sendAllMessage("Hit", "1");
                s = 1;
            }
            if (HOST_FLAG) {
                Card c = deck.retrieveTop();
                String m = p.id + "," + c.toString() + ",0";
                dealCard(p, c, s);
                sendAllMessage("DealCard", m);
                if(checkHand(s)) {
                    sendAllMessage("Bust", " ");
                    p.stand = true;
                }
                disablePlayerControls();
                nextTurn();
            }
        }
    }


    //Adds a card to the specified Hand Layout on the screen
    public void bet(View view) {
        int bet = getBet();
        if (bet <= 0)
            return;
        removeMoney(bet);
        mediaPlayer.setVolume(volumeValue, volumeValue);
        mediaPlayer.start();
        // mediaPlayer.release();

        //Hide Bet Buttons
        findViewById(R.id.betButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.betButtonAdd).setVisibility(View.INVISIBLE);
        findViewById(R.id.betButtonSub).setVisibility(View.INVISIBLE);

        //If Single Player
        if(!MP_FLAG) {
            //Show Game Buttons
            findViewById(R.id.hitButton).setVisibility(View.VISIBLE);
            // mediaPlayer=MediaPlayer.create(this,R.raw.drawn);
            findViewById(R.id.foldButton).setVisibility(View.VISIBLE);

            setup();
        }
        else {
            players.get(playerID).setBet(bet);
            sendAllMessage("SetBet", Integer.toString(bet));
            if(HOST_FLAG)
                interpretMessage(new String[]{"0", "SetBet", Integer.toString(bet)});
        }
    }

    private void changeBetAmount(int amount) {
        int bet = getBet() + amount;
        if (bet < minBet)
            bet = minBet;
        String betString = ((TextView) findViewById(R.id.betTextView)).getText().toString();
        betString = betString.substring(0, 6) + bet;

        ((TextView) findViewById(R.id.betTextView)).setText(betString);
    }



    private int getBet() {
        String bet = ((TextView) findViewById(R.id.betTextView)).getText().toString().substring(6);
        return Integer.parseInt(bet);
    }

    //when fold button is clicked, this function will run
    //Fold also currently crashes the app
    public void foldHand(View view){
        foldHandHelper();
        Log.d("Fold Button Test", "fold");
    }
    private void foldHandHelper() {
        if(MP_FLAG) {
            players.get(playerID).stand = true;
            sendAllMessage("Stand", " ");
            if(HOST_FLAG) {
                disablePlayerControls();
                nextTurn();
            }
        }
        else {
            dealerTurn();
        }
    }

    //update the debug textview of current hand
    private void updateCurrentHand(){
        Player p = players.get(playerID);
        if(!p.getHand().isSplit()){
            StringBuilder currentHand = new StringBuilder();
            for(int i = 0; i < p.getHand().size(); i++){
                currentHand.append(" ").append(p.getHand().get(i).getRank());
                Log.d("card debug", "card #" + i + "| value: " + p.getHand().get(i).getRank());
            }

            currentHand.append(" (").append(p.getHand().getTotalValue()).append(")");
            currentHandText.setText(currentHand.toString());
        }
        else{
            StringBuilder hand1 = new StringBuilder();
            StringBuilder hand2 = new StringBuilder();
            for(int i = 0; i < p.getHand().retrieveHand(0).size(); i++){
                hand1.append(" ").append(p.getHand().retrieveHand(0).get(i).getRank());
                Log.d("hand 1 debug", "card #" + i + "| value: " + p.getHand().retrieveHand(0).get(i).getRank());
            }
            for(int i = 0; i < p.getHand().retrieveHand(1).size(); i++){
                hand2.append(" ").append(p.getHand().retrieveHand(1).get(i).getRank());
                Log.d("hand 2 debug", "card #" + i + "| value: " + p.getHand().retrieveHand(1).get(i).getRank());

            }


            currentHandText.setText(hand1.toString());
            splitHandText.setText(hand2.toString());

        }
    }

    //when split button is pushed, split hand and make button invisible
    public void splitButton(View view){
        Player p = players.get(playerID);
        if(!p.getHand().splitHand()){
            Log.d("split debug", "Player cannot split hand");
        }
        else{
            Log.d("split debug", "Player successfully split");
            findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.viewSplit).setVisibility(View.VISIBLE);
            p.split();
            updateCurrentHand();
        }
    }

    private void updateMoneyText() {
        moneyTextView.setText(MessageFormat.format("{0}", players.get(playerID).getMoney()));
    }

    // Function to add money
    public void addMoney(Player p, int amount) {
        stats.recordCurrency(amount);
        p.setMoney(p.getMoney() + amount);
        if(MP_FLAG && p.id == playerID)
            sendAllMessage("SetMoney", Integer.toString(p.getMoney()));
        runOnUiThread(this::updateMoneyText);
    }

    public void addMoney(int amount) {
        addMoney(players.get(playerID), amount);
    }

    // Function to remove money
    public void removeMoney(Player p, int amount) {
        p.setMoney(p.getMoney() - amount);
        if(p.getMoney() < 0)
            p.setMoney(0);
        runOnUiThread(this::updateMoneyText);
        if(MP_FLAG && p.id == playerID)
            sendAllMessage("SetMoney", Integer.toString(p.getMoney()));
        Log.d("removeMoney", "Player " + p.id + " | Money: " + p.getMoney());
    }

    public void removeMoney(int amount) {
        removeMoney(players.get(playerID), amount);
    }


    //Will hand out two cards to each entity at the table
    private void setup() {
        for (Player p: players) {
            p.clearHand();
        }

        for (int i=0; i<2; i++)  {
            hitHelper();
            dealCard(players.get(0));
        }

        Log.d("dealer.getTotalValue", "" + players.get(0).getHand().getTotalValue());
        Log.d("playerHand.getTotalValue", "" + players.get(playerID).getHand().getTotalValue());

        // Early version of standard Blackjack peek procedure (still needs to be tied to actual animation)
        // Animation should play if dealer's visible card is an "Ace" or "Face" card, regardless if getTotalValue()==21
        if(players.get(0).getHand().getTotalValue() == 21 || players.get(1).getHand().getTotalValue() == 21) {
            dealerTurn();
        }
        else {
            //Enable Player Controls
            findViewById(R.id.hitButton).setEnabled(true);
            findViewById(R.id.foldButton).setEnabled(true);
            findViewById(R.id.restart).setEnabled(false);
            if(players.get(playerID).getHand().isPair()){
                findViewById(R.id.splitButton).setVisibility(View.VISIBLE);
            }
            else{
                findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
            }
            findViewById(R.id.restart).setVisibility(View.INVISIBLE);
            findViewById(R.id.viewSplit).setVisibility(View.INVISIBLE);

            updateCurrentHand();
        }
    }

    private void setupMP() {
        for (int i = 0; i < 2; i++) {
            for (Player p : players) {
                Card c = deck.retrieveTop();
                String m = p.id + "," + c.toString() + ",0";
                dealCard(p,c,0);
                sendAllMessage("DealCard",m);
            }
        }

        sendAllMessage("TurnStart", Integer.toString(currentTurn));
        runOnUiThread(this::enablePlayerControls);
        runOnUiThread(this::showPlayerControls);
    }

    private void nextTurn() {
        //Check for dealer's turn
        boolean dealersTurn = true;
        for (Player p : players) {
            if(p.id == 0)
                continue;
            if (!p.stand) {
                dealersTurn = false;
                break;
            }
        }

        if(dealersTurn) {
            dealerTurn();
            return;
        }



        if(currentTurn == playerNum)
            currentTurn = 1;
        else
            currentTurn++;

        //If player is out, go to next player.
        if(players.get(currentTurn).stand) {
            nextTurn();
            return;
        }

        if(currentTurn == playerID)
            runOnUiThread(this::enablePlayerControls);
        sendAllMessage("TurnStart", Integer.toString(currentTurn));
    }

    private void disablePlayerControls() {
        findViewById(R.id.hitButton).setEnabled(false);
        findViewById(R.id.foldButton).setEnabled(false);
        //findViewById(R.id.splitButton).setEnabled(false);
    }

    private void enablePlayerControls() {
        findViewById(R.id.hitButton).setEnabled(true);
        findViewById(R.id.foldButton).setEnabled(true);
        //findViewById(R.id.splitButton).setEnabled(true);
    }

    private void showPlayerControls() {
        findViewById(R.id.hitButton).setVisibility(View.VISIBLE);
        findViewById(R.id.foldButton).setVisibility(View.VISIBLE);
        //findViewById(R.id.splitButton).setVisibility(View.VISIBLE);
    }

    private void hidePlayerControls() {
        findViewById(R.id.hitButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.foldButton).setVisibility(View.INVISIBLE);
        //findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
    }

    private Card dealCard(Player p, int split) {
        Card topCard = deck.retrieveTop();
        p.addCard(topCard, split);
        runOnUiThread(p::refreshHand);
        Log.d("dealCard Test", "Dealer Card: " + topCard.getRank());
        return topCard;
    }

    private void dealCard(Player p, Card c, int split) {
        p.addCard(c, split);
        runOnUiThread(p::refreshHand);
        Log.d("dealCard Test", "Card: " + c.getRank());
    }

    private Card dealCard(Player p) {
        return dealCard(p, 0);
    }

    private void dealerTurn() {
        players.get(0).setDealerTurn(true);
        runOnUiThread(()->players.get(0).refreshHand());


        if(!MP_FLAG) {
            int dealerTotal = players.get(0).getMainTotal(),
                    playerTotal = players.get(playerID).getMainTotal();

            //Disable Player controls
            findViewById(R.id.hitButton).setEnabled(false);
            findViewById(R.id.foldButton).setEnabled(false);
            findViewById(R.id.restart).setEnabled(true);
            findViewById(R.id.restart).setVisibility(View.VISIBLE);

            //Insert win view below

            while (dealerTotal < 17) {
                Card card = dealCard(players.get(0));
                dealerTotal += card.getValue();
                Log.d("dealerTurn Test", "Current Dealer Total: " + dealerTotal);
            }

            View rootView = findViewById(android.R.id.content);

            if (dealerTotal > 21) {
                Log.d("dealerTurn Test", "Final Dealer Total: " + dealerTotal + " BUST");
                if (playerTotal <= 21) {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " WIN");
                    stats.recordWin();
                    roundEnd.setMessage("PLAYER WINS");
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(this, R.raw.cheer);
                    mediaPlayer.setVolume(volumeValue,volumeValue);
                    mediaPlayer.start();

                    addMoney(getBet() * 2);
                } else {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " BUST");
                    stats.recordLoss();
                    roundEnd.setMessage("PLAYER BUSTS");
                }
                roundEnd.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            } else {
                Log.d("dealerTurn Test", "Final Dealer Total: " + dealerTotal);
                if (playerTotal > dealerTotal && playerTotal <= 21) {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " WIN");
                    stats.recordWin();
                    roundEnd.setMessage("PLAYER WINS");
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(this, R.raw.cheer);
                    mediaPlayer.setVolume(volumeValue,volumeValue);
                    mediaPlayer.start();

                    addMoney(getBet() * 2);
                } else if (playerTotal == dealerTotal) {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " PUSH");
                    stats.recordPush();
                    roundEnd.setMessage("PUSH");
                    addMoney(getBet());
                } //should stats of a draw be recorded?
                else {
                    stats.recordLoss();
                    if (playerTotal < 21) {
                        Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " LOSS");
                        roundEnd.setMessage("PLAYER LOSES");
                        mediaPlayer.release();
                        mediaPlayer = MediaPlayer.create(this, R.raw.losing);
                        mediaPlayer.setVolume(volumeValue,volumeValue);
                        mediaPlayer.start();
                    } else {
                        Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " BUST");
                        roundEnd.setMessage("PLAYER BUST");
                    }
                }
                roundEnd.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            }

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                roundEnd.dismiss();
//            }
//        }, 2500);

            Log.d("End Stats", "Wins: " + stats.getData()[0]);
            Log.d("End Stats", "Losses: " + stats.getData()[1]);
            Log.d("End Stats", "Total Games: " + stats.getData()[2]);
        }
        else if (HOST_FLAG){
            sendAllMessage("flipDealer", " ");
            Player dealer = players.get(0);
            while(dealer.getMainTotal() < 17) {
                Log.d("Drawing Card", "Dealer Total: " + dealer.getMainTotal());

                Card c = deck.retrieveTop();
                dealer.addCard(c);
                runOnUiThread(dealer::refreshHand);
                String m = 0 + "," + c.toString() + ",0";
                sendAllMessage("DealCard", m);
            }
            sendAllMessage("RoundEnd", " ");
            checkScore();
        }
    }

    private void checkScore() {
        Player dealer = players.get(0);
        Player p = players.get(playerID);

        if(p.getMainTotal() > 21) {
            Log.d("checkScore", "Final Player Total: " + p.getMainTotal() + " BUST");
            sendAllMessage("Lose", " ");
            roundEnd.setMessage("PLAYER BUSTS");

        }
        else if(dealer.getMainTotal() > p.getMainTotal() && dealer.getMainTotal() <= 21) {
            Log.d("checkScore", "Final Player Total: " + p.getMainTotal() + " LOSE");
            Log.d("checkScore", "Final Dealer Total: " + dealer.getMainTotal() + " WIN");
            sendAllMessage("Lose", " ");
            roundEnd.setMessage("PLAYER LOSES");

        }
        else if ((dealer.getMainTotal() < p.getMainTotal()) || (dealer.getMainTotal() > 21)) {
            Log.d("checkScore", "Final Player Total: " + p.getMainTotal() + " WIN");
            Log.d("checkScore", "Final Dealer Total: " + dealer.getMainTotal() + " LOSE");
            sendAllMessage("Win", " ");
            if(dealer.getMainTotal() == 21)
                addMoney(p.getBet() * 3);
            else
                addMoney(p.getBet() * 2);
            roundEnd.setMessage("PLAYER WINS");
        }
        else {
            addMoney(p.getBet());
            sendAllMessage("Tie", " ");
            roundEnd.setMessage("PUSH");
        }
        sendAllMessage("SetMoney", Integer.toString(p.getMoney()));
        sendAllMessage("SetBet", "0");
        runOnUiThread(()-> roundEnd.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0));


        if(HOST_FLAG)
            runOnUiThread(() -> {
                findViewById(R.id.restart).setVisibility(View.VISIBLE);
                findViewById(R.id.restart).setEnabled(true);});


    }

    /**
     * Checks for messages on the provided socket.
     * Messages should be formatted the following way: "Source_ID : Job : Message"
     */
    private void handleClientConnection(Socket socket) {
        Log.d("Thread Started", "Listener Thread (" + socket.getInetAddress().toString() + ")");
        while (true) {
            String message = netHandle.receiveMessageFromClient(socket);
            if (message != null) {
                //Log.d("Received Message", message);

                // Handle the received message
                String[] args = message.split(" : ");
                if(HOST_FLAG) {
                    netHandle.sendToAllClients(message); //If message recieved by host echo message to all clients.
                    interpretMessage(args);
                }
                //Since every message gets echoed to all clients. It will get returned to sender, this will ignore the message.
                else if(Integer.parseInt(args[0]) != netHandle.id) {
                    interpretMessage(args);
                }
            }
        }
    }

    private void interpretMessage(String[] args) {
        String job = args[1];
        String message = args[2];
        int id = Integer.parseInt(args[0]) + 1;
        if(id != playerID)
            logMessage(job, message, id);


        switch(job) {
            case "SetBet":  //SetBet - Set the bet of id to `message`
                players.get(id).setBet(Integer.parseInt(message));

                //Check for all bets if Host
                if(HOST_FLAG) {
                    boolean start = true;
                    for (Player p : players) {
                        Log.d("BetCheck", "Player " + p.id + " | Bet: " + p.getBet());
                        if(p.id == 0) //Ignore Dealer Hand
                            continue;
                        if(p.getBet() == 0) //If any bets unset, set false
                            start = false;
                    }
                    if(start) {
                        Log.d("GameFlow", "All Bets Received. Dealing Starting Hand...");
                        setupMP(); //Start Game
                    }
                }

                break;
            case "DealCard": //Deal the Specified Card to player id in message[0] (Always comes from host)
                String[] arg = message.split(","); //"id,value of suit,splitHandSpecifier"
                dealCard(players.get(Integer.parseInt(arg[0])), new Card(arg[1]), Integer.parseInt(arg[2]));
                if(Integer.parseInt(arg[0]) == playerID && checkHand(Integer.parseInt(arg[2]))) {
                    sendAllMessage("Bust", " ");
                }
                runOnUiThread(this::disablePlayerControls);
                break;
            case "SetMoney": //SetMoney - Set the current Money of the player
                players.get(id).setMoney(Integer.parseInt(message));
                runOnUiThread(() -> players.get(id).refreshHand());
                break;
            case "Hit":
                if(!HOST_FLAG)
                    break;
                Card c = dealCard(players.get(id), Integer.parseInt(message));
                String m = id + "," + c + "," + Integer.parseInt(message);
                sendAllMessage("DealCard", m);
                nextTurn();
                break;
            case "flipDealer":
                players.get(0).setDealerTurn(true);
                runOnUiThread(()->players.get(0).refreshHand());
                break;
            case "Split":
                break;
            case "Stand":
            case "Bust":
                players.get(id).stand = true;
                nextTurn();
                break;
            case "TurnStart":
                if(Integer.parseInt(message) != playerID) {
                    runOnUiThread(this::showPlayerControls);
                    runOnUiThread(this::disablePlayerControls);
                    break;
                }
                runOnUiThread(this::enablePlayerControls);
                break;
            case "RoundEnd":
                if(players.get(0).getMainTotal() > 21)
                    logMessage("Bust", " ", 0);
                runOnUiThread(this::hidePlayerControls);
                checkScore();
                break;
            case "PLAYER_NAME":
                players.get(id).setNickname(message);
            case "RESET":
                runOnUiThread(this::resetGame);
                break;
            default:
                break;
        }



    }

    private boolean checkHand(int splitHand) {
        Player p = players.get(playerID);
        if (splitHand == 2) {
            Log.d("checkHand", "1 | " + p.getHand().getTotalValue(1));
            return p.getHand().getTotalValue(1) > 21;
        }
        Log.d("checkHand", playerID + " | " + p.getHand().getTotalValue(0));
        return p.getHand().getTotalValue() > 21;
    }

    @SuppressLint("SetTextI18n")
    private void logMessage(String job, String message, int id){
        String fullMessage = generateMessage(job, message, id);
        if(fullMessage == null)
            return;
        TableRow newRow = new TableRow(this);
        newRow.setId(++messageNum);
        int bgColor;
        if(messageNum % 2 == 0){
            bgColor = Color.LTGRAY;
        } else {bgColor = Color.WHITE;}
        newRow.setBackgroundColor(bgColor);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        TextView numCol = new TextView(this);
        numCol.setText(Integer.toString(messageNum));
        numCol.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        newRow.addView(numCol);
        TextView messageView = new TextView(this);
        messageView.setText(fullMessage);
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        newRow.addView(messageView);
        runOnUiThread(()-> logTable.addView(newRow));
    }

    private String generateMessage(String job, String message, int id){
        String msg = null;
        switch(job){
            case "SetBet":  //SetBet - Set the bet of id to `message`
                if(Integer.parseInt(message) == 0) //Don't log bet reset
                    break;
                msg = "Player " + id + " has bet $" + message;
                break;
            case "DealCard": //Deal the Specified Card to player id in message[0] (Always comes from host)
                String[] parts = message.split(",");
                msg = "Player " + parts[0] + " has drawn the " + parts[1];
                id = Integer.parseInt(parts[0]);
                break;
            case "Hit":
                msg = "Player " + id + " hits.";
                break;
            case "Split":
                msg = "Player " + id + " has split their hand.";
                break;
            case "Stand":
                msg = "Player " + id +" stands.";
                break;
            case "Bust":
                msg = "Player "+ id +" has busted.";
                break;
            default:
                break;
        }
        if(id==0)
            msg = "The Dealer " + msg.split(" ", 3)[2];
        return msg;
    }

    private void clearLog(){
        logTable.removeAllViews();
        messageNum = 0;
    }

    /** @noinspection deprecation*/
    private void sendAllMessage(String job, String message) {
        new MessageSender().execute(String.format(getResources().getConfiguration().getLocales().get(0), "%d : %s : %s", netHandle.id, job, message));
    }

    private class MessageSender extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d("Sending Message", params[0]);
            String[] args = params[0].split(" : ");
            logMessage(args[1], args[2], Integer.parseInt(args[0])+1);
            netHandle.sendToAllClients(params[0]);
            return null;
        }
    }

}

 class Player {
    private int money;
    private int bet;
    private final float scale;
    private boolean split;
    public boolean stand;
    private LinearLayout visualHand;
    private LinearLayout visualRow;
    private LinearLayout splitHand1;
    private LinearLayout splitHand2;
    private final CardHand gameHand;
    private final Context parentContext;
    int id;
    boolean dealerTurn = false;

    private LinearLayout vr; // each tab in the side bar has two rows for formatting
    String nickname;        // vr contains images and visual row contains the nickname
    Player(int money, LinearLayout row, LinearLayout vr, Context c) {
        this.money = money;
        this.visualRow = row; //Name and money
        this.gameHand = new CardHand();
        this.parentContext = c;
        this.bet = 0;
        this.visualHand = vr; //Cards
        scale = c.getResources().getDisplayMetrics().density;
        split = false;
        stand = false;
    }

    Player(int money, LinearLayout row, Context c) {
        this.money = money;
        this.visualHand = row;
        this.gameHand = new CardHand();
        this.parentContext = c;
        this.bet = 0;
        scale = c.getResources().getDisplayMetrics().density;
        split = false;
        stand = false;
    }

    public CardHand getHand() {
        return gameHand;
    }

    public void addCard(Card c, int split) {
        gameHand.addCard(c, split);
    }

    public void setDealerTurn(boolean turn){
        dealerTurn = turn;
    }

    public void addCard(Card c) {
        addCard(c, 0);
    }

    public void clearHand() {
        if (gameHand.isSplit()) {
            splitHand1.removeAllViews();
            splitHand2.removeAllViews();
        }
        if (visualHand != null) {
            visualHand.removeAllViews();

        }
        else{
            vr.removeAllViews();
        }
        gameHand.clearHand();
        split = false;
    }

    public int getMoney() {
        return money;
    }

    public int getBet() {
        return bet;
    }

    public void setMoney(int m) {
        money = m;
    }

    public void setBet(int b) {
        bet = b;
    }

    public void setNickname(String s) {
        nickname = s;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMainTotal() {
        return gameHand.getTotalValue();
    }

    public void refreshHand(){
        int margin;
        int cardCount = gameHand.size();
        if (gameHand.isSplit())
            margin = 8 - ((cardCount - 3) * 80);
        else if (cardCount > 2)
            margin = 8 - ((cardCount - 1) * 40);
        else
            margin = 8;
        if (visualHand != null) {
            visualHand.removeAllViews();
        }
        if( vr != null) {
            vr.removeAllViews();
        }
        try {
            gameHand.retrieveHand(0).forEach((card -> addCardToHand(card, margin, 0)));
            if (split)
                gameHand.retrieveHand(1).forEach((card -> addCardToHand(card, margin, 1)));
        }
        catch (ConcurrentModificationException e)
        {
            try {
                sleep(100);
            } catch (InterruptedException ignored) {}
            visualHand.removeAllViews();
            gameHand.retrieveHand(0).forEach((card -> addCardToHand(card, margin, 0)));
            if (split) {
                ImageView space = new ImageView(parentContext);
                space.setImageResource(R.drawable.cardbackblack);
                space.setVisibility(View.INVISIBLE);
                visualHand.addView(space);
                gameHand.retrieveHand(1).forEach((card -> addCardToHand(card, margin, 1)));
            }
        }
    }

    public LinearLayout getVisualHand() {
        return visualHand;
    }

    private void addCardToHand(Card c, int margin, int s) {
        //Log.d("AddCardToHand", c.getSuit() + " | " + c.getRank());
        ImageView cardView = new ImageView(parentContext);
        int cardImageNum;
        if(id == 0 && visualHand.getChildCount() == 1 && !dealerTurn)
            cardImageNum = c.getCardBack();
        else
            cardImageNum = c.getCardImage(parentContext);
        cardView.setImageResource(cardImageNum);

        if (visualHand != null) { //Main Screen Layout
            if (visualHand.getChildCount() == 0)
                margin = 0;
            if (margin < -130)
                margin = -160;
            Log.d("addCardToHand", "Margin: " + (int) ((margin / 2) * scale));
            Log.d("addCardToHand", "PARAMS: (" + (int) (scale * 100) + ", " + (int) (scale * 130) + ")");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (scale * 100), (int) (scale * 130));

            params.setMargins((int) ((margin / 2) * scale), 8, 100, 8);
            params.gravity = Gravity.START | Gravity.TOP;
            cardView.setLayoutParams(params);
            visualHand.addView(cardView);
            /*
            if (!split)

            else {
                Log.d("addCardToHand", "Split");
                if (s == 0) {
                    Log.d("addCardToHand", "Split1");
                    splitHand1.addView(cardView);
                    TextView t = new TextView(parentContext);
                    t.setText("test");
                    splitHand1.addView(t);
                } else {
                    Log.d("addCardToHand", "Split2");
                    splitHand2.addView(cardView);
                }
            }*/
        }  //Side Bar Layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 217);
        params.setMargins(4, 8, -3, 8);


        TextView tview = new TextView(parentContext, null, 0, R.style.customTextStyle);
        tview.setText(MessageFormat.format("{0} - ${1}", this.nickname, this.getMoney()));
        //cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
        //LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        cardView.setLayoutParams(params);
        if(vr != null) {
            this.vr.addView(cardView);
        }
        if(visualRow != null){
                visualRow.removeAllViews();
                visualRow.addView(tview);
        }

    }

    public void split() {
        gameHand.splitHand();
        split = true;
        /*splitHand1 = new LinearLayout(parentContext);
        splitHand2 = new LinearLayout(parentContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        splitHand1.setOrientation(LinearLayout.HORIZONTAL);
        splitHand2.setOrientation(LinearLayout.HORIZONTAL);
        splitHand1.setLayoutParams(lp);
        splitHand2.setLayoutParams(lp);
        visualHand.addView(splitHand1);
        visualHand.addView(splitHand2);*/
        refreshHand();
    }
}
