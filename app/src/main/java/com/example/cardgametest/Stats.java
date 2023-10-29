package com.example.cardgametest;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Stats {

    Context context;
    private final String FILENAME = "stats.txt";

    public Stats(Context context) {
        this.context = context;
        create();
    }

    private void create() {
        String s = "0, 0, 0, ";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(context.getFileStreamPath(FILENAME), false));
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] read() {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(context.getFileStreamPath(FILENAME)));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("Stats.read()", String.valueOf(content));

        return String.valueOf(content).split(",\\s*");
    }

    private void write(int n) {

        String[] currentStats = read();
        int[] updatedStats = new int[currentStats.length];

        for (int i = 0; i < currentStats.length; i++) {
            try {
                updatedStats[i] = Integer.parseInt(currentStats[i]);
            } catch (NumberFormatException e) {
                // Handle the case where the string is not a valid integer
            }
        }

        updatedStats[n] += 1;
        //updatedStats[2] += 1;

        String output = "";
        for (int i=0; i<updatedStats.length; i++) {
            output += updatedStats[i] + ", ";
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(context.getFileStreamPath(FILENAME), false));
            //writer.write("0, 0, 1, ");
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordWin() {
        write(0);
        write(2);
    }

    public void recordLoss() {
        write(1);
        write(2);
    }

    public void recordPush() {
        write(2);
    }

    public String[] getStats() {
        return read();
    }
}



/*
On event, call function.
Perhaps pass in "loss" or "win" to a method.
That method will then call read, then write the appropriate updated information
 */
