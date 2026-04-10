package com.example.dailyroutine.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppDao {
    // Tasks
    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    // Habits
    @Query("SELECT * FROM habits")
    List<Habit> getAllHabits();

    @Insert
    void insertHabit(Habit habit);

    @Update
    void updateHabit(Habit habit);

    // Water
    @Query("SELECT * FROM water_logs")
    List<WaterLog> getAllWaterLogs();

    @Insert
    void insertWaterLog(WaterLog log);

    // Sleep
    @Query("SELECT * FROM sleep_logs")
    List<SleepLog> getAllSleepLogs();

    @Insert
    void insertSleepLog(SleepLog log);
}