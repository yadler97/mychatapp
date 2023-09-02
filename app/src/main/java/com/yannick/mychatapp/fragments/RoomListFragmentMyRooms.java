package com.yannick.mychatapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.activities.ChatActivity;
import com.yannick.mychatapp.adapters.RoomAdapter;
import com.yannick.mychatapp.data.Message;
import com.yannick.mychatapp.data.Room;
import com.yannick.mychatapp.data.User;

import java.util.ArrayList;

public class RoomListFragmentMyRooms extends Fragment {

    private ListView listView;
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

        adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MY_ROOMS);
        listView.setAdapter(adapter);

        fileOperations = new FileOperations(getActivity());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(roomLeaveReceiver, new IntentFilter("leaveroom"));
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
                try {
                    Toast.makeText(getActivity(), R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Log.e("NullPointerException", e.toString());
                }
            }
        });

        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            int position = listView.getPositionForView(view1);
            Room room = roomList.get(position);
            requestPassword(room);
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

        for (final DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
            final String roomKey = uniqueKeySnapshot.getKey();
            for (DataSnapshot roomSnapshot : uniqueKeySnapshot.getChildren()) {
                final Room room = roomSnapshot.getValue(Room.class);
                room.setKey(roomKey);
                if (room.getPasswd().equals(fileOperations.readFromFile(String.format(FileOperations.passwordFilePattern, roomKey)))) {
                    if (uniqueKeySnapshot.getChildrenCount() > 1) {
                        DatabaseReference newestMessageRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms").child(roomKey);
                        Query lastQuery = newestMessageRoot.orderByKey().limitToLast(1);
                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    String key = child.getKey();
                                    String message = child.child("msg").getValue().toString();
                                    String image = child.child("img").getValue().toString();
                                    String userid = child.child("name").getValue().toString();
                                    boolean pinned = (boolean) child.child("pinned").getValue();
                                    String quote = child.child("quote").getValue().toString();
                                    String time = child.child("time").getValue().toString();

                                    Message newestMessage;
                                    if (!image.isEmpty()) {
                                        newestMessage = new Message(null, image, time, false, key, Message.Type.IMAGE_RECEIVED, "", "", quote, pinned);
                                    } else {
                                        newestMessage = new Message(null, message, time, false, key, Message.Type.MESSAGE_RECEIVED, "", "", quote, pinned);
                                    }
                                    room.setNewestMessage(newestMessage);

                                    sortByTime(room, userid);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //Handle possible errors.
                            }
                        });
                    } else {
                        sortByTime(room, room.getAdmin());
                    }
                }
                break;
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void sortByTime(final Room room, String userid) {
        DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("users").child(userid);
        userRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                User u = dataSnapshot.getValue(User.class);
                u.setUserID(key);
                if (room.getNewestMessage() != null) {
                    room.getNewestMessage().setUser(u);
                } else {
                    room.setUsername(u.getName());
                }

                int index = 0;
                if (!roomList.isEmpty()) {
                    for (Room r : roomList) {
                        long t, t2;
                        if (r.getNewestMessage() != null) {
                            t = Long.parseLong(r.getNewestMessage().getTime().substring(0, 8) + r.getNewestMessage().getTime().substring(9, 15));
                        } else {
                            t = Long.parseLong(r.getTime().substring(0, 8) + r.getTime().substring(9, 15));
                        }
                        if (room.getNewestMessage() != null) {
                            t2 = Long.parseLong(room.getNewestMessage().getTime().substring(0, 8) + room.getNewestMessage().getTime().substring(9, 15));
                        } else {
                            t2 = Long.parseLong(room.getTime().substring(0, 8) + room.getTime().substring(9, 15));
                        }
                        if (t < t2) {
                            break;
                        } else {
                            index++;
                        }
                    }
                    if (!roomList.contains(room)) {
                        boolean inList = false;
                        for (Room r : roomList) {
                            if (r.getKey().equals(room.getKey())) {
                                inList = true;
                            }
                        }
                        if (!inList) {
                            roomList.add(index, room);
                        }
                    }
                } else {
                    roomList.add(room);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requestPassword(final Room room) {
        String roomKey = room.getKey();

        if (room.getPasswd().equals(fileOperations.readFromFile(String.format(FileOperations.passwordFilePattern, roomKey)))) {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("room_name", room.getName());
            intent.putExtra("room_key", roomKey);
            intent.putExtra("last_read_message", fileOperations.readFromFile(String.format(FileOperations.newestMessageFilePattern, roomKey)));
            if (room.getNewestMessage() != null) {
                intent.putExtra("nmid", room.getNewestMessage().getKey());
                fileOperations.writeToFile(room.getNewestMessage().getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
            } else {
                intent.putExtra("nmid", roomKey);
                fileOperations.writeToFile(room.getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
            }
            adapter.notifyDataSetChanged();
            startActivity(intent);
        }
    }

    private void updateRoomList(String key) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getKey().equals(key)) {
                roomList.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public BroadcastReceiver roomLeaveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRoomList(intent.getStringExtra("roomkey"));
        }
    };

    public BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getStringExtra("searchkey");
            if (!s.trim().isEmpty()) {
                ArrayList<Room> searchResultList = searchRoom(s);

                if (!searchResultList.isEmpty()) {
                    adapter = new RoomAdapter(getContext(), searchResultList, RoomAdapter.RoomListType.MY_ROOMS);
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    noRoomFound.setText("");
                    adapter.notifyDataSetChanged();
                } else {
                    listView.setVisibility(View.GONE);
                    noRoomFound.setText(R.string.noroomfound);
                }
            } else {
                adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MY_ROOMS);
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