package com.yannick.mychatapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.ImageOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.data.Theme;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Theme theme;
    private ImageButton profileImage;
    private ImageButton profileBanner;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private static String userID = "";
    private static final String birthday = "01.01.2000";
    private static String ownpi = "0";
    private static int colour = 0;
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("users");

    private String img = "";
    private String banner = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.activity_login);

        ImageView imgSplash = findViewById(R.id.imgsplash);
        MaterialButton loginButton = findViewById(R.id.loginbutton);
        MaterialButton createButton = findViewById(R.id.createbutton);
        EditText inputEmail = findViewById(R.id.login_email);
        EditText inputPassword = findViewById(R.id.login_password);
        TextInputLayout inputEmailLayout = findViewById(R.id.login_email_layout);
        TextInputLayout inputPasswordLayout = findViewById(R.id.login_password_layout);

        inputEmail.setTextColor(getResources().getColor(R.color.black));
        inputPassword.setTextColor(getResources().getColor(R.color.black));

        if (theme == Theme.DARK) {
            imgSplash.setImageResource(R.drawable.ic_splash_dark);
        } else {
            imgSplash.setImageResource(R.drawable.ic_splash);
        }

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    inputEmailLayout.setError(null);
                }
            }
        });

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                if (!email.isEmpty()) {
                    if (!password.isEmpty()) {
                        login(email, password);
                    } else {
                        inputPasswordLayout.setError(getResources().getString(R.string.enterpassword));
                    }
                } else {
                    inputEmailLayout.setError(getResources().getString(R.string.enteremail));
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Auth", "successful");
                            mAuth.getCurrentUser().reload();
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(homeIntent);
                                finish();
                            } else {
                                openResendEmailDialog();
                            }
                        } else {
                            Log.d("Auth", "failed");
                            Toast.makeText(getApplicationContext(), R.string.loginfailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resendEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification();
        Toast.makeText(LoginActivity.this, R.string.verificationmailsent, Toast.LENGTH_SHORT).show();
        mAuth.signOut();
    }

    private void openResendEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.resend_email, null);

        final MaterialButton resendEmailButton = view.findViewById(R.id.resendemailbutton);

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.pleaseverifyemail)));
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
            }
        });

        final AlertDialog alert = builder.create();

        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendEmail();
                alert.cancel();
            }
        });

        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void createAccount() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.create_account, null);

        final EditText email = view.findViewById(R.id.account_email);
        final EditText password = view.findViewById(R.id.account_password);
        final EditText passwordRepeat = view.findViewById(R.id.account_password_repeat);

        final TextInputLayout emailLayout = view.findViewById(R.id.account_email_layout);
        final TextInputLayout passwordLayout = view.findViewById(R.id.account_password_layout);
        final TextInputLayout passwordRepeatLayout = view.findViewById(R.id.account_password_repeat_layout);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    emailLayout.setError(null);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
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
        passwordRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    passwordRepeatLayout.setError(null);
                }
            }
        });

        img = "";
        banner = "";
        ownpi = "0";

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createprofile)));
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
                        if (!email.getText().toString().isEmpty()) {
                            if (!password.getText().toString().trim().isEmpty()) {
                                if(password.getText().toString().trim().length()>=6)
                                    if (!passwordRepeat.getText().toString().trim().isEmpty()) {
                                        if (password.getText().toString().trim().equals(passwordRepeat.getText().toString().trim())) {
                                            createAccountData(email.getText().toString().trim(), password.getText().toString().trim());
                                            alert.cancel();
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.passwordsdontmatch, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        passwordRepeatLayout.setError(getResources().getString(R.string.repeatpassword));
                                    }
                                else {
                                    passwordLayout.setError(getResources().getString(R.string.passwordmustcontainatleastsixcharacters));
                                }
                            } else {
                                passwordLayout.setError(getResources().getString(R.string.enterpassword));
                            }
                        } else {
                            emailLayout.setError(getResources().getString(R.string.enteremail));
                        }
                    }
                });
            }
        });

        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void createAccountAuth(final String email, final String password, final String name, final String description, final String location, final String birthday) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();

                            if (ownpi.equals("0")) {
                                String imguuid = UUID.randomUUID().toString();
                                img = imguuid;
                            }

                            DatabaseReference user_root = userRoot.child(userID);
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("profileDescription", description);
                            map.put("location", location);
                            map.put("birthday", birthday.substring(6, 10) + birthday.substring(3, 5) + birthday.substring(0, 2));
                            map.put("favColour", String.valueOf(colour));
                            map.put("img", img);
                            map.put("banner", banner);
                            if (ownpi.equals("0")) {
                                map.put("ownpi", "0");
                            } else {
                                map.put("ownpi", "1");
                            }
                            user_root.updateChildren(map);
                            Toast.makeText(getApplicationContext(), R.string.profilecreated, Toast.LENGTH_SHORT).show();

                            if (!ownpi.equals("1")) {
                                storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
                                TextDrawable drawable = TextDrawable.builder()
                                        .beginConfig()
                                        .bold()
                                        .endConfig()
                                        .buildRect(name.substring(0, 1), getResources().getIntArray(R.array.favcolors)[colour]);
                                Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                drawable.draw(canvas);

                                byte[] byteArray;
                                final StorageReference pathReference_image = storageRef.child("profile_images/" + img);
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

                            user.sendEmailVerification();
                            login(email, password);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.profilecreationfailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createAccountData(final String email, final String password) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.edit_profile, null);

        final EditText usernameEdit = view.findViewById(R.id.user_name);
        final EditText profileDescriptionEdit = view.findViewById(R.id.user_bio);
        final EditText birthdayEdit = view.findViewById(R.id.user_birthday);
        final EditText locationEdit = view.findViewById(R.id.user_location);

        final TextInputLayout usernameLayout = view.findViewById(R.id.user_name_layout);
        final TextInputLayout profileDescriptionLayout = view.findViewById(R.id.user_bio_layout);
        final TextInputLayout locationLayout = view.findViewById(R.id.user_location_layout);

        usernameEdit.addTextChangedListener(new TextWatcher() {
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
        profileDescriptionEdit.addTextChangedListener(new TextWatcher() {
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
        locationEdit.addTextChangedListener(new TextWatcher() {
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
        profileImage = view.findViewById(R.id.user_profile_image);
        profileBanner = view.findViewById(R.id.user_profile_banner);

        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference pathReference_image = storageRef.child("profile_images/" + img);
        final StorageReference pathReference_banner = storageRef.child("profile_banners/" + banner);

        birthdayEdit.setText(birthday);

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImage);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_banner)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBanner);
        } else {
            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_image)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileImage);

            GlideApp.with(getApplicationContext())
                    //.using(new FirebaseImageLoader())
                    .load(pathReference_banner)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBanner);
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 0);
            }
        });

        profileBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
            }
        });

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(getResources().getIntArray(R.array.favcolors)[colour]);
        favColour.setBackground(shape);

        birthdayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date;
                        if (dayOfMonth < 10) {
                            date = "0" + dayOfMonth;
                        } else {
                            date = "" + dayOfMonth;
                        }
                        monthOfYear = monthOfYear + 1;
                        if (monthOfYear < 10) {
                            date = date + ".0" + monthOfYear + "." + year;
                        } else {
                            date = date + "." + monthOfYear + "." + year;
                        }

                        birthdayEdit.setText(date);
                    }
                }, Integer.parseInt(birthday.substring(6, 10)), Integer.parseInt(birthday.substring(3, 5)) - 1, Integer.parseInt(birthday.substring(0, 2)));
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
                builder.setColors(R.array.favcolors).setTitle(R.string.chooseacolor).setSelectedColor(getResources().getIntArray(R.array.favcolors)[colour]).setFixedColumnCount(5).setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int scolor) {
                        if (positiveResult) {
                            int i = 0;
                            for (int c : getResources().getIntArray(R.array.favcolors)) {
                                if (c == scolor) {
                                    colour = i;
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

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createprofile)));
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
                userID = "";
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
                        if (!usernameEdit.getText().toString().isEmpty()) {
                            if (!profileDescriptionEdit.getText().toString().isEmpty()) {
                                if (!locationEdit.getText().toString().isEmpty()) {
                                    if (!birthdayEdit.getText().toString().isEmpty()) {
                                        String username = usernameEdit.getText().toString();
                                        String profileDescription = profileDescriptionEdit.getText().toString();
                                        String location = locationEdit.getText().toString();
                                        String birthday = birthdayEdit.getText().toString();

                                        createAccountAuth(email, password, username, profileDescription, location, birthday);

                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }

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

    private void changeTheme(Theme theme) {
        this.theme = theme;
        if (theme == Theme.DARK) {
            setTheme(R.style.SplashDark);
        } else {
            setTheme(R.style.Splash);
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
            img = UUID.randomUUID().toString();
            ref = storageRef.child("profile_images/" + img);
        } else {
            banner = UUID.randomUUID().toString();
            ref = storageRef.child("profile_banners/" + banner);
        }

        byte[] byteArray;
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        if (bmp.getWidth() < bmp.getHeight() && type == 0) {
            bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/2, bmp.getWidth(), bmp.getWidth());
        } else if (bmp.getWidth() > bmp.getHeight() && type == 0) {
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
        if (type == 0) {
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
                    ownpi = "1";
                    DatabaseReference user_root = userRoot.child(userID);
                    Map<String, Object> map = new HashMap<>();
                    map.put("ownpi", "1");
                    user_root.updateChildren(map);
                }
                updateEditProfileImages();
                Toast.makeText(LoginActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
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

    private void updateEditProfileImages() {
        storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference pathReference_image = storageRef.child("profile_images/" + img);
        pathReference_image.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                GlideApp.with(getApplicationContext())
                        //.using(new FirebaseImageLoader())
                        .load(pathReference_image)
                        .signature(new ObjectKey(String.valueOf(storageMetadata.getCreationTimeMillis())))
                        .centerCrop()
                        .into(profileImage);
            }
        });
        StorageReference pathReference_banner = storageRef.child("profile_banners/" + banner);
        pathReference_banner.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                GlideApp.with(getApplicationContext())
                        //.using(new FirebaseImageLoader())
                        .load(pathReference_banner)
                        .signature(new ObjectKey(String.valueOf(storageMetadata.getCreationTimeMillis())))
                        .centerCrop()
                        .thumbnail(0.05f)
                        .into(profileBanner);
            }
        });
    }
}
