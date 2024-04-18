package com.example.adriantasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//Clase principal de la aplicacion
    //Atributos de la clase
    private DBHelper dbHelper;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerViewTasks;
    private String idioma = "es";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        taskList = dbHelper.getAllTasks();

        recyclerViewTasks = findViewById(R.id.recycler_view_tasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setAdapter(taskAdapter);

        Button btnAddTask = findViewById(R.id.btn_new_task);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
        //Gestion de los evento de los botones Editar y Eliminar de cada tarea
        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteTaskDialog(position);
            }
        });
    }

    private void showAddTaskDialog() {
        //Metodo que se encarga de mostrar el dialogo de añadir nueva tarea, obtener la informacion introducida y
        // pasarsela a la base de datos
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);

        dialogBuilder.setTitle("Añadir Tarea");
        dialogBuilder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            //Si se aprieta en aceptar, se comprobara que ambos campos esten rellenados
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();

                if (!title.isEmpty() && !description.isEmpty()) {
                    long taskId = dbHelper.addTask(title,description);
                    Task newTask = new Task(taskId,title, description, false);
                    newTask.setId((int)taskId);
                    taskList.add(newTask);
                    taskAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Tarea añadida correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showEditDialog(final int position) {
        //Metodo que se encarga de mostrar el dialogo de editar una tarea, obtener la informacion introducida y
        // pasarsela a la base de datos
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);

        Task task = taskList.get(position);
        editTextTitle.setText(task.getTitle());
        editTextDescription.setText(task.getDescription());

        dialogBuilder.setTitle("Editar Tarea");
        dialogBuilder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();

                if (!title.isEmpty() && !description.isEmpty()) {
                    Task updatedTask = taskList.get(position);
                    updatedTask.setTitle(title);
                    updatedTask.setDescription(description);
                    dbHelper.updateTask(updatedTask);
                    taskAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showDeleteTaskDialog(final int position) {
        //Metodo que se encarga de mostrar el dialogo de eliminar una tarea y comunicarlo a la base de datos
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Tarea");
        builder.setMessage("¿Estás seguro de que deseas eliminar esta tarea?");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTask(position);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteTask(int position) {
        Task task = taskList.get(position);
        dbHelper.deleteTask(task.getId());
        taskList.remove(position);
        taskAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
    }
}
