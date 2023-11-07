package com.example.cardgametest;

import android.content.Context;
import android.util.Log;

import java.io.*;

// Reads/Write to a comma separated txt file that holds W/L record
public class Stats {

    Context context;
    private final String FILENAME = "stats.txt";
    private static final String TAG = "Stats.java";


    public Stats(Context context) {
        this.context = context;
        create();
    }

    // Creates/overwrites a file stats.txt to store W/L. Can be reworked to not overwrite if append
    // is set to "true". Such as if we wanted to record match history over time
    private void create() {
        //deleteFile();
        File file = context.getFileStreamPath(FILENAME);
        if (!file.exists() || file.length() == 0) {
            String s = "0, 0, 0, 0";
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(context.getFileStreamPath(FILENAME), false));
                writer.write(s);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteFile() {
        File file = context.getFileStreamPath(FILENAME);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully.");
            } else {
                System.err.println("Failed to delete the file.");
            }
        } else {
            System.out.println("File does not exist, no need to delete.");
        }
    }

    // Operates on the notion that file is overwritten but can be adjusted needed
    public String[] getData() {
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
        //Log.d("Stats.getStats()", String.valueOf(content));

        return String.valueOf(content).split(",\\s*");
    }

    private void write(int n, int amount) {

        String[] currentStats = getData();
        int[] updatedStats = new int[currentStats.length];

        for (int i = 0; i < currentStats.length; i++) {
            try {
                updatedStats[i] = Integer.parseInt(currentStats[i]);
            } catch (NumberFormatException e) {
                // Handle the case where the string is not a valid integer
            }
        }

        if (n < 3)
            updatedStats[n] += 1;
        else
            updatedStats[3] += (int) (amount * 0.1);

        String output = "";
        for (int i=0; i<updatedStats.length; i++) {
            output += updatedStats[i] + ", ";
        }

        Log.d(TAG, "write(): " + output);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(context.getFileStreamPath(FILENAME), false));
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordWin() {
        write(0, 0);
        write(2, 0);
    }

    public void recordLoss() {
        write(1, 0);
        write(2, 0);
    }

    public void recordPush() {
        write(2, 0);
    }

    public void recordCurrency(int amount) {
        write(3, amount);
    }

}
