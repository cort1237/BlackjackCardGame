package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import com.example.cardgametest.R.color.*;

import org.w3c.dom.Text;

import org.w3c.dom.Text;

import java.net.Socket;
import java.util.ArrayList;

public class BlackjackGameActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private TextView splitHandText;
    private CardHand playerHand = new CardHand();
    private LinearLayout playerLayout;
    private LinearLayout splitLayout1;
    private LinearLayout splitLayout2;
    private CardHand dealerHand = new CardHand();
    private LinearLayout dealerLayout;
    private Deck deck = new Deck();
    private TableLayout tabLayout;
    private NetworkHandler netHandle;
    private Stats stats;
    private CustomPopupWindow roundEnd;
    private boolean MP_FLAG;
    private boolean HOST_FLAG;
    MediaPlayer mediaPlayer;
    private ArrayList<Player> players = new ArrayList<Player>();
    private int messageNum = 0;
    private TableLayout logTable;
    private ScrollView logScroll;
    private Button logButton;
    private float scale;
    private int playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scale = getResources().getDisplayMetrics().density;

        playerLayout = findViewById(R.id.main_player_hand);
        splitLayout1 = findViewById(R.id.split_player_hand_1);
        splitLayout2 = findViewById(R.id.split_player_hand_2);
        dealerLayout = findViewById(R.id.dealer_hand);
        tabLayout = findViewById(R.id.expandTab);
        logTable = findViewById(R.id.logTable);
        logScroll = findViewById(R.id.logTableView);
        // Money Text Field
        moneyTextView = findViewById(R.id.moneyTextView);
        currentHandText = findViewById(R.id.viewHand);
        splitHandText = findViewById(R.id.viewSplit);
        //logTable = findViewById(R.id.logTable);
        logButton = findViewById(R.id.logViewButton);
        updateMoneyText();
        MP_FLAG = getIntent().getStringExtra("type").equals("MP");

        //If in multiplayer, setup socket threads for each connection
        if(MP_FLAG) {
            HOST_FLAG = getIntent().getStringExtra("host").equals("HOST");
            netHandle = ((GameApplication) getApplication()).getNetworkHandler();
            Log.d("Net Handle ID", Integer.toString(netHandle.id));
            for (Socket socket : netHandle.getClientSockets()) {
                Thread t = new Thread(()->{handleClientConnection(socket);});
                t.start();
            }
            logButton.setVisibility(View.VISIBLE);
        }

        ImageButton optionsButton = findViewById(R.id.optionButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), options.class);
                intent.putExtra("exit", 1);
                startActivity(intent);
            }
        });


        //expand tab and button
        tabLayout.bringToFront();
        Button tabButton = findViewById(R.id.expandButton);
        if(MP_FLAG){
            tabButton.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }

        findViewById(R.id.expandButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("YUP", "onClick: ");
                if(tabLayout.getVisibility() == View.VISIBLE){
                    tabLayout.setVisibility(View.GONE);
                    //roundEnd.showAtLocation(v, Gravity.CENTER, 0, -200);
                }
                else{
                    tabLayout.setVisibility(View.VISIBLE);
                    //roundEnd.dismiss();
                }
            }
        });

        logButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(logScroll.getVisibility() == View.VISIBLE){
                    logScroll.setVisibility(View.GONE);
                    Log.d("Message Log", "Message log is hidden");
                } else {
                    logScroll.setVisibility(View.VISIBLE);
                    Log.d("Message Log", "Message log is visible");
                }
            }
        });


        //Bet Add & Sub
        findViewById(R.id.betButtonAdd).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               changeBetAmount(10);
           }
        });
        findViewById(R.id.betButtonSub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBetAmount(-10);
            }
        });

        roundEnd = new CustomPopupWindow(this);

        stats = new Stats(getApplicationContext());
        findViewById(R.id.restart).setVisibility(View.INVISIBLE);
        findViewById(R.id.hitButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.foldButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
        resetGame();
    }
    // generate the hands for each row in the side bar
    protected void generateHand(){
        int NUM_PLAYERS = players.size();
        if(NUM_PLAYERS != 0){
        CardHand handList[] = new CardHand[NUM_PLAYERS];

        TableRow.LayoutParams params = new TableRow.LayoutParams(150,217);
        params.setMargins(4,8,-30,8);
        TableRow tabLayout1 = findViewById(R.id.row1);
        TableRow tabLayout2 = findViewById(R.id.row2);
        TableRow tabLayout3 = findViewById(R.id.row3);
        TableRow t[] = new TableRow[3];
        t[0] = tabLayout1;
        t[1] = tabLayout2;
        t[2] = tabLayout3;
        for(int x = 0; x < NUM_PLAYERS; x++) {
            t[x].removeAllViews();
            ArrayList<Card> hand = handList[x].retrieveHand();
            TextView tview = new TextView(this, null , 0 , R.style.customTextStyle);
            t[x].addView(tview);

            for (int i = 0; i < handList[x].size(); i++) {
                ImageView cardView = new ImageView(this);
                cardView.setImageResource(hand.get(i).getCardImage());
                cardView.setLayoutParams(params);
                t[x].addView(cardView);
            }
        }
    }}
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Resumed", "Returned from settings");

    }

    //Adds a card to the player's Hand, and updates the Hand Layout.
    public void hit(View view){
        findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
        hitHelper();
        mediaPlayer.release();
        mediaPlayer=MediaPlayer.create(this,R.raw.drawn);
        mediaPlayer.start();
    }

    private void resetGame() {
        //for each player
        if(playerHand.isSplit()){
            splitLayout1.removeAllViews();
            splitLayout2.removeAllViews();
        }
        playerHand.clearHand();
        playerLayout.removeAllViews();
        dealerHand.clearHand();
        dealerLayout.removeAllViews();
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
        findViewById(R.id.betButtonAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.betButtonSub).setVisibility(View.VISIBLE);
    }

    public void resetGame(View view) {
        resetGame();
    }

    private void hitHelper() {
        if(!playerHand.isSplit()) {
            dealCard(playerHand, playerLayout);
            Log.d("Hit Button Test", "hit");
            updateCurrentHand();

            if (playerHand.getTotalValue() >= 21)
                dealerTurn();
        } else if(playerHand.getTotalValue(0) < 21) {
            dealCard(playerHand, splitLayout1);
            Log.d("Hit Button Test", "hit");
            updateCurrentHand();
        } else {
            dealCard(playerHand, splitLayout2, 1);
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
            margin = -160;
        ImageView cardView = new ImageView(this);
        cardView.setImageResource(c.getCardImage());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(scale*100),(int)(scale*130));
        Log.d("Margin", Integer.toString(margin));
        params.setMargins((int)((margin/2)*scale),8,0,8);
        cardView.setLayoutParams(params);
        mainPlayerHand.addView(cardView);
        generateHand();
    }

    public void bet(View view) {
        int bet = getBet();
        if (bet <= 0)
            return;
        removeMoney(bet);

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
            sendAllMessage("SetBet", Integer.toString(bet));
        }
    }

    private void changeBetAmount(int amount) {
        int bet = getBet() + amount;
        if (bet < 0)
            bet = 0;
        String betString = ((TextView) findViewById(R.id.betTextView)).getText().toString();
        betString = betString.substring(0, 6) + Integer.toString(bet);

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
        dealerTurn();
    }

    //update the debug textview of current hand
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
            findViewById(R.id.splitButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.viewSplit).setVisibility(View.VISIBLE);
            playerLayout.removeAllViews();
            refreshHand(playerHand, splitLayout1, 0);
            refreshHand(playerHand, splitLayout2, 1);
            updateCurrentHand();
        }
    }


    private void updateMoneyText() {
        moneyTextView.setText("" + playerMoney);
    }

    // Function to add money
    public void addMoney(int amount) {
        stats.recordCurrency(amount);
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
            findViewById(R.id.hitButton).setEnabled(true);
            findViewById(R.id.foldButton).setEnabled(true);
            findViewById(R.id.restart).setEnabled(false);
            if(playerHand.isPair()){
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

    private Card dealCard(CardHand entity, LinearLayout handLayout, int split) {
        Card topCard = deck.retrieveTop();
        entity.addCard(topCard, split);
        refreshHand(entity, handLayout, split);
        Log.d("dealCard Test", "Dealer Card: " + topCard.getRank());
        return topCard;
    }

    private void refreshHand(CardHand hand, LinearLayout handLayout, int split) {
        int margin;
        int cardCount = handLayout.getChildCount();
        if(hand.isSplit())
            margin = 8 - ((cardCount) * 80);
        else if(cardCount > 2)
            margin = 8 - ((cardCount-1) * 40);
        else
            margin = 8;
        handLayout.removeAllViews();
        hand.retrieveHand(split).forEach((card -> addCardToHand(handLayout, card, margin)));
    }

    private Card dealCard(CardHand entity, LinearLayout handLayout) {
        return dealCard(entity, handLayout, 0);
    }

    private void dealerTurn() {
        int dealerTotal = dealerHand.getTotalValue(), playerTotal = playerHand.getTotalValue();
        //dealerTotal = 16;

        //Disable Player controls
        findViewById(R.id.hitButton).setEnabled(false);
        findViewById(R.id.foldButton).setEnabled(false);
        findViewById(R.id.restart).setEnabled(true);
        findViewById(R.id.restart).setVisibility(View.VISIBLE);

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
                mediaPlayer.release();
                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
                mediaPlayer.start();
//                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
//                mediaPlayer.start();
//                mediaPlayer.release();
                addMoney(getBet()*2);
            }
            else {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " BUST");
                stats.recordLoss();
                roundEnd.setMessage("PLAYER BUSTS");
            }
            roundEnd.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        }
        else {
            Log.d("dealerTurn Test", "Final Dealer Total: " + dealerTotal);
            if (playerTotal > dealerTotal && playerTotal<=21) {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " WIN");
                stats.recordWin();
                roundEnd.setMessage("PLAYER WINS");
                mediaPlayer.release();
                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
                mediaPlayer.start();
//                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
//                mediaPlayer.start();
//                mediaPlayer.release();
                addMoney(getBet()*2);
            }
            else if (playerTotal == dealerTotal) {
                Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " PUSH");
                stats.recordPush();
                roundEnd.setMessage("PUSH");
                addMoney(getBet());
            } //should stats of a draw be recorded?
            else {
                stats.recordLoss();
                if (playerTotal<21) {
                    Log.d("dealerTurn Test", "Final Player Total: " + playerTotal + " LOSS");
                    roundEnd.setMessage("PLAYER LOSES");
                    mediaPlayer.release();
                    mediaPlayer=MediaPlayer.create(this,R.raw.losing);
                    mediaPlayer.start();
                }
                else {
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

    /**
     * Checks for messages on the provided socket.
     * Messages should be formatted the following way: "Source_ID : Job : Message"
     * @param socket
     */
    private void handleClientConnection(Socket socket) {
        Log.d("Thread Started", "Listener Thread (" + socket.getInetAddress().toString() + ")");
        while (true) {
            String message = netHandle.receiveMessageFromClient(socket);
            if (message != null) {
                Log.d("Received Message", message);

                // Handle the received message
                String[] args = message.split(" : ");
                if(HOST_FLAG) {
                    netHandle.sendToAllClients(message); //If message recieved by host echo message to all clients.
                    interpretMessage(args);
                }
                //Since every message gets echoed to all clients. It will get returned to sender, this will ignore the message.
                else if(Integer.parseInt(args[0]) == netHandle.id) {
                    continue;
                }
                else {
                    interpretMessage(args);
                }
            }
        }
    }

    private void interpretMessage(String[] args) {
        String job = args[1];
        String message = args[2];
        int id = Integer.parseInt(args[0]);
        logMessage(job, message, id);


    }
    private void logMessage(String job, String message, int id){
        int playerID = id + 1;
        String fullMessage = "Player " + playerID + " " + job + " " + message;
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
        runOnUiThread(()->{logTable.addView(newRow);});
    }

    private void clearLog(){
        logTable.removeAllViews();
        messageNum = 0;
    }

    private void sendAllMessage(int id, String job, String message) {
        new MessageSender().execute(String.format("%d : %s : %s", id, job, message));
    }
    private void sendAllMessage(String job, String message) {
        new MessageSender().execute(String.format("%d : %s : %s", netHandle.id, job, message));
    }

    private class MessageSender extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d("Sending Message", params[0]);
            String[] args = params[0].split(" : ");
            logMessage(args[1], args[2], Integer.parseInt(args[0]));
            netHandle.sendToAllClients(params[0]);
            return null;
        }
    }

}

class Player {
    private int money;
    private int bet;
    private int id;
    public LinearLayout visualHand;
    public LinearLayout splitHand1;
    public LinearLayout splitHand2;
    public CardHand gameHand;

    Player(int money, int id, CardHand gameHand, LinearLayout visualHand) {
        this.money = money;
        this.id = id;
        this.gameHand = gameHand;
        this.visualHand = visualHand;
    }

    public CardHand getHand(){
        return this.gameHand;
    }
}