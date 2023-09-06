package com.yannick.mychatapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.ImageOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.StringOperations;
import com.yannick.mychatapp.data.Theme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Theme theme;
    private FirebaseStorage storage;
    private static String userID = "";
    private static final String DEFAULT_BIRTHDAY = "01.01.2000";
    private static boolean ownProfileImage = false;
    private static int colour = 0;
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child("users");

    private String img = "";
    private String banner = "";

    private ImageButton profileImageButton;
    private ImageButton profileBannerButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.activity_login);

        MaterialButton loginButton = findViewById(R.id.loginbutton);
        MaterialButton createButton = findViewById(R.id.createbutton);
        EditText inputEmail = findViewById(R.id.login_email);
        EditText inputPassword = findViewById(R.id.login_password);
        TextInputLayout inputEmailLayout = findViewById(R.id.login_email_layout);
        TextInputLayout inputPasswordLayout = findViewById(R.id.login_password_layout);

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

        loginButton.setOnClickListener(view -> {
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
        });

        createButton.setOnClickListener(view -> createAccount());
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
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
        builder.setPositiveButton(R.string.cancel, (dialogInterface, i) -> mAuth.signOut());

        final AlertDialog alert = builder.create();

        resendEmailButton.setOnClickListener(view1 -> {
            resendEmail();
            alert.cancel();
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
        ownProfileImage = false;

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createprofile)));
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
                if (!email.getText().toString().isEmpty()) {
                    if (!password.getText().toString().trim().isEmpty()) {
                        if (password.getText().toString().trim().length()>=6)
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
            });
        });

        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void createAccountAuth(final String email, final String password, final String name, final String description, final String location, final String birthday) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        userID = user.getUid();

                        if (!ownProfileImage) {
                            img = UUID.randomUUID().toString();
                        }

                        DatabaseReference newUserRoot = userRoot.child(userID);
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("profileDescription", description);
                        map.put("location", location);
                        map.put("birthday", birthday.substring(6, 10) + birthday.substring(3, 5) + birthday.substring(0, 2));
                        map.put("favColour", colour);
                        map.put("img", img);
                        map.put("banner", banner);
                        map.put("ownProfileImage", ownProfileImage);
                        newUserRoot.updateChildren(map);
                        Toast.makeText(getApplicationContext(), R.string.profilecreated, Toast.LENGTH_SHORT).show();

                        if (!ownProfileImage) {
                            StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
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
                            final StorageReference refProfileImage = storageRef.child("profile_images/" + img);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byteArray = stream.toByteArray();
                            try {
                                stream.close();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                            refProfileImage.putBytes(byteArray);
                        }

                        user.sendEmailVerification();
                        login(email, password);
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.profilecreationfailed, Toast.LENGTH_SHORT).show();
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
        profileImageButton = view.findViewById(R.id.user_profile_image);
        profileBannerButton = view.findViewById(R.id.user_profile_banner);

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        final StorageReference refProfileImage = storageRef.child("profile_images/" + img);
        final StorageReference refProfileBanner = storageRef.child("profile_banners/" + banner);

        birthdayEdit.setText(DEFAULT_BIRTHDAY);

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

        profileImageButton.setOnClickListener(view15 -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), ImageOperations.PICK_PROFILE_IMAGE_REQUEST);
        });

        profileBannerButton.setOnClickListener(view16 -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), ImageOperations.PICK_PROFILE_BANNER_REQUEST);
        });

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(getResources().getIntArray(R.array.favcolors)[colour]);
        favColour.setBackground(shape);

        birthdayEdit.setOnClickListener(view14 -> {
            DatePickerDialog datePicker = new DatePickerDialog(view14.getContext(), (view141, year, monthOfYear, dayOfMonth) -> {
                String date = StringOperations.buildDate(year, monthOfYear, dayOfMonth);
                birthdayEdit.setText(date);
            }, StringOperations.getYear(DEFAULT_BIRTHDAY), StringOperations.getMonth(DEFAULT_BIRTHDAY), StringOperations.getDay(DEFAULT_BIRTHDAY));
            if (theme == Theme.DARK) {
                datePicker.getWindow().setBackgroundDrawableResource(R.color.dark_background);
            }
            Calendar c = Calendar.getInstance();
            c.set(2004, 11, 31);
            datePicker.getDatePicker().setMaxDate(c.getTimeInMillis());
            datePicker.show();
        });

        favColour.setOnClickListener(view13 -> {
            SpectrumDialog.Builder builder;
            if (theme == Theme.DARK) {
                builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialogDark);
            } else {
                builder = new SpectrumDialog.Builder(getApplicationContext(), R.style.AlertDialog);
            }
            builder.setColors(R.array.favcolors).setTitle(R.string.chooseacolor).setSelectedColor(getResources().getIntArray(R.array.favcolors)[colour]).setFixedColumnCount(5).setOnColorSelectedListener((positiveResult, scolor) -> {
                if (positiveResult) {
                    int i = 0;
                    for (int c : getResources().getIntArray(R.array.favcolors)) {
                        if (c == scolor) {
                            colour = i;
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

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.createprofile)));
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
            userID = "";
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {

            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view12 -> {
                if (!usernameEdit.getText().toString().isEmpty()) {
                    if (!profileDescriptionEdit.getText().toString().isEmpty()) {
                        if (!locationEdit.getText().toString().isEmpty()) {
                            if (!birthdayEdit.getText().toString().isEmpty()) {
                                String username = usernameEdit.getText().toString();
                                String profileDescription = profileDescriptionEdit.getText().toString();
                                String location = locationEdit.getText().toString();
                                String birthday = birthdayEdit.getText().toString();

                                createAccountAuth(email, password, username, profileDescription, location, birthday);

                                if (view12 != null) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
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
            });
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
        if (resultCode == RESULT_OK && data != null) {
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

        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());
        StorageReference ref;
        if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
            img = UUID.randomUUID().toString();
            ref = storageRef.child("profile_images/" + img);
        } else {
            banner = UUID.randomUUID().toString();
            ref = storageRef.child("profile_banners/" + banner);
        }

        ImageOperations imageOperations = new ImageOperations(getContentResolver());
        byte[] byteArray = imageOperations.getImageAsBytes(this, filePath, type);

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
                ownProfileImage = true;
                DatabaseReference newUserRoot = userRoot.child(userID);
                Map<String, Object> map = new HashMap<>();
                map.put("ownProfileImage", ownProfileImage);
                newUserRoot.updateChildren(map);
            }
            updateEditProfileImages();
            Toast.makeText(LoginActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("Upload failed", e.toString());
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, R.string.imagetoolarge, Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                    .getTotalByteCount());
            progressDialog.setMessage((int)progress+"% " + getResources().getString(R.string.uploaded));
        });
    }

    private void updateEditProfileImages() {
        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseStorage.getInstance().getReference().toString());

        StorageReference refProfileImage = storageRef.child("profile_images/" + img);
        refProfileImage.getMetadata().addOnSuccessListener(storageMetadata -> GlideApp.with(getApplicationContext())
                //.using(new FirebaseImageLoader())
                .load(refProfileImage)
                .signature(new ObjectKey(String.valueOf(storageMetadata.getCreationTimeMillis())))
                .centerCrop()
                .into(profileImageButton));

        StorageReference refProfileBanner = storageRef.child("profile_banners/" + banner);
        refProfileBanner.getMetadata().addOnSuccessListener(storageMetadata -> GlideApp.with(getApplicationContext())
                //.using(new FirebaseImageLoader())
                .load(refProfileBanner)
                .signature(new ObjectKey(String.valueOf(storageMetadata.getCreationTimeMillis())))
                .centerCrop()
                .thumbnail(0.05f)
                .into(profileBannerButton));
    }
}
