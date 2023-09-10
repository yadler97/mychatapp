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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yannick.mychatapp.Constants;
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
    private RoomAdapter adapter, searchAdapter;
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms");
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

        adapter = new RoomAdapter(getContext(), roomList, RoomAdapter.RoomListType.MY_ROOMS);
        searchAdapter = new RoomAdapter(getContext(), searchRoomList, RoomAdapter.RoomListType.MY_ROOMS);
        listView.setAdapter(adapter);

        fileOperations = new FileOperations(getActivity());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(muteReceiver, new IntentFilter("muteroom"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(roomLeaveReceiver, new IntentFilter("leaveroom"));
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
        final Room room = dataSnapshot.child(Constants.roomDataKey).getValue(Room.class);
        room.setKey(roomKey);
        room.setMuted(fileOperations.readFromFile(String.format(FileOperations.muteFilePattern, roomKey)).equals("1"));

        if (room.getPasswd().equals(fileOperations.readFromFile(String.format(FileOperations.passwordFilePattern, roomKey)))) {
            if (dataSnapshot.child(Constants.messagesKey).getChildrenCount() > 0) {
                DatabaseReference newestMessageRoot = root.child(roomKey).child(Constants.messagesKey);
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
                        if (room.isNewer(r)) {
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
                                break;
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

    public BroadcastReceiver muteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (Room r : roomList) {
                if (r.getKey().equals(intent.getStringExtra("roomkey"))) {
                    r.setMuted(intent.getBooleanExtra("muted", false));
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    public BroadcastReceiver roomLeaveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            roomList.removeIf(r -> r.getKey().equals(intent.getStringExtra("roomkey")));
            adapter.notifyDataSetChanged();
        }
    };

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
                Room r2 = new Room(r.getKey(), r.getName(), r.getCategory(), r.getTime(), r.getPasswd(), r.getAdmin());
                r2.setImg(r.getImg());
                r2.setNewestMessage(r.getNewestMessage());
                r2.setSearchString(text);
                searchRoomList.add(r2);
            }
        }
    }
}