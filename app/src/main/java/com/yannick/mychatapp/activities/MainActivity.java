package com.yannick.mychatapp.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yannick.mychatapp.StringOperations;
import com.yannick.mychatapp.data.Background;
import com.yannick.mychatapp.adapters.BackgroundAdapter;
import com.yannick.mychatapp.BuildConfig;
import com.yannick.mychatapp.CatchViewPager;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.adapters.FullScreenImageAdapter;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.ImageOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.fragments.RoomListFragmentFavorites;
import com.yannick.mychatapp.fragments.RoomListFragmentMore;
import com.yannick.mychatapp.fragments.RoomListFragmentMyRooms;
import com.yannick.mychatapp.adapters.SectionsPageAdapter;
import com.yannick.mychatapp.data.Theme;
import com.yannick.mychatapp.adapters.ThemeAdapter;
import com.yannick.mychatapp.data.User;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private String img_room, img_user, img_banner;

    private Theme theme;

    private Background background;
    private User currentUser;
    private DatabaseReference roomRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("rooms");
    private DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("users");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
    private int categoryIndex = 0;
    private static int color = 0;
    private int tmpcolor = -1;
    private final ArrayList<String> userIdList = new ArrayList<>();
    private boolean userListCreated = false;
    private TabLayout tabLayout;
    private RoomListFragmentMore rFragMore = new RoomListFragmentMore();
    private RoomListFragmentFavorites rFragFavs = new RoomListFragmentFavorites();
    private RoomListFragmentMyRooms rFragMyRooms = new RoomListFragmentMyRooms();

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private CircleImageView profileImage;
    private ImageView banner;
    private ImageView banner_profile;
    private CircleImageView icon_profile;
    private TextView text_profile;

    private StorageReference pathReference_image;
    private StorageReference pathReference_banner;
    private StorageReference pathReference_roomimage;

    private ViewPager mViewPager;
    private SectionsPageAdapter pageadapter;

    private SearchView searchView;

    private Dialog fullscreendialog;

    private FirebaseAuth mAuth;

    private final FileOperations fileOperations = new FileOperations(this);

    public static final String settingsPushNotificationsKey = "settingsPushNotifications";
    public static final String settingsSaveEnteredTextKey = "settingsSaveEnteredText";
    public static final String settingsPreviewImagesKey = "settingsPreviewImages";
    public static final String settingsStoreCameraPicturesKey = "settingsStoreCameraPictures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pageadapter = new SectionsPageAdapter(getSupportFragmentManager());
        rFragMore = new RoomListFragmentMore();
        rFragMyRooms = new RoomListFragmentMyRooms();
        rFragFavs = new RoomListFragmentFavorites();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(tabReceiver, new IntentFilter("tab"));
        LocalBroadcastManager.getInstance(this).registerReceiver(themeReceiver, new IntentFilter("themeOption"));
        LocalBroadcastManager.getInstance(this).registerReceiver(backgroundReceiver, new IntentFilter("backgroundOption"));
        LocalBroadcastManager.getInstance(this).registerReceiver(closeFullscreenReceiver, new IntentFilter("closefullscreen"));

        fileOperations.writeToFile("0", FileOperations.currentRoomFile);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);

        icon_profile = v.findViewById(R.id.icon_profile);
        text_profile = v.findViewById(R.id.name_profile);
        banner_profile = v.findViewById(R.id.banner_profile);

        icon_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile();
            }
        });

        if (theme == Theme.DARK) {
            banner_profile.setBackground(getResources().getDrawable(R.drawable.side_nav_bar_dark));
        } else {
            banner_profile.setBackground(getResources().getDrawable(R.drawable.side_nav_bar));
        }

        storage = FirebaseStorage.getInstance();

        FloatingActionButton btn_add_room = findViewById(R.id.addroom);
        btn_add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRoom();
            }
        });

        userRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userIdList.add(ds.getKey());
                }
                userListCreated = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!userListCreated) {
                    handler.postDelayed(this, 1000);
                } else {
                    createUserProfile(mAuth.getCurrentUser().getUid());
                }
            }
        }, 100);
    }

    private void addRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.add_room, null);

        final EditText room_name = view.findViewById(R.id.room_name);
        final EditText room_desc = view.findViewById(R.id.room_description);
        final EditText room_password = view.findViewById(R.id.room_password);
        final EditText room_password_repeat = view.findViewById(R.id.room_password_repeat);

        final TextInputLayout room_name_layout = view.findViewById(R.id.room_name_layout);
        final TextInputLayout room_desc_layout = view.findViewById(R.id.room_description_layout);
        final TextInputLayout room_password_layout = view.findViewById(R.id.room_password_layout);
        final TextInputLayout room_password_repeat_layout = view.findViewById(R.id.room_password_repeat_layout);

        final Spinner spinner = view.findViewById(R.id.spinner);
        final ImageButton roomImageButton = view.findViewById(R.id.room_image);

        Random rand = new Random();
        img_room = "standard" + (rand.nextInt(4)+1);

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

        room_name.addTextChangedListener(new TextWatcher() {
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
        room_desc.addTextChangedListener(new TextWatcher() {
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
        room_password.addTextChangedListener(new TextWatcher() {
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
        room_password_repeat.addTextChangedListener(new TextWatcher() {
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

        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        pathReference_roomimage = storageRef.child("room_images/" + "0");

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    .load(pathReference_roomimage)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomImageButton);
        } else {
            GlideApp.with(getApplicationContext())
                    .load(pathReference_roomimage)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomImageButton);
        }

        roomImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 2);
            }
        });

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createroom)));
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
                        final String roomName = room_name.getText().toString().trim();
                        final String roomPassword = room_password.getText().toString().trim();
                        final String roomPasswordRepeat = room_password_repeat.getText().toString().trim();
                        final String roomDescription = room_desc.getText().toString().trim();
                        if (!roomName.isEmpty()) {
                            if (!roomDescription.isEmpty()) {
                                if (categoryIndex !=0) {
                                    if (!roomPassword.isEmpty()) {
                                        if (!roomPasswordRepeat.isEmpty()) {
                                            if (roomPassword.equals(roomPasswordRepeat)) {
                                                if (view != null) {
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                }
                                                if (!searchView.isIconified()) {
                                                    searchView.setIconified(true);
                                                    searchView.setIconified(true);
                                                }
                                                final String roomKey = roomRoot.push().getKey();
                                                roomRoot = FirebaseDatabase.getInstance().getReference().child("rooms").child(roomKey);
                                                roomRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot snapshot) {
                                                        if (!snapshot.exists()) {
                                                            DatabaseReference message_root = roomRoot.child("-0roomdata");
                                                            Map<String, Object> map = new HashMap<>();
                                                            String currentDateAndTime = sdf.format(new Date());
                                                            map.put("admin", currentUser.getUserID());
                                                            map.put("name", roomName);
                                                            map.put("time", currentDateAndTime);
                                                            map.put("passwd", roomPassword);
                                                            map.put("desc", roomDescription);
                                                            map.put("category", String.valueOf(categoryIndex));
                                                            map.put("img", img_room);
                                                            message_root.updateChildren(map);
                                                            fileOperations.writeToFile(roomPassword, String.format(FileOperations.passwordFilePattern, roomKey));
                                                            fileOperations.writeToFile("-0roomdata", String.format(FileOperations.newestMessageFilePattern, roomKey));
                                                            FirebaseMessaging.getInstance().subscribeToTopic(roomKey);
                                                            Toast.makeText(getApplicationContext(), R.string.roomcreated, Toast.LENGTH_SHORT).show();
                                                            alert.cancel();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), R.string.roomalreadyexists, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    public void onCancelled(DatabaseError error) {

                                                    }
                                                });
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

    private void showCopyright() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.about_us, null);

        TextView aboutUs = view.findViewById(R.id.aboutus);
        aboutUs.setText(getResources().getString(R.string.copyright, BuildConfig.VERSION_NAME));

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.aboutus)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showChangelog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.changelog, null);

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.changelog)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showProfile() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.profile, null);

        profileImage = view.findViewById(R.id.icon_profile);
        TextView profileName = view.findViewById(R.id.name_profile);
        TextView profileDescription = view.findViewById(R.id.profile_bio);
        TextView birthday = view.findViewById(R.id.profile_birthday);
        TextView location = view.findViewById(R.id.profile_location);
        banner = view.findViewById(R.id.background_profile);
        AlertDialog.Builder builder;

        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            banner.setBackground(getResources().getDrawable(R.drawable.side_nav_bar_dark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
            banner.setBackground(getResources().getDrawable(R.drawable.side_nav_bar));
        }

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_banner = storageRef.child("profile_banners/" + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(pathReference_banner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);

        final StorageReference pathReference_image = storageRef.child("profile_images/" + currentUser.getImg());
        GlideApp.with(getApplicationContext())
                //.using(new FirebaseImageLoader())
                .load(pathReference_image)
                .centerCrop()
                .into(profileImage);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullscreenImage(0);
            }
        });

        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullscreenImage(1);
            }
        });

        profileName.setText(currentUser.getName());
        profileDescription.setText(currentUser.getProfileDescription());
        birthday.setText(currentUser.getBirthday());
        location.setText(currentUser.getLocation());

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.myprofile)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        builder.setNegativeButton(R.string.editprofile, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editProfile();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void editProfile() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.edit_profile, null);

        final EditText username = view.findViewById(R.id.user_name);
        final EditText profileDescription = view.findViewById(R.id.user_bio);
        final EditText birthday = view.findViewById(R.id.user_birthday);
        final EditText location = view.findViewById(R.id.user_location);

        final TextInputLayout usernameLayout = view.findViewById(R.id.user_name_layout);
        final TextInputLayout profileDescriptionLayout = view.findViewById(R.id.user_bio_layout);
        final TextInputLayout locationLayout = view.findViewById(R.id.user_location_layout);

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    usernameLayout.setError(null);
                }
            }
        });
        profileDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    profileDescriptionLayout.setError(null);
                }
            }
        });
        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    locationLayout.setError(null);
                }
            }
        });

        final ImageButton favColour = view.findViewById(R.id.user_favcolor);
        final ImageButton profileImageButton = view.findViewById(R.id.user_profile_image);
        final ImageButton profileBannerButton = view.findViewById(R.id.user_profile_banner);

        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_image = storageRef.child("profile_images/" + currentUser.getImg());
        final StorageReference pathReference_banner = storageRef.child("profile_banners/" + currentUser.getBanner());

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImageButton);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_banner)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        } else {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImageButton);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_banner)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        }

        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 0);
            }
        });

        profileBannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
            }
        });

        username.setText(currentUser.getName());
        profileDescription.setText(currentUser.getProfileDescription());
        birthday.setText(currentUser.getBirthday());
        location.setText(currentUser.getLocation());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(getResources().getIntArray(R.array.favcolors)[color]);
        favColour.setBackground(shape);

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date = StringOperations.buildDate(year, monthOfYear, dayOfMonth);
                        birthday.setText(date);
                    }
                }, StringOperations.getYear(currentUser.getBirthday()), StringOperations.getMonth(currentUser.getBirthday()), StringOperations.getDay(currentUser.getBirthday()));
                if (theme == Theme.DARK) {
                    datePicker.getWindow().setBackgroundDrawableResource(R.color.dark_background);
                }
                Calendar c = Calendar.getInstance();
                c.set(2004, 11, 31);
                datePicker.getDatePicker().setMaxDate(c.getTimeInMillis());
                datePicker.show();
            }
        });

        favColour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpectrumDialog.Builder builder;
                if (theme == Theme.DARK) {
                    builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialogDark);
                } else {
                    builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialog);
                }
                builder.setColors(R.array.favcolors).setTitle(R.string.chooseacolor).setSelectedColor(getResources().getIntArray(R.array.favcolors)[color]).setFixedColumnCount(5).setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int scolor) {
                        if (positiveResult) {
                            int i = 0;
                            for (int c : getResources().getIntArray(R.array.favcolors)) {
                                if (c == scolor) {
                                    tmpcolor = i;
                                    GradientDrawable shape = new GradientDrawable();
                                    shape.setShape(GradientDrawable.OVAL);
                                    shape.setColor(getResources().getIntArray(R.array.favcolors)[i]);
                                    favColour.setBackground(shape);
                                }
                                i++;
                            }
                        }
                    }
                }).build().show(getSupportFragmentManager(), "ColorPicker");
            }
        });

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.editprofile)));
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                dialogInterface.cancel();
                showProfile();
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
                        if (!username.getText().toString().isEmpty()) {
                            if (!profileDescription.getText().toString().isEmpty()) {
                                if (!location.getText().toString().isEmpty()) {
                                    if (!birthday.getText().toString().isEmpty()) {
                                        String old_name = currentUser.getName();
                                        int old_color = color;
                                        currentUser.setName(username.getText().toString());
                                        currentUser.setProfileDescription(profileDescription.getText().toString());
                                        currentUser.setLocation(location.getText().toString());
                                        currentUser.setBirthday(birthday.getText().toString());
                                        if (tmpcolor >= 0) {
                                            color = tmpcolor;
                                        }
                                        DatabaseReference user_root = userRoot.child(currentUser.getUserID());
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", currentUser.getName());
                                        map.put("profileDescription", currentUser.getProfileDescription());
                                        map.put("location", currentUser.getLocation());
                                        map.put("birthday", currentUser.getBirthday().substring(6, 10) + currentUser.getBirthday().substring(3, 5) + currentUser.getBirthday().substring(0, 2));
                                        map.put("favColour", String.valueOf(color));
                                        if (!currentUser.getOwnpi().equals("1") && ((!currentUser.getName().substring(0, 1).equals(old_name.substring(0, 1)) || color != old_color))) {
                                            img_user = UUID.randomUUID().toString();
                                            map.put("img", img_user);
                                        }
                                        user_root.updateChildren(map);
                                        text_profile.setText(currentUser.getName());
                                        Toast.makeText(getApplicationContext(), R.string.profileedited, Toast.LENGTH_SHORT).show();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                        if (!currentUser.getOwnpi().equals("1") && ((!currentUser.getName().substring(0, 1).equals(old_name.substring(0, 1)) || color != old_color))) {
                                            storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
                                            TextDrawable drawable = TextDrawable.builder()
                                                    .beginConfig()
                                                    .bold()
                                                    .endConfig()
                                                    .buildRect(currentUser.getName().substring(0, 1), getResources().getIntArray(R.array.favcolors)[color]);
                                            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
                                            Canvas canvas = new Canvas(bitmap);
                                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                            drawable.draw(canvas);

                                            byte[] byteArray;
                                            final StorageReference pathReference_image = storageRef.child("profile_images/" + img_user);
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            byteArray = stream.toByteArray();
                                            try {
                                                stream.close();
                                            } catch (IOException ioe) {
                                                ioe.printStackTrace();
                                            }
                                            pathReference_image.putBytes(byteArray);
                                        }
                                        showProfile();
                                        alert.cancel();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.incompletedata, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    locationLayout.setError(getResources().getString(R.string.enterlocation));
                                }
                            } else {
                                profileDescriptionLayout.setError(getResources().getString(R.string.enterbio));
                            }
                        } else {
                            usernameLayout.setError(getResources().getString(R.string.entername));
                        }
                    }
                });
            }
        });
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void changeDesign() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_design, null);

        Theme currentTheme = Theme.getCurrentTheme(getApplicationContext());
        Background currentBackground = Background.getCurrentBackground(getApplicationContext());

        GridView themeGrid = view.findViewById(R.id.gridview_themes);
        themeGrid.setAdapter(new ThemeAdapter(this, getResources().obtainTypedArray(R.array.themeDrawables), currentTheme, theme));

        GridView backgroundGrid = view.findViewById(R.id.gridview_backgrounds);
        backgroundGrid.setAdapter(new BackgroundAdapter(this, getResources().obtainTypedArray(R.array.backgroundDrawables), currentBackground, theme));

        AlertDialog.Builder builder;
        if (this.theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.changedesign)));
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
                        Theme.setTheme(getApplicationContext(), theme);
                        Background.setBackground(getApplicationContext(), background);
                        if (currentTheme != theme) {
                            FragmentManager mFragmentManager = getSupportFragmentManager();
                            mFragmentManager.beginTransaction().remove(rFragMore).commit();
                            mFragmentManager.beginTransaction().remove(rFragMyRooms).commit();
                            mFragmentManager.beginTransaction().remove(rFragFavs).commit();
                            recreate();
                        }
                        Toast.makeText(getApplicationContext(), R.string.settingssaved, Toast.LENGTH_SHORT).show();
                        alert.cancel();
                    }
                });
            }
        });
        alert.show();
    }

    private void showSettings() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.settings, null);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        SwitchCompat push = view.findViewById(R.id.push);
        SwitchCompat save = view.findViewById(R.id.save);
        SwitchCompat preview = view.findViewById(R.id.preview);
        SwitchCompat camera = view.findViewById(R.id.camera);
        boolean s_push = sharedPref.getBoolean(settingsPushNotificationsKey, false);
        boolean s_save = sharedPref.getBoolean(settingsSaveEnteredTextKey, false);
        boolean s_preview = sharedPref.getBoolean(settingsPreviewImagesKey, false);
        boolean s_camera = sharedPref.getBoolean(settingsStoreCameraPicturesKey, false);
        if (s_push) {
            push.setChecked(true);
        }
        if (s_save) {
            save.setChecked(true);
        }
        if (s_preview) {
            preview.setChecked(true);
        }
        if (s_camera) {
            camera.setChecked(true);
        }

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
            push.setTextColor(getResources().getColor(R.color.white));
            save.setTextColor(getResources().getColor(R.color.white));
            preview.setTextColor(getResources().getColor(R.color.white));
            camera.setTextColor(getResources().getColor(R.color.white));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.settings)));
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(settingsPushNotificationsKey, push.isChecked());
                editor.putBoolean(settingsSaveEnteredTextKey, save.isChecked());
                editor.putBoolean(settingsPreviewImagesKey, preview.isChecked());
                editor.putBoolean(settingsStoreCameraPicturesKey, camera.isChecked());
                editor.apply();

                Toast.makeText(getApplicationContext(), R.string.settingssaved, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profil) {
            showProfile();
        } else if (id == R.id.nav_themes) {
            changeDesign();
        } else if (id == R.id.nav_einstellungen) {
            showSettings();
        } else if (id == R.id.nav_changelog) {
            showChangelog();
        } else if (id == R.id.nav_impressum) {
            showCopyright();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Toast.makeText(this, R.string.successfullyloggedout, Toast.LENGTH_SHORT).show();
            Intent homeIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(homeIntent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_roomsearch, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem search_item = menu.findItem(R.id.roomsearch);
        searchView = (SearchView) search_item.getActionView();
        searchView.setFocusable(false);
        searchView.setQueryHint(getResources().getString(R.string.searchroom));
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Intent intent = new Intent("searchroom");
                intent.putExtra("searchkey", s);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void changeTheme(Theme theme) {
        this.theme = theme;
        if (theme == Theme.DARK) {
            setTheme(R.style.Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void updateNavigationDrawerIcon() {
        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        pathReference_image = storageRef.child("profile_images/" + currentUser.getImg());
        GlideApp.with(getApplicationContext())
                .load(pathReference_image)
                .centerCrop()
                .into(icon_profile);

        banner_profile.setImageDrawable(null);
        pathReference_banner = storageRef.child("profile_banners/" + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(pathReference_banner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner_profile);
    }

    private void updateProfileImages() {
        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        pathReference_image = storageRef.child("profile_images/" + currentUser.getImg());
        GlideApp.with(getApplicationContext())
                .load(pathReference_image)
                .centerCrop()
                .into(profileImage);

        pathReference_banner = storageRef.child("profile_banners/" + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(pathReference_banner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);
    }

    private void updateEditProfileImages() {
        final ImageButton profileImageButton = findViewById(R.id.user_profile_image);
        final ImageButton profileBannerButton = findViewById(R.id.user_profile_banner);

        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        pathReference_image = storageRef.child("profile_images/" + currentUser.getImg());
        GlideApp.with(getApplicationContext())
                .load(pathReference_image)
                .centerCrop()
                .into(profileImageButton);

        pathReference_banner = storageRef.child("profile_banners/" + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(pathReference_banner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(profileBannerButton);
    }

    private void setupViewPager(ViewPager viewPager) {
        pageadapter.addFragment(rFragMyRooms, getResources().getString(R.string.myrooms));
        pageadapter.addFragment(rFragFavs, getResources().getString(R.string.favorites));
        pageadapter.addFragment(rFragMore, getResources().getString(R.string.more));
        viewPager.setAdapter(pageadapter);
    }

    private void readUserData(final String userID) {
        userRoot.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                currentUser.setUserID(userID);
                currentUser.setBirthday(currentUser.getBirthday().substring(6, 8) + "." + currentUser.getBirthday().substring(4, 6) + "." + currentUser.getBirthday().substring(0, 4));
                updateNavigationDrawerIcon();
                text_profile.setText(currentUser.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public BroadcastReceiver tabReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
        }
    };

    public BroadcastReceiver themeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra("position", 0);

            theme = Theme.getByPosition(position);
        }
    };

    public BroadcastReceiver backgroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra("position", 0);

            background = Background.getByPosition(position);
        }
    };

    public BroadcastReceiver closeFullscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                View decorView = fullscreendialog.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(uiOptions);
                fullscreendialog.dismiss();
            } catch (NullPointerException npe) {
                Log.e("NullPointerException", npe.toString());
            }
        }
    };

    private void createUserProfile(final String userID) {
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                readUserData(userID);
            }
        }, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null ) {
            Uri filePath = data.getData();
            if (filePath != null) {
                uploadImage(filePath, requestCode);
            }
        }
    }

    private void uploadImage(Uri filePath, final int type) {
        final ProgressDialog progressDialog;
        if (theme == Theme.DARK) {
            progressDialog = new ProgressDialog(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setTitle(R.string.upload);
        progressDialog.show();

        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference ref;
        if (type == 0) {
            img_user = UUID.randomUUID().toString();
            ref = storageRef.child("profile_images/" + img_user);
        } else if (type == 1) {
            img_banner = UUID.randomUUID().toString();
            ref = storageRef.child("profile_banners/" + img_banner);
        } else {
            img_room = UUID.randomUUID().toString();
            ref = storageRef.child("room_images/" + img_room);
        }

        byte[] byteArray;
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        if (bmp.getWidth() < bmp.getHeight() && (type == 0 || type == 2)) {
            bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/2, bmp.getWidth(), bmp.getWidth());
        } else if (bmp.getWidth() > bmp.getHeight() && (type == 0 || type == 2)) {
            bmp = Bitmap.createBitmap(bmp, bmp.getWidth()/2-bmp.getHeight()/2, 0, bmp.getHeight(), bmp.getHeight());
        } else if (bmp.getWidth()/16*9 < bmp.getHeight() && type == 1) {
            bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/16*9/2, bmp.getWidth(), bmp.getWidth()/16*9);
        } else if (bmp.getWidth()/16*9 > bmp.getHeight() && type == 1) {
            bmp = Bitmap.createBitmap(bmp, bmp.getWidth()/2-bmp.getHeight()/9*16/2, 0, bmp.getHeight()/9*16, bmp.getHeight());
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int compression = 100;
        int compressFactor = 2;
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        ImageOperations imageOperations = new ImageOperations(getContentResolver());
        if (imageOperations.getImgSize(filePath) > height * width) {
            compressFactor = 4;
        }
        if (type == 0 || type == 2) {
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
            bmp = imageOperations.rotateImageIfRequired(this, bmp, filePath);
        } catch (IOException e) { }
        bmp.compress(Bitmap.CompressFormat.JPEG, compression, stream);
        byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                if (type == 0) {
                    currentUser.setOwnpi("1");
                    DatabaseReference user_root = userRoot.child(currentUser.getUserID());
                    Map<String, Object> map = new HashMap<>();
                    map.put("ownpi", "1");
                    map.put("img", img_user);
                    user_root.updateChildren(map);
                }
                if (type == 1) {
                    DatabaseReference user_root = userRoot.child(currentUser.getUserID());
                    Map<String, Object> map = new HashMap<>();
                    map.put("banner", img_banner);
                    user_root.updateChildren(map);
                }
                if (type != 2) {
                    updateNavigationDrawerIcon();
                    updateProfileImages();
                    updateEditProfileImages();
                }
                Toast.makeText(MainActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();
                if (type == 2) {
                    final ImageButton roomImageButton = findViewById(R.id.room_image);

                    pathReference_roomimage = storageRef.child("room_images/" + img_room);
                    GlideApp.with(getApplicationContext())
                            .load(pathReference_roomimage)
                            .centerCrop()
                            .into(roomImageButton);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
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

    private void showFullscreenImage(int type) {
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

        if (type == 0) {
            ArrayList<String> images = new ArrayList<>();
            images.add(currentUser.getImg());
            mViewPager.setAdapter(new FullScreenImageAdapter(this, images, 0));
        } else if (type == 1) {
            ArrayList<String> images = new ArrayList<>();
            images.add(currentUser.getBanner());
            mViewPager.setAdapter(new FullScreenImageAdapter(this, images, 1));
        }

        fullscreendialog.show();
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