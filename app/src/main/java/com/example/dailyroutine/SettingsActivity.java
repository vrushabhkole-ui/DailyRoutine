package com.example.dailyroutine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.dailyroutine.database.AppDatabase;
import com.example.dailyroutine.database.Task;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_IMAGE = "userImageUri";
    private static final int CREATE_FILE_REQUEST_CODE = 2;

    private TextView tvUserName;
    private ImageView ivProfileImage;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = AppDatabase.getInstance(this);

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
        View cardAppInfo = findViewById(R.id.cardAppInfo);
        View cardDownloadPdf = findViewById(R.id.cardDownloadPdf);

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

        cardAppInfo.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AppInfoActivity.class));
        });

        cardDownloadPdf.setOnClickListener(v -> openFilePicker());

        findViewById(R.id.cardAbout).setOnClickListener(v -> 
            Toast.makeText(this, "Daily Routine App v1.0.0", Toast.LENGTH_SHORT).show()
        );
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "RoutineReport_" + timeStamp + ".pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                generateRoutinePdf(data.getData());
            }
        }
    }

    private void generateRoutinePdf(Uri uri) {
        executorService.execute(() -> {
            List<Task> tasks = db.appDao().getAllTasks();
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(20);
            paint.setFakeBoldText(true);

            canvas.drawText("Weekly Routine Report", 40, 50, paint);
            
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            int y = 100;

            for (Task task : tasks) {
                if (y > 800) break;
                String status = task.isCompleted ? "[Done]" : "[Pending]";
                String time = task.startTime > 0 ? sdf.format(new Date(task.startTime)) : "No Time";
                canvas.drawText(status + " " + task.title + " - " + time, 40, y, paint);
                y += 25;
            }

            document.finishPage(page);

            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                 FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())) {
                document.writeTo(fos);
                runOnUiThread(() -> Toast.makeText(this, "PDF saved to your selected location!", Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show());
            } finally {
                document.close();
            }
        });
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