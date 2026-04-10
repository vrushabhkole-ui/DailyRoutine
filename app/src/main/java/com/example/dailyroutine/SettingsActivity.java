package com.example.dailyroutine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_IMAGE = "userImageUri";

    private TextView tvUserName;
    private ImageView ivProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvSettingsUserName);
        ivProfileImage = findViewById(R.id.ivSettingsProfileImage);
        View profileSection = findViewById(R.id.profileSection);

        MaterialSwitch switchDarkMode = findViewById(R.id.switchDarkMode);
        MaterialSwitch switchNotifications = findViewById(R.id.switchNotifications);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });

        profileSection.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.cardAbout).setOnClickListener(v -> 
            Toast.makeText(this, "Daily Routine App v1.0.0", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "User Name");
        tvUserName.setText(name);

        String imageUriStr = prefs.getString(KEY_USER_IMAGE, null);
        if (imageUriStr != null && ivProfileImage != null) {
            try {
                ivProfileImage.setImageURI(Uri.parse(imageUriStr));
                ivProfileImage.setColorFilter(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}