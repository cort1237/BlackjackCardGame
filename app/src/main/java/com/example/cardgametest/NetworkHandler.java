package com.example.cardgametest;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkHandler {
    private ArrayList<Socket> clientSockets = new ArrayList<>();
    public int id;

    public void addClientSocket(Socket socket) {
        clientSockets.add(socket);
    }

    public void removeClientSocket(Socket socket) {
        clientSockets.remove(socket);
    }

    public void setID(int i) {
        id = i;
    }

    public ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public void sendToAllClients(String message) {
        for (Socket socket : clientSockets) {
            try {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initalizeClients() {
        int i = 1;
        for (Socket socket: clientSockets) {
            try {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF("ASSIGN_ID : " + Integer.toString(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String receiveMessageFromClient(Socket socket) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            String s = inputStream.readUTF();
            Log.d("NetworkHandler Recieve", s);
            return s;
        } catch (EOFException e) {
            return null;
        } catch (IOException e) {
            // Handle any exceptions or remove the socket if there's an issue
        }
        return null;
    }
}
