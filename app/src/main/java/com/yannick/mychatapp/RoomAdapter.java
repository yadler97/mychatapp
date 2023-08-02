package com.yannick.mychatapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomAdapter extends ArrayAdapter<Room> {

    private final Context context;
    private final ArrayList<Room> roomList;
    private final int typ;
    private final SimpleDateFormat sdf_local = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
    private final FirebaseStorage storage;
    private final FirebaseAuth mAuth;

    static class ViewHolder {
        LinearLayout background;
        TextView roomNameText;
        TextView newestMessageText;
        TextView categoryText;
        TextView lockText;
        ImageView muteIcon;
        CircleImageView roomImage;
    }

    public RoomAdapter(Context context, ArrayList<Room> roomList, int typ) {
        super(context, -1, roomList);
        this.context = context;
        this.roomList = roomList;
        this.typ = typ;
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;

        final ViewHolder viewHolder = new ViewHolder();

        rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_list, parent, false);

        viewHolder.roomNameText = rowView.findViewById(R.id.raumname);
        viewHolder.newestMessageText = rowView.findViewById(R.id.raumdatum);
        viewHolder.categoryText = rowView.findViewById(R.id.raumkat);
        viewHolder.lockText = rowView.findViewById(R.id.raumlock);
        viewHolder.background = rowView.findViewById(R.id.raumbackground);
        viewHolder.muteIcon = rowView.findViewById(R.id.raummute);
        viewHolder.roomImage = rowView.findViewById(R.id.roomimage);

        viewHolder.roomNameText.setText(roomList.get(position).getName());
        if (typ == 2) {
            viewHolder.categoryText.setText(context.getResources().getStringArray(R.array.categories)[Integer.parseInt(roomList.get(position).getCaty())]);
        } else {
            if (roomList.get(position).getnM() != null) {
                if (roomList.get(position).getnM().getTyp() == 1) {
                    if (roomList.get(position).getnM().getUser().getUserID().equals(mAuth.getCurrentUser().getUid())) {
                        viewHolder.categoryText.setText(context.getResources().getString(R.string.you) + ": " + roomList.get(position).getnM().getMsg());
                    } else {
                        viewHolder.categoryText.setText(roomList.get(position).getnM().getUser().getName() + ": " + roomList.get(position).getnM().getMsg());
                    }
                } else if (roomList.get(position).getnM().getTyp() == 13) {
                    if (roomList.get(position).getnM().getUser().getUserID().equals(mAuth.getCurrentUser().getUid())) {
                        viewHolder.categoryText.setText(context.getResources().getString(R.string.yousharedapicture));
                    } else {
                        viewHolder.categoryText.setText(roomList.get(position).getnM().getUser().getName() + " " + context.getResources().getString(R.string.sharedapicture));
                    }
                }
            } else {
                if (roomList.get(position).getAdmin().equals(mAuth.getCurrentUser().getUid())) {
                    viewHolder.categoryText.setText(R.string.youcreatedthisroom);
                } else {
                    viewHolder.categoryText.setText(roomList.get(position).getUsername() + " " + context.getResources().getString(R.string.createdthisroom));
                }
            }
        }
        if (typ == 0 || typ == 1) {
            if (roomList.get(position).getnM() != null) {
                viewHolder.newestMessageText.setText(parseTime(roomList.get(position).getnM().getbTime()));
            } else {
                viewHolder.newestMessageText.setText(parseTime(roomList.get(position).getTime()));
            }
        }
        if (typ == 1) {
            viewHolder.lockText.setText("\u2764");
        } else if (typ == 2) {
            viewHolder.lockText.setText("\uD83D\uDD12");
        }

        FileOperations fileOperations = new FileOperations(this.context);
        if (typ == 0 || typ == 1) {
            if (roomList.get(position).getnM() != null) {
                if (!roomList.get(position).getnM().getKey().equals(fileOperations.readFromFile("mychatapp_raum_" + roomList.get(position).getKey() + "_nm.txt"))) {
                    if (Theme.getCurrentTheme(context) == Theme.DARK) {
                        viewHolder.background.setBackgroundColor(context.getResources().getColor(R.color.roomhighlight_dark));
                    } else {
                        viewHolder.background.setBackgroundColor(context.getResources().getColor(R.color.roomhighlight));
                    }
                }
            }
        }

        if (typ != 2) {
            if (fileOperations.readFromFile("mychatapp_" + roomList.get(position).getKey() + "_mute.txt").equals("1")) {
                viewHolder.muteIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_muted));
                if (Theme.getCurrentTheme(context) == Theme.DARK) {
                    viewHolder.muteIcon.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                } else {
                    viewHolder.muteIcon.setColorFilter(context.getResources().getColor(R.color.iconGrey), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_image = storageRef.child("room_images/" + roomList.get(position).getImg());
        GlideApp.with(context)
                .load(pathReference_image)
                .centerCrop()
                .thumbnail(0.05f)
                .into(viewHolder.roomImage);

        return rowView;
    }

    private String parseTime(String time) {
        try {
            time = sdf_local.format(sdf_local.parse(time));
        } catch (ParseException e) {

        }
        if (time.substring(0, 8).equals(sdf_local.format(new Date()).substring(0, 8))) {
            return time.substring(9, 11) + ":" + time.substring(11, 13);
        } else {
            return time.substring(6, 8) + "." + time.substring(4, 6) + "." + time.substring(0, 4);
        }
    }
}