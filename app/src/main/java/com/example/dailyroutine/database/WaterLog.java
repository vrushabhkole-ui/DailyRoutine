package com.example.dailyroutine.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_logs")
public class WaterLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public float amount; // in liters
    public long timestamp;
}