package com.yannick.mychatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yannick.mychatapp.R;
import com.yannick.mychatapp.SquareImageView;
import com.yannick.mychatapp.data.Theme;

import java.util.ArrayList;

public class ThemeAdapter extends BaseAdapter {
    private final Context context;
    private final TypedArray imageList;
    private final Theme selected;
    private final Theme theme;
    private final ArrayList<SquareImageView> viewList = new ArrayList<>();

    public ThemeAdapter(Context context, TypedArray imageList, Theme selected, Theme theme) {
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
            if (theme == Theme.DARK) {
                imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
            } else {
                imageView.setBorderColor(context.getResources().getColor(R.color.red));
            }
            imageView.setBorderWidth((float)8);
        } else {
            imageView.setBorderWidth((float)2);
            imageView.setBorderColor(context.getResources().getColor(R.color.grey));
        }

        if (selected == Theme.getByPosition(position) && position != 0) {
            for (SquareImageView v : viewList) {
                v.setBorderColor(context.getResources().getColor(R.color.grey));
                v.setBorderWidth((float)2);
            }
            if (theme == Theme.DARK) {
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
                if (theme == Theme.DARK) {
                    imageView.setBorderColor(context.getResources().getColor(R.color.dark_button));
                } else {
                    imageView.setBorderColor(context.getResources().getColor(R.color.red));
                }
                imageView.setBorderWidth((float)8);
                Intent intent = new Intent("themeOption");
                intent.putExtra("position", position);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        int image = imageList.getResourceId(position, -1);
        imageView.setImageResource(image);

        viewList.add(imageView);

        return imageView;
    }
}
