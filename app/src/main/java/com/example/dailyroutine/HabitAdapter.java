package com.example.dailyroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dailyroutine.database.Habit;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habits;
    private final OnHabitActionListener listener;

    public interface OnHabitActionListener {
        void onHabitComplete(Habit habit);
        void onHabitDelete(Habit habit);
    }

    public HabitAdapter(List<Habit> habits, OnHabitActionListener listener) {
        this.habits = habits;
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvName.setText(habit.name);
        holder.tvStreak.setText("🔥 " + habit.streak + " day streak");
        
        if (habit.isCompletedToday) {
            holder.btnComplete.setText("Done");
            holder.btnComplete.setEnabled(false);
        } else {
            holder.btnComplete.setText("Mark Done");
            holder.btnComplete.setEnabled(true);
        }

        holder.btnComplete.setOnClickListener(v -> listener.onHabitComplete(habit));
        holder.btnDelete.setOnClickListener(v -> listener.onHabitDelete(habit));
    }

    @Override
    public int getItemCount() {
        return habits == null ? 0 : habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStreak;
        MaterialButton btnComplete;
        ImageButton btnDelete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHabitName);
            tvStreak = itemView.findViewById(R.id.tvHabitStreak);
            btnComplete = itemView.findViewById(R.id.btnCompleteHabit);
            btnDelete = itemView.findViewById(R.id.btnDeleteHabit);
        }
    }
}