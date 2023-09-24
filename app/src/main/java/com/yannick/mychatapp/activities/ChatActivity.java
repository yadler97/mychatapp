package com.yannick.mychatapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.signature.ObjectKey;
import com.chrisrenke.giv.GravityImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.adapters.ForwardMessageAdapter;
import com.yannick.mychatapp.data.Background;
import com.yannick.mychatapp.BuildConfig;
import com.yannick.mychatapp.CatchViewPager;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.adapters.FullScreenImageAdapter;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.adapters.ImageAdapter;
import com.yannick.mychatapp.ImageOperations;
import com.yannick.mychatapp.adapters.MemberListAdapter;
import com.yannick.mychatapp.data.Image;
import com.yannick.mychatapp.data.Message;
import com.yannick.mychatapp.adapters.MessageAdapter;
import com.yannick.mychatapp.adapters.PinboardAdapter;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.data.Room;
import com.yannick.mychatapp.data.Theme;
import com.yannick.mychatapp.data.User;
import com.yannick.mychatapp.ZoomOutPageTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;

    private Theme theme;

    private Room room;

    private String userID;
    private String roomKey;
    private String imageURL;
    private String appName;
    private String roomName;
    private String lastReadMessage;
    private String lastKey;
    private String lastSearch = "";
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.usersDatabaseKey);
    private final DatabaseReference roomRoot = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.roomsDatabaseKey);
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri photoURI;

    private RecyclerView recyclerView;
    private MessageAdapter mAdapter;
    private CoordinatorLayout layout;
    private GestureDetector gestureDetector;
    private String quoteStatus = "";
    private int messageCount = 0;

    private final ArrayList<Message> messageList = new ArrayList<>();
    private final ArrayList<User> userList = new ArrayList<>();
    private ArrayList<Message> searchResultList = new ArrayList<>();
    private final ArrayList<Room> roomList = new ArrayList<>();
    private final ArrayList<String> imageList = new ArrayList<>();
    private final ArrayList<Message> pinnedList = new ArrayList<>();
    private final ArrayList<User> memberList = new ArrayList<>();

    private AlertDialog imageListAlert;
    private AlertDialog pinboardAlert;
    private boolean userListCreated = false;
    private boolean cancelFullscreenImage = false;
    private boolean lastReadMessageReached = false;
    private FloatingActionButton scrollDownButton;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");

    private int categoryIndex = 0;

    private GravityImageView backgroundView;
    private ImageButton roomImageButton;

    private TextView quoteText;
    private LinearLayout quoteLayout;
    private ImageView quoteImageImageView;

    private SearchView searchView;

    private Dialog fullscreendialog;

    private FirebaseAuth mAuth;

    private final FileOperations fileOperations = new FileOperations(this);

    private SharedPreferences settings;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.chat_room);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        recyclerView = findViewById(R.id.recycler_view);
        layout = findViewById(R.id.coordinatorlayout);
        backgroundView = findViewById(R.id.backgroundview);
        quoteText = findViewById(R.id.quote_text);
        ImageButton removeQuoteButton = findViewById(R.id.quote_remove);
        quoteLayout = findViewById(R.id.quote_layout);
        quoteImageImageView = findViewById(R.id.quote_image);

        mAdapter = new MessageAdapter(messageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        setBackgroundImage();

        mAuth = FirebaseAuth.getInstance();

        scrollDownButton = findViewById(R.id.scrolldown);
        scrollDownButton.setOnClickListener(view -> recyclerView.scrollToPosition(messageList.size() - 1));

        appName = getResources().getString(R.string.app_name);

        scrollDownButton.hide();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    scrollDownButton.hide();
                } else if (dy <0 && !scrollDownButton.isShown()) {
                    scrollDownButton.show();
                }
            }
        });

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        LocalBroadcastManager.getInstance(this).registerReceiver(quoteReceiver, new IntentFilter("quote"));
        LocalBroadcastManager.getInstance(this).registerReceiver(quotedReceiver, new IntentFilter("quotedMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(permissionReceiver, new IntentFilter("permission"));
        LocalBroadcastManager.getInstance(this).registerReceiver(userReceiver, new IntentFilter("userprofile"));
        LocalBroadcastManager.getInstance(this).registerReceiver(forwardReceiver, new IntentFilter("forward"));
        LocalBroadcastManager.getInstance(this).registerReceiver(fullscreenReceiver, new IntentFilter("fullscreenimage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(pinReceiver, new IntentFilter("pinMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(jumppinnedReceiver, new IntentFilter("jumppinned"));
        LocalBroadcastManager.getInstance(this).registerReceiver(closeFullscreenReceiver, new IntentFilter("closefullscreen"));

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button sendMessageButton = findViewById(R.id.btn_send);
        messageInput = findViewById(R.id.message_input);
        ImageButton cameraButton = findViewById(R.id.btn_camera);
        ImageButton imageButton = findViewById(R.id.btn_image);

        userID = mAuth.getCurrentUser().getUid();
        roomName = getIntent().getExtras().get("room_name").toString();
        roomKey = getIntent().getExtras().get("room_key").toString();
        String newestMessageId = getIntent().getExtras().get("nmid").toString();
        lastReadMessage = getIntent().getExtras().get("last_read_message").toString();
        lastReadMessageReached = (newestMessageId.equals(lastReadMessage));
        setTitle(roomName);
        fileOperations.writeToFile(roomKey, FileOperations.currentInputFilePattern);

        int pushID = 0;
        for (int i = 0; i < roomKey.length(); ++i) {
            pushID += (int) roomKey.charAt(i);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(pushID);

        if (settings.getBoolean(Constants.settingsSaveEnteredTextKey, true)) {
            messageInput.setText(fileOperations.readFromFile(String.format(FileOperations.currentInputFilePattern, roomKey)).replaceAll("<br />", "\n"));
        }

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        messageInput.setOnClickListener(view -> recyclerView.scrollToPosition(messageList.size() - 1));

        recyclerView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if (i3 < i7 && !cancelFullscreenImage) {
                recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(
                        recyclerView.getAdapter().getItemCount() - 1), 0);
            } else {
                cancelFullscreenImage = false;
            }
        });

        DatabaseReference root = roomRoot.child(roomKey);

        sendMessageButton.setOnClickListener(view -> {
            String newMessage = messageInput.getText().toString().trim();
            if (!newMessage.isEmpty()) {
                if (!newMessage.replaceAll("\\*", "").replaceAll("_", "").replaceAll("~", "").isEmpty()) {
                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                        searchView.setIconified(true);
                    }

                    String newMessageKey = root.push().getKey();

                    String currentDateAndTime = sdf.format(new Date());

                    DatabaseReference messageRoot = root.child(Constants.messagesDatabaseKey).child(newMessageKey);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("sender", userID);
                    map.put("text", messageInput.getText().toString().trim());
                    map.put("image", "");
                    map.put("pinned", false);
                    map.put("quote", quoteStatus);
                    map.put("time", currentDateAndTime);

                    messageRoot.updateChildren(map);
                    messageInput.getText().clear();
                    quoteStatus = "";
                    quoteText.setText("");
                    quoteImageImageView.setImageDrawable(null);
                    quoteImageImageView.setVisibility(View.GONE);
                    quoteLayout.setVisibility(View.GONE);
                    fileOperations.writeToFile("", String.format(FileOperations.currentInputFilePattern, roomKey));
                    fileOperations.writeToFile(newMessageKey, String.format(FileOperations.newestMessageFilePattern, roomKey));
                } else {
                    Toast.makeText(ChatActivity.this, R.string.illegalinput, Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButton.setOnTouchListener((view, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                chooseImage();
                imageButton.setBackgroundResource(R.drawable.icon_image);
                return true;
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    imageButton.setBackgroundResource(R.drawable.icon_image_pressed);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    imageButton.setBackgroundResource(R.drawable.icon_image);
                    return true;
                }
            }
            return false;
        });

        cameraButton.setOnTouchListener((view, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                takePicture();
                cameraButton.setBackgroundResource(R.drawable.icon_camera);
                return true;
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cameraButton.setBackgroundResource(R.drawable.icon_camera_pressed);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    cameraButton.setBackgroundResource(R.drawable.icon_camera);
                    return true;
                }
            }
            return false;
        });

        removeQuoteButton.setOnTouchListener((view, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                quoteStatus = "";
                quoteText.setText("");
                quoteImageImageView.setImageDrawable(null);
                quoteImageImageView.setVisibility(View.GONE);
                quoteLayout.setVisibility(View.GONE);
                removeQuoteButton.setBackgroundResource(R.drawable.icon_clear);
                return true;
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    removeQuoteButton.setBackgroundResource(R.drawable.icon_clear_pressed);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    removeQuoteButton.setBackgroundResource(R.drawable.icon_clear);
                    return true;
                }
            }
            return false;
        });

        userRoot.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addUser(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (mAuth.getCurrentUser() != null) {
                    Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!userListCreated) {
                    handler.postDelayed(this, 1000);
                } else {
                    root.child(Constants.roomDataDatabaseKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            getRoomData(snapshot);
                            if (messageList.isEmpty()) {
                                addHeaderMessage();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    root.child(Constants.messagesDatabaseKey).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            addMessage(dataSnapshot, -1);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            changeMessage(dataSnapshot);
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            removeMessage(dataSnapshot);
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (mAuth.getCurrentUser() != null) {
                                Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }, 10);

        roomRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
                    String roomKey = uniqueKeySnapshot.getKey();
                    if (roomKey.equals(ChatActivity.this.roomKey)) {
                        messageCount = (int)uniqueKeySnapshot.child(Constants.messagesDatabaseKey).getChildrenCount();
                    }

                    Room room = uniqueKeySnapshot.child(Constants.roomDataDatabaseKey).getValue(Room.class);
                    room.setKey(roomKey);
                    if (room.getPassword().equals(fileOperations.readFromFile(String.format(FileOperations.passwordFilePattern, roomKey))) && !roomKey.equals(ChatActivity.this.roomKey)) {
                        roomList.add(room);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (mAuth.getCurrentUser() != null) {
                    Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getRoomData(DataSnapshot dataSnapshot) {
        room = dataSnapshot.getValue(Room.class);
        if (room != null) {
            room.setKey(dataSnapshot.getRef().getParent().getKey());
            User user = getUser(room.getAdmin());

            if (!memberList.contains(user)) {
                memberList.add(user);
            }
        }
    }

    private void addHeaderMessage() {
        String creationTime = room.getTime();

        try {
            creationTime = sdf.format(sdf.parse(creationTime));
        } catch (ParseException e) {
            Log.e("ParseException", e.toString());
        }

        User user = getUser(room.getAdmin());
        String creationTimeCon = creationTime.substring(6, 8) + "." + creationTime.substring(4,6) + "." + creationTime.substring(0, 4);
        String text = getResources().getString(R.string.roomintro, creationTimeCon, user.getName());
        Message m = new Message(user, text, creationTime, room.getKey(), Message.Type.HEADER, null, false);

        messageList.add(m);
        mAdapter.notifyDataSetChanged();
    }

    private void addMessage(DataSnapshot dataSnapshot, int index) {
        String key = dataSnapshot.getKey();
        String text = dataSnapshot.child("text").getValue().toString();
        String image = dataSnapshot.child("image").getValue().toString();
        String userId = dataSnapshot.child("sender").getValue().toString();
        boolean pinned = (boolean) dataSnapshot.child("pinned").getValue();
        String quote = dataSnapshot.child("quote").getValue().toString();
        String time = dataSnapshot.child("time").getValue().toString();

        User user = getUser(userId);

        try {
            time = sdf.format(sdf.parse(time));
        } catch (ParseException e) {
            Log.e("ParseException", e.toString());
        }
        if (lastReadMessage.equals(lastKey) && !lastReadMessageReached) {
            Message m = new Message(user, getResources().getString(R.string.unreadmessages), time, "-", Message.Type.HEADER, null, false);
            messageList.add(m);
        }
        lastKey = key;

        if (index == -1 && !messageList.get(messageList.size() - 1).getTime().substring(0, 8).equals(time.substring(0, 8))) {
            String timeText = time.substring(6, 8) + "." + time.substring(4, 6) + "." + time.substring(0, 4);
            Message m = new Message(user, timeText, time, "-", Message.Type.HEADER, null, false);
            messageList.add(m);
        }

        boolean sender = userID.equals(user.getUserID());
        boolean con = false;
        int ind = (index == -1) ? messageList.size() : index;
        if (messageList.size() - 1 > 0 && messageList.get(ind - 1).getType() != Message.Type.HEADER && messageList.get(ind - 1).getUser().getUserID().equals(userId) && messageList.get(ind - 1).getTime().substring(0, 13).equals(time.substring(0, 13))) {
            con = true;
            mAdapter.notifyDataSetChanged();
        }

        Message m;
        if (quote.equals("")) {
            if (!text.equals("")) {
                if (text.length() > 11 && text.substring(0, 12).equals("(Forwarded) ")) {
                    if (text.length() > 2000 + 12) {
                        m = new Message(user, text.substring(12), time, key, Message.getFittingForwardedExpandableMessageType(sender, con), null, pinned);
                    } else {
                        m = new Message(user, text.substring(12), time, key, Message.getFittingForwardedMessageType(sender, con), null, pinned);
                    }
                } else {
                    if (text.length() > 2000) {
                        m = new Message(user, text, time, key, Message.getFittingExpandableMessageType(sender, con), null, pinned);
                    } else {
                        m = new Message(user, text, time, key, Message.getFittingBasicMessageType(sender, con), null, pinned);
                    }
                }
            } else {
                if (!imageList.contains(image)) {
                    imageList.add(image);
                }
                m = new Message(user, image, time, key, Message.getFittingImageMessageType(sender, con), null, pinned);
            }
        } else {
            Message quotedMessage = null;
            for (Message quoteMsg : messageList) {
                if (quoteMsg.getKey().equals(quote)) {
                    quotedMessage = quoteMsg;
                    break;
                }
            }

            if (quotedMessage != null) {
                if (!Message.isImage(quotedMessage.getType())) {
                    m = new Message(user, text, time, key, Message.getFittingQuoteMessageType(sender, con), quotedMessage, pinned);
                } else {
                    m = new Message(user, text, time, key, Message.getFittingQuoteImageMessageType(sender, con), quotedMessage, pinned);
                }
            } else {
                quotedMessage = new Message();
                quotedMessage.setText(getResources().getString(R.string.quotedmessagenolongeravailable));
                quotedMessage.setKey(quote);
                m = new Message(user, text, time, key, Message.getFittingQuoteDeletedMessageType(sender, con), quotedMessage, pinned);
            }
        }
        if (index != -1 && messageList.get(ind).getTime().equals("")) {
            m.setTime("");
        }
        if (index == -1) {
            messageList.add(m);
            if (memberList.stream().noneMatch(u -> m.getUser().getUserID().equals(u.getUserID()))) {
                memberList.add(m.getUser());
            }
            if (pinned) {
                pinnedList.add(m);
            }
        } else {
            ArrayList<Message> templist = new ArrayList<>(messageList);
            messageList.clear();
            templist.add(index+1, m);
            messageList.addAll(templist);
            mAdapter.notifyDataSetChanged();
        }

        recyclerView.scrollToPosition(messageList.size() - 1);
        scrollDownButton.hide();
    }

    private void removeMessage(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        String image = dataSnapshot.child("image").getValue().toString();

        if (!image.equals("")) {
            StorageReference pathReference = storage.getReference().child(Constants.imagesStorageKey + image);
            imageList.remove(image);
            pathReference.delete();
        }

        List<Message> tempMessageList = new ArrayList<>();
        for (Message m : messageList) {
            if (!(m.getKey().equals(key))) {
                tempMessageList.add(m);
            } else if (m.getType() != Message.Type.HEADER && Message.isConMessage(m.getType()) && !m.getTime().equals("")) {
                tempMessageList.get(tempMessageList.size()-1).setTime(m.getTime());
            }
        }

        if (tempMessageList.get(tempMessageList.size()-1).getType() == Message.Type.HEADER && tempMessageList.size()!=1) {
            tempMessageList.remove(tempMessageList.size()-1);
        }
        for (int j = 1; j < tempMessageList.size()-1; j++) {
            if (tempMessageList.get(j).getType() == Message.Type.HEADER && tempMessageList.get(j+1).getType() == Message.Type.HEADER) {
                tempMessageList.remove(j);
            }
        }

        messageList.clear();
        for (Message m : tempMessageList) {
            if (m.getQuotedMessage() != null) {
                if (m.getQuotedMessage().getKey().equals(key)) {
                    m.getQuotedMessage().setText(getResources().getString(R.string.quotedmessagenolongeravailable));
                    m.getQuotedMessage().setUser(null);
                    m.setType(Message.getQuoteDeletedTypeForQuoteType(m.getType()));
                }
            }
            messageList.add(m);
        }

        mAdapter.notifyDataSetChanged();
    }

    private void changeMessage(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        String image = dataSnapshot.child("image").getValue().toString();
        String text = dataSnapshot.child("text").getValue().toString();
        String userID = dataSnapshot.child("sender").getValue().toString();

        for (Message m : messageList) {
            if (m.getType() != Message.Type.HEADER) {
                if (m.getKey().equals(key)) {
                    int index = messageList.indexOf(m);
                    addMessage(dataSnapshot, index);
                    ArrayList<Message> templist = new ArrayList<>(messageList);
                    messageList.clear();
                    templist.remove(index);
                    messageList.addAll(templist);
                    break;
                }
            }
        }

        for (Message m : messageList) {
            if (m.getType() != Message.Type.HEADER) {
                if (m.getQuotedMessage() != null) {
                    if (m.getQuotedMessage().getKey().equals(key)) {
                        if (image.equals("")) {
                            m.getQuotedMessage().setText(text);
                        } else {
                            m.getQuotedMessage().setText(image);
                        }
                        m.getQuotedMessage().setUser(getUser(userID));
                    }
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void setBackgroundImage() {
        Background background = Background.getCurrentBackground(getApplicationContext());
        if (getResources().getConfiguration().orientation != 2) {
            switch (background) {
                case BREATH_OF_THE_WILD:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_botw, null));
                    break;
                case SPLATOON_2:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_splatoon2, null));
                    break;
                case PERSONA_5:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_persona, null));
                    break;
                case KIMI_NO_NA_WA:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_kiminonawa, null));
                    break;
                case SUPER_MARIO_BROS:
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_smb, null));
                    break;
                case SUPER_MARIO_MAKER:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_smm, null));
                    break;
                case XENOBLADE_CHRONICLES_2:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_xc2, null));
                    break;
                case FIRE_EMBLEM_FATES:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_fef, null));
                    break;
                case SUPER_SMASH_BROS_ULTIMATE:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_ssbu, null));
                    break;
                case DETECTIVE_PIKACHU:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_dp, null));
                    break;
                default:
                    break;
            }
        } else {
            switch (background) {
                case BREATH_OF_THE_WILD:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_botw_horizontal, null));
                    break;
                case SPLATOON_2:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_splatoon2_horizontal, null));
                    break;
                case PERSONA_5:
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_persona_horizontal, null));
                    break;
                case KIMI_NO_NA_WA:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_kiminonawa_horizontal, null));
                    break;
                case SUPER_MARIO_BROS:
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_smb_horizontal, null));
                    break;
                case SUPER_MARIO_MAKER:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_smm_horizontal, null));
                    break;
                case XENOBLADE_CHRONICLES_2:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_xc2_horizontal, null));
                    break;
                case FIRE_EMBLEM_FATES:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_fef_horizontal, null));
                    break;
                case SUPER_SMASH_BROS_ULTIMATE:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_ssbu_horizontal, null));
                    break;
                case DETECTIVE_PIKACHU:
                    backgroundView.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_dp_horizontal, null));
                    break;
                default:
                    break;
            }
        }
    }

    private void changeTheme(Theme theme) {
        this.theme = theme;
        if (theme == Theme.DARK) {
            setTheme(R.style.DarkThemeChat);
        } else {
            setTheme(R.style.AppThemeChat);
        }
    }

    private void uploadImage(Uri filePath, final int type) {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            searchView.setIconified(true);
        }

        final ProgressDialog progressDialog;
        if (theme == Theme.DARK) {
            progressDialog = new ProgressDialog(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setTitle(R.string.upload);
        progressDialog.show();

        String imageName = UUID.randomUUID().toString();
        StorageReference ref;
        if (type == ImageOperations.PICK_ROOM_IMAGE_REQUEST) {
            ref = storageReference.child(Constants.roomImagesStorageKey + imageName);
        } else {
            ref = storageReference.child(Constants.imagesStorageKey + imageName);
        }

        ImageOperations imageOperations = new ImageOperations(getContentResolver());
        byte[] byteArray = imageOperations.getImageAsBytes(this, filePath, type);

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();

            if (type != ImageOperations.PICK_ROOM_IMAGE_REQUEST) {
                String newMessageKey = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).push().getKey();

                String currentDateAndTime = sdf.format(new Date());

                DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).child(newMessageKey);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("sender", userID);
                map.put("text", "");
                map.put("image", imageName);
                map.put("pinned", false);
                map.put("quote", "");
                map.put("time", currentDateAndTime);

                messageRoot.updateChildren(map);

                quoteStatus = "";

                if (type == ImageOperations.CAPTURE_IMAGE_REQUEST && settings.getBoolean(Constants.settingsStoreCameraPicturesKey, true)) {
                    downloadImage(imageName, type);
                }
            } else {
                DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.roomDataDatabaseKey);
                Map<String, Object> map = new HashMap<>();
                map.put("image", imageName);
                messageRoot.updateChildren(map);

                room.setImage(imageName);

                StorageReference pathReference = storage.getReference().child(Constants.roomImagesStorageKey + imageName);
                GlideApp.with(getApplicationContext())
                        .load(pathReference)
                        .centerCrop()
                        .thumbnail(0.05f)
                        .into(roomImageButton);
            }
        }).addOnFailureListener(e -> {
            Log.e("Upload failed", e.toString());
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                    .getTotalByteCount());
            progressDialog.setMessage((int)progress+"% " + getResources().getString(R.string.uploaded));
        });
    }

    ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri filePath = data.getData();
                    if (filePath != null) {
                        uploadImage(filePath, ImageOperations.PICK_IMAGE_REQUEST);
                    }
                }
            });

    ActivityResultLauncher<Intent> pickRoomImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri filePath = data.getData();
                    if (filePath != null) {
                        uploadImage(filePath, ImageOperations.PICK_ROOM_IMAGE_REQUEST);
                    }
                }
            });

    ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoURI != null) {
                        uploadImage(photoURI, ImageOperations.CAPTURE_IMAGE_REQUEST);
                    }
                }
            });

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e("error creating file", e.toString());
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
                if (isStoragePermissionGranted(1)) {
                    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                    takePhotoLauncher.launch(takePictureIntent);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = sdf.format(new Date()).substring(0, 15);
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpeg", storageDir);
    }

    public BroadcastReceiver quoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            quoteStatus = intent.getStringExtra("quoteID");
            for (Message m : messageList) {
                if (m.getKey().equals(quoteStatus)) {
                    String user;
                    if (m.getUser().getUserID().equals(userID)) {
                        user = getResources().getString(R.string.you);
                    } else {
                        user = m.getUser().getName();
                    }
                    if (!Message.isImage(m.getType())) {
                        String text = user + " " + m.getText();
                        SpannableStringBuilder str = new SpannableStringBuilder(text);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, user.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        str.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), user.length()+1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        quoteImageImageView.setVisibility(View.GONE);
                        quoteText.setText(str);
                    } else {
                        SpannableStringBuilder str = new SpannableStringBuilder(user);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, user.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        String imageURL = m.getText();
                        StorageReference pathReference = storage.getReference().child(Constants.imagesStorageKey + imageURL);
                        GlideApp.with(context)
                                .load(pathReference)
                                .placeholder(R.color.grey)
                                .centerCrop()
                                .thumbnail(0.05f)
                                .into(quoteImageImageView);
                        quoteImageImageView.setVisibility(View.VISIBLE);
                        quoteText.setText(str);
                    }
                    quoteLayout.setVisibility(View.VISIBLE);
                    break;
                }
            }
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    };

    public BroadcastReceiver quotedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!searchView.isIconified()) {
                searchView.setIconified(true);
                searchView.setIconified(true);
            }
            
            String quoted = intent.getStringExtra("quoteID");
            int pos = 0;
            for (Message m : messageList) {
                if (m.getKey().equals(quoted)) {
                    break;
                } else {
                    pos++;
                }
            }
            recyclerView.scrollToPosition(pos);
        }
    };

    public BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            imageURL = intent.getStringExtra("imageURL");
            if (isStoragePermissionGranted(0)) {
                downloadImage(imageURL, ImageOperations.PICK_IMAGE_REQUEST);
            }
        }
    };

    public BroadcastReceiver userReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra("userid");
            showProfile(userId);
        }
    };

    public BroadcastReceiver fullscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String image = intent.getStringExtra("image");
            showFullscreenImage(image, Image.MESSAGE_IMAGE);
        }
    };

    public BroadcastReceiver forwardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String messageId = intent.getStringExtra("forwardID");
            if (!roomList.isEmpty()) {
                forwardMessage(messageId);
            } else {
                Toast.makeText(getApplicationContext(), R.string.noroomfound, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public BroadcastReceiver pinReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pin = intent.getStringExtra("pinID");
            pinMessage(pin);
        }
    };

    public BroadcastReceiver jumppinnedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pinnedKey = intent.getStringExtra("pinnedKey");
            int pos = 0;
            for (Message m : messageList) {
                if (m.getKey().equals(pinnedKey)) {
                    break;
                } else {
                    pos++;
                }
            }
            pinboardAlert.dismiss();
            recyclerView.scrollToPosition(pos);
        }
    };

    public BroadcastReceiver closeFullscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            cancelFullscreenImage = true;
            View decorView = fullscreendialog.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
            fullscreendialog.dismiss();
        }
    };

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public boolean isStoragePermissionGranted(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v("StoragePermission", "Permission is granted");
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("StoragePermission", "Permission is granted");
            if (requestCode == 0) {
                downloadImage(imageURL, ImageOperations.PICK_IMAGE_REQUEST);
            } else if (requestCode == 1) {
                takePicture();
            } else if (requestCode == 2) {
                writeBackup(createBackup());
            }
        } else {
            Log.v("StoragePermission", "Permission is rejected");
        }
    }

    private void downloadImage(String imageURL, final int type) {
        final StorageReference pathReference = storage.getReference().child(Constants.imagesStorageKey + imageURL);

        final Context context = getApplicationContext();
        final File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/" + appName);
        if (!rootPath.exists()) {
            if (!rootPath.mkdirs()) {
                Log.e("firebase ", "Creating dir failed");
            }
        }

        pathReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            String mimeType = storageMetadata.getContentType();
            String currentDateAndTime = sdf.format(new Date());
            final String filename = appName + "_" + currentDateAndTime.substring(0,15) + "." + mimeType.substring(6);
            final File localFile = new File(rootPath,filename);

            pathReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                notifyGallery(localFile.getAbsolutePath());
                if (type == ImageOperations.PICK_IMAGE_REQUEST) {
                    createSnackbar(localFile, mimeType, getResources().getString(R.string.imagesaved));
                }
            }).addOnFailureListener(exception -> {
                Toast.makeText(context, R.string.savingimagefailed, Toast.LENGTH_SHORT).show();
                Log.e("firebase", "Saving image failed: " + exception);
            });
        });
    }

    private void notifyGallery(String path) {
        MediaScannerConnection.scanFile(getApplicationContext(), new String[] { path }, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!fileOperations.readFromFile(String.format(FileOperations.favFilePattern, roomKey)).equals("1")) {
            if (!fileOperations.readFromFile(String.format(FileOperations.muteFilePattern, roomKey)).equals("1")) {
                inflater.inflate(R.menu.menu_chatroom, menu);
            } else {
                inflater.inflate(R.menu.menu_chatroom_unmute, menu);
            }
        } else {
            if (!fileOperations.readFromFile(String.format(FileOperations.muteFilePattern, roomKey)).equals("1")) {
                inflater.inflate(R.menu.menu_chatroom_unfav, menu);
            } else {
                inflater.inflate(R.menu.menu_chatroom_unmute_unfav, menu);
            }
        }

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem searchItem = menu.findItem(R.id.roomsearch);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setFocusable(false);
        searchView.setQueryHint(getResources().getString(R.string.searchmessage));
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                TextView noMessageFound = findViewById(R.id.no_message_found);
                if (!s.trim().isEmpty()) {
                    searchResultList = searchMessage(s);

                    if (!searchResultList.isEmpty()) {
                        mAdapter = new MessageAdapter(searchResultList);
                        recyclerView.setAdapter(mAdapter);
                        recyclerView.setVisibility(View.VISIBLE);
                        noMessageFound.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(searchResultList.size() - 1);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noMessageFound.setVisibility(View.VISIBLE);
                    }
                } else {
                    mAdapter = new MessageAdapter(messageList);
                    recyclerView.setVisibility(View.VISIBLE);
                    noMessageFound.setVisibility(View.GONE);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.roominfo) {
            openInfo();
            return true;
        } else if (item.getItemId() == R.id.roomfav) {
            markAsFav();
            return true;
        } else if (item.getItemId() == R.id.roomsearch) {
            return super.onOptionsItemSelected(item);
        } else if (item.getItemId() == R.id.roomphotos) {
            openImageList();
            return true;
        } else if (item.getItemId() == R.id.roompinboard) {
            openPinboard();
            return true;
        } else if (item.getItemId() == R.id.roommute) {
            muteRoom();
            return true;
        } else if (item.getItemId() == R.id.roombackup) {
            if (messageList.size() > 1) {
                writeBackup(createBackup());
            } else {
                Toast.makeText(this, R.string.nomessagesfound, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.roomleave) {
            leaveRoom();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(userReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(forwardReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(fullscreenReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(jumppinnedReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(closeFullscreenReceiver);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if (settings.getBoolean(Constants.settingsSaveEnteredTextKey, true)) {
                fileOperations.writeToFile(messageInput.getText().toString().trim().replaceAll("\\n", "<br />"), String.format(FileOperations.currentInputFilePattern, roomKey));
                if (!messageInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, R.string.messagesaved, Toast.LENGTH_SHORT).show();
                }
            }
            if (!messageList.isEmpty()) {
                fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
            }
            fileOperations.writeToFile("0", FileOperations.currentRoomFile);
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openInfo() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.room_info, null);

        final CircleImageView roomImage = view.findViewById(R.id.room_image);
        final TextView roomNameText = view.findViewById(R.id.room_name);
        final TextView roomDescriptionText = view.findViewById(R.id.room_description);
        final TextView roomCategoryText = view.findViewById(R.id.room_cat);
        final TextView roomCreationDateText = view.findViewById(R.id.room_creation);
        final TextView roomMessageCountText = view.findViewById(R.id.room_amount_messages);

        final ListView memberListView = view.findViewById(R.id.memberList);
        memberListView.setAdapter(new MemberListAdapter(getApplicationContext(), memberList, room.getAdmin()));

        String time = room.getTime().substring(6, 8) + "." + room.getTime().substring(4, 6) + "." + room.getTime().substring(0, 4);
        roomNameText.setText(room.getName());
        roomDescriptionText.setText(room.getDescription());
        roomCategoryText.setText(getResources().getStringArray(R.array.categories)[room.getCategory()]);
        roomCreationDateText.setText(time);
        roomMessageCountText.setText(String.valueOf(messageCount));

        final StorageReference refRoomImage = storage.getReference().child(Constants.roomImagesStorageKey + room.getImage());
        GlideApp.with(getApplicationContext())
                //.using(new FirebaseImageLoader())
                .load(refRoomImage)
                .centerCrop()
                .into(roomImage);

        roomImage.setOnClickListener(v -> showFullscreenImage(room.getImage(), Image.ROOM_IMAGE));

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.roominfo)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        if (room.getAdmin().equals(userID)) {
            builder.setNegativeButton(R.string.editroom, (dialogInterface, i) -> editRoom());
            builder.setNeutralButton(R.string.delete_room, (dialogInterface, i) -> deleteRoom());
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(forwardReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fullscreenReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(jumppinnedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeFullscreenReceiver);
        if (settings.getBoolean(Constants.settingsSaveEnteredTextKey, true)) {
            fileOperations.writeToFile(messageInput.getText().toString().trim().replaceAll("\\n", "<br />"), String.format(FileOperations.currentInputFilePattern, roomKey));
        }
        fileOperations.writeToFile("0", FileOperations.currentRoomFile);
        if ((messageList.size() - 1) >= 0) {
            fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        if (settings.getBoolean(Constants.settingsSaveEnteredTextKey, true)) {
            fileOperations.writeToFile(messageInput.getText().toString().trim().replaceAll("\\n", "<br />"), String.format(FileOperations.currentInputFilePattern, roomKey));
        }
        if (!messageList.isEmpty()) {
            fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), String.format(FileOperations.newestMessageFilePattern, roomKey));
        }
        fileOperations.writeToFile("0", FileOperations.currentRoomFile);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        fileOperations.writeToFile("0", FileOperations.currentRoomFile);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        fileOperations.writeToFile(roomKey, FileOperations.currentRoomFile);
        super.onResume();
    }

    private ArrayList<Message> searchMessage(String text) {
        ArrayList<Message> searchedMessageList = new ArrayList<>();
        if (!lastSearch.isEmpty() && text.contains(lastSearch)) {
            for (Message m : searchResultList) {
                searchStringInMessage(searchedMessageList, m, text);
            }
        } else {
            for (Message m : messageList) {
                searchStringInMessage(searchedMessageList, m, text);
            }
        }

        lastSearch = text;
        return searchedMessageList;
    }

    private ArrayList<Message> searchStringInMessage(ArrayList<Message> searchedMessageList, Message m, String text) {
        if (m.getText().toLowerCase().contains(text.toLowerCase()) && m.getType() != Message.Type.HEADER && !Message.isImage(m.getType())) {
            if (searchedMessageList.isEmpty() || !searchedMessageList.get(searchedMessageList.size() - 1).getTime().substring(0, 8).equals(m.getTime().substring(0, 8))) {
                String time = m.getTime().substring(6, 8) + "." + m.getTime().substring(4, 6) + "." + m.getTime().substring(0, 4);
                Message m2 = new Message(m.getUser(), time, m.getTime(), "-", Message.Type.HEADER, null, m.isPinned());
                searchedMessageList.add(m2);
            }
            Message m2;
            if (Message.isConMessage(m.getType())) {
                m2 = new Message(m.getUser(), m.getText(), m.getTime(), m.getKey(), Message.getNonConTypeForConType(m.getType()), m.getQuotedMessage(), m.isPinned());
            } else {
                m2 = new Message(m.getUser(), m.getText(), m.getTime(), m.getKey(), m.getType(), m.getQuotedMessage(), m.isPinned());
            }
            m2.setSearchString(text);
            searchedMessageList.add(m2);
        }
        return searchedMessageList;
    }

    private String createBackup() {
        String currentDateAndTime = sdf.format(new Date());
        String fcdat = currentDateAndTime.substring(0, 4) + "." + currentDateAndTime.substring(4, 6) + "." + currentDateAndTime.substring(6, 8) + " " + currentDateAndTime.substring(9, 11) + ":" + currentDateAndTime.substring(11, 13) + ":" + currentDateAndTime.substring(13, 15);
        String ftime = room.getTime().substring(0, 4) + "." + room.getTime().substring(4, 6) + "." + room.getTime().substring(6, 8) + " " + room.getTime().substring(9, 11) + ":" + room.getTime().substring(11, 13) + ":" + room.getTime().substring(13, 15);
        String backup = getResources().getString(R.string.backupof) + " " + roomName + "\n" + getResources().getString(R.string.createdon) + ": " + fcdat + "\n\n" +
                getResources().getString(R.string.category) + ": " + getResources().getStringArray(R.array.categories)[room.getCategory()] + "\n" + getResources().getString(R.string.admin) + ": " + getUser(room.getAdmin()).getName() + "\n" + getResources().getString(R.string.foundation) + ": " + ftime + "\n" + getResources().getString(R.string.sentmessages) + ": " + messageCount + "\n----------------------------------------\n";

        String newDay = "";
        for (Message m : messageList) {
            String btimeDay = "";
            if (m.getType() != Message.Type.HEADER) {
                btimeDay = m.getTime().substring(0, 4) + "." + m.getTime().substring(4, 6) + "." + m.getTime().substring(6, 8);
                String btime = m.getTime().substring(9, 11) + ":" + m.getTime().substring(11, 13) + ":" + m.getTime().substring(13, 15);
                if (!newDay.equals(btimeDay)) {
                    backup += "\n" + btimeDay + "\n";
                }
                if (Message.isQuote(m.getType()) || m.getType() == Message.Type.QUOTE_IMAGE_RECEIVED_CON || m.getType() == Message.Type.QUOTE_IMAGE_SENT_CON) {
                    String quote = m.getQuotedMessage().getText();
                    if (quote.length() > 40) {
                        quote = quote.substring(0, 40) + "...";
                    }
                    backup += btime + " - [" + m.getQuotedMessage().getUser().getName() + ": " + quote + "] - "  + m.getUser().getName() + ": " + m.getText() + "\n";
                } else {
                    backup += btime + " - " + m.getUser().getName() + ": " + m.getText() + "\n";
                }
            }
            newDay = btimeDay;
        }

        return backup;
    }

    private void writeBackup(String text) {
        if (isStoragePermissionGranted(2)) {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/" + getResources().getString(R.string.app_name) + "/");

            if (!path.exists()) {
                path.mkdirs();
            }

            final File file = new File(path, getResources().getString(R.string.app_name) + "_" + roomName.replace(" ", "") + "_backup.txt");

            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(text);

                myOutWriter.close();

                fOut.flush();
                fOut.close();

                createSnackbar(file, "text/plain", getResources().getString(R.string.backupcreated));
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e);
            }
        }
    }

    private void createSnackbar(final File file, final String mime, final String text) {
        Snackbar snack = Snackbar.make(layout, text, Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(R.color.white))
                .setAction(R.string.open, view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileURI = FileProvider.getUriForFile(ChatActivity.this,
                            BuildConfig.APPLICATION_ID + ".fileprovider",
                            file);
                    intent.setDataAndType(fileURI, mime);
                    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION|FLAG_GRANT_WRITE_URI_PERMISSION|FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
        View sbView = snack.getView();
        if (theme == Theme.DARK) {
            sbView.setBackgroundColor(getResources().getColor(R.color.dark_actionbar));
        } else {
            sbView.setBackgroundColor(getResources().getColor(R.color.red));
        }
        snack.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mAdapter = new MessageAdapter(messageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.scrollToPosition(messageList.size() - 1);

        if (imageListAlert.isShowing()) {
            imageListAlert.dismiss();
            openImageList();
        }

        setBackgroundImage();
    }

    private void markAsFav() {
        Intent intent = new Intent("favroom");
        intent.putExtra("roomKey", roomKey);
        intent.putExtra("roomName", roomName);
        intent.putExtra("admin", room.getAdmin());
        intent.putExtra("category", room.getCategory());
        String creationTime = "";
        try {
            creationTime = sdf.format(sdf.parse(room.getTime()));
        } catch (ParseException e) {
            Log.e("ParseException", e.toString());
        }
        intent.putExtra("newestMessage", creationTime);
        intent.putExtra("password", room.getPassword());
        intent.putExtra("roomImage", room.getImage());
        Message newest = messageList.get(messageList.size() - 1);
        if (messageList.size() != 1) {
            intent.putExtra("nmMessage", newest.getText());
            String parsedTime = "";
            try {
                parsedTime = sdf.format(sdf.parse(newest.getTime()));
            } catch (ParseException e) {
                Log.e("ParseException", e.toString());
            }
            intent.putExtra("nmTime", parsedTime);
            intent.putExtra("nmKey", newest.getKey());
            if (Message.isImage(newest.getType())) {
                intent.putExtra("nmType", Message.Type.IMAGE_RECEIVED.toString());
            } else {
                intent.putExtra("nmType", Message.Type.MESSAGE_RECEIVED.toString());
            }
            intent.putExtra("username", newest.getUser().getName());
            intent.putExtra("userid", newest.getUser().getUserID());
        } else {
            intent.putExtra("nmMessage", "");
            intent.putExtra("nmTime", "");
            intent.putExtra("nmKey", "");
            intent.putExtra("nmType", Message.Type.HEADER.toString());
            intent.putExtra("username", "");
            intent.putExtra("userid", "");
        }
        if (!fileOperations.readFromFile(String.format(FileOperations.favFilePattern, roomKey)).equals("1")) {
            fileOperations.writeToFile("1", String.format(FileOperations.favFilePattern, roomKey));
            Toast.makeText(this, R.string.addedtofavorites, Toast.LENGTH_SHORT).show();
        } else {
            fileOperations.writeToFile("0", String.format(FileOperations.favFilePattern, roomKey));
            Toast.makeText(this, R.string.removedfromfavorites, Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void addUser(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
        user.setUserID(dataSnapshot.getKey());
        userList.add(user);

        userListCreated = true;
    }

    private void showProfile(final String userId) {
        User user = getUser(userId);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.profile, null);

        CircleImageView profileIcon = view.findViewById(R.id.icon_profile);
        TextView profileName = view.findViewById(R.id.name_profile);
        TextView profileDescription = view.findViewById(R.id.profile_bio);
        TextView birthday = view.findViewById(R.id.profile_birthday);
        TextView location = view.findViewById(R.id.profile_location);
        ImageView banner = view.findViewById(R.id.background_profile);
        AlertDialog.Builder builder;

        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            banner.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar_dark, null));
        } else {
            builder = new AlertDialog.Builder(this);
            banner.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar, null));
        }

        final StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + user.getBanner());
        GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);

        final StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + user.getImage());
        GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileIcon);

        profileIcon.setOnClickListener(v -> showFullscreenImage(user.getImage(), Image.PROFILE_IMAGE));
        banner.setOnClickListener(v -> showFullscreenImage(user.getBanner(), Image.PROFILE_BANNER));

        profileName.setText(user.getName());
        profileDescription.setText(user.getDescription());
        birthday.setText(user.getBirthday().substring(6, 8) + "." + user.getBirthday().substring(4, 6) + "." + user.getBirthday().substring(0, 4));
        location.setText(user.getLocation());

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.profile, user.getName())));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void forwardMessage(final String messageID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.forward_message, null);

        final ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(new ForwardMessageAdapter(this, roomList));
        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.forwardmessage)));
        builder.setView(view);
        builder.setPositiveButton(R.string.cancel, null);
        final AlertDialog alert = builder.create();
        alert.show();
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            int position = listView.getPositionForView(view1);
            String roomKey = roomList.get(position).getKey();
            Message fMessage = new Message();
            for (Message m : messageList) {
                if (m.getKey().equals(messageID)) {
                    fMessage = m;
                    break;
                }
            }
            String newMessageKey = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).push().getKey();

            String currentDateAndTime = sdf.format(new Date());

            DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).child(newMessageKey);
            Map<String, Object> map = new HashMap<>();
            map.put("sender", userID);
            if (Message.isImage(fMessage.getType())) {
                map.put("text", "");
                map.put("image", fMessage.getText());
            } else {
                map.put("text", "(Forwarded) " + fMessage.getText());
                map.put("image", "");
            }
            map.put("pinned", false);
            map.put("quote", quoteStatus);
            map.put("time", currentDateAndTime);

            messageRoot.updateChildren(map);
            fileOperations.writeToFile(newMessageKey, String.format(FileOperations.newestMessageFilePattern, roomKey));
            alert.cancel();
            if (Message.isImage(fMessage.getType())) {
                Toast.makeText(ChatActivity.this, R.string.imageforwarded, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatActivity.this, R.string.messageforwarded, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private User getUser(final String userId) {
        for (User u : userList) {
            if (u.getUserID().equals(userId)) {
                return u;
            }
        }

        return User.getUnknownUser(this, userId);
    }

    private void showFullscreenImage(String image, Image type) {
        final View dialogView = getLayoutInflater().inflate(R.layout.fullscreen_image, null);
        if (theme == Theme.DARK) {
            fullscreendialog = new Dialog(this, R.style.FullScreenImageDark);
        } else {
            fullscreendialog = new Dialog(this, R.style.FullScreenImage);
        }
        fullscreendialog.setContentView(dialogView);

        final View decorView = fullscreendialog.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener(i -> {
            if (fullscreendialog.isShowing()) {
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    int uiOptions1 = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE;
                    decorView.setSystemUiVisibility(uiOptions1);
                }, 2000);
            }
        });

        CatchViewPager mViewPager = dialogView.findViewById(R.id.pager);
        mViewPager.setContext(this);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        if (type == Image.PROFILE_IMAGE || type == Image.PROFILE_BANNER || type == Image.ROOM_IMAGE) {
            ArrayList<String> images = new ArrayList<>();
            images.add(image);
            mViewPager.setAdapter(new FullScreenImageAdapter(this, images, type));
        } else {
            mViewPager.setAdapter(new FullScreenImageAdapter(this, imageList, type));
            mViewPager.setCurrentItem(imageList.indexOf(image));
        }

        fullscreendialog.show();
    }

    private void openImageList() {
        if (!imageList.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.image_list, null);
            GridView imageGrid = view.findViewById(R.id.gridview);
            imageGrid.setAdapter(new ImageAdapter(this, imageList));
            imageGrid.setOnItemClickListener((adapterView, view1, i, l) -> showFullscreenImage(imageList.get(i), Image.MESSAGE_IMAGE));
            AlertDialog.Builder builder;
            if (theme == Theme.DARK) {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setCustomTitle(setupHeader(getResources().getString(R.string.images)));
            builder.setView(view);
            builder.setPositiveButton(R.string.close, null);
            imageListAlert = builder.create();
            imageListAlert.show();
        } else {
            Toast.makeText(this, R.string.noimagesfound, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPinboard() {
        if (!pinnedList.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.pinboard, null);

            ListView pinListView = view.findViewById(R.id.pinboardList);
            pinListView.setAdapter(new PinboardAdapter(this, pinnedList));
            AlertDialog.Builder builder;
            if (theme == Theme.DARK) {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setCustomTitle(setupHeader(getResources().getString(R.string.pinboard)));
            builder.setView(view);
            builder.setPositiveButton(R.string.close, null);
            pinboardAlert = builder.create();
            pinboardAlert.show();
        } else {
            Toast.makeText(this, R.string.nopinnedmessagesfound, Toast.LENGTH_SHORT).show();
        }

    }

    private void pinMessage(String messageToPin) {
        for (Message m : messageList) {
            if (m.getKey().equals(messageToPin)) {
                boolean removed = false;
                for (Message m2 : pinnedList) {
                    if (m2.getKey().equals(m.getKey())) {
                        m.setPinned(false);
                        pinnedList.remove(m2);

                        Map<String, Object> map = new HashMap<String, Object>();
                        DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).child(m.getKey());
                        map.put("pinned", false);
                        messageRoot.updateChildren(map);

                        Toast.makeText(getApplicationContext(), R.string.messageunpinned, Toast.LENGTH_SHORT).show();

                        removed = true;
                        break;
                    }
                }
                if (!removed) {
                    m.setPinned(true);
                    if (!pinnedList.isEmpty()) {
                        int index = 0;
                        for (Message pm : pinnedList) {
                            if (Long.parseLong(pm.getTime().substring(0, 8) + pm.getTime().substring(9, 15)) > Long.parseLong(m.getTime().substring(0, 8) + m.getTime().substring(9, 15))) {
                                break;
                            } else {
                                index++;
                            }
                        }
                        pinnedList.add(index, m);
                    } else {
                        pinnedList.add(m);
                    }

                    Map<String, Object> map = new HashMap<String, Object>();
                    DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.messagesDatabaseKey).child(m.getKey());
                    map.put("pinned", true);
                    messageRoot.updateChildren(map);

                    Toast.makeText(getApplicationContext(), R.string.messagepinned, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void muteRoom() {
        Intent intent = new Intent("muteroom");

        if (!fileOperations.readFromFile(String.format(FileOperations.muteFilePattern, roomKey)).equals("1")) {
            fileOperations.writeToFile("1", String.format(FileOperations.muteFilePattern, roomKey));
            FirebaseMessaging.getInstance().unsubscribeFromTopic(roomKey);
            intent.putExtra("muted", true);
            Toast.makeText(this, R.string.roommuted, Toast.LENGTH_SHORT).show();
        } else {
            fileOperations.writeToFile("0", String.format(FileOperations.muteFilePattern, roomKey));
            FirebaseMessaging.getInstance().subscribeToTopic(roomKey);
            intent.putExtra("muted", false);
            Toast.makeText(this, R.string.roomunmuted, Toast.LENGTH_SHORT).show();
        }

        intent.putExtra("roomkey", roomKey);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        invalidateOptionsMenu();
    }

    private void leaveRoom() {
        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.reallyleaveroom);
        builder.setPositiveButton(R.string.yes, (dialogInterface, which) -> {
            fileOperations.writeToFile("", String.format(FileOperations.passwordFilePattern, roomKey));
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            if (fileOperations.readFromFile(String.format(FileOperations.favFilePattern, roomKey)).equals("1")) {
                markAsFav();
            }
            Intent intent = new Intent("leaveroom");
            intent.putExtra("roomkey", roomKey);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            Toast.makeText(getApplicationContext(), R.string.roomleft, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.no, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void editRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.add_room, null);

        final EditText roomNameEditText = view.findViewById(R.id.room_name);
        final EditText roomDescriptionEditText = view.findViewById(R.id.room_description);
        final EditText roomPasswordEditText = view.findViewById(R.id.room_password);
        final EditText roomPasswordRepeatEditText = view.findViewById(R.id.room_password_repeat);

        final TextInputLayout roomNameLayout = view.findViewById(R.id.room_name_layout);
        final TextInputLayout roomPasswordLayout = view.findViewById(R.id.room_password_layout);
        final TextInputLayout roomPasswordRepeatLayout = view.findViewById(R.id.room_password_repeat_layout);

        roomNameEditText.setText(room.getName());
        roomDescriptionEditText.setText(room.getDescription());
        roomPasswordEditText.setText(room.getPassword());
        roomPasswordRepeatEditText.setText(room.getPassword());

        final Spinner spinner = view.findViewById(R.id.spinner);
        roomImageButton = view.findViewById(R.id.room_image);

        spinner.setSelection(room.getCategory());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,int position, long id) {
                String category = adapter.getItemAtPosition(position).toString();
                String[] categories = getResources().getStringArray(R.array.categories);
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(category)) {
                        categoryIndex = i;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        roomNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    roomNameLayout.setError(null);
                }
            }
        });

        roomPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    roomPasswordLayout.setError(null);
                }
            }
        });

        roomPasswordRepeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    roomPasswordRepeatLayout.setError(null);
                }
            }
        });

        final StorageReference refImage = storage.getReference().child(Constants.roomImagesStorageKey + room.getImage());

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    .load(refImage)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomImageButton);
        } else {
            GlideApp.with(getApplicationContext())
                    .load(refImage)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomImageButton);
        }

        roomImageButton.setOnClickListener(view13 -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickRoomImageLauncher.launch(intent);
        });

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.editroom)));
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            View view1 = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
            dialogInterface.cancel();
            openInfo();
        });
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view12 -> {
                final String roomName = roomNameEditText.getText().toString().trim();
                final String roomPassword = roomPasswordEditText.getText().toString().trim();
                final String roomPasswordRepeat = roomPasswordRepeatEditText.getText().toString().trim();
                final String roomDescription = roomDescriptionEditText.getText().toString().trim();
                if (!roomName.isEmpty()) {
                    if (categoryIndex != 0) {
                        if (!roomPassword.isEmpty()) {
                            if (!roomPasswordRepeat.isEmpty()) {
                                if (roomPassword.equals(roomPasswordRepeat)) {
                                    if (view12 != null) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
                                    }
                                    DatabaseReference messageRoot = roomRoot.child(roomKey).child(Constants.roomDataDatabaseKey);
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", roomName);
                                    map.put("password", roomPassword);
                                    map.put("description", roomDescription);
                                    map.put("category", categoryIndex);
                                    messageRoot.updateChildren(map);
                                    fileOperations.writeToFile(roomPassword, String.format(FileOperations.passwordFilePattern, roomKey));
                                    setTitle(roomName);
                                    Toast.makeText(getApplicationContext(), R.string.roomedited, Toast.LENGTH_SHORT).show();
                                    room.setDescription(roomDescription);
                                    room.setCategory(categoryIndex);
                                    room.setPassword(roomPassword);
                                    alert.cancel();
                                    openInfo();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.passwordsdontmatch, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                roomPasswordRepeatLayout.setError(getResources().getString(R.string.repeatpassword));
                            }
                        } else {
                            roomPasswordLayout.setError(getResources().getString(R.string.enterpassword));
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.selectcategory, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    roomNameLayout.setError(getResources().getString(R.string.enterroomname));
                }
            });
        });
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void deleteRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.delete_room, null);

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.delete_room)));
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.close, null);
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view12 -> {
                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(room.getAdmin())) {
                    roomRoot.child(roomKey).removeValue((error, ref) -> {
                        Intent homeIntent = new Intent(ChatActivity.this, MainActivity.class);
                        startActivity(homeIntent);
                        finish();
                        Toast.makeText(ChatActivity.this, R.string.room_successfully_deleted, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        alert.show();
    }

    private TextView setupHeader(String title) {
        TextView header = new TextView(this);

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