package com.yannick.mychatapp.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yannick.mychatapp.Constants;
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
import com.yannick.mychatapp.data.Image;
import com.yannick.mychatapp.fragments.RoomListFragmentFavorites;
import com.yannick.mychatapp.fragments.RoomListFragmentMore;
import com.yannick.mychatapp.fragments.RoomListFragmentMyRooms;
import com.yannick.mychatapp.adapters.SectionsPageAdapter;
import com.yannick.mychatapp.data.Theme;
import com.yannick.mychatapp.adapters.ThemeAdapter;
import com.yannick.mychatapp.data.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;
import hakobastvatsatryan.DropdownTextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private String imageRoom;

    private Theme theme;

    private Background background;
    private User currentUser;
    private final DatabaseReference roomRoot = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.roomsDatabaseKey);
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.usersDatabaseKey);
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
    private int categoryIndex = 0;
    private static int color = 0;
    private int tmpcolor = -1;
    private TabLayout tabLayout;
    private RoomListFragmentMore rFragMore = new RoomListFragmentMore();
    private RoomListFragmentFavorites rFragFavs = new RoomListFragmentFavorites();
    private RoomListFragmentMyRooms rFragMyRooms = new RoomListFragmentMyRooms();

    private FirebaseStorage storage;
    private CircleImageView profileImage;
    private ImageView banner;
    private ImageView profileBannerImageView;
    private ImageButton profileImageButton;
    private ImageButton profileBannerButton;
    private ImageButton roomImageButton;
    private CircleImageView profileImageImageView;
    private TextView profileNameText;

    private SectionsPageAdapter pageadapter;

    private SearchView searchView;

    private Dialog fullscreendialog;

    private FirebaseAuth mAuth;

    private final FileOperations fileOperations = new FileOperations(this);

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

        ViewPager viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

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

        profileNameText = v.findViewById(R.id.name_profile);
        profileImageImageView = v.findViewById(R.id.icon_profile);
        profileBannerImageView = v.findViewById(R.id.banner_profile);

        profileImageImageView.setOnClickListener(view -> showProfile());

        if (theme == Theme.DARK) {
            profileBannerImageView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar_dark, null));
        } else {
            profileBannerImageView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar, null));
        }

        storage = FirebaseStorage.getInstance();

        FloatingActionButton addRoomButton = findViewById(R.id.addroom);
        addRoomButton.setOnClickListener(view -> addRoom());

        createUserProfile(mAuth.getCurrentUser().getUid());
    }

    private void addRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.add_room, null);

        final EditText roomNameEditText = view.findViewById(R.id.room_name);
        final EditText roomDescriptionEditText = view.findViewById(R.id.room_description);
        final EditText roomPasswordEditText = view.findViewById(R.id.room_password);
        final EditText roomPasswordRepeatEditText = view.findViewById(R.id.room_password_repeat);

        final TextInputLayout roomNameLayout = view.findViewById(R.id.room_name_layout);
        final TextInputLayout roomPasswordLayout = view.findViewById(R.id.room_password_layout);
        final TextInputLayout roomPasswordRepeatLayout = view.findViewById(R.id.room_password_repeat_layout);

        final Spinner spinner = view.findViewById(R.id.spinner);
        roomImageButton = view.findViewById(R.id.room_image);

        imageRoom = ImageOperations.getStandardRoomImage();

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

        StorageReference refRoomImage = storage.getReference().child(Constants.roomImagesStorageKey + "0");

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    .load(refRoomImage)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(roomImageButton);
        } else {
            GlideApp.with(getApplicationContext())
                    .load(refRoomImage)
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
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createroom)));
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
                                    if (!searchView.isIconified()) {
                                        searchView.setIconified(true);
                                        searchView.setIconified(true);
                                    }
                                    final String roomKey = roomRoot.push().getKey();
                                    DatabaseReference root = roomRoot.child(roomKey);
                                    root.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                DatabaseReference messageRoot = root.child(Constants.roomDataDatabaseKey);
                                                Map<String, Object> map = new HashMap<>();
                                                String currentDateAndTime = sdf.format(new Date());
                                                map.put("admin", currentUser.getUserID());
                                                map.put("name", roomName);
                                                map.put("time", currentDateAndTime);
                                                map.put("password", roomPassword);
                                                map.put("description", roomDescription);
                                                map.put("category", categoryIndex);
                                                map.put("image", imageRoom);
                                                messageRoot.updateChildren(map);
                                                fileOperations.writeToFile(roomPassword, String.format(FileOperations.passwordFilePattern, roomKey));
                                                fileOperations.writeToFile(Constants.roomDataDatabaseKey, String.format(FileOperations.newestMessageFilePattern, roomKey));
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

        String[] versions = getResources().getStringArray(R.array.changelog_titles);
        String[] texts = getResources().getStringArray(R.array.changelog_text);

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);

        for (int i = 0; i < versions.length; i++) {
            View inflatedView = inflater.inflate(R.layout.changelog_item, null);
            DropdownTextView dropdownTextView = inflatedView.findViewById(R.id.dropdownTextView);
            dropdownTextView.setTitleText(versions[i]);
            dropdownTextView.setContentText(texts[i]);
            linearLayout.addView(inflatedView, i);
        }

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
            banner.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar_dark, null));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
            banner.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.side_nav_bar, null));
        }

        final StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);

        final StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + currentUser.getImage());
        GlideApp.with(getApplicationContext())
                //.using(new FirebaseImageLoader())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImage);

        profileImage.setOnClickListener(v -> showFullscreenImage(currentUser.getImage(), Image.PROFILE_IMAGE));

        banner.setOnClickListener(v -> showFullscreenImage(currentUser.getBanner(), Image.PROFILE_BANNER));

        profileName.setText(currentUser.getName());
        profileDescription.setText(currentUser.getDescription());
        birthday.setText(currentUser.getBirthday());
        location.setText(currentUser.getLocation());

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.myprofile)));
        builder.setView(view);
        builder.setPositiveButton(R.string.close, null);
        builder.setNegativeButton(R.string.editprofile, (dialogInterface, i) -> editProfile());

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
        profileImageButton = view.findViewById(R.id.user_profile_image);
        profileBannerButton = view.findViewById(R.id.user_profile_banner);

        StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + currentUser.getImage());
        StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + currentUser.getBanner());

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(refProfileImage)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImageButton);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(refProfileBanner)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        } else {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(refProfileImage)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImageButton);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(refProfileBanner)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        }

        profileImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickProfileImageLauncher.launch(intent);
        });

        profileBannerButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickProfileBannerLauncher.launch(intent);
        });

        username.setText(currentUser.getName());
        profileDescription.setText(currentUser.getDescription());
        birthday.setText(currentUser.getBirthday());
        location.setText(currentUser.getLocation());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(getResources().getIntArray(R.array.favcolors)[color]);
        favColour.setBackground(shape);

        AtomicReference<String> selectedBirthday = new AtomicReference<>(currentUser.getBirthday());

        birthday.setOnClickListener(view15 -> {
            DatePickerDialog datePicker = new DatePickerDialog(view15.getContext(), (view14, year, monthOfYear, dayOfMonth) -> {
                String date = StringOperations.buildDate(year, monthOfYear, dayOfMonth);
                selectedBirthday.set(date);
                birthday.setText(date);
            }, StringOperations.getYear(selectedBirthday.toString()), StringOperations.getMonth(selectedBirthday.toString()), StringOperations.getDay(selectedBirthday.toString()));
            if (theme == Theme.DARK) {
                datePicker.getWindow().setBackgroundDrawableResource(R.color.dark_background);
            }
            datePicker.getDatePicker().setMaxDate(calculateMinBirthday());
            datePicker.show();
        });

        favColour.setOnClickListener(view13 -> {
            SpectrumDialog.Builder builder;
            if (theme == Theme.DARK) {
                builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialogDark);
            } else {
                builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialog);
            }
            builder.setColors(R.array.favcolors).setTitle(R.string.chooseacolor)
                    .setSelectedColor(getResources().getIntArray(R.array.favcolors)[color])
                    .setFixedColumnCount(5)
                    .setOnColorSelectedListener((positiveResult, scolor) -> {
                if (positiveResult) {
                    int i = 0;
                    for (int c : getResources().getIntArray(R.array.favcolors)) {
                        if (c == scolor) {
                            tmpcolor = i;
                            GradientDrawable shape1 = new GradientDrawable();
                            shape1.setShape(GradientDrawable.OVAL);
                            shape1.setColor(getResources().getIntArray(R.array.favcolors)[i]);
                            favColour.setBackground(shape1);
                        }
                        i++;
                    }
                }
            }).build().show(getSupportFragmentManager(), "ColorPicker");
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
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            View view12 = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (view12 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
            }
            dialogInterface.cancel();
            showProfile();
        });
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view1 -> {
                if (!username.getText().toString().isEmpty()) {
                    if (!location.getText().toString().isEmpty()) {
                        if (!birthday.getText().toString().isEmpty()) {
                            String oldUserName = currentUser.getName();
                            int oldColor = color;
                            currentUser.setName(username.getText().toString());
                            currentUser.setDescription(profileDescription.getText().toString());
                            currentUser.setLocation(location.getText().toString());
                            currentUser.setBirthday(birthday.getText().toString());
                            if (tmpcolor >= 0) {
                                color = tmpcolor;
                            }
                            DatabaseReference currentUserRoot = userRoot.child(currentUser.getUserID());
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", currentUser.getName());
                            map.put("description", currentUser.getDescription());
                            map.put("location", currentUser.getLocation());
                            map.put("birthday", StringOperations.convertDateToDatabaseFormat(currentUser.getBirthday()));
                            map.put("favColour", color);
                            String profileImage = UUID.randomUUID().toString();
                            if (!currentUser.getOwnProfileImage() && ((!currentUser.getName().substring(0, 1).equals(oldUserName.substring(0, 1)) || color != oldColor))) {
                                map.put("image", profileImage);
                            }
                            currentUserRoot.updateChildren(map);
                            profileNameText.setText(currentUser.getName());
                            Toast.makeText(getApplicationContext(), R.string.profileedited, Toast.LENGTH_SHORT).show();
                            if (view1 != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                            }
                            if (!currentUser.getOwnProfileImage() && ((!currentUser.getName().substring(0, 1).equals(oldUserName.substring(0, 1)) || color != oldColor))) {
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
                                final StorageReference refNewProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + profileImage);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                byteArray = stream.toByteArray();
                                try {
                                    stream.close();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }
                                refNewProfileImage.putBytes(byteArray);
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
                    usernameLayout.setError(getResources().getString(R.string.entername));
                }
            });
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
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {

            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view1 -> {
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
            });
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
        Button deleteAccount = view.findViewById(R.id.delete_account);

        boolean settingPushNotification = sharedPref.getBoolean(Constants.settingsPushNotificationsKey, true);
        boolean settingSavEnteredText = sharedPref.getBoolean(Constants.settingsSaveEnteredTextKey, true);
        boolean settingPreviewImages = sharedPref.getBoolean(Constants.settingsPreviewImagesKey, true);
        boolean settingStoreCameraPictures = sharedPref.getBoolean(Constants.settingsStoreCameraPicturesKey, true);

        push.setChecked(settingPushNotification);
        save.setChecked(settingSavEnteredText);
        preview.setChecked(settingPreviewImages);
        camera.setChecked(settingStoreCameraPictures);

        deleteAccount.setOnClickListener(view1 -> showPasswordRequest());

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }
        builder.setCustomTitle(setupHeader(getResources().getString(R.string.settings)));
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.settingsPushNotificationsKey, push.isChecked());
            editor.putBoolean(Constants.settingsSaveEnteredTextKey, save.isChecked());
            editor.putBoolean(Constants.settingsPreviewImagesKey, preview.isChecked());
            editor.putBoolean(Constants.settingsStoreCameraPicturesKey, camera.isChecked());
            editor.apply();

            Toast.makeText(getApplicationContext(), R.string.settingssaved, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showPasswordRequest() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.enter_profile_password, null);

        final EditText passwordEdit = view.findViewById(R.id.profile_password);
        final TextInputLayout passwordLayout = view.findViewById(R.id.profile_password_layout);

        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    passwordLayout.setError(null);
                }
            }
        });

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.delete_account)));
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});

        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            View view1 = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
            dialogInterface.cancel();
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view12 -> {
                if (!passwordEdit.getText().toString().isEmpty()) {
                    AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), passwordEdit.getText().toString());
                    mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            deleteAccount();

                            if (view12 != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
                            }

                            alert.cancel();
                        } else {
                            passwordLayout.setError(getResources().getString(R.string.wrongpassword));
                        }
                    });
                } else {
                    passwordLayout.setError(getResources().getString(R.string.enterpassword));
                }
            });
        });
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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

        if (id == R.id.nav_profile) {
            showProfile();
        } else if (id == R.id.nav_themes) {
            changeDesign();
        } else if (id == R.id.nav_settings) {
            showSettings();
        } else if (id == R.id.nav_changelog) {
            showChangelog();
        } else if (id == R.id.nav_aboutus) {
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

        MenuItem searchItem = menu.findItem(R.id.roomsearch);
        searchView = (SearchView) searchItem.getActionView();
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
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void updateNavigationDrawerIcon() {
        StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + currentUser.getImage());
        GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImageImageView);

        profileBannerImageView.setImageDrawable(null);
        StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(profileBannerImageView);
    }

    private void updateProfileImages() {
        StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + currentUser.getImage());
        GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImage);

        StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(banner);
    }

    private void updateEditProfileImages() {
        StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + currentUser.getImage());
        GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImageButton);

        StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + currentUser.getBanner());
        GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
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
                currentUser.setBirthday(StringOperations.convertDateToDisplayFormat(currentUser.getBirthday()));
                updateNavigationDrawerIcon();
                profileNameText.setText(currentUser.getName());
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
        final Handler handler = new Handler();
        handler.postDelayed(() -> readUserData(userID), 100);
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

        String imageName = UUID.randomUUID().toString();
        StorageReference ref;
        if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
            ref = storage.getReference().child(Constants.profileImagesStorageKey + imageName);
        } else if (type == ImageOperations.PICK_PROFILE_BANNER_REQUEST) {
            ref = storage.getReference().child(Constants.profileBannersStorageKey + imageName);
        } else {
            ref = storage.getReference().child(Constants.roomImagesStorageKey + imageName);
        }

        ImageOperations imageOperations = new ImageOperations(getContentResolver());
        byte[] byteArray = imageOperations.getImageAsBytes(this, filePath, type);

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();

            if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
                currentUser.setOwnProfileImage(true);
                currentUser.setImage(imageName);
                DatabaseReference currentUserRoot = userRoot.child(currentUser.getUserID());
                Map<String, Object> map = new HashMap<>();
                map.put("ownProfileImage", true);
                map.put("image", imageName);
                currentUserRoot.updateChildren(map);
            } else if (type == ImageOperations.PICK_PROFILE_BANNER_REQUEST) {
                currentUser.setBanner(imageName);
                DatabaseReference currentUserRoot = userRoot.child(currentUser.getUserID());
                Map<String, Object> map = new HashMap<>();
                map.put("banner", imageName);
                currentUserRoot.updateChildren(map);
            }

            if (type != ImageOperations.PICK_ROOM_IMAGE_REQUEST) {
                updateNavigationDrawerIcon();
                updateProfileImages();
                updateEditProfileImages();
            }

            if (type == ImageOperations.PICK_ROOM_IMAGE_REQUEST) {
                imageRoom = imageName;
                StorageReference refRoomImage = storage.getReference().child(Constants.roomImagesStorageKey + imageName);
                GlideApp.with(getApplicationContext())
                        .load(refRoomImage)
                        .centerCrop()
                        .into(roomImageButton);
            }
        }).addOnFailureListener(e -> {
            Log.e("Upload failed", e.toString());
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                    .getTotalByteCount());
            progressDialog.setMessage((int)progress+"% " + getResources().getString(R.string.uploaded));
        });
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

        ArrayList<String> images = new ArrayList<>();
        images.add(image);
        mViewPager.setAdapter(new FullScreenImageAdapter(this, images, type));

        fullscreendialog.show();
    }

    private void deleteAccount() {
        if (mAuth.getCurrentUser() != null) {
            userRoot.child(currentUser.getUserID()).removeValue((error, ref) -> mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent homeIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(homeIntent);
                    finish();
                    Toast.makeText(MainActivity.this, R.string.room_successfully_deleted, Toast.LENGTH_SHORT).show();
                }
            }));
        }
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

    ActivityResultLauncher<Intent> pickProfileImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri filePath = data.getData();
                    if (filePath != null) {
                        uploadImage(filePath, ImageOperations.PICK_PROFILE_IMAGE_REQUEST);
                    }
                }
            });

    ActivityResultLauncher<Intent> pickProfileBannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri filePath = data.getData();
                    if (filePath != null) {
                        uploadImage(filePath, ImageOperations.PICK_PROFILE_BANNER_REQUEST);
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

    public long calculateMinBirthday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -14);
        return cal.getTimeInMillis();
    }
}