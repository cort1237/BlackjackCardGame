package com.example.cardgametest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.LoggingPermission;


public class BlackjackGameActivity extends AppCompatActivity {

    private int playerMoney = 1000; // Example initial money amount
    private TextView moneyTextView;
    private TextView currentHandText;
    private TextView splitHandText;
    private final Deck deck = new Deck();
    private LinearLayout tabLayout;
    private NetworkHandler netHandle;
    private Stats stats;
    private CustomPopupWindow roundEnd;
    private boolean MP_FLAG;
    private boolean HOST_FLAG;
    MediaPlayer mediaPlayer;
    private final ArrayList<Player> players = new ArrayList<>(); //Players 0 - Dealer; Player 1 - Host
    private int messageNum = 0;
    private TableLayout logTable;
    private ScrollView logScroll;
    private int playerID;
    private int currentTurn = 1 ;
    private int playerNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout playerLayout = findViewById(R.id.main_player_hand);
        LinearLayout dealerLayout = findViewById(R.id.dealer_hand);
        tabLayout = findViewById(R.id.expandTab);
        logTable = findViewById(R.id.logTable);
        logScroll = findViewById(R.id.logTableView);
        moneyTextView = findViewById(R.id.moneyTextView);
        currentHandText = findViewById(R.id.viewHand);
        splitHandText = findViewById(R.id.viewSplit);
        Button logButton = findViewById(R.id.logViewButton);
        updateMoneyText();


        players.add(new Player(0, dealerLayout, this));


        MP_FLAG = getIntent().getStringExtra("type").equals("MP");

        //If in multiplayer, setup socket threads for each connection
        if(MP_FLAG) {
            HOST_FLAG = getIntent().getStringExtra("host").equals("HOST");
            netHandle = ((GameApplication) getApplication()).getNetworkHandler();
            Log.d("Net Handle ID", Integer.toString(netHandle.id));
            for (Socket socket : netHandle.getClientSockets()) {
                Thread t = new Thread(()-> handleClientConnection(socket));
                t.start();
            }
            logButton.setVisibility(View.VISIBLE);

            //Initialize Players
            playerNum = netHandle.getClientSockets().size()+1;
            switch(playerNum) {
                case 4:
                    players.add(1, new Player(playerMoney,findViewById(R.id.row3 ) , findViewById(R.id.Rrow3),this));
                case 3:
                    players.add(1, new Player(playerMoney,findViewById(R.id.row2), findViewById(R.id.Rrow2),this));
                case 2:
                    players.add(1, new Player(playerMoney,findViewById(R.id.row1), findViewById(R.id.Rrow1),this));
                default:
                    players.add(netHandle.id+1, new Player(playerMoney, playerLayout,this));
            }
            playerID = netHandle.id+1;
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
        //tabLayout.bringToFront();
        Button tabButton = findViewById(R.id.expandButton);
        if(!MP_FLAG){
            tabButton.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }

        findViewById(R.id.expandButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

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
    /**
    protected void generateHand(){

        int NUM_PLAYERS = players.size();
        if(NUM_PLAYERS != 0){
        CardHand[] handList = new CardHand[NUM_PLAYERS];
        for(int i = 0; i < players.size(); i++){
            handList[i] = players.get(i).getHand();
        }

        TableRow tabLayout1 = findViewById(R.id.row1);
        TableRow tabLayout2 = findViewById(R.id.row2);
        TableRow tabLayout3 = findViewById(R.id.row3);
        TableRow[] t = new TableRow[3];
        t[0] = tabLayout1;
        t[1] = tabLayout2;
        t[2] = tabLayout3;
        for(int x = 0; x < NUM_PLAYERS; x++) {
            t[x].removeAllViews();
            ArrayList<Card> hand = handList[x].retrieveHand();


            for (int i = 0; i < handList[x].size(); i++) {

            }
        }
    }}**/
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
        for (Player p: players) {
            p.clearHand();
            p.stand = false;
        }
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
            String currentHand = "";
            for(int i = 0; i < p.getHand().size(); i++){
                currentHand += " " + p.getHand().get(i).getRank();
                Log.d("card debug", "card #" + i + "| value: " + p.getHand().get(i).getRank());
            }

            currentHand += " (" + p.getHand().getTotalValue() + ")";
            currentHandText.setText(currentHand);
        }
        else{
            String hand1 = "";
            String hand2 = "";
            for(int i = 0; i < p.getHand().retrieveHand(0).size(); i++){
                hand1 += " " + p.getHand().retrieveHand(0).get(i).getRank();
                Log.d("hand 1 debug", "card #" + i + "| value: " + p.getHand().retrieveHand(0).get(i).getRank());
            }
            for(int i = 0; i < p.getHand().retrieveHand(1).size(); i++){
                hand2 += " " + p.getHand().retrieveHand(1).get(i).getRank();
                Log.d("hand 2 debug", "card #" + i + "| value: " + p.getHand().retrieveHand(1).get(i).getRank());

            }


            currentHandText.setText(hand1);
            splitHandText.setText(hand2);

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
            /**
            playerLayout.removeAllViews();
            refreshHand(playerHand, splitLayout1, 0);
            refreshHand(playerHand, splitLayout2, 1);**/
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
            if(!p.stand)
                dealersTurn = false;
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
        runOnUiThread(()->p.addCard(topCard, split));
        Log.d("dealCard Test", "Dealer Card: " + topCard.getRank());
        return topCard;
    }

    private void dealCard(Player p, Card c, int split) {
        runOnUiThread(()->p.addCard(c, split));
        Log.d("dealCard Test", "Card: " + c.getRank());
    }

    private Card dealCard(Player p) {
        return dealCard(p, 0);
    }

    private void dealerTurn() {
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
                    mediaPlayer.start();
//                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
//                mediaPlayer.start();
//                mediaPlayer.release();
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
                    mediaPlayer.start();
//                mediaPlayer=MediaPlayer.create(this,R.raw.cheer);
//                mediaPlayer.start();
//                mediaPlayer.release();
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
            Player dealer = players.get(0);
            while(dealer.getMainTotal() < 17) {
                Log.d("Drawing Card", "Dealer Total: " + dealer.getMainTotal());

                Card c = deck.retrieveTop();
                runOnUiThread(()->dealer.addCard(c));
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
            p.setMoney(p.getMoney()-p.getBet());
            sendAllMessage("SetMoney", Integer.toString(p.getMoney()));
            sendAllMessage("SetBet", "0");
            roundEnd.setMessage("PLAYER BUSTS");

        }
        else if(dealer.getMainTotal() > p.getMainTotal() && dealer.getMainTotal() <= 21) {
            Log.d("checkScore", "Final Player Total: " + p.getMainTotal() + " LOSE");
            Log.d("checkScore", "Final Dealer Total: " + dealer.getMainTotal() + " WIN");
            sendAllMessage("Lose", " ");
            p.setMoney(p.getMoney()-p.getBet());
            sendAllMessage("SetMoney", Integer.toString(p.getMoney()));
            sendAllMessage("SetBet", "0");
            roundEnd.setMessage("PLAYER LOSES");

        }
        else if ((dealer.getMainTotal() < p.getMainTotal()) || (dealer.getMainTotal() > 21)) {
            Log.d("checkScore", "Final Player Total: " + p.getMainTotal() + " WIN");
            Log.d("checkScore", "Final Dealer Total: " + dealer.getMainTotal() + " LOSE");
            sendAllMessage("Lose", " ");
            if(dealer.getMainTotal() == 21)
                p.setMoney(p.getMoney()+(p.getBet()*2));
            else
                p.setMoney(p.getMoney()+p.getBet());
            roundEnd.setMessage("PLAYER WINS");

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
        int id = Integer.parseInt(args[0]) + 1;
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
                if(checkHand(Integer.parseInt(arg[2]))) {
                    sendAllMessage("Bust", " ");
                }
                runOnUiThread(this::disablePlayerControls);
                break;
            case "SetMoney": //SetMoney - Set the current Money of the player
                players.get(id).setMoney(Integer.parseInt(message));
                break;
            case "Hit":
                if(!HOST_FLAG)
                    break;
                Card c = dealCard(players.get(id), Integer.parseInt(message));
                String m = id + "," + c + "," + Integer.parseInt(message);
                sendAllMessage("DealCard", m);
                nextTurn();
                break;
            case "Split":
                break;
            case "Stand":
            case "Bust":
                players.get(id).stand = true;
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
                runOnUiThread(this::hidePlayerControls);
                checkScore();
                break;
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
        Log.d("checkHand", "0 | " + p.getHand().getTotalValue(0));
        return p.getHand().getTotalValue() > 21;
    }

    private void logMessage(String job, String message, int id){
        String fullMessage = "Player " + id + " " + job + " " + message;
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

    private LinearLayout vr; // each tab in the side bar has two rows for formatting
    String nickname;        // vr contains images and visual row contains the nickname
    Player(int money, LinearLayout row, LinearLayout vr, Context c) {
        this.money = money;
        this.visualRow = row;
        this.gameHand = new CardHand();
        this.parentContext = c;
        this.bet = 0;
        this.vr = vr;
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
        refreshHand();
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

    public void setId(int id) {
        this.id = id;
        String nicks[] = {"Blackjack Bill", "Peeking Paul", "Card Countin Charles", "Strangle-Eye Saul", "Kevin" };
        this.nickname = nicks[id];
    }

    public int getMainTotal() {
        return gameHand.getTotalValue();
    }


    public void refreshHand() {
        int margin;
        int cardCount = gameHand.size();
        if (gameHand.isSplit())
            margin = 8 - ((cardCount) * 80);
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
        gameHand.retrieveHand(0).forEach((card -> addCardToHand(card, margin, 0)));
        if (split)
            gameHand.retrieveHand(1).forEach((card -> addCardToHand(card, margin, 1)));
    }

    private void addCardToHand(Card c, int margin, int s) {
        Log.d("AddCardToHand", c.getSuit() + " | " + c.getRank());
        ImageView cardView = new ImageView(parentContext);
        cardView.setImageResource(c.getCardImage());
        if (visualHand != null) { //Main Screen Layout
            if (visualHand.getChildCount() == 0)
                margin = 0;
            if (margin < -130)
                margin = -160;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (scale * 100), (int) (scale * 130));
            Log.d("Margin", Integer.toString(margin));
            params.setMargins((int) ((margin / 2) * scale), 8, 0, 8);
            cardView.setLayoutParams(params);

            if (!split)
                visualHand.addView(cardView);
            else {
                if (s == 1) {
                    splitHand1.addView(cardView);
                } else {
                    splitHand2.addView(cardView);
                }
            }
        }  //Side Bar Layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 217);
            params.setMargins(4, 8, -3, 8);


            TextView tview = new TextView(parentContext, null, 0, R.style.customTextStyle);
            tview.setText(this.nickname);
            //cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    //LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            cardView.setLayoutParams(params);
            if(visualRow != null){
            if(visualRow.getChildCount() < 2 ) {// add the nickname text only for the first time
                visualRow.addView(tview);
            }}
            if(vr != null) {
                this.vr.addView(cardView);
            }



    }

    public void split() {
        gameHand.splitHand();
        visualHand.removeAllViews();
        splitHand1 = new LinearLayout(parentContext);
        splitHand2 = new LinearLayout(parentContext);
        visualHand.addView(splitHand1);
        visualHand.addView(splitHand2);

    }
}