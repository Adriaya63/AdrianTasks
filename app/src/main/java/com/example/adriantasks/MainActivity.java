package com.example.adriantasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Base64;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
//Clase principal de la aplicacion
    //Atributos de la clase
    private DBHelper dbHelper;
    private List<Tarea> taskList;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerViewTasks;
    private String idioma = "es";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String name = intent.getStringExtra("usuario");

        Log.d("User","Nombre user: "+name);

        cargarUsuario(name);

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

    private void cargarUsuario(String name){
        TextView textViewUser = findViewById(R.id.textViewUser);
        textViewUser.setText("Usuario: " + name);

        // URL del script para recuperar la imagen
        String url = "http://34.175.200.65:81/get_image.php"; // URL del script para recuperar la imagen
        String parametros = "usuario=" + Uri.encode(name);
        Log.d("Datos enviados", parametros);

        // Ejecutar la tarea asíncrona para recuperar la imagen
        new ObtenerImagenTask().execute(url, parametros);
    }

    private void mostrarImagen(Bitmap imagen) {
        ImageView imageView = findViewById(R.id.imageViewProfile);
        imageView.setImageBitmap(imagen);
    }

    private class ObtenerImagenTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            String urlString = params[0];
            String parametros = params[1];
            Bitmap imagen = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametros.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();


                // Establecer la conexión HTTP y recibir la respuesta del servidor
                int responseCode = conexion.getResponseCode();

                // Verificar si la conexión fue exitosa (código de respuesta HTTP 200)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta del servidor
                    InputStream inputStream = conexion.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    // Leer línea por línea y construir el StringBuilder
                    while ((line = bufferedReader.readLine()) != null) {
                        Log.d("Buff",line);
                        stringBuilder.append(line);
                    }

                    // Cerrar el BufferedReader y el InputStream
                    bufferedReader.close();
                    inputStream.close();

                    // Obtener el string de la respuesta del servidor
                    String base64String = stringBuilder.toString();
                    Log.d("Data","Image: "+base64String);

                    // Decodificar la cadena base64 en un array de bytes
                    byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

                    // Crear un Bitmap a partir de los bytes decodificados
                    imagen = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);


                } else {
                    // Si la conexión no fue exitosa, manejar el error adecuadamente
                    Log.d("Error","Imagen no recibida correctamente");
                }

                conexion.disconnect();
            } catch (Exception e) {
                Log.e("Error", "Error al recuperar la imagen: " + e.getMessage());
            }

            return imagen;
        }

        @Override
        protected void onPostExecute(Bitmap imagen) {
            if (imagen != null) {
                mostrarImagen(imagen);
            } else {
                Log.e("Error", "No se pudo obtener la imagen o la imagen recibida es nula");
            }
        }
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
                    Tarea newTask = new Tarea(taskId,title, description, false);
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

        Tarea task = taskList.get(position);
        editTextTitle.setText(task.getTitle());
        editTextDescription.setText(task.getDescription());

        dialogBuilder.setTitle("Editar Tarea");
        dialogBuilder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();

                if (!title.isEmpty() && !description.isEmpty()) {
                    Tarea updatedTask = taskList.get(position);
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
        Tarea task = taskList.get(position);
        dbHelper.deleteTask(task.getId());
        taskList.remove(position);
        taskAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
    }
}
