package com.example.cardgametest;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
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
    private Button hostButton;
    private Button joinButton;
    private Button backToTitleButton;
    private TextView connectionList;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        statusText = findViewById(R.id.statusText);
        ipAddress = findViewById(R.id.ipAddress);
        hostButton = findViewById(R.id.hostButton);
        joinButton = findViewById(R.id.joinButton);
        backToTitleButton = findViewById(R.id.backToTitleButton);
        connectionList = findViewById(R.id.connectionListView);
        startButton = findViewById(R.id.startGame);

        netHandle = new NetworkHandler();
        ((GameApplication) getApplication()).setNetworkHandler(netHandle); //Store NetworkHandler in application for global use

        /*
         *  Button Listeners
         */
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ServerTask().execute();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipAddress.getText().toString();
                new ClientTask().execute(ip);
            }
        });

        backToTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add code to navigate back to the TitleScreen activity here
                finish(); // This will simply close the ConnectActivity and return to the previous activity (TitleScreen)
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MessageSender().execute("initialize");
                new MessageSender().execute("play");
                Intent i = new Intent(ConnectActivity.this, BlackjackGameActivity.class);
                startActivity(i);
            }
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

                runOnUiThread(() -> statusText.setText("Server started. Waiting for clients..."));

                Socket clientSocket = serverSocket.accept();
                netHandle.addClientSocket(clientSocket);

                connectionList.setText(String.format("%s\n%s", connectionList.getText(), clientSocket.getInetAddress().toString()));
                runOnUiThread(() -> startButton.setVisibility(View.VISIBLE)); //Make play button visible after a connection.

                Thread clientThread = new Thread(() -> handleClientConnection(clientSocket));  //Await messages from client on a new thread.
                clientThread.start();
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
                    Intent i = new Intent(this, BlackjackGameActivity.class);
                    startActivity(i);
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
    }
}
