package com.yannick.mychatapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileOperations {
    Context context;

    public final static String favFilePattern = "mychatapp_%s_fav.txt";
    public final static String muteFilePattern = "mychatapp_%s_mute.txt";
    public final static String currentInputFilePattern = "mychatapp_%s_currentInput.txt";
    public final static String currentRoomFile = "mychatapp_currentRoom.txt";

    public FileOperations(Context context) {
        this.context = context;
    }

    public String readFromFile(String file) {
        String result = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                result = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("FileOperations", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileOperations", "Can not read file: " + e.toString());
        } catch (NullPointerException e) {
            Log.e("FileOperations", "NullPointerException: " + e.toString());
        }

        return result;
    }

    public void writeToFile(String text, String file) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(text);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("FileOperations", "File write failed: " + e.toString());
        }
    }
}
