package com.yannick.mychatapp;

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
        View rowView;

        final ViewHolder viewHolder = new ViewHolder();

        rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list, parent, false);

        viewHolder.userText = rowView.findViewById(R.id.user);
        viewHolder.profileImage = rowView.findViewById(R.id.icon_profile);
        viewHolder.adminText = rowView.findViewById(R.id.admin);

        viewHolder.userText.setText(memberList.get(position).getName());

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_image = storageRef.child("profile_images/" + memberList.get(position).getImg());
        GlideApp.with(context)
                .load(pathReference_image)
                .centerCrop()
                .into(viewHolder.profileImage);

        if (memberList.get(position).getUserID().equals(admin)) {
            viewHolder.adminText.setText(R.string.admin);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("userprofile");
                intent.putExtra("userid", memberList.get(position).getUserID());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        return rowView;
    }
}
