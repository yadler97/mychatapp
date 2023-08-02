package com.yannick.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter {
    private Context context;
    private TypedArray imageList;
    private String selected;
    private String theme;
    private int typ;
    private ArrayList<SquareImageView> viewList = new ArrayList<>();

    public MenuAdapter(Context context, TypedArray imageList, String selected, String theme, int typ) {
        this.context = context;
        this.imageList = imageList;
        this.selected = selected;
        this.theme = theme;
        this.typ = typ;
    }

    public int getCount() {
        return this.imageList.length();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final SquareImageView imageView = new SquareImageView(this.context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (position == 0) {
            if(readFromFile("mychatapp_theme.txt").equals("1")) {
                imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
            } else {
                imageView.setBorderColor(context.getResources().getColor(R.color.red));
            }
            imageView.setBorderWidth((float)8);
        } else {
            imageView.setBorderWidth((float)2);
            imageView.setBorderColor(context.getResources().getColor(R.color.grey));
        }

        if (selected.equals(String.valueOf(position)) && position != 0) {
            for (SquareImageView v : viewList) {
                v.setBorderColor(context.getResources().getColor(R.color.grey));
                v.setBorderWidth((float)2);
            }
            if(readFromFile("mychatapp_theme.txt").equals("1")) {
                imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
            } else {
                imageView.setBorderColor(context.getResources().getColor(R.color.red));
            }
            imageView.setBorderWidth((float)8);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (SquareImageView v : viewList) {
                    v.setBorderColor(context.getResources().getColor(R.color.grey));
                    v.setBorderWidth((float)2);
                }
                if(readFromFile("mychatapp_theme.txt").equals("1")) {
                    imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
                } else {
                    imageView.setBorderColor(context.getResources().getColor(R.color.red));
                }
                imageView.setBorderWidth((float)8);
                Intent intent = new Intent("designOption");
                intent.putExtra("position", String.valueOf(position));
                intent.putExtra("typ", String.valueOf(typ));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        if (typ == 1 && position == 0) {
            if (theme.equals("1")) {
                imageView.setImageDrawable(new ColorDrawable(context.getResources().getColor(R.color.dark_background)));
            } else {
                imageView.setImageDrawable(new ColorDrawable(context.getResources().getColor(R.color.white)));
            }
        } else {
            int image = imageList.getResourceId(position, -1);
            imageView.setImageResource(image);
        }

        viewList.add(imageView);

        return imageView;
    }

    private String readFromFile(String file) {
        String erg = "";

        try {
            InputStream inputStream = context.openFileInput(file);

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
