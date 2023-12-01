package com.example.cardgametest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This Activity allows the user to initiate connections with other players using socket connections.
 * This Activity recieves user input to create the sockets in the {@link NetworkHandler} netHandle, which
 * is stored in the Application level.
 *
 */
public class ConnectActivity extends Activity {
    private ServerSocket serverSocket;
    private NetworkHandler netHandle;
    private TextView statusText;
    private EditText ipAddress;
    private EditText nicknameField;
    private Button hostButton;
    private Button joinButton;
    private TextView connectionList;
    private Button startButton;
    private Spinner betSpinner;
    private Spinner moneySpinner;
    private EditText hostName;
    private int playerCount = 1;
    private int minBet;
    private int startMoney;
    private String name;
    private boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        statusText = findViewById(R.id.statusText);
        ipAddress = findViewById(R.id.ipAddress);
        hostButton = findViewById(R.id.hostButton);
        joinButton = findViewById(R.id.joinButton);
        Button backToTitleButton = findViewById(R.id.backToTitleButton);
        connectionList = findViewById(R.id.connectionListView);
        startButton = findViewById(R.id.startGame);
        nicknameField = findViewById(R.id.nicknameField);


        netHandle = new NetworkHandler();
        ((GameApplication) getApplication()).setNetworkHandler(netHandle); //Store NetworkHandler in application for global use
        @SuppressLint("InflateParams") PopupWindow window = new PopupWindow(LayoutInflater.from(this).inflate(R.layout.server_setup_popup, null), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        Button startHost = window.getContentView().findViewById(R.id.startHostButton);
        betSpinner = window.getContentView().findViewById(R.id.betSpinner);
        moneySpinner = window.getContentView().findViewById(R.id.moneySpinner);
        hostName = window.getContentView().findViewById(R.id.hostNameTextField);

        /*
         *  Button Listeners
         */
        hostButton.setOnClickListener(v -> {
            //new ServerTask().execute();
            window.showAtLocation(findViewById(R.id.joinButton), Gravity.CENTER, 0, 0);
        });

        joinButton.setOnClickListener(v -> {
            String ip = ipAddress.getText().toString();
            new ClientTask().execute(ip);
        });

        backToTitleButton.setOnClickListener(v -> {
            // Add code to navigate back to the TitleScreen activity here
            finish(); // This will simply close the ConnectActivity and return to the previous activity (TitleScreen)
        });

        startButton.setOnClickListener(view -> {
            playerCount = netHandle.getClientSockets().size()+1;
            start = true;
            try {
                serverSocket.close();
            } catch (IOException ignore) {}

            new MessageSender().execute("SETUP : " + minBet + " : " + startMoney);
            new MessageSender().execute("PLAYER_COUNT : " + playerCount);
            new MessageSender().execute("initialize");
            new MessageSender().execute("play");
            Intent i = new Intent(ConnectActivity.this, BlackjackGameActivity.class);
            i.putExtra("type", "MP");
            i.putExtra("host", "HOST");
            i.putExtra("players", playerCount);
            i.putExtra("min_bet", minBet);
            i.putExtra("start_money", startMoney);
            i.putExtra("my_name", name);
            startActivity(i);
        });

        //Host Setup Popup
        startHost.setOnClickListener(v -> {
            minBet = Integer.parseInt((betSpinner.getSelectedItem().toString().substring(1)));
            startMoney = Integer.parseInt((moneySpinner.getSelectedItem().toString().substring(1)));
            name = hostName.getText().toString();
            if(name.equals(""))
                name = "Host";
            window.dismiss();
            hostButton.setEnabled(false);
            joinButton.setEnabled(false);
            ipAddress.setEnabled(false);
            new ServerTask().execute();
        });
    }

    /**
     * ServerTask is an async class that will create a server socket and listen for connections.
     * When a connection is found it creates a socket and adds it to the {@link NetworkHandler}.
     */
    private class ServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                serverSocket = new ServerSocket(12345);

                runOnUiThread(() -> statusText.setText("Server started.\nWaiting for clients..."));

                while(!start && netHandle.getClientSockets().size() < 4) {
                    Socket clientSocket = serverSocket.accept();
                    netHandle.addClientSocket(clientSocket);

                    connectionList.setText(String.format("%s\n%s", connectionList.getText(), clientSocket.getInetAddress().toString()));
                    runOnUiThread(() -> startButton.setVisibility(View.VISIBLE)); //Make play button visible after a connection.
                }
                Log.d("Server", "No longer accepting connections.");

                //Thread clientThread = new Thread(() -> handleClientConnection(clientSocket));  //Await messages from client on a new thread.
                //clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * This async class sends a message to all current sockets.
     */
    private class MessageSender extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if(params[0].equals("initialize"))
                netHandle.initalizeClients();
            else
                netHandle.sendToAllClients(params[0]);
            return null;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String ip = params[0];
            try {
                Socket clientSocket = new Socket(ip, 12345);
                netHandle.addClientSocket(clientSocket);

                runOnUiThread(() -> statusText.setText("Connected to the host."));

                Thread hostThread = new Thread(() -> handleClientConnection(clientSocket)); //Await messages from host on a new thread.
                hostThread.start();
            } catch (ConnectException e) {
                runOnUiThread(() -> statusText.setText("Could not connect to the host."));
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * This class awaits messages on the specified socket and interprets them.
     * @param socket
     */
    private void handleClientConnection(Socket socket) {
        while (true) {
            String message = netHandle.receiveMessageFromClient(socket);
            if (message != null) {
                // Handle the received message
                // For example, you can display it on the UI
                Log.d("Message Received", message);
                String[] args = message.split(" : ");

                //If the message is play send all players to the game screen.
                if(args[0].equals("play")) {
                    break;
                }
                else if(args[0].equals("PLAYER_COUNT")) {
                    playerCount = Integer.parseInt(args[1]);
                }
                else if(args[0].equals("SETUP")) {
                    minBet = Integer.parseInt(args[1]);
                    startMoney = Integer.parseInt(args[2]);
                }
                else if(args[0].equals("ASSIGN_ID")) {
                    netHandle.id = Integer.parseInt(args[1]);
                    Log.d("Game ID", args[1]);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Received message from client: " + message);
                    }
                });
            }
        }
        Intent i = new Intent(this, BlackjackGameActivity.class);
        String name = nicknameField.getText().toString();
        if(name.equals("") || name == null)
            name = "player";
        i.putExtra("type", "MP");
        i.putExtra("host", "CLIENT");
        i.putExtra("players", playerCount);
        i.putExtra("min_bet", minBet);
        i.putExtra("start_money", startMoney);
        i.putExtra("my_name", name);
        startActivity(i);
        Thread.currentThread().interrupt(); // Close this activities message threads.
    }
}
