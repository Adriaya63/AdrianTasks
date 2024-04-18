package com.example.adriantasks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    //Mostrar la informacion de las distintas tareas
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.textViewTitle.setText(task.getTitle());
        holder.textViewDescription.setText(task.getDescription());
        holder.checkBoxCompleted.setChecked(task.isCompleted());

        holder.checkBoxCompleted.setOnClickListener(new View.OnClickListener() {
            //Gestionar eventos del checkBox de cada tarea
            @Override
            public void onClick(View v) {
                boolean isChecked = holder.checkBoxCompleted.isChecked();
                task.setCompleted(isChecked);
                if (isChecked) {
                    sendNotification(holder.itemView.getContext(), task.getTitle());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        CheckBox checkBoxCompleted;
        Button btnEdit;
        Button btnDelete;

        TaskViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            //Gestion de los eventos de los botones Editar y Eliminar de cada tarea
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            checkBoxCompleted = itemView.findViewById(R.id.check_box_completed);
            btnEdit = itemView.findViewById(R.id.btn_edit_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    private void sendNotification(Context context, String taskTitle) {
        //Metodo para enviar una notificacion local
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Verificar si el dispositivo está ejecutando Android Oreo (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("task_notification_channel",
                    "Task Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(context, "task_notification_channel")
                .setContentTitle("Tarea Completada")
                .setContentText("La tarea " + taskTitle + " ha sido completada")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        notificationManager.notify(0, builder.build());
    }
}
