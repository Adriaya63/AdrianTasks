package com.example.adriantasks;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



public class StartActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;

    private String fotoEnBase64;

    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        editTextUsername = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);

        Button enterButton = findViewById(R.id.btn_enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
    }

    private void iniciarSesion() {
        // Obtener referencias a los campos de usuario y contraseña
        EditText editTextUsuario = findViewById(R.id.edit_text_username);
        EditText editTextContrasena = findViewById(R.id.edit_text_password);

        // Obtener los valores de usuario y contraseña
        String usuario = editTextUsuario.getText().toString();
        String contrasena = editTextContrasena.getText().toString();

        user = usuario;

        // Enviar solicitud al servidor para verificar la autenticación
        String url = "http://34.175.200.65:81/verificar_login.php"; // URL del script para verificar la autenticación
        String parametros = "usuario=" + Uri.encode(usuario) + "&contrasena=" + Uri.encode(contrasena);

        // Ejecutar la tarea asíncrona para enviar la solicitud
        new VerificarLoginTask().execute(url, parametros);
    }

    // Clase para enviar datos al servidor y verificar autenticación
    private class VerificarLoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String urlString = params[0];
            String parametros = params[1];
            boolean autenticado = false;

            try {
                // Crear la conexión HTTP
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametros.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Leer la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("Buff",line);
                    if (line.contains("Autenticado")) {
                        autenticado = true;
                    }
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return autenticado;
        }

        @Override
        protected void onPostExecute(Boolean autenticado) {
            if (autenticado) {
                // Si el usuario está autenticado, iniciar la actividad del menú
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("usuario",user);
                startActivity(intent);
            } else {
                // Si no está autenticado, mostrar un mensaje de error
                Toast.makeText(StartActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_register, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextNewUsername = dialogView.findViewById(R.id.edit_text_new_username);
        final EditText editTextNewPassword = dialogView.findViewById(R.id.edit_text_new_password);
        Button buttonCaptureImage = dialogView.findViewById(R.id.buttonCaptureImage);

        dialogBuilder.setTitle("Registro");
        dialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newUsername = editTextNewUsername.getText().toString();
                String newPassword = editTextNewPassword.getText().toString();

                // Guardar los datos del nuevo usuario utilizando el método saveNewUser
                // Implementa este método para comunicarte con tu servidor y guardar los datos en la base de datos
                saveNewUser(newUsername, newPassword, fotoEnBase64);
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //lanzar foto con camara
                Intent elIntentFoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureLauncher.launch(elIntentFoto);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK &&
                        result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap laminiatura = (Bitmap) bundle.get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    laminiatura.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] fototransformada = stream.toByteArray();
                    fotoEnBase64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);
                } else {
                    Log.d("TakenPicture", "No photo taken");
                }
            });


    private void saveNewUser(String usuario, String contrasena, String foto) {
        // URL de tu archivo PHP en el servidor
        String url = "http://34.175.200.65:81/add_user.php";


        // Dentro del método enviarDatos()
        String parametros = "usuario=" + Uri.encode(usuario) + "&contrasena=" + Uri.encode(contrasena) + "&image=" + Uri.encode((foto));
        Log.d("Datos enviados", parametros); // Imprime los datos enviados en el registro (Logcat)



        // Ejecutar la tarea asíncrona
        new EnviarDatosTask().execute(url, parametros);
    }

    // Clase para enviar datos al servidor en segundo plano
    private static class EnviarDatosTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Obtener la URL y los parámetros de los argumentos
            String urlString = params[0];
            String parametros = params[1];

            try {
                // Crear la conexión HTTP
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametros.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Obtener la respuesta del servidor (si es necesario)
                int responseCode = conexion.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Bien", "Todo ok");
                } else {
                    Log.d("Mal", "Algo no ok");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


