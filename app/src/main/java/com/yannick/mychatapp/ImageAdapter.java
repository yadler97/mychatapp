package com.yannick.mychatapp;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> imageList;

    public ImageAdapter(Context context, ArrayList<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    public int getCount() {
        return this.imageList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SquareImageView imageView = new SquareImageView(this.context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String imgurl = imageList.get(position);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference pathReference = storageRef.child("images/" + imgurl);

        if (readFromFile("mychatapp_settings_preview.txt").equals("off")) {
            GlideApp.with(context)
                    .load(pathReference)
                    .onlyRetrieveFromCache(true)
                    .placeholder(R.color.gray_material)
                    .thumbnail(0.05f)
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(pathReference)
                    .placeholder(R.color.gray_material)
                    .thumbnail(0.05f)
                    .into(imageView);
        }

        return imageView;
    }

    private String readFromFile(String datei) {
        String erg = "";

        try {
            InputStream inputStream = context.openFileInput(datei);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                erg = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return erg;
    }
}