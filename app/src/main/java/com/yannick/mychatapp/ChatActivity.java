package com.yannick.mychatapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.chrisrenke.giv.GravityImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ChatActivity extends AppCompatActivity {

    private EditText input_msg;

    private Theme theme;

    private String userID, room_key, key, temp_key, chat_msg, chat_user_id, time, img, passwd, caty, img_name, quote, imgurl, roomKey, admin, time_creation, desc, pin, mimeType, app_name, name, room_name, lastReadMessage, key_last, nmid, room_img;
    private String lastSearch = "";

    private DatabaseReference root;
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("users");
    private final DatabaseReference roomRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms");
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference storageReferenceRoomImages;
    private Uri photoURI;

    private RecyclerView recyclerView;
    private MessageAdapter mAdapter;
    private CoordinatorLayout layout;
    private GestureDetector gestureDetector;
    private String quoteStatus = "";
    private int amount = 0;
    private int action = 0;
    private User user = new User();
    private TextView noMessageFound;

    private ArrayList<Message> messageList = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<Message> searchResultList = new ArrayList<>();
    private ArrayList<String> roomList = new ArrayList<>();
    private ArrayList<String> roomKeysList = new ArrayList<>();
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<Message> pinnedList = new ArrayList<>();
    private ArrayList<User> memberList = new ArrayList<>();

    private AlertDialog imageListAlert;
    private AlertDialog pinboardAlert;

    private boolean firstMessage = true;
    private boolean userListCreated = false;
    private boolean cancelFullscreenImage = false;
    private boolean imageListOpened = false;
    private boolean lastReadMessageReached = false;

    private ImageButton btn_image, btn_camera;
    private Button btn_send_msg;
    private FloatingActionButton btn_scrolldown;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int PICK_ROOM_IMAGE_REQUEST = 3;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
    private SimpleDateFormat sdf_local = new SimpleDateFormat("yyyyMMdd_HHmmss_z");

    private String[] kat;
    private int katindex = 0;
    private String kategorie;

    private GravityImageView backgroundview;

    private TextView quote_text;
    private ImageButton quote_remove;
    private LinearLayout quote_layout;
    private ImageView quote_image;

    private SearchView searchView;

    private Dialog fullscreendialog;

    private ImageButton roomimage;

    private FirebaseAuth mAuth;

    private int index = -1;

    private final FileOperations fileOperations = new FileOperations(this);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.chat_room);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        layout = findViewById(R.id.coordinatorlayout);
        backgroundview = findViewById(R.id.backgroundview);
        quote_text = findViewById(R.id.quote_text);
        quote_remove = findViewById(R.id.quote_remove);
        quote_layout = findViewById(R.id.quote_layout);
        quote_image = findViewById(R.id.quote_image);

        mAdapter = new MessageAdapter(messageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        setBackgroundImage();

        mAuth = FirebaseAuth.getInstance();

        btn_scrolldown = findViewById(R.id.scrolldown);
        btn_scrolldown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });

        kat = getResources().getStringArray(R.array.categories);
        app_name = getResources().getString(R.string.app_name);

        btn_scrolldown.hide();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    btn_scrolldown.hide();
                } else if (dy <0 && !btn_scrolldown.isShown()) {
                    btn_scrolldown.show();
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
        LocalBroadcastManager.getInstance(this).registerReceiver(pinReceiver, new IntentFilter("pinnen"));
        LocalBroadcastManager.getInstance(this).registerReceiver(jumppinnedReceiver, new IntentFilter("jumppinned"));
        LocalBroadcastManager.getInstance(this).registerReceiver(closeFullscreenReceiver, new IntentFilter("closefullscreen"));

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        noMessageFound = findViewById(R.id.keinenachrichtgefunden);
        btn_send_msg = findViewById(R.id.btn_send);
        input_msg = findViewById(R.id.msg_input);
        btn_camera = findViewById(R.id.btn_camera);
        btn_image = findViewById(R.id.btn_image);

        userID = mAuth.getCurrentUser().getUid();
        room_name = getIntent().getExtras().get("room_name").toString();
        room_key = getIntent().getExtras().get("room_key").toString();
        nmid = getIntent().getExtras().get("nmid").toString();
        lastReadMessage = getIntent().getExtras().get("last_read_message").toString();
        lastReadMessageReached = (nmid.equals(lastReadMessage));
        setTitle(room_name);
        fileOperations.writeToFile(room_key, "mychatapp_current.txt");

        int pushID = 0;
        for (int i = 0; i < room_key.length(); ++i) {
            pushID += (int) room_key.charAt(i);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(pushID);

        if (!fileOperations.readFromFile("mychatapp_settings_save.txt").equals("off")) {
            input_msg.setText(fileOperations.readFromFile("mychatapp_" + room_key + "_eingabe.txt").replaceAll("<br />", "\n"));
        }

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        input_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i3 < i7 && !cancelFullscreenImage) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(
                                    recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 0);
                } else {
                    cancelFullscreenImage = false;
                }
            }
        });

        root = FirebaseDatabase.getInstance().getReference().child("rooms").child(room_key);

        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!input_msg.getText().toString().trim().isEmpty()) {
                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                        searchView.setIconified(true);
                    }
                    Map<String, Object> map = new HashMap<String, Object>();
                    temp_key = root.push().getKey();
                    root.updateChildren(map);

                    String currentDateAndTime = sdf.format(new Date());

                    DatabaseReference message_root = root.child(temp_key);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("name", userID);
                    map2.put("msg", input_msg.getText().toString().trim());
                    map2.put("img", "");
                    map2.put("pin", "0");
                    map2.put("quote", quoteStatus);
                    map2.put("time", currentDateAndTime);

                    message_root.updateChildren(map2);
                    input_msg.getText().clear();
                    quoteStatus = "";
                    quote_text.setText("");
                    quote_image.setImageDrawable(null);
                    quote_image.setVisibility(View.GONE);
                    quote_layout.setVisibility(View.GONE);
                    fileOperations.writeToFile("", "mychatapp_" + room_key + "_eingabe.txt");
                    fileOperations.writeToFile(temp_key, "mychatapp_raum_" + room_key + "_nm.txt");
                }
            }
        });

        btn_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    chooseImage();
                    btn_image.setBackgroundResource(R.drawable.ic_image);
                    return true;
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_image.setBackgroundResource(R.drawable.ic_image_light);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        btn_image.setBackgroundResource(R.drawable.ic_image);
                        return true;
                    }
                }
                return false;
            }
        });

        btn_camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    takePicture();
                    btn_camera.setBackgroundResource(R.drawable.ic_camera);
                    return true;
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_camera.setBackgroundResource(R.drawable.ic_camera_light);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        btn_camera.setBackgroundResource(R.drawable.ic_camera);
                        return true;
                    }
                }
                return false;
            }
        });

        quote_remove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    quoteStatus = "";
                    quote_text.setText("");
                    quote_image.setImageDrawable(null);
                    quote_image.setVisibility(View.GONE);
                    quote_layout.setVisibility(View.GONE);
                    quote_remove.setBackgroundResource(R.drawable.ic_clear);
                    return true;
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        quote_remove.setBackgroundResource(R.drawable.ic_clear_light);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        quote_remove.setBackgroundResource(R.drawable.ic_clear);
                        return true;
                    }
                }
                return false;
            }
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
                Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!userListCreated) {
                    handler.postDelayed(this, 1000);
                } else {
                    root.addChildEventListener(new ChildEventListener() {
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
                            Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, 10);

        roomRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
                    String roomKey = uniqueKeySnapshot.getKey();
                    if (roomKey.equals(room_key)) {
                        amount = (int)uniqueKeySnapshot.getChildrenCount() - 1;
                    }
                    for(DataSnapshot roomSnapshot : uniqueKeySnapshot.getChildren()) {
                        Room room = roomSnapshot.getValue(Room.class);
                        room.setKey(roomKey);
                        if (room.getPasswd().equals(fileOperations.readFromFile("mychatapp_raum_" + roomKey + ".txt")) && !roomKey.equals(room_key)) {
                            roomList.add(room.getName());
                            roomKeysList.add(room.getKey());
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, R.string.nodatabaseconnection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(DataSnapshot dataSnapshot, int index) {
        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()){
            if (firstMessage) {
                firstMessage = false;

                roomKey = dataSnapshot.getKey();
                admin = (String) ((DataSnapshot) i.next()).getValue();
                caty = (String) ((DataSnapshot) i.next()).getValue();
                desc = (String) ((DataSnapshot) i.next()).getValue();
                room_img = (String) ((DataSnapshot) i.next()).getValue();
                name = (String) ((DataSnapshot) i.next()).getValue();
                passwd = (String) ((DataSnapshot) i.next()).getValue();
                time_creation = (String) ((DataSnapshot) i.next()).getValue();
                /*Room room = dataSnapshot.getValue(Room.class);
                Log.d("HEYHY", room.getCaty());
                Log.d("HEYHY", room.getDesc());
                Log.d("HEYHY", room.getName());
                Log.d("HEYHY", room.getNewestMessage());
                Log.d("HEYHY", room.getPasswd());
                Log.d("HEYHY", room.getTime());
                caty = room.getCaty();
                desc = room.getDesc();
                admin = room.getName();
                newestMessage = room.getNewestMessage();
                passwd = room.getPasswd();
                time_creation = room.getTime();*/
                user = getUser(admin);

                /*addUser(roomkey, admin, "", img, pin, quote, time_creation, new MyCallback() {
                    @Override
                    public void onCallback(String roomkey, User user, String time_creation, String chat_msg, String img, String pin, String quote) {
                        //Log.d("HEYHY", chat_msg + " - " + user.getName());*/
                        try {
                            time_creation = sdf_local.format(sdf_local.parse(time_creation));
                        } catch (ParseException e) {

                        }
                        String time_creation_con = time_creation.substring(6, 8) + "." + time_creation.substring(4,6) + "." + time_creation.substring(0, 4);
                        String text = getResources().getString(R.string.roomintro, time_creation_con, user.getName());
                        Message m = new Message(user, text, time_creation, time_creation, false, roomKey, 0, "", "", "", "0");

                        messageList.add(m);
                        if (!memberList.contains(m.getUser())) {
                            memberList.add(m.getUser());
                        }

                        //recyclerView.scrollToPosition(messageList.size() - 1);
                        //btn_scrolldown.hide();
                    /*}
                });*/
            } else {
                key = dataSnapshot.getKey();
                img = (String) ((DataSnapshot) i.next()).getValue();
                chat_msg = (String) ((DataSnapshot) i.next()).getValue();
                chat_user_id = (String) ((DataSnapshot) i.next()).getValue();
                pin = (String) ((DataSnapshot) i.next()).getValue();
                quote = (String) ((DataSnapshot) i.next()).getValue();
                time = (String) ((DataSnapshot) i.next()).getValue();

                user = getUser(chat_user_id);
                boolean sender = userID.equals(user.getUserID());

                try {
                    time = sdf_local.format(sdf_local.parse(time));
                } catch (ParseException e) {

                }
                if (lastReadMessage.equals(key_last) && !lastReadMessageReached) {
                    Message m = new Message(user, getResources().getString(R.string.unreadmessages), time, time, false, "-", 0, "", "", "", "0");
                    messageList.add(m);
                }
                key_last = key;

                if (index == -1 && !messageList.get(messageList.size() - 1).getTime().substring(0, 8).equals(time.substring(0, 8))) {
                    String text = time.substring(6, 8) + "." + time.substring(4, 6) + "." + time.substring(0, 4);
                    Message m = new Message(user, text, time, time, false, "-", 0, "", "", "", "0");
                    messageList.add(m);
                }

                int offset = 0;
                int ind = (index == -1) ? messageList.size() : index;
                if (messageList.size() - 1 > 0 && messageList.get(ind - 1).getTyp() != 0 && messageList.get(ind - 1).getUser().getUserID().equals(chat_user_id) && messageList.get(ind - 1).getbTime().substring(0, 13).equals(time.substring(0, 13))) {
                    offset = sender ? 3 : 1;
                    messageList.get(ind - 1).setTime("");
                    mAdapter.notifyDataSetChanged();
                } else {
                    offset = sender ? 2 : 0;
                }

                Message m;
                if (quote.equals("")) {
                    if (!chat_msg.equals("")) {
                        if (chat_msg.length() > 11 && chat_msg.substring(0, 12).equals("(Forwarded) ")) {
                            if (chat_msg.length() > 2000 + 12) {
                                m = new Message(user, chat_msg.substring(12), time, time, sender, key, 33 + offset, "", "", "", pin);
                            } else {
                                m = new Message(user, chat_msg.substring(12), time, time, sender, key, 21 + offset, "", "", "", pin);
                            }
                        } else {
                            if (chat_msg.length() > 2000) {
                                m = new Message(user, chat_msg, time, time, sender, key, 29+offset, "", "", "", pin);
                            } else {
                                m = new Message(user, chat_msg, time, time, sender, key, 1 + offset, "", "", "", pin);
                            }
                        }
                    } else {
                        if (!imageList.contains(img)) {
                            imageList.add(img);
                        }
                        m = new Message(user, img, time, time, sender, key, 13+offset, "", "", "", pin);
                    }
                } else {
                    String quote_name, quote_message, quote_key;
                    int quote_typ = 0;
                    quote_message = getResources().getString(R.string.quotedmessagenolongeravailable);
                    quote_name = "";
                    quote_key = "";
                    for (Message quote : messageList) {
                        if (quote.getKey().equals(this.quote)) {
                            quote_message = quote.getMsg();
                            quote_name = quote.getUser().getName();
                            quote_key = quote.getKey();
                            quote_typ = quote.getTyp();
                            break;
                        }
                    }
                    if (quote_typ != 13 && quote_typ != 15 && quote_typ != 14 && quote_typ != 16) {
                        if (!quote_message.equals(getResources().getString(R.string.quotedmessagenolongeravailable))) {
                            m = new Message(user, chat_msg, time, time, sender, key, 5+offset, quote_name, quote_message, quote_key, pin);
                        } else {
                            m = new Message(user, chat_msg, time, time, sender, key, 9+offset, quote_name, quote_message, quote_key, pin);
                        }
                    } else {
                        m = new Message(user, chat_msg, time, time, sender, key, 17+offset, quote_name, quote_message, quote_key, pin);
                    }
                }
                if (index != -1 && messageList.get(ind).getTime().equals("")) {
                    m.setTime("");
                }
                if (index == -1) {
                    messageList.add(m);
                    if (!memberList.contains(m.getUser())) {
                        memberList.add(m.getUser());
                    }
                    if (pin.equals("1")) {
                        pinnedList.add(m);
                    }
                } else {
                    ArrayList<Message> templist = new ArrayList<>(messageList);
                    messageList.clear();
                    templist.add(index+1, m);
                    messageList.addAll(templist);
                    mAdapter.notifyDataSetChanged();
                }

                /*addUser(key, chat_user_id, chat_msg, img, pin, quote, time, new MyCallback() {
                    @Override
                    public void onCallback(String key, User user, String time, String chat_msg, String img, String pin, String quote) {
                        //Log.d("HEYHY", chat_msg + " - " + user.getName());


                    }
                });*/
            }

            recyclerView.scrollToPosition(messageList.size() - 1);
            btn_scrolldown.hide();
        }
    }

    private void removeMessage(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();

        key = dataSnapshot.getKey();
        img = (String) ((DataSnapshot) i.next()).getValue();
        chat_msg = (String) ((DataSnapshot) i.next()).getValue();
        chat_user_id = (String) ((DataSnapshot) i.next()).getValue();
        pin = (String) ((DataSnapshot) i.next()).getValue();
        quote = (String) ((DataSnapshot) i.next()).getValue();
        time = (String) ((DataSnapshot) i.next()).getValue();

        if (!img.equals("")) {
            StorageReference storageRef = storage.getReferenceFromUrl(storageReference.toString());
            StorageReference pathReference = storageRef.child("images/" + img);
            imageList.remove(img);
            pathReference.delete();
        }

        List<Message> tempMessageList = new ArrayList<>();
        for (Message m : messageList) {
            if (!(m.getKey().equals(key))) {
                tempMessageList.add(m);
            } else if (m.getTyp() != 0 && m.getTyp() % 2 == 0 && !m.getTime().equals("")) {
                tempMessageList.get(tempMessageList.size()-1).setTime(m.getTime());
            }
        }
        if (tempMessageList.get(tempMessageList.size()-1).getTyp() == 0 && tempMessageList.size()!=1) {
            tempMessageList.remove(tempMessageList.size()-1);
        }
        for (int j = 1; j < tempMessageList.size()-1; j++) {
            if (tempMessageList.get(j).getTyp() == 0 && tempMessageList.get(j+1).getTyp() == 0) {
                tempMessageList.remove(j);
            }
        }
        messageList.clear();
        for (Message m : tempMessageList) {
            if (m.getQuote_key().equals(key)) {
                if (img.equals("")) {
                    m.setQuote_message(getResources().getString(R.string.quotedmessagenolongeravailable));
                } else {
                    m.setQuote_message(img);
                }
                m.setQuote_name("");
                m.setTyp(m.getTyp()+4);
            }
            messageList.add(m);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void changeMessage(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();

        key = dataSnapshot.getKey();
        img = (String) ((DataSnapshot) i.next()).getValue();
        chat_msg = (String) ((DataSnapshot) i.next()).getValue();
        chat_user_id = (String) ((DataSnapshot) i.next()).getValue();
        pin = (String) ((DataSnapshot) i.next()).getValue();
        quote = (String) ((DataSnapshot) i.next()).getValue();
        time = (String) ((DataSnapshot) i.next()).getValue();

        for (Message m : messageList) {
            if (m.getTyp() != 0) {
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
            if (m.getTyp() != 0) {
                if (m.getQuote_key().equals(key)) {
                    if (img.equals("")) {
                        m.setQuote_message(chat_msg);
                    } else {
                        m.setQuote_message(img);
                    }
                    m.setQuote_name(getUser(chat_user_id).getName());
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setBackgroundImage() {
        Background background = Background.valueOf(fileOperations.readFromFile("mychatapp_background.txt"));
        if (getResources().getConfiguration().orientation != 2) {
            switch (background) {
                case BREATH_OF_THE_WILD:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_botw));
                    break;
                case SPLATOON_2:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_splatoon2));
                    break;
                case PERSONA_5:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_persona));
                    break;
                case KIMI_NO_NA_WA:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_kiminonawa));
                    break;
                case SUPER_MARIO_BROS:
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_smb));
                    break;
                case SUPER_MARIO_MAKER:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_smm));
                    break;
                case XENOBLADE_CHRONICLES_2:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_xc2));
                    break;
                case FIRE_EMBLEM_FATES:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_fef));
                    break;
                case SUPER_SMASH_BROS_ULTIMATE:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_ssbu));
                    break;
                case DETECTIVE_PIKACHU:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_dp));
                    break;
                default:
                    break;
            }
        } else {
            switch (background) {
                case BREATH_OF_THE_WILD:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_botw_horizontal));
                    break;
                case SPLATOON_2:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_splatoon2_horizontal));
                    break;
                case PERSONA_5:
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_persona_horizontal));
                    break;
                case KIMI_NO_NA_WA:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_kiminonawa_horizontal));
                    break;
                case SUPER_MARIO_BROS:
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_smb_horizontal));
                    break;
                case SUPER_MARIO_MAKER:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_smm_horizontal));
                    break;
                case XENOBLADE_CHRONICLES_2:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.BOTTOM);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_xc2_horizontal));
                    break;
                case FIRE_EMBLEM_FATES:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_fef_horizontal));
                    break;
                case SUPER_SMASH_BROS_ULTIMATE:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.CENTER);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_ssbu_horizontal));
                    break;
                case DETECTIVE_PIKACHU:
                    backgroundview.setImageGravity(GravityImageView.CENTER_HORIZONTAL|GravityImageView.TOP);
                    backgroundview.setImageDrawable(getResources().getDrawable(R.drawable.background_dp_horizontal));
                    break;
                default:
                    break;
            }
        }
    }

    private void changeTheme(Theme theme) {
        this.theme = theme;
        if (theme == Theme.DARK) {
            setTheme(R.style.DarkChat);
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

        img_name = UUID.randomUUID().toString();
        StorageReference ref;
        if (type == 2) {
            ref = storageReference.child("room_images/" + img_name);
        } else {
            ref = storageReference.child("images/" + img_name);
        }

        byte[] byteArray = new byte[0];
        ContentResolver cR = getApplicationContext().getContentResolver();
        if (cR.getType(filePath).equals("image/gif") && type != 2) {
            try {
                InputStream iStream = getContentResolver().openInputStream(filePath);
                byteArray = getBytes(iStream);
            } catch (IOException ioe) { }
        } else {
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            if (bmp.getWidth() < bmp.getHeight() && type == 2) {
                bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/2, bmp.getWidth(), bmp.getWidth());
            } else if (bmp.getWidth() > bmp.getHeight() && type == 2) {
                bmp = Bitmap.createBitmap(bmp, bmp.getWidth()/2-bmp.getHeight()/2, 0, bmp.getHeight(), bmp.getHeight());
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int compression = 100;
            int compressFactor = 2;
            int height = bmp.getHeight();
            int width = bmp.getWidth();
            if (getImgSize(filePath) > height * width) {
                compressFactor = 4;
            }
            if (type == 2) {
                while (height * width > 500 * 500) {
                    height /= 1.1;
                    width /= 1.1;
                    compression -= compressFactor;
                }
            } else {
                while (height * width > 1920 * 1080) {
                    height /= 1.1;
                    width /= 1.1;
                    compression -= compressFactor;
                }
            }
            bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
            try {
                bmp = rotateImageIfRequired(this, bmp, filePath);
            } catch (IOException e) { }
            bmp.compress(Bitmap.CompressFormat.JPEG, compression, stream);
            byteArray = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();

                if (type != 2) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    temp_key = root.push().getKey();
                    root.updateChildren(map);

                    String currentDateAndTime = sdf.format(new Date());

                    DatabaseReference message_root = root.child(temp_key);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("name", userID);
                    map2.put("msg", "");
                    map2.put("img", img_name);
                    map2.put("pin", "0");
                    map2.put("quote", "");
                    map2.put("time", currentDateAndTime);

                    message_root.updateChildren(map2);

                    quoteStatus = "";

                    if (type == 1 && !fileOperations.readFromFile("mychatapp_settings_camera.txt").equals("off")) {
                        downloadImage(img_name, type);
                    }
                } else {
                    DatabaseReference message_root = FirebaseDatabase.getInstance().getReference().child("rooms").child(room_key).child(roomKey);
                    Map<String, Object> map = new HashMap<>();
                    map.put("img", img_name);
                    message_root.updateChildren(map);

                    room_img = img_name;

                    storageReferenceRoomImages = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
                    final StorageReference pathReference = storageReferenceRoomImages.child("room_images/" + img_name);
                    GlideApp.with(getApplicationContext())
                            .load(pathReference)
                            .centerCrop()
                            .thumbnail(0.05f)
                            .into(roomimage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage((int)progress+"% " + getResources().getString(R.string.uploaded));
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE_REQUEST);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
                action = 2;
                if (isStoragePermissionGranted()) {
                    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = sdf.format(new Date()).substring(0, 15);
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpeg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            Uri filePath = data.getData();
            if(filePath != null) {
                uploadImage(filePath, 0);
            }
        }
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (photoURI != null) {
                uploadImage(photoURI, 1);
            }
        }
        if (requestCode == PICK_ROOM_IMAGE_REQUEST) {
            Uri filePath = data.getData();
            uploadImage(filePath, 2);
        }
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
                    if (m.getTyp() != 13 && m.getTyp() != 14 && m.getTyp() != 15 && m.getTyp() != 16) {
                        String text = user + " " + m.getMsg();
                        SpannableStringBuilder str = new SpannableStringBuilder(text);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, user.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        str.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), user.length()+1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        quote_image.setVisibility(View.GONE);
                        quote_text.setText(str);
                    } else {
                        SpannableStringBuilder str = new SpannableStringBuilder(user);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, user.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        String imgurl = m.getMsg();
                        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
                        StorageReference pathReference = storageRef.child("images/" + imgurl);
                        GlideApp.with(context)
                                .load(pathReference)
                                .placeholder(R.color.gray_material)
                                .centerCrop()
                                .thumbnail(0.05f)
                                .into(quote_image);
                        quote_image.setVisibility(View.VISIBLE);
                        quote_text.setText(str);
                    }
                    quote_layout.setVisibility(View.VISIBLE);
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
            imgurl = intent.getStringExtra("imgurl");
            action = 1;
            if(isStoragePermissionGranted()) {
                downloadImage(imgurl, 0);
            }
        }
    };

    public BroadcastReceiver userReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String user_id = intent.getStringExtra("userid");
            showProfile(user_id);
        }
    };

    public BroadcastReceiver fullscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String image = intent.getStringExtra("image");
            showFullscreenImage(image, 2);
        }
    };

    public BroadcastReceiver forwardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String message_id = intent.getStringExtra("forwardID");
            if (!roomList.isEmpty()) {
                forwardMessage(message_id);
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

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("DL","Permission is granted");
                return true;
            } else {
                Log.v("DL","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v("DL","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("DL","Permission is granted");
            if (action == 1) {
                downloadImage(imgurl, 0);
            } else if (action == 2) {
                takePicture();
            } else if (action == 3) {
                writeBackup(createBackup());
            }
        }
    }

    private void downloadImage(String imgurl, final int type) {
        StorageReference storageRef = storage.getReferenceFromUrl(storageReference.toString());
        final StorageReference pathReference = storageRef.child("images/" + imgurl);

        final Context context = getApplicationContext();
        final File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/" + app_name);
        if(!rootPath.exists()) {
            if (!rootPath.mkdirs()) {
                Log.e("firebase ", "Erstellung des Ordners fehlgeschlagen");
            }
        }

        pathReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                mimeType = storageMetadata.getContentType();
                String currentDateandTime = sdf.format(new Date());
                final String filename = app_name + "_" + currentDateandTime.substring(0,15) + "." + mimeType.substring(6);
                final File localFile = new File(rootPath,filename);

                pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        notifyGallery(localFile.getAbsolutePath());
                        if (type == 0) {
                            createSnackbar(localFile, mimeType, getResources().getString(R.string.imagesaved));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, R.string.savingimagefailed, Toast.LENGTH_SHORT).show();
                        Log.e("firebase ","Bild speichern fehlgeschlagen!: " +exception.toString());
                    }
                });
            }
        });
    }

    private void notifyGallery(String path) {
        MediaScannerConnection.scanFile(getApplicationContext(), new String[] { path }, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!fileOperations.readFromFile("mychatapp_" + room_key + "_fav.txt").equals("1")) {
            if (!fileOperations.readFromFile("mychatapp_" + room_key + "_mute.txt").equals("1")) {
                inflater.inflate(R.menu.menu_chatroom, menu);
            } else {
                inflater.inflate(R.menu.menu_chatroom_unmute, menu);
            }
        } else {
            if (!fileOperations.readFromFile("mychatapp_" + room_key + "_mute.txt").equals("1")) {
                inflater.inflate(R.menu.menu_chatroom_unfav, menu);
            } else {
                inflater.inflate(R.menu.menu_chatroom_unmute_unfav, menu);
            }
        }

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem search_item = menu.findItem(R.id.roomsearch);
        searchView = (SearchView) search_item.getActionView();
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
        switch (item.getItemId()) {
            case R.id.roominfo:
                openInfo();
                return true;
            case R.id.roomfav:
                markAsFav();
                return true;
            case R.id.roomsearch:
                return super.onOptionsItemSelected(item);
            case R.id.roomphotos:
                openImageList();
                return true;
            case R.id.roompinboard:
                openPinboard();
                return true;
            case R.id.roommute:
                if (!fileOperations.readFromFile("mychatapp_" + room_key + "_mute.txt").equals("1")) {
                    fileOperations.writeToFile("1", "mychatapp_" + room_key + "_mute.txt");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(room_key);
                    Toast.makeText(this, R.string.roommuted, Toast.LENGTH_SHORT).show();
                } else {
                    fileOperations.writeToFile("0", "mychatapp_" + room_key + "_mute.txt");
                    FirebaseMessaging.getInstance().subscribeToTopic(room_key);
                    Toast.makeText(this, R.string.roomunmuted, Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                return true;
            case R.id.roombackup:
                if (messageList.size() > 1) {
                    writeBackup(createBackup());
                } else {
                    Toast.makeText(this, R.string.nomessagesfound, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.roomleave:
                AlertDialog.Builder builder;
                if (theme == Theme.DARK) {
                    builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle(R.string.reallyleaveroom);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileOperations.writeToFile("", "mychatapp_raum_" + room_key + ".txt");
                        startActivity(new Intent(ChatActivity.this, MainActivity.class));
                        if (fileOperations.readFromFile("mychatapp_" + room_key + "_fav.txt").equals("1")) {
                            markAsFav();
                        }
                        Intent intent = new Intent("leaveroom");
                        intent.putExtra("roomkey", room_key);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        Toast.makeText(getApplicationContext(), R.string.roomleaved, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case android.R.id.home:
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
                if (!fileOperations.readFromFile("mychatapp_settings_save.txt").equals("off")) {
                    fileOperations.writeToFile(input_msg.getText().toString().trim().replaceAll("\\n", "<br />"), "mychatapp_" + room_key + "_eingabe.txt");
                    if (!input_msg.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, R.string.messagesaved, Toast.LENGTH_SHORT).show();
                    }
                }
                if (!messageList.isEmpty()) {
                    fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), "mychatapp_raum_" + room_key + "_nm.txt");
                }
                fileOperations.writeToFile("0", "mychatapp_current.txt");
                startActivity(new Intent(ChatActivity.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openInfo() {
        LayoutInflater inflater= LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.room_info, null);

        final TextView room_desc = view.findViewById(R.id.room_description);
        final TextView room_cat = view.findViewById(R.id.room_cat);
        final TextView room_creation = view.findViewById(R.id.room_creation);
        final TextView room_amount_messages = view.findViewById(R.id.room_amount_messages);

        final ListView memberListView = view.findViewById(R.id.memberList);
        memberListView.setAdapter(new MemberListAdapter(getApplicationContext(), memberList, admin));

        String time = time_creation.substring(6, 8) + "." + time_creation.substring(4, 6) + "." + time_creation.substring(0, 4);
        room_desc.setText(desc);
        room_cat.setText(getResources().getStringArray(R.array.categories)[Integer.parseInt(caty)]);
        room_creation.setText(time);
        room_amount_messages.setText(String.valueOf(amount));

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.roominfo)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        if (admin.equals(userID)) {
            builder.setNegativeButton(R.string.editroom, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    editRoom();
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    private Long getImgSize(Uri filePath) {
        Cursor returnCursor = getContentResolver().query(filePath, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        return returnCursor.getLong(sizeIndex);
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23) {
            ei = new ExifInterface(input);
        } else {
            ei = new ExifInterface(selectedImage.getPath());
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(forwardReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fullscreenReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(jumppinnedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeFullscreenReceiver);
        if (!fileOperations.readFromFile("mychatapp_settings_save.txt").equals("off")) {
            fileOperations.writeToFile(input_msg.getText().toString().trim().replaceAll("\\n", "<br />"), "mychatapp_" + room_key + "_eingabe.txt");
        }
        fileOperations.writeToFile("0", "mychatapp_current.txt");
        if ((messageList.size() - 1) >= 0) {
            fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), "mychatapp_raum_" + room_key + "_nm.txt");
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        if (!fileOperations.readFromFile("mychatapp_settings_save.txt").equals("off")) {
            fileOperations.writeToFile(input_msg.getText().toString().trim().replaceAll("\\n", "<br />"), "mychatapp_" + room_key + "_eingabe.txt");
        }
        if (!messageList.isEmpty()) {
            fileOperations.writeToFile(messageList.get(messageList.size() - 1).getKey(), "mychatapp_raum_" + room_key + "_nm.txt");
        }
        fileOperations.writeToFile("0", "mychatapp_current.txt");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        fileOperations.writeToFile("0", "mychatapp_current.txt");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        fileOperations.writeToFile(room_key, "mychatapp_current.txt");
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
        if (m.getMsg().toLowerCase().contains(text.toLowerCase()) && m.getTyp() != 0 && m.getTyp() != 13 && m.getTyp() != 15 && m.getTyp() != 14 && m.getTyp() != 16) {
            if (searchedMessageList.isEmpty() || !searchedMessageList.get(searchedMessageList.size() - 1).getbTime().substring(0, 8).equals(m.getbTime().substring(0, 8))) {
                String time = m.getbTime().substring(6, 8) + "." + m.getbTime().substring(4, 6) + "." + m.getbTime().substring(0, 4);
                Message m2 = new Message(user, time, m.getTime(), m.getbTime(), false, "-", 0, "", "", "", m.getPin());
                searchedMessageList.add(m2);
            }
            Message m2;
            if (m.getTyp() % 2 == 0) {
                m2 = new Message(m.getUser(), m.getMsg(), m.getTime(), m.getbTime(), m.isSender(), m.getKey(), m.getTyp()-1, m.getQuote_name(), m.getQuote_message(), m.getQuote_key(), m.getPin());
            } else {
                m2 = new Message(m.getUser(), m.getMsg(), m.getTime(), m.getbTime(), m.isSender(), m.getKey(), m.getTyp(), m.getQuote_name(), m.getQuote_message(), m.getQuote_key(), m.getPin());
            }
            m2.setSearchString(text);
            searchedMessageList.add(m2);
        }
        return searchedMessageList;
    }

    private String createBackup() {
        String currentDateAndTime = sdf_local.format(new Date());
        String fcdat = currentDateAndTime.substring(0, 4) + "." + currentDateAndTime.substring(4, 6) + "." + currentDateAndTime.substring(6, 8) + " " + currentDateAndTime.substring(9, 11) + ":" + currentDateAndTime.substring(11, 13) + ":" + currentDateAndTime.substring(13, 15);
        String ftime = time_creation.substring(0, 4) + "." + time_creation.substring(4, 6) + "." + time_creation.substring(6, 8) + " " + time_creation.substring(9, 11) + ":" + time_creation.substring(11, 13) + ":" + time_creation.substring(13, 15);
        String backup = getResources().getString(R.string.backupof) + " " + room_name + "\n" + getResources().getString(R.string.createdon) + ": " + fcdat + "\n\n" +
                getResources().getString(R.string.category) + ": " + getResources().getStringArray(R.array.categories)[Integer.parseInt(caty)] + "\n" + getResources().getString(R.string.admin) + ": " + getUser(admin).getName() + "\n" + getResources().getString(R.string.foundation) + ": " + ftime + "\n" + getResources().getString(R.string.sentmessages) + ": " + amount + "\n----------------------------------------\n";

        String newDay = "";
        for (Message m : messageList) {
            String btimeDay = "";
            if (m.getTyp() != 0) {
                btimeDay = m.getbTime().substring(0, 4) + "." + m.getbTime().substring(4, 6) + "." + m.getbTime().substring(6, 8);
                String btime = m.getbTime().substring(9, 11) + ":" + m.getbTime().substring(11, 13) + ":" + m.getbTime().substring(13, 15);
                if (!newDay.equals(btimeDay)) {
                    backup += "\n" + btimeDay + "\n";
                }
                if (m.getTyp() == 5 || m.getTyp() == 7 || m.getTyp() == 6 || m.getTyp() == 8 || m.getTyp() == 18 || m.getTyp() == 20) {
                    String quote = m.getQuote_message();
                    if (quote.length() > 40) {
                        quote = quote.substring(0, 40) + "...";
                    }
                    backup += btime + " - [" + m.getQuote_name() + ": " + quote + "] - "  + m.getUser().getName() + ": " + m.getMsg() + "\n";
                } else {
                    backup += btime + " - " + m.getUser().getName() + ": " + m.getMsg() + "\n";
                }
            }
            newDay = btimeDay;
        }

        return backup;
    }

    private void writeBackup(String text) {
        action = 3;
        if (isStoragePermissionGranted()) {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/" + getResources().getString(R.string.app_name) + "/");

            if (!path.exists()) {
                path.mkdirs();
            }

            final File file = new File(path, getResources().getString(R.string.app_name) + "_" + room_name.replace(" ", "") + "_Backup.txt");

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
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    private void createSnackbar(final File file, final String mime, final String text) {
        Snackbar snack = Snackbar.make(layout, text, Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(R.color.white))
                .setAction(R.string.open, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if(android.os.Build.VERSION.SDK_INT >=24) {
                            Uri fileURI = FileProvider.getUriForFile(ChatActivity.this,
                                    BuildConfig.APPLICATION_ID+".fileprovider",
                                    file);
                            intent.setDataAndType(fileURI, mime);
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), mime);
                        }
                        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION|FLAG_GRANT_WRITE_URI_PERMISSION|FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
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

        if (imageListOpened) {
            imageListAlert.dismiss();
            openImageList();
        }

        setBackgroundImage();
    }

    private void markAsFav() {
        Intent intent = new Intent("favroom");
        intent.putExtra("roomkey", room_key);
        intent.putExtra("roomname", room_name);
        intent.putExtra("admin", admin);
        intent.putExtra("caty",caty);
        String time_creation_temp = "";
        try {
            time_creation_temp = sdf.format(sdf.parse(time_creation));
        } catch (ParseException e) {}
        intent.putExtra("newestMessage",time_creation_temp);
        intent.putExtra("passwd",passwd);
        Message newest = messageList.get(messageList.size() - 1);
        if (messageList.size()!=1) {
            intent.putExtra("nm_message", newest.getMsg());
            String parsedTime = "";
            try {
                parsedTime = sdf.format(sdf.parse(newest.getbTime()));
            } catch (ParseException e) {}
            intent.putExtra("nm_time", parsedTime);
            intent.putExtra("nm_key", newest.getKey());
            intent.putExtra("nm_typ", newest.getTyp());
        } else {
            intent.putExtra("nm_message", "");
            intent.putExtra("nm_time", "");
            intent.putExtra("nm_key", "");
            intent.putExtra("nm_typ", 0);
        }
        if (!fileOperations.readFromFile("mychatapp_" + room_key + "_fav.txt").equals("1")) {
            fileOperations.writeToFile("1", "mychatapp_" + room_key + "_fav.txt");
            Toast.makeText(this, R.string.addedtofavorites, Toast.LENGTH_SHORT).show();
        } else {
            fileOperations.writeToFile("0", "mychatapp_" + room_key + "_fav.txt");
            Toast.makeText(this, R.string.removedfromfavorites, Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void addUser(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()){
            String tmpuserid = dataSnapshot.getKey();
            String tmpbanner = (String) ((DataSnapshot) i.next()).getValue();
            String tmpbday = (String) ((DataSnapshot) i.next()).getValue();
            String tmpbio = (String) ((DataSnapshot) i.next()).getValue();
            String tmpfavc = (String) ((DataSnapshot) i.next()).getValue();
            String tmpimg = (String) ((DataSnapshot) i.next()).getValue();
            String tmploc = (String) ((DataSnapshot) i.next()).getValue();
            String tmpname = (String) ((DataSnapshot) i.next()).getValue();
            String tmpownpi = (String) ((DataSnapshot) i.next()).getValue();
            User user = new User(tmpuserid, tmpname, tmpbday, tmpbio, tmploc, tmpfavc, tmpimg, tmpbanner);
            userList.add(user);
        }

        userListCreated = true;
    }

    public void addUser(final String key, final String user_id, final String chat_msg, final String img, final String pin, final String quote, final String time, final MyCallback myCallback) {
        userRoot.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setUserID(user_id);
                userList.add(user);
                myCallback.onCallback(key, user, time, chat_msg, img, pin, quote);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showProfile(final String user_ID) {
        User user = getUser(user_ID);
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
            banner.setBackground(getResources().getDrawable(R.drawable.side_nav_bar_dark));
        } else {
            builder = new AlertDialog.Builder(this);
            banner.setBackground(getResources().getDrawable(R.drawable.side_nav_bar));
        }

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_banner = storageRef.child("profile_banners/" + user.getBanner());
        GlideApp.with(getApplicationContext())
                .load(pathReference_banner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);

        final StorageReference pathReference_image = storageRef.child("profile_images/" + user.getImg());
        GlideApp.with(getApplicationContext())
                .load(pathReference_image)
                .centerCrop()
                .into(profileIcon);

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullscreenImage(user_ID, 0);
            }
        });

        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullscreenImage(user_ID, 1);
            }
        });

        profileName.setText(user.getName());
        profileDescription.setText(user.getProfileDescription());
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
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomList));
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position = listView.getPositionForView(view);
                String roomKey = roomKeysList.get(position);
                Message fMessage = new Message();
                for (Message m : messageList) {
                    if (m.getKey().equals(messageID)) {
                        fMessage = m;
                        break;
                    }
                }
                temp_key = roomRoot.child(roomKey).push().getKey();

                String currentDateAndTime = sdf.format(new Date());

                DatabaseReference message_root = roomRoot.child(roomKey).child(temp_key);
                Map<String, Object> map2 = new HashMap<>();
                map2.put("name", userID);
                if (fMessage.getTyp() == 13 || fMessage.getTyp() == 14 || fMessage.getTyp() == 15 || fMessage.getTyp() == 16) {
                    map2.put("msg", "");
                    map2.put("img", fMessage.getMsg());
                } else {
                    map2.put("msg", "(Forwarded) " + fMessage.getMsg());
                    map2.put("img", "");
                }
                map2.put("pin", "0");
                map2.put("quote", quoteStatus);
                map2.put("time", currentDateAndTime);

                message_root.updateChildren(map2);
                fileOperations.writeToFile(temp_key, "mychatapp_raum_" + roomKey + "_nm.txt");
                alert.cancel();
                if (fMessage.getTyp() == 13 || fMessage.getTyp() == 14 || fMessage.getTyp() == 15 || fMessage.getTyp() == 16) {
                    Toast.makeText(ChatActivity.this, R.string.imageforwarded, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, R.string.messageforwarded, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private User getUser(final String user_id) {
        for (User u : userList) {
            if (u.getUserID().equals(user_id)) {
                return u;
            }
        }
        return null;
    }

    private void showFullscreenImage(String image, int type) {
        final View dialogView = getLayoutInflater().inflate(R.layout.fullscreen_image, null);
        if (theme == Theme.DARK) {
            fullscreendialog = new Dialog(this,R.style.FullScreenImageDark);
        } else {
            fullscreendialog = new Dialog(this,R.style.FullScreenImage);
        }
        fullscreendialog.setContentView(dialogView);

        final View decorView = fullscreendialog.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if (fullscreendialog.isShowing()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                    }, 2000);
                }
            }
        });

        CatchViewPager mViewPager = dialogView.findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        if (type == 0) {
            ArrayList<String> images = new ArrayList<>();
            images.add(image);
            mViewPager.setAdapter(new FullScreenImageAdapter(this, images, 0));
        } else if (type == 1) {
            ArrayList<String> images = new ArrayList<>();
            images.add(image);
            mViewPager.setAdapter(new FullScreenImageAdapter(this, images, 1));
        } else {
            mViewPager.setAdapter(new FullScreenImageAdapter(this, imageList, 2));
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
            imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showFullscreenImage(imageList.get(i), 2);
                }
            });
            AlertDialog.Builder builder;
            if (theme == Theme.DARK) {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setCustomTitle(setupHeader(getResources().getString(R.string.images)));
            builder.setView(view);
            builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imageListOpened = false;
                }
            });
            imageListAlert = builder.create();
            imageListAlert.show();
            imageListOpened = true;
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
                        m.setPin("0");
                        pinnedList.remove(m2);

                        Map<String, Object> map = new HashMap<String, Object>();
                        DatabaseReference message_root = root.child(m.getKey());
                        map.put("pin", "0");
                        message_root.updateChildren(map);

                        Toast.makeText(getApplicationContext(), R.string.messageunpinned, Toast.LENGTH_SHORT).show();

                        removed = true;
                        break;
                    }
                }
                if (!removed) {
                    m.setPin("1");
                    if (!pinnedList.isEmpty()) {
                        int index = 0;
                        for (Message pm : pinnedList) {
                            if (Long.parseLong(pm.getbTime().substring(0, 8) + pm.getbTime().substring(9, 15)) > Long.parseLong(m.getbTime().substring(0, 8) + m.getbTime().substring(9, 15))) {
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
                    DatabaseReference message_root = root.child(m.getKey());
                    map.put("pin", "1");
                    message_root.updateChildren(map);

                    Toast.makeText(getApplicationContext(), R.string.messagepinned, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void editRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.add_room, null);

        final EditText edit_room_name = view.findViewById(R.id.room_name);
        final EditText edit_room_desc = view.findViewById(R.id.room_description);
        final EditText edit_room_password = view.findViewById(R.id.room_password);
        final EditText edit_room_password_repeat = view.findViewById(R.id.room_password_repeat);

        final TextInputLayout room_name_layout = view.findViewById(R.id.room_name_layout);
        final TextInputLayout room_desc_layout = view.findViewById(R.id.room_description_layout);
        final TextInputLayout room_password_layout = view.findViewById(R.id.room_password_layout);
        final TextInputLayout room_password_repeat_layout = view.findViewById(R.id.room_password_repeat_layout);

        edit_room_name.setText(room_name);
        edit_room_desc.setText(desc);
        edit_room_password.setText(passwd);
        edit_room_password_repeat.setText(passwd);

        final Spinner spinner = view.findViewById(R.id.spinner);
        roomimage = view.findViewById(R.id.room_image);

        spinner.setSelection(Integer.parseInt(caty));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,int position, long id) {
                kategorie = adapter.getItemAtPosition(position).toString();
                for (int i = 0; i < kat.length; i++) {
                    if (kat[i].equals(kategorie)) {
                        katindex = i;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        edit_room_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    room_name_layout.setError(null);
                }
            }
        });
        edit_room_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    room_desc_layout.setError(null);
                }
            }
        });
        edit_room_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    room_password_layout.setError(null);
                }
            }
        });
        edit_room_password_repeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    room_password_repeat_layout.setError(null);
                }
            }
        });

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_image = storageRef.child("room_images/" + room_img);

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomimage);
        } else {
            GlideApp.with(getApplicationContext())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomimage);
        }

        roomimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_ROOM_IMAGE_REQUEST);
            }
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
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                dialogInterface.cancel();
                openInfo();
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
                        final String roomName = edit_room_name.getText().toString().trim();
                        final String roomPassword = edit_room_password.getText().toString().trim();
                        final String roomPasswordRepeat = edit_room_password_repeat.getText().toString().trim();
                        final String roomDescription = edit_room_desc.getText().toString().trim();
                        if (!roomName.isEmpty()) {
                            if (!roomDescription.isEmpty()) {
                                if (katindex!=0) {
                                    if (!roomPassword.isEmpty()) {
                                        if (!roomPasswordRepeat.isEmpty()) {
                                            if (roomPassword.equals(roomPasswordRepeat)) {
                                                if (view != null) {
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                }
                                                DatabaseReference message_root = FirebaseDatabase.getInstance().getReference().child("rooms").child(room_key).child(roomKey);
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("name", roomName);
                                                map.put("passwd", roomPassword);
                                                map.put("desc", roomDescription);
                                                map.put("caty", String.valueOf(katindex));
                                                message_root.updateChildren(map);
                                                fileOperations.writeToFile(roomPassword, "mychatapp_raum_" + room_key + ".txt");
                                                setTitle(roomName);
                                                Toast.makeText(getApplicationContext(), R.string.roomedited, Toast.LENGTH_SHORT).show();
                                                desc = roomDescription;
                                                caty = String.valueOf(katindex);
                                                passwd = roomPassword;
                                                alert.cancel();
                                                openInfo();
                                            } else {
                                                Toast.makeText(getApplicationContext(), R.string.passwordsdontmatch, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            room_password_repeat_layout.setError(getResources().getString(R.string.repeatpassword));
                                        }
                                    } else {
                                        room_password_layout.setError(getResources().getString(R.string.enterpassword));
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.selectcategory, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                room_desc_layout.setError(getResources().getString(R.string.enterroomdesc));
                            }
                        } else {
                            room_name_layout.setError(getResources().getString(R.string.enterroomname));
                        }
                    }
                });
            }
        });
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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