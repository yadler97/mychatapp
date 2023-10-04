package com.yannick.mychatapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.GlideApp;
import com.yannick.mychatapp.ImageOperations;
import com.yannick.mychatapp.R;
import com.yannick.mychatapp.StringOperations;
import com.yannick.mychatapp.TextWatcher;
import com.yannick.mychatapp.data.Theme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Theme theme;
    private FirebaseStorage storage;
    private static boolean ownProfileImage = false;
    private static int colour = 0;
    private final DatabaseReference userRoot = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.usersDatabaseKey);

    private String image = "";
    private String banner = "";

    private CircleImageView profileImageButton;
    private ImageButton profileBannerButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme(Theme.getCurrentTheme(this));
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.loginbutton);
        Button createButton = findViewById(R.id.createbutton);
        Button resetButton = findViewById(R.id.forgotpasswordbutton);
        EditText inputEmail = findViewById(R.id.login_email);
        EditText inputPassword = findViewById(R.id.login_password);
        TextInputLayout inputEmailLayout = findViewById(R.id.login_email_layout);
        TextInputLayout inputPasswordLayout = findViewById(R.id.login_password_layout);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        inputEmail.addTextChangedListener(new TextWatcher(inputEmailLayout));
        inputPassword.addTextChangedListener(new TextWatcher(inputPasswordLayout));

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

        resetButton.setOnClickListener(view -> openForgotPasswordDialog());
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

        final Button resendEmailButton = view.findViewById(R.id.resendemailbutton);

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.pleaseverifyemail)));
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.close, (dialogInterface, i) -> mAuth.signOut());

        final AlertDialog alert = builder.create();

        resendEmailButton.setOnClickListener(buttonView -> {
            resendEmail();
            alert.cancel();
        });

        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    private void openForgotPasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.enter_email, null);

        final TextView email = view.findViewById(R.id.account_email);
        final TextInputLayout emailLayout = view.findViewById(R.id.account_email_layout);

        email.addTextChangedListener(new TextWatcher(emailLayout));

        AlertDialog.Builder builder;
        if (theme == Theme.DARK) {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
        }

        builder.setCustomTitle(setupHeader(getResources().getString(R.string.forgot_password)));
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {});
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            View currentFocus = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            dialogInterface.cancel();
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(buttonView -> {
                if (!email.getText().toString().isEmpty()) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
                        mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, R.string.password_reset_mail_sent, Toast.LENGTH_SHORT).show();
                                alert.cancel();
                            }
                        });
                    } else {
                        emailLayout.setError(getResources().getString(R.string.invalid_email));
                    }
                } else {
                    emailLayout.setError(getResources().getString(R.string.enteremail));
                }
            });
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

        email.addTextChangedListener(new TextWatcher(emailLayout));
        password.addTextChangedListener(new TextWatcher(passwordLayout));
        passwordRepeat.addTextChangedListener(new TextWatcher(passwordRepeatLayout));

        image = "";
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
            View currentFocus = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            dialogInterface.cancel();
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(buttonView -> {
                if (!email.getText().toString().isEmpty()) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
                        if (!password.getText().toString().trim().isEmpty()) {
                            if (password.getText().toString().trim().length() >= 6)
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
                        emailLayout.setError(getResources().getString(R.string.invalid_email));
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
                        String userID = user.getUid();

                        if (!ownProfileImage) {
                            image = UUID.randomUUID().toString();
                        }

                        DatabaseReference newUserRoot = userRoot.child(userID);
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("description", description);
                        map.put("location", location);
                        map.put("birthday", StringOperations.convertDateToDatabaseFormat(birthday));
                        map.put("favColour", colour);
                        map.put("image", image);
                        map.put("banner", banner);
                        map.put("ownProfileImage", ownProfileImage);
                        newUserRoot.updateChildren(map);
                        Toast.makeText(getApplicationContext(), R.string.profilecreated, Toast.LENGTH_SHORT).show();

                        if (!ownProfileImage) {
                            Bitmap bitmap = ImageOperations.generateStandardProfileImage(this, name, colour);

                            byte[] byteArray;
                            final StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + image);
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

    @SuppressLint("ClickableViewAccessibility")
    private void createAccountData(final String email, final String password) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.edit_profile, null);

        final EditText usernameEdit = view.findViewById(R.id.user_name);
        final EditText profileDescriptionEdit = view.findViewById(R.id.user_bio);
        final EditText birthdayEdit = view.findViewById(R.id.user_birthday);
        final EditText locationEdit = view.findViewById(R.id.user_location);

        final TextInputLayout usernameLayout = view.findViewById(R.id.user_name_layout);
        final TextInputLayout locationLayout = view.findViewById(R.id.user_location_layout);

        final ImageButton removeProfileImage = view.findViewById(R.id.user_profile_image_remove);
        final ImageButton removeProfileBanner = view.findViewById(R.id.user_profile_banner_remove);

        usernameEdit.addTextChangedListener(new TextWatcher(usernameLayout));
        locationEdit.addTextChangedListener(new TextWatcher(locationLayout));

        final ImageButton favColour = view.findViewById(R.id.user_favcolor);
        profileImageButton = view.findViewById(R.id.user_profile_image);
        profileBannerButton = view.findViewById(R.id.user_profile_banner);

        final StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + "unknown_user");
        final StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + banner);

        birthdayEdit.setText(Constants.DEFAULT_BIRTHDAY);

        GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImageButton);

        if (theme == Theme.DARK) {
            GlideApp.with(getApplicationContext())
                    .load(refProfileBanner)
                    .placeholder(R.drawable.side_nav_bar_dark)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        } else {
            GlideApp.with(getApplicationContext())
                    .load(refProfileBanner)
                    .placeholder(R.drawable.side_nav_bar)
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                    .centerCrop()
                    .into(profileBannerButton);
        }

        profileImageButton.setOnTouchListener((profileImageView, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                profileImageButton.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.image_overlay_profile, null));
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (action == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    pickProfileImageLauncher.launch(intent);
                }

                profileImageButton.setForeground(null);
            }

            return true;
        });

        profileBannerButton.setOnTouchListener((profileBannerView, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                profileBannerButton.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.image_overlay, null));
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (action == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    pickProfileBannerLauncher.launch(intent);
                }

                profileBannerButton.setForeground(null);
            }

            return true;
        });

        removeProfileImage.setOnTouchListener((removeImageView, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                removeProfileImage.setBackgroundResource(R.drawable.icon_clear_pressed);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (action == MotionEvent.ACTION_UP) {
                    image = "";
                    ownProfileImage = false;
                    updateEditProfileImages();
                }

                removeProfileImage.setBackgroundResource(R.drawable.icon_clear);
            }

            return true;
        });

        removeProfileBanner.setOnTouchListener((removeImageView, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                removeProfileBanner.setBackgroundResource(R.drawable.icon_clear_pressed);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (action == MotionEvent.ACTION_UP) {
                    banner = "";
                    updateEditProfileImages();
                }

                removeProfileBanner.setBackgroundResource(R.drawable.icon_clear);
            }

            return true;
        });

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(getResources().getIntArray(R.array.favcolors)[colour]);
        favColour.setBackground(shape);

        AtomicReference<String> selectedBirthday = new AtomicReference<>(Constants.DEFAULT_BIRTHDAY);

        birthdayEdit.setOnClickListener(birthdayEditView -> {
            DatePickerDialog datePicker = new DatePickerDialog(birthdayEditView.getContext(), (datePickerView, year, monthOfYear, dayOfMonth) -> {
                String date = StringOperations.buildDate(year, monthOfYear, dayOfMonth);
                selectedBirthday.set(date);
                birthdayEdit.setText(date);
            }, StringOperations.getYear(selectedBirthday.toString()), StringOperations.getMonth(selectedBirthday.toString()), StringOperations.getDay(selectedBirthday.toString()));
            if (theme == Theme.DARK) {
                datePicker.getWindow().setBackgroundDrawableResource(R.color.dark_background);
            }
            datePicker.getDatePicker().setMaxDate(calculateMinBirthday());
            datePicker.show();
        });

        favColour.setOnClickListener(favColourEditView -> {
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
            View currentFocus = ((AlertDialog) dialogInterface).getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            dialogInterface.cancel();
        });

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(buttonView -> {
                if (!usernameEdit.getText().toString().isEmpty()) {
                    if (!locationEdit.getText().toString().isEmpty()) {
                        if (!birthdayEdit.getText().toString().isEmpty()) {
                            String username = usernameEdit.getText().toString();
                            String description = profileDescriptionEdit.getText().toString();
                            String location = locationEdit.getText().toString();
                            String birthday = birthdayEdit.getText().toString();

                            createAccountAuth(email, password, username, description, location, birthday);

                            if (buttonView != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(buttonView.getWindowToken(), 0);
                            }

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

    private void uploadImage(Uri filePath, final int type) {
        final ProgressDialog progressDialog;
        if (theme == Theme.DARK) {
            progressDialog = new ProgressDialog(new ContextThemeWrapper(this, R.style.AlertDialogDark));
        } else {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setTitle(R.string.upload);
        progressDialog.show();

        StorageReference ref;
        if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
            image = UUID.randomUUID().toString();
            ref = storage.getReference().child(Constants.profileImagesStorageKey + image);
        } else {
            banner = UUID.randomUUID().toString();
            ref = storage.getReference().child(Constants.profileBannersStorageKey + banner);
        }

        ImageOperations imageOperations = new ImageOperations(getContentResolver());
        byte[] byteArray = imageOperations.getImageAsBytes(this, filePath, type);

        UploadTask uploadTask = ref.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            if (type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST) {
                ownProfileImage = true;
            }
            updateEditProfileImages();
            Toast.makeText(LoginActivity.this, R.string.imageuploaded, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("Upload failed", e.toString());
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, R.string.image_upload_failed, Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage((int)progress + "% " + getResources().getString(R.string.uploaded));
        });
    }

    private void updateEditProfileImages() {
        StorageReference refProfileImage = storage.getReference().child(Constants.profileImagesStorageKey + image);
        refProfileImage.getMetadata().addOnSuccessListener(storageMetadata -> GlideApp.with(getApplicationContext())
                .load(refProfileImage)
                .centerCrop()
                .into(profileImageButton));

        StorageReference refProfileBanner = storage.getReference().child(Constants.profileBannersStorageKey + banner);
        refProfileBanner.getMetadata().addOnSuccessListener(storageMetadata -> GlideApp.with(getApplicationContext())
                .load(refProfileBanner)
                .centerCrop()
                .thumbnail(0.05f)
                .into(profileBannerButton));
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

    public long calculateMinBirthday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -14);
        return cal.getTimeInMillis();
    }
}
