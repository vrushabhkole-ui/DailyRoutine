package com.example.dailyroutine;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dailyroutine.database.AppDatabase;
import com.example.dailyroutine.database.Task;
import com.example.dailyroutine.database.Habit;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "DailyRoutinePrefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_IMAGE = "userImageUri";
    private static final String KEY_WATER_AMOUNT = "waterAmount";
    private static final String KEY_SLEEP_HOURS = "sleepDuration";
    
    private TextView tvUserName, tvTaskCount, tvHabitCount, tvWaterCount, tvSleepCount;
    private TextView tvUpcomingTaskTitle, tvUpcomingTaskTime, tvProgressSubtitle;
    private LinearProgressIndicator progressDaily;
    private CheckBox cbUpcomingTask;
    private ImageView ivAppLogo;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Task currentUpcomingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySettings();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        db = AppDatabase.getInstance(this);
        checkPermissions();
    }

    private void applySettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission is required for alarms.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        tvHabitCount = findViewById(R.id.tvHabitCount);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        tvSleepCount = findViewById(R.id.tvSleepCount);
        tvUpcomingTaskTitle = findViewById(R.id.tvUpcomingTaskTitle);
        tvUpcomingTaskTime = findViewById(R.id.tvUpcomingTaskTime);
        tvProgressSubtitle = findViewById(R.id.tvProgressSubtitle);
        progressDaily = findViewById(R.id.progressDaily);
        cbUpcomingTask = findViewById(R.id.cbUpcomingTask);
        ivAppLogo = findViewById(R.id.ivAppLogo);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        findViewById(R.id.cardTasks).setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));
        findViewById(R.id.cardHabits).setOnClickListener(v -> startActivity(new Intent(this, HabitActivity.class)));
        findViewById(R.id.cardWater).setOnClickListener(v -> startActivity(new Intent(this, WaterActivity.class)));
        findViewById(R.id.cardSleep).setOnClickListener(v -> startActivity(new Intent(this, SleepActivity.class)));
        
        findViewById(R.id.fabAdd).setOnClickListener(v -> startActivity(new Intent(this, AddRoutineActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        cbUpcomingTask.setOnClickListener(v -> {
            if (currentUpcomingTask != null) {
                executorService.execute(() -> {
                    currentUpcomingTask.isCompleted = cbUpcomingTask.isChecked();
                    db.appDao().updateTask(currentUpcomingTask);
                    loadDashboardData();
                });
            }
        });
    }

    private void loadDashboardData() {
        executorService.execute(() -> {
            List<Task> tasks = db.appDao().getAllTasks();
            List<Habit> habits = db.appDao().getAllHabits();
            
            int totalTasksCount = tasks.size();
            int completedTasksCount = 0;
            for (Task t : tasks) {
                if (t.isCompleted) completedTasksCount++;
            }
            final int finalCompletedTasks = completedTasksCount;
            final int finalPendingTasks = totalTasksCount - completedTasksCount;

            int totalHabitsCount = habits.size();
            int completedHabitsCount = 0;
            for (Habit h : habits) {
                if (h.isCompletedToday) completedHabitsCount++;
            }
            final int finalTotalHabits = totalHabitsCount;

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            float waterVal = prefs.getFloat(KEY_WATER_AMOUNT, 0.0f);
            float sleepVal = prefs.getFloat(KEY_SLEEP_HOURS, 0.0f);

            int totalItems = totalTasksCount + totalHabitsCount;
            int totalCompleted = completedTasksCount + completedHabitsCount;
            final int finalProgress = (totalItems > 0) ? (totalCompleted * 100 / totalItems) : 0;
            
            currentUpcomingTask = null;
            for (Task t : tasks) {
                if (!t.isCompleted) {
                    currentUpcomingTask = t;
                    break;
                }
            }

            String userName = prefs.getString(KEY_USER_NAME, "User");
            String userImageUri = prefs.getString(KEY_USER_IMAGE, null);

            runOnUiThread(() -> {
                tvUserName.setText(userName);
                if (userImageUri != null) {
                    ivAppLogo.setImageURI(Uri.parse(userImageUri));
                    ivAppLogo.setColorFilter(0);
                } else {
                    ivAppLogo.setImageResource(R.drawable.ic_app_logo);
                    ivAppLogo.setColorFilter(ContextCompat.getColor(this, R.color.primary));
                }

                tvTaskCount.setText(finalPendingTasks + " Pending / " + finalCompletedTasks + " Done");
                tvHabitCount.setText(finalTotalHabits + " Active");
                tvWaterCount.setText(String.format("%.2fL / 2L", waterVal));
                tvSleepCount.setText(String.format("%.1fh Goal", sleepVal));
                
                tvProgressSubtitle.setText(finalProgress + "% of your goals completed");
                progressDaily.setProgress(finalProgress);

                if (currentUpcomingTask != null) {
                    tvUpcomingTaskTitle.setText(currentUpcomingTask.title);
                    tvUpcomingTaskTime.setText("Today");
                    cbUpcomingTask.setChecked(false);
                    cbUpcomingTask.setEnabled(true);
                } else {
                    if (totalTasksCount > 0 && finalPendingTasks == 0) {
                        tvUpcomingTaskTitle.setText("All tasks caught up!");
                    } else {
                        tvUpcomingTaskTitle.setText("No tasks added yet");
                    }
                    tvUpcomingTaskTime.setText("--");
                    cbUpcomingTask.setChecked(true);
                    cbUpcomingTask.setEnabled(false);
                }
            });
        });
    }
}