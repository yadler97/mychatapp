package com.yannick.mychatapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.activities.ChatActivity;
import com.yannick.mychatapp.adapters.RoomAdapter;
import com.yannick.mychatapp.data.Room;
import com.yannick.mychatapp.data.Theme;

import java.util.ArrayList;

public class RoomListFragmentMore extends Fragment {

    private ListView listView;
    private Theme theme;
    private RoomAdapter adapter, searchAdapter;
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.roomsDatabaseKey);
    private final ArrayList<Room> roomList = new ArrayList<>();
    private final ArrayList<Room> searchRoomList = new ArrayList<>();
    private TextView noRoomFound;

    private FileOperations fileOperations;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roomlist_fragment, container, false);

        listView = view.findViewById(R.id.listView);
        noRoomFound = view.findViewById(R.id.no_room_found);

        theme = Theme.getCurrentTheme(getContext());
        fileOperations = new FileOperations(getActivity());

        adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MORE);
        searchAdapter = new RoomAdapter(getContext(), searchRoomList, RoomAdapter.RoomListType.MORE);
        listView.setAdapter(adapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchReceiver, new IntentFilter("searchroom"));

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (isAdded()) {
                    addRoom(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (isAdded()) {
                    changeRoom(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    removeRoom(dataSnapshot);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            int position = listView.getPositionForView(view1);
            Room room = roomList.get(position);
            requestPassword(room, position);
        });

        noRoomFound.setText(R.string.noroomfound);

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

    private void addRoom(DataSnapshot dataSnapshot) {
        final String roomKey = dataSnapshot.getKey();
        final Room room = dataSnapshot.child(Constants.roomDataDatabaseKey).getValue(Room.class);
        room.setKey(roomKey);

        if (!room.getPassword().equals(fileOperations.readFromFile(String.format(FileOperations.passwordFilePattern, roomKey)))) {
            boolean inList = false;
            for (Room r : roomList) {
                if (r.getKey().equals(room.getKey())) {
                    inList = true;
                    break;
                }
            }
            if (!inList) {
                roomList.add(room);
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void changeRoom(DataSnapshot dataSnapshot) {
        final String roomKey = dataSnapshot.getKey();
        roomList.removeIf(r -> r.getKey().equals(roomKey));
        addRoom(dataSnapshot);
    }

    public void removeRoom(DataSnapshot dataSnapshot) {
        final String roomKey = dataSnapshot.getKey();
        roomList.removeIf(r -> r.getKey().equals(roomKey));
        adapter.notifyDataSetChanged();
    }

    private void requestPassword(final Room room, final int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.enter_room, null);
        final EditText inputPassword = view.findViewById(R.id.room_password);
        final TextInputLayout inputPasswordLayout = view.findViewById(R.id.room_password_layout);
        inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    inputPasswordLayout.setError(null);
                }
            }
        });
        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.pleaseenterpassword)));
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            View view1 = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
            dialogInterface.cancel();
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {

            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view12 -> {
                if (!inputPassword.getText().toString().isEmpty()) {
                    if (inputPassword.getText().toString().trim().equals(room.getPassword())) {
                        String roomKey = room.getKey();
                        Intent tabIntent = new Intent("tab");
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(tabIntent);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("room_name", room.getName());
                        intent.putExtra("room_key", roomKey);
                        intent.putExtra("last_read_message", fileOperations.readFromFile(String.format(FileOperations.newestMessageFilePattern, roomKey)));
                        if (room.getNewestMessage() != null) {
                            intent.putExtra("nmid", room.getNewestMessage().getKey());
                        } else {
                            intent.putExtra("nmid", roomKey);
                        }
                        if (room.getNewestMessage() != null) {
                            fileOperations.writeToFile(room.getNewestMessage().getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
                        } else {
                            fileOperations.writeToFile(roomKey, String.format(FileOperations.newestMessageFilePattern, roomKey));
                        }
                        updateRoomList(position);
                        fileOperations.writeToFile(room.getPassword(), String.format(FileOperations.passwordFilePattern, roomKey));
                        FirebaseMessaging.getInstance().subscribeToTopic(roomKey);
                        alert.cancel();
                        startActivity(intent);
                    } else {
                        inputPasswordLayout.setError(getResources().getString(R.string.wrongpassword));
                    }
                } else {
                    inputPasswordLayout.setError(getResources().getString(R.string.enterpassword));
                }
            });
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
                searchRoom(s);

                if (!searchRoomList.isEmpty()) {
                    listView.setAdapter(searchAdapter);
                    listView.setVisibility(View.VISIBLE);
                    noRoomFound.setText("");
                    searchAdapter.notifyDataSetChanged();
                } else {
                    listView.setVisibility(View.GONE);
                    noRoomFound.setText(R.string.noroomfound);
                }
            } else {
                if (!roomList.isEmpty()) {
                    noRoomFound.setText("");
                } else {
                    noRoomFound.setText(R.string.noroomfound);
                }
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private void searchRoom(String text) {
        searchRoomList.clear();
        for (Room r : roomList) {
            if (r.getName().toLowerCase().contains(text.toLowerCase())) {
                Room r2 = new Room(r.getKey(), r.getName(), r.getCategory(), r.getTime(), r.getPassword(), r.getAdmin());
                r2.setImage(r.getImage());
                r2.setNewestMessage(r.getNewestMessage());
                r2.setSearchString(text);
                searchRoomList.add(r2);
            }
        }
    }

    private TextView setupHeader(String title) {
        TextView header = new TextView(getContext());

        if (theme == Theme.DARK) {
            header.setBackgroundColor(getResources().getColor(R.color.dark_button));
        } else {
            header.setBackgroundColor(getResources().getColor(R.color.red));
        }

        header.setText(title);
        header.setPadding(30, 30, 30, 30);
        header.setTextSize(20F);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(Color.WHITE);

        return header;
    }
}