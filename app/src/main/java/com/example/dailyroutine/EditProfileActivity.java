package com.example.dailyroutine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ADDRESS = "userAddress";
    private static final String KEY_USER_IMAGE = "userImageUri";

    private ImageView ivProfileImage;
    private TextInputEditText etName, etAddress;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivProfileImage = findViewById(R.id.ivEditProfileImage);
        etName = findViewById(R.id.etProfileName);
        etAddress = findViewById(R.id.etProfileAddress);
        MaterialCardView cardImage = findViewById(R.id.cardProfileImage);
        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);

        loadProfileData();

        cardImage.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfileImage.setImageURI(selectedImageUri);
            ivProfileImage.setColorFilter(0); // Clear tint
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etName.setText(prefs.getString(KEY_USER_NAME, "User"));
        etAddress.setText(prefs.getString(KEY_USER_ADDRESS, ""));
        String imageUriStr = prefs.getString(KEY_USER_IMAGE, null);
        if (imageUriStr != null) {
            selectedImageUri = Uri.parse(imageUriStr);
            ivProfileImage.setImageURI(selectedImageUri);
            ivProfileImage.setColorFilter(0);
        }
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ADDRESS, address);
        if (selectedImageUri != null) {
            editor.putString(KEY_USER_IMAGE, selectedImageUri.toString());
        }
        editor.apply();

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}