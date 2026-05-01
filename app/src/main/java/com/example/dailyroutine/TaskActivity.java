package com.example.dailyroutine;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyroutine.database.AppDatabase;
import com.example.dailyroutine.database.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        db = AppDatabase.getInstance(this);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(new ArrayList<>(), this);
        rvTasks.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbarTasks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Routines");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            startActivity(new Intent(this, AddRoutineActivity.class));
        });

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        executorService.execute(() -> {
            List<Task> tasks = db.appDao().getAllTasks();
            runOnUiThread(() -> adapter.setTasks(tasks));
        });
    }

    @Override
    public void onTaskToggle(Task task) {
        executorService.execute(() -> {
            task.isCompleted = !task.isCompleted;
            db.appDao().updateTask(task);
            loadTasks();
        });
    }

    @Override
    public void onTaskDelete(Task task) {
        executorService.execute(() -> {
            db.appDao().deleteTask(task);
            loadTasks();
        });
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, AddRoutineActivity.class);
        intent.putExtra("task_id", task.id);
        startActivity(intent);
    }

    @Override
    public void onAlarmToggle(Task task, boolean isEnabled) {
        executorService.execute(() -> {
            task.isAlarmOn = isEnabled;
            db.appDao().updateTask(task);
            // In a full implementation, you would also call AlarmManager here
            // to actually cancel or reschedule the system alarm.
        });
    }
}