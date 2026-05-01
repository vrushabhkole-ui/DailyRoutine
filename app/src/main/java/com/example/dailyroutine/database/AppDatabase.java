package com.example.dailyroutine.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class, Habit.class, WaterLog.class, SleepLog.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract AppDao appDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "daily_routine_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}