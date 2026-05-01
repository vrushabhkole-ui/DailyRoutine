package com.example.dailyroutine;

import android.app.AlarmManager;
import android.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddRoutineActivity extends AppCompatActivity {

    private static final int RINGTONE_PICKER_REQUEST = 100;

    private EditText etRoutineName;
    private ChipGroup chipGroupCategory, chipGroupDays;
    private MaterialButton btnSaveRoutine, btnStartTime, btnEndTime, btnSelectTone;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int startHour = -1, startMinute = -1;
    private int endHour = -1, endMinute = -1;
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
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnSelectTone = findViewById(R.id.btnSelectTone);

        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));
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
            List<Task> allTasks = db.appDao().getAllTasks();
            Task task = null;
            for(Task t : allTasks) { if(t.id == taskId) { task = t; break; } }

            if (task != null) {
                Task finalTask = task;
                runOnUiThread(() -> {
                    etRoutineName.setText(finalTask.title);
                    chipGroupCategory.check(R.id.chipTask);
                    chipGroupCategory.setEnabled(false);
                    
                    if (finalTask.startTime > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(finalTask.startTime);
                        startHour = cal.get(Calendar.HOUR_OF_DAY);
                        startMinute = cal.get(Calendar.MINUTE);
                        btnStartTime.setText(String.format("Start: %02d:%02d", startHour, startMinute));
                    }
                    if (finalTask.endTime > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(finalTask.endTime);
                        endHour = cal.get(Calendar.HOUR_OF_DAY);
                        endMinute = cal.get(Calendar.MINUTE);
                        btnEndTime.setText(String.format("End: %02d:%02d", endHour, endMinute));
                    }
                    
                    selectedToneUri = finalTask.alarmToneUri;
                    if (selectedToneUri != null && !selectedToneUri.isEmpty()) {
                        btnSelectTone.setText("Tone Selected");
                    }

                    if (finalTask.daysOfWeek != null) {
                        String[] days = finalTask.daysOfWeek.split(",");
                        for (String day : days) {
                            for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
                                Chip chip = (Chip) chipGroupDays.getChildAt(i);
                                if (chip.getText().toString().equals(day)) chip.setChecked(true);
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

    private void showTimePicker(boolean isStart) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    if (isStart) {
                        startHour = hourOfDay;
                        startMinute = minuteOfHour;
                        btnStartTime.setText(String.format("Start: %02d:%02d", hourOfDay, minuteOfHour));
                    } else {
                        endHour = hourOfDay;
                        endMinute = minuteOfHour;
                        btnEndTime.setText(String.format("End: %02d:%02d", hourOfDay, minuteOfHour));
                    }
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

        if (name.isEmpty()) { etRoutineName.setError("Enter a name"); return; }
        if (selectedChipId == -1) { Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show(); return; }
        if (startHour == -1) { Toast.makeText(this, "Select a start time", Toast.LENGTH_SHORT).show(); return; }

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
            // New Start Time (normalized to a specific date to compare times only)
            Calendar newStart = Calendar.getInstance();
            newStart.set(2000, 0, 1, startHour, startMinute, 0);
            newStart.set(Calendar.MILLISECOND, 0);
            long newStartTimeVal = newStart.getTimeInMillis();

            // New End Time (if set, normalized)
            long newEndTimeVal = 0;
            if (endHour != -1) {
                Calendar newEnd = Calendar.getInstance();
                newEnd.set(2000, 0, 1, endHour, endMinute, 0);
                newEnd.set(Calendar.MILLISECOND, 0);
                // If end time is before start time, it means it's on the next day
                if (newEnd.before(newStart)) newEnd.add(Calendar.DATE, 1);
                newEndTimeVal = newEnd.getTimeInMillis();
            } else {
                // If no end time is set, treat it as a point in time or 1 minute task for collision check
                newEndTimeVal = newStartTimeVal + 60000;
            }

            // Check for overlaps with existing Tasks
            List<Task> existingTasks = db.appDao().getAllTasks();
            for (Task t : existingTasks) {
                if (isEditMode && t.id == taskId) continue;
                
                if (checkOverlap(newStartTimeVal, newEndTimeVal, t.startTime, t.endTime, t.title)) return;
            }

            // Check for overlaps with existing Habits
            List<Habit> existingHabits = db.appDao().getAllHabits();
            for (Habit h : existingHabits) {
                if (checkOverlap(newStartTimeVal, newEndTimeVal, h.startTime, h.endTime, h.name)) return;
            }

            // Real timestamps for AlarmManager (today or tomorrow)
            Calendar calStart = Calendar.getInstance();
            calStart.set(Calendar.HOUR_OF_DAY, startHour);
            calStart.set(Calendar.MINUTE, startMinute);
            calStart.set(Calendar.SECOND, 0);
            if (calStart.getTimeInMillis() <= System.currentTimeMillis()) calStart.add(Calendar.DAY_OF_YEAR, 1);
            long finalStartTimeStamp = calStart.getTimeInMillis();

            long finalEndTimeStamp = 0;
            if (endHour != -1) {
                Calendar calEnd = Calendar.getInstance();
                calEnd.set(Calendar.HOUR_OF_DAY, endHour);
                calEnd.set(Calendar.MINUTE, endMinute);
                calEnd.set(Calendar.SECOND, 0);
                if (calEnd.getTimeInMillis() <= calStart.getTimeInMillis()) calEnd.add(Calendar.DAY_OF_YEAR, 1);
                finalEndTimeStamp = calEnd.getTimeInMillis();
            }

            if (selectedChipId == R.id.chipTask) {
                Task task = new Task();
                if (isEditMode) task.id = taskId;
                task.title = name;
                task.isCompleted = false;
                task.dueDate = finalStartTimeStamp;
                task.startTime = finalStartTimeStamp;
                task.endTime = finalEndTimeStamp;
                task.alarmToneUri = selectedToneUri;
                task.daysOfWeek = days;
                task.isAlarmOn = true;
                if (isEditMode) db.appDao().updateTask(task); else db.appDao().insertTask(task);
            } else if (selectedChipId == R.id.chipHabit) {
                Habit habit = new Habit();
                habit.name = name;
                habit.streak = 0;
                habit.isCompletedToday = false;
                habit.reminderTime = finalStartTimeStamp;
                habit.startTime = finalStartTimeStamp;
                habit.endTime = finalEndTimeStamp;
                habit.alarmToneUri = selectedToneUri;
                habit.daysOfWeek = days;
                habit.isAlarmOn = true;
                db.appDao().insertHabit(habit);
            }

            scheduleAlarm(name + " (Start)", finalStartTimeStamp, selectedToneUri);
            if (finalEndTimeStamp > 0) scheduleAlarm(name + " (End)", finalEndTimeStamp, selectedToneUri);

            runOnUiThread(() -> {
                Toast.makeText(this, "Routine Scheduled!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private boolean checkOverlap(long newStart, long newEnd, long existStart, long existEnd, String routineName) {
        // Normalize existing times for comparison
        Calendar cS = Calendar.getInstance();
        cS.setTimeInMillis(existStart);
        Calendar nS = Calendar.getInstance();
        nS.set(2000, 0, 1, cS.get(Calendar.HOUR_OF_DAY), cS.get(Calendar.MINUTE), 0);
        nS.set(Calendar.MILLISECOND, 0);
        long eStart = nS.getTimeInMillis();

        long eEnd;
        if (existEnd > 0) {
            Calendar cE = Calendar.getInstance();
            cE.setTimeInMillis(existEnd);
            Calendar nE = Calendar.getInstance();
            nE.set(2000, 0, 1, cE.get(Calendar.HOUR_OF_DAY), cE.get(Calendar.MINUTE), 0);
            nE.set(Calendar.MILLISECOND, 0);
            if (nE.before(nS)) nE.add(Calendar.DATE, 1);
            eEnd = nE.getTimeInMillis();
        } else {
            eEnd = eStart + 60000; // 1 min duration
        }

        // Overlap formula: max(start1, start2) < min(end1, end2)
        if (Math.max(newStart, eStart) < Math.min(newEnd, eEnd)) {
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Schedule Conflict")
                        .setMessage("This time overlaps with your routine: '" + routineName + "'. Please choose a different time.")
                        .setPositiveButton("OK", null)
                        .show();
            });
            return true;
        }
        return false;
    }

    private void scheduleAlarm(String title, long timeInMillis, String toneUri) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("toneUri", toneUri);
        int requestCode = (int) timeInMillis; 
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                else alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } catch (SecurityException e) { Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show(); }
        }
    }
}