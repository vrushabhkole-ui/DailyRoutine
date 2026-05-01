package com.example.dailyroutine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class WaterActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_WATER_AMOUNT = "waterAmount";
    private float currentWater = 0.0f;
    private TextView tvWaterAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);

        Toolbar toolbar = findViewById(R.id.toolbarWater);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Water Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        MaterialButton btnAddWater = findViewById(R.id.btnAddWater);
        MaterialButton btnReset = findViewById(R.id.btnResetWater);

        loadWaterData();

        btnAddWater.setOnClickListener(v -> {
            currentWater += 0.25f;
            updateWaterUI();
            saveWaterData();
            Toast.makeText(this, "Added 250ml!", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            currentWater = 0.0f;
            updateWaterUI();
            saveWaterData();
            Toast.makeText(this, "Reset successful", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadWaterData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentWater = prefs.getFloat(KEY_WATER_AMOUNT, 0.0f);
        updateWaterUI();
    }

    private void saveWaterData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_WATER_AMOUNT, currentWater);
        editor.apply();
    }

    private void updateWaterUI() {
        tvWaterAmount.setText(String.format("%.2f", currentWater));
    }
}