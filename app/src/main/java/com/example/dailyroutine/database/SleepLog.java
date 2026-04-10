package com.example.dailyroutine.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_logs")
public class SleepLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long startTime; // Timestamp
    public long endTime;   // Timestamp
    public float quality;  // 0.0 to 5.0
}