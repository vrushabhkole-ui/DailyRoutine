package com.example.dailyroutine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class SleepActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_SLEEP_HOURS = "sleepDuration";
    private float currentSleep = 0.0f;
    private TextView tvSleepDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        Toolbar toolbar = findViewById(R.id.toolbarSleep);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sleep Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSleepDuration = findViewById(R.id.tvSleepDuration);
        MaterialButton btnLogSleep = findViewById(R.id.btnLogSleep);
        MaterialButton btnReset = findViewById(R.id.btnResetSleep);

        loadSleepData();

        btnLogSleep.setOnClickListener(v -> {
            currentSleep += 1.0f;
            updateSleepUI();
            saveSleepData();
            Toast.makeText(this, "Logged 1 hour of sleep!", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            currentSleep = 0.0f;
            updateSleepUI();
            saveSleepData();
            Toast.makeText(this, "Reset successful", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSleepData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentSleep = prefs.getFloat(KEY_SLEEP_HOURS, 0.0f);
        updateSleepUI();
    }

    private void saveSleepData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_SLEEP_HOURS, currentSleep);
        editor.apply();
    }

    private void updateSleepUI() {
        tvSleepDuration.setText(String.format("%.1f", currentSleep));
    }
}