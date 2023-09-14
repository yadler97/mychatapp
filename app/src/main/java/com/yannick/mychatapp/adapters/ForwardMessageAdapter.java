package com.yannick.mychatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.data.Room;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardMessageAdapter extends ArrayAdapter<Room> {

    private final Context context;
    private final ArrayList<Room> roomList;
    private final FirebaseStorage storage;

    static class ViewHolder {
        TextView roomNameText;
        CircleImageView roomImage;
    }

    public ForwardMessageAdapter(Context context, ArrayList<Room> roomList) {
        super(context, -1, roomList);
        this.context = context;
        this.roomList = roomList;
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forward_message_room_list, parent, false);

            viewHolder.roomNameText = view.findViewById(R.id.room_name);
            viewHolder.roomImage = view.findViewById(R.id.icon_room);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.roomNameText.setText(roomList.get(position).getName());

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference refImage = storageRef.child(Constants.roomImagesStorageKey + roomList.get(position).getImage());
        GlideApp.with(context)
                .load(refImage)
                .centerCrop()
                .into(viewHolder.roomImage);

        return view;
    }
}
