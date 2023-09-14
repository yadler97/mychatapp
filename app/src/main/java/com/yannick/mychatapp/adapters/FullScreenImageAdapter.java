package com.yannick.mychatapp.adapters;

import androidx.viewpager.widget.PagerAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.data.Image;

import java.util.ArrayList;

public class FullScreenImageAdapter extends PagerAdapter {

    private final Context context;
    private final ArrayList<String> imageList;
    private final LayoutInflater layoutInflater;
    private final Image type;

    public FullScreenImageAdapter(Context context, ArrayList<String> imageList, Image type) {
        this.context = context;
        this.imageList = imageList;
        this.type = type;
        layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = itemView.findViewById(R.id.imageView);

        String imageURL = imageList.get(position);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference pathReference;

        if (type == Image.PROFILE_IMAGE) {
            pathReference = storageRef.child(Constants.profileImagesStorageKey + imageURL);
        } else if (type == Image.PROFILE_BANNER) {
            pathReference = storageRef.child(Constants.profileBannersStorageKey + imageURL);
        } else if (type == Image.ROOM_IMAGE) {
            pathReference = storageRef.child(Constants.roomImagesStorageKey + imageURL);
        } else {
            pathReference = storageRef.child(Constants.imagesStorageKey + imageURL);
        }

        if (type == Image.PROFILE_IMAGE || type == Image.PROFILE_BANNER || type == Image.ROOM_IMAGE) {
            GlideApp.with(context)
                    .load(pathReference)
                    .placeholder(R.color.black)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(pathReference)
                    .placeholder(R.color.black)
                    .into(imageView);
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
