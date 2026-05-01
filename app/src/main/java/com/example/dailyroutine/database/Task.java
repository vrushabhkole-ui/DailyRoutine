package com.example.dailyroutine.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public boolean isCompleted;
    public long dueDate; // Timestamp
    public String alarmToneUri;
    public String daysOfWeek; // e.g., "Mon,Tue,Wed"
    public long startTime; // For timer
    public long endTime;   // For timer
    public boolean isAlarmOn; // New field to enable/disable alarm
}