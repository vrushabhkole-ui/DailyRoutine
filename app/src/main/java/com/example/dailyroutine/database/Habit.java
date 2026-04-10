package com.example.dailyroutine.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int streak;
    public boolean isCompletedToday;
    public long reminderTime; // Timestamp for time of day
    public String alarmToneUri;
    public String daysOfWeek; // e.g., "Mon,Tue,Wed"
}