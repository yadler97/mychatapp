package com.yannick.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class BackgroundAdapter extends BaseAdapter {
    private final Context context;
    private final TypedArray imageList;
    private final Background selected;
    private final Theme theme;
    private final ArrayList<SquareImageView> viewList = new ArrayList<>();

    public BackgroundAdapter(Context context, TypedArray imageList, Background selected, Theme theme) {
        this.context = context;
        this.imageList = imageList;
        this.selected = selected;
        this.theme = theme;
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
            if(theme == Theme.DARK) {
                imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
            } else {
                imageView.setBorderColor(context.getResources().getColor(R.color.red));
            }
            imageView.setBorderWidth((float)8);
        } else {
            imageView.setBorderWidth((float)2);
            imageView.setBorderColor(context.getResources().getColor(R.color.grey));
        }

        if (selected == Background.getByPosition(position) && position != 0) {
            for (SquareImageView v : viewList) {
                v.setBorderColor(context.getResources().getColor(R.color.grey));
                v.setBorderWidth((float)2);
            }
            if(theme == Theme.DARK) {
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
                if(theme == Theme.DARK) {
                    imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
                } else {
                    imageView.setBorderColor(context.getResources().getColor(R.color.red));
                }
                imageView.setBorderWidth((float)8);
                Intent intent = new Intent("backgroundOption");
                intent.putExtra("position", position);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        if (position == 0) {
            if (theme == Theme.DARK) {
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
}
