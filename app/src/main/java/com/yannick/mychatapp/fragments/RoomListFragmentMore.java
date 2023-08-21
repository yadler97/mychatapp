package com.yannick.mychatapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.activities.ChatActivity;
import com.yannick.mychatapp.adapters.RoomAdapter;
import com.yannick.mychatapp.data.Message;
import com.yannick.mychatapp.data.Room;
import com.yannick.mychatapp.data.Theme;

import java.util.ArrayList;

public class RoomListFragmentMore extends Fragment {

    private ListView listView;
    private Theme theme;
    private RoomAdapter adapter;
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms");
    private final ArrayList<Room> roomList = new ArrayList<>();
    private TextView noRoomFound;

    private FileOperations fileOperations;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roomlist_fragment,container,false);

        listView = view.findViewById(R.id.listView);
        noRoomFound = view.findViewById(R.id.keinraumgefunden);

        theme = Theme.getCurrentTheme(getContext());
        fileOperations = new FileOperations(getActivity());

        adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MORE);
        listView.setAdapter(adapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchReceiver, new IntentFilter("searchroom"));

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    addRoomToList(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position = listView.getPositionForView(view);
                Room room = roomList.get(position);
                requestPassword(room, position);
            }
        });

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (roomList.isEmpty()) {
                    noRoomFound.setText(R.string.noroomfound);
                } else {
                    noRoomFound.setText("");
                }
            }
        });

        return view;
    }

    private void addRoomToList(DataSnapshot dataSnapshot) {
        roomList.clear();

        for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){
            String name = uniqueKeySnapshot.getKey();
            for(DataSnapshot roomSnapshot : uniqueKeySnapshot.getChildren()){
                final Room room = roomSnapshot.getValue(Room.class);
                room.setKey(name);
                if (!room.getPasswd().equals(fileOperations.readFromFile("mychatapp_room_" + name + ".txt"))) {
                    if (uniqueKeySnapshot.getChildrenCount() > 1) {
                        DatabaseReference newestMessageRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms").child(name);
                        Query lastQuery = newestMessageRoot.orderByKey().limitToLast(1);
                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    String key = child.getKey();
                                    String message = child.child("msg").getValue().toString();
                                    String pin = child.child("pin").getValue().toString();
                                    String quote = child.child("quote").getValue().toString();
                                    String time = child.child("time").getValue().toString();

                                    Message newestMessage = new Message(null, message, time, false, key, Message.Type.MESSAGE_RECEIVED, "", "", quote, pin);

                                    room.setnM(newestMessage);
                                    roomList.add(room);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //Handle possible errors.
                            }
                        });
                    } else {
                        roomList.add(room);
                    }
                }
                break;
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void requestPassword(final Room room, final int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.enter_room, null);
        final EditText input_field = view.findViewById(R.id.room_password);
        final TextInputLayout input_field_layout = view.findViewById(R.id.room_password_layout);
        input_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    input_field_layout.setError(null);
                }
            }
        });
        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));
        }
        builder.setTitle(R.string.pleaseenterpassword);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                View view = ((AlertDialog) dialogInterface).getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                dialogInterface.cancel();
            }
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (!input_field.getText().toString().isEmpty()) {
                            if (input_field.getText().toString().trim().equals(room.getPasswd())) {
                                String roomKey = room.getKey();
                                Intent tabIntent = new Intent("tab");
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(tabIntent);
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("room_name", room.getName());
                                intent.putExtra("room_key", roomKey);
                                intent.putExtra("last_read_message", fileOperations.readFromFile("mychatapp_room_" + roomKey + "_nm.txt"));
                                if (room.getnM() != null) {
                                    intent.putExtra("nmid", room.getnM().getKey());
                                } else {
                                    intent.putExtra("nmid", roomKey);
                                }
                                if (room.getnM() != null) {
                                    fileOperations.writeToFile(room.getnM().getKey(), "mychatapp_room_" + roomKey + "_nm.txt");
                                } else {
                                    fileOperations.writeToFile(roomKey, "mychatapp_room_" + roomKey + "_nm.txt");
                                }
                                updateRoomList(position);
                                fileOperations.writeToFile(room.getPasswd(), "mychatapp_room_" + roomKey + ".txt");
                                FirebaseMessaging.getInstance().subscribeToTopic(roomKey);
                                alert.cancel();
                                startActivity(intent);
                            } else {
                                input_field_layout.setError(getResources().getString(R.string.wrongpassword));
                            }
                        } else {
                            input_field_layout.setError(getResources().getString(R.string.enterpassword));
                        }
                    }
                });
            }
        });
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        alert.show();
    }

    private void updateRoomList(int position) {
        roomList.remove(position);
        adapter.notifyDataSetChanged();
    }

    public BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getStringExtra("searchkey");
            if (!s.trim().isEmpty()) {
                ArrayList<Room> searchResultList = searchRoom(s);

                if (!searchResultList.isEmpty()) {
                    adapter = new RoomAdapter(getContext(), searchResultList, RoomAdapter.RoomListType.MORE);
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    noRoomFound.setText("");
                    adapter.notifyDataSetChanged();
                } else {
                    listView.setVisibility(View.GONE);
                    noRoomFound.setText(R.string.noroomfound);
                }
            } else {
                adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MORE);
                listView.setVisibility(View.VISIBLE);
                if (!roomList.isEmpty()) {
                    noRoomFound.setText("");
                } else {
                    noRoomFound.setText(R.string.noroomfound);
                }
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private ArrayList<Room> searchRoom(String text) {
        ArrayList<Room> searchedRoomList = new ArrayList<>();
        for (Room r : roomList) {
            if (r.getName().toLowerCase().contains(text.toLowerCase())) {
                searchedRoomList.add(r);
            }
        }

        return searchedRoomList;
    }
}