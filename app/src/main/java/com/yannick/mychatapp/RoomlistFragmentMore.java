package com.yannick.mychatapp;

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
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class RoomlistFragmentMore extends Fragment {

    private ListView listView;
    private String raumname, theme;
    private RoomAdapter adapter;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms");
    private ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<Room> searchResultList = new ArrayList<>();
    private TextView noRoomFound;
    private Message newestMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roomlist_fragment,container,false);

        listView = view.findViewById(R.id.listView);
        noRoomFound = view.findViewById(R.id.keinraumgefunden);

        theme = readFromFile("mychatapp_theme.txt");

        adapter = new RoomAdapter(getContext(), roomList, 2);
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
                if (!room.getPasswd().equals(readFromFile("mychatapp_raum_" + name + ".txt"))) {
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

                                    newestMessage = new Message(null, message, time, time, false, key, 1, "", "", quote, pin);

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
        raumname = room.getKey();
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
        if (theme.equals("1")) {
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
                                Intent tabIntent = new Intent("tab");
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(tabIntent);
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("room_name", room.getName());
                                intent.putExtra("room_key", room.getKey());
                                intent.putExtra("last_read_message", readFromFile("mychatapp_raum_" + room.getKey() + "_nm.txt"));
                                if (room.getnM() != null) {
                                    intent.putExtra("nmid", room.getnM().getKey());
                                } else {
                                    intent.putExtra("nmid", room.getKey());
                                }
                                if (room.getnM() != null) {
                                    writeToFile(room.getnM().getKey(), "mychatapp_raum_" + room.getKey() + "_nm.txt");
                                } else {
                                    writeToFile(room.getKey(), "mychatapp_raum_" + room.getKey() + "_nm.txt");
                                }
                                updateRoomList(position);
                                writeToFile(room.getPasswd(), "mychatapp_raum_" + raumname + ".txt");
                                FirebaseMessaging.getInstance().subscribeToTopic(room.getKey());
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

    private void writeToFile(String text, String datei) {
        Context context = getActivity();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(datei, Context.MODE_PRIVATE));
            outputStreamWriter.write(text);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String datei) {
        Context context = getActivity();
        String erg = "";

        try {
            InputStream inputStream = context.openFileInput(datei);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                erg = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return erg;
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
                searchResultList = searchRoom(s);

                if (!searchResultList.isEmpty()) {
                    adapter = new RoomAdapter(getContext(), searchResultList, 2);
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    noRoomFound.setText("");
                    adapter.notifyDataSetChanged();
                } else {
                    listView.setVisibility(View.GONE);
                    noRoomFound.setText(R.string.noroomfound);
                }
            } else {
                adapter = new RoomAdapter(getContext(), roomList, 2);
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