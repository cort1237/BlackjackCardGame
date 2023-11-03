package com.example.cardgametest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectActivity extends Activity {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private TextView statusText;
    private EditText ipAddress;
    private Button hostButton;
    private Button joinButton;
    private Button backToTitleButton;
    private final int MAX_PLAYERS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        statusText = findViewById(R.id.statusText);
        ipAddress = findViewById(R.id.ipAddress);
        hostButton = findViewById(R.id.hostButton);
        joinButton = findViewById(R.id.joinButton);
        backToTitleButton = findViewById(R.id.backToTitleButton);

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
    }

    private class ServerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                serverSocket = new ServerSocket(12345);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Server started. Waiting for clients...");
                    }
                });

                clientSocket = serverSocket.accept();

                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleClientConnection(clientSocket);
                    }
                });
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String ip = params[0];
            try {
                clientSocket = new Socket(ip, 12345);

                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                outputStream.writeUTF("Client connected");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Connected to the host.");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void handleClientConnection(Socket socket) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            String message = inputStream.readUTF();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("Client connected: " + message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
