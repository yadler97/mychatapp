package com.yannick.mychatapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.preference.PreferenceManager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.SquareImageView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<String> imageList;

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

        String imageURL = imageList.get(position);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference pathReference = storageRef.child(Constants.imagesStorageKey + imageURL);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.context);
        if (!settings.getBoolean(Constants.settingsPreviewImagesKey, true)) {
            GlideApp.with(context)
                    .load(pathReference)
                    .onlyRetrieveFromCache(true)
                    .placeholder(R.color.grey)
                    .thumbnail(0.05f)
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(pathReference)
                    .placeholder(R.color.grey)
                    .thumbnail(0.05f)
                    .into(imageView);
        }

        return imageView;
    }
}