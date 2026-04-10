package com.example.dailyroutine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyroutine.database.AppDatabase;
import com.example.dailyroutine.database.Habit;
import com.example.dailyroutine.database.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddRoutineActivity extends AppCompatActivity {

    private static final int RINGTONE_PICKER_REQUEST = 100;

    private EditText etRoutineName;
    private ChipGroup chipGroupCategory, chipGroupDays;
    private MaterialButton btnSaveRoutine, btnSelectTime, btnSelectTone;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int selectedHour = -1, selectedMinute = -1;
    private String selectedToneUri = "";
    private int taskId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_routine);

        db = AppDatabase.getInstance(this);

        etRoutineName = findViewById(R.id.etRoutineName);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupDays = findViewById(R.id.chipGroupDays);
        btnSaveRoutine = findViewById(R.id.btnSaveRoutine);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSelectTone = findViewById(R.id.btnSelectTone);

        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSelectTone.setOnClickListener(v -> showTonePicker());
        btnSaveRoutine.setOnClickListener(v -> saveRoutine());

        checkAlarmPermission();
        checkIntentData();
    }

    private void checkIntentData() {
        if (getIntent().hasExtra("task_id")) {
            taskId = getIntent().getIntExtra("task_id", -1);
            isEditMode = true;
            btnSaveRoutine.setText("Update Routine");
            loadTaskData();
        }
    }

    private void loadTaskData() {
        executorService.execute(() -> {
            Task task = null;
            List<Task> allTasks = db.appDao().getAllTasks();
            for(Task t : allTasks) {
                if(t.id == taskId) {
                    task = t;
                    break;
                }
            }

            if (task != null) {
                Task finalTask = task;
                runOnUiThread(() -> {
                    etRoutineName.setText(finalTask.title);
                    chipGroupCategory.check(R.id.chipTask);
                    chipGroupCategory.setEnabled(false); // Can't change category on edit
                    
                    if (finalTask.dueDate > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(finalTask.dueDate);
                        selectedHour = cal.get(Calendar.HOUR_OF_DAY);
                        selectedMinute = cal.get(Calendar.MINUTE);
                        btnSelectTime.setText(String.format("Time: %02d:%02d", selectedHour, selectedMinute));
                    }
                    
                    selectedToneUri = finalTask.alarmToneUri;
                    if (selectedToneUri != null && !selectedToneUri.isEmpty()) {
                        btnSelectTone.setText("Tone Selected");
                    }

                    // Pre-select days
                    if (finalTask.daysOfWeek != null) {
                        String[] days = finalTask.daysOfWeek.split(",");
                        for (String day : days) {
                            for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
                                Chip chip = (Chip) chipGroupDays.getChildAt(i);
                                if (chip.getText().toString().equals(day)) {
                                    chip.setChecked(true);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        int hour = selectedHour != -1 ? selectedHour : c.get(Calendar.HOUR_OF_DAY);
        int minute = selectedMinute != -1 ? selectedMinute : c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minuteOfHour;
                    btnSelectTime.setText(String.format("Time: %02d:%02d", hourOfDay, minuteOfHour));
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showTonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedToneUri = uri.toString();
                btnSelectTone.setText("Tone Selected");
            }
        }
    }

    private void saveRoutine() {
        String name = etRoutineName.getText().toString().trim();
        int selectedChipId = chipGroupCategory.getCheckedChipId();

        if (name.isEmpty()) {
            etRoutineName.setError("Enter a name");
            return;
        }

        if (selectedChipId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHour == -1) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> selectedIds = chipGroupDays.getCheckedChipIds();
        StringBuilder daysBuilder = new StringBuilder();
        for (int id : selectedIds) {
            Chip chip = findViewById(id);
            if (chip != null) {
                if (daysBuilder.length() > 0) daysBuilder.append(",");
                daysBuilder.append(chip.getText().toString());
            }
        }
        String days = daysBuilder.toString();

        executorService.execute(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, selectedHour);
            cal.set(Calendar.MINUTE, selectedMinute);
            cal.set(Calendar.SECOND, 0);

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            long timestamp = cal.getTimeInMillis();

            if (selectedChipId == R.id.chipTask) {
                Task task = new Task();
                if (isEditMode) task.id = taskId;
                task.title = name;
                task.isCompleted = false;
                task.dueDate = timestamp;
                task.alarmToneUri = selectedToneUri;
                task.daysOfWeek = days;
                
                if (isEditMode) db.appDao().updateTask(task);
                else db.appDao().insertTask(task);
            } else if (selectedChipId == R.id.chipHabit) {
                Habit habit = new Habit();
                habit.name = name;
                habit.streak = 0;
                habit.isCompletedToday = false;
                habit.reminderTime = timestamp;
                habit.alarmToneUri = selectedToneUri;
                habit.daysOfWeek = days;
                db.appDao().insertHabit(habit);
            }

            scheduleAlarm(name, timestamp, selectedToneUri);

            runOnUiThread(() -> {
                Toast.makeText(this, isEditMode ? "Routine updated!" : "Routine saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void scheduleAlarm(String title, long timeInMillis, String toneUri) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("toneUri", toneUri);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) timeInMillis, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "Alarm permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}