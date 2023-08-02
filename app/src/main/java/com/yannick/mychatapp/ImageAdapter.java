package com.yannick.mychatapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        String imgurl = imageList.get(position);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference pathReference = storageRef.child("images/" + imgurl);

        FileOperations fileOperations = new FileOperations(this.context);
        if (fileOperations.readFromFile("mychatapp_settings_preview.txt").equals("off")) {
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
}