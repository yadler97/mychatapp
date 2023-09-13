package com.yannick.mychatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.data.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final ArrayList<User> memberList;
    private final String admin;
    private final FirebaseStorage storage;

    static class ViewHolder {
        TextView userText;
        CircleImageView profileImage;
        TextView adminText;
    }

    public MemberListAdapter(Context context, ArrayList<User> memberList, String admin) {
        super(context, -1, memberList);
        this.context = context;
        this.memberList = memberList;
        this.admin = admin;
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list, parent, false);

            viewHolder.userText = view.findViewById(R.id.user);
            viewHolder.profileImage = view.findViewById(R.id.icon_profile);
            viewHolder.adminText = view.findViewById(R.id.admin);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.userText.setText(memberList.get(position).getName());

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference refImage = storageRef.child(Constants.profileImagesStorageKey + memberList.get(position).getImg());
        GlideApp.with(context)
                .load(refImage)
                .centerCrop()
                .into(viewHolder.profileImage);

        if (memberList.get(position).getUserID().equals(admin)) {
            viewHolder.adminText.setText(R.string.admin);
        }

        view.setOnClickListener(view1 -> {
            Intent intent = new Intent("userprofile");
            intent.putExtra("userid", memberList.get(position).getUserID());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        });

        return view;
    }
}
