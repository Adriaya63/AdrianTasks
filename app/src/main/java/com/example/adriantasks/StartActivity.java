package com.example.adriantasks;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class StartActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;

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
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // Autenticar al usuario utilizando el método authenticateUser
                // Implementa este método para comunicarte con tu servidor y verificar las credenciales
                if (authenticateUser(username, password)) {
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Termina la actividad actual (pantalla de inicio)
                } else {
                    Toast.makeText(StartActivity.this, "Error: Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
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

    private boolean authenticateUser(String username, String password) {
        // Método para autenticar al usuario utilizando el servidor
        // Implementa la lógica para comunicarte con tu servidor y verificar las credenciales
        // Devuelve true si la autenticación es exitosa, de lo contrario, devuelve false
        // Por simplicidad, esta implementación devuelve true siempre
        return true;
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_register, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextNewUsername = dialogView.findViewById(R.id.edit_text_new_username);
        final EditText editTextNewPassword = dialogView.findViewById(R.id.edit_text_new_password);

        dialogBuilder.setTitle("Registro");
        dialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newUsername = editTextNewUsername.getText().toString();
                String newPassword = editTextNewPassword.getText().toString();

                // Guardar los datos del nuevo usuario utilizando el método saveNewUser
                // Implementa este método para comunicarte con tu servidor y guardar los datos en la base de datos
                saveNewUser(newUsername, newPassword);
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

    private void saveNewUser(String username, String password) {
        // Método para guardar los datos del nuevo usuario utilizando el servidor
        // Implementa la lógica para comunicarte con tu servidor y guardar los datos en la base de datos

        // Aquí puedes hacer una solicitud HTTP al servidor para guardar los datos del nuevo usuario
        // Puedes usar bibliotecas como Retrofit o Volley para hacer la solicitud

        // Ejemplo de implementación con HttpURLConnection:
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construir la URL del archivo PHP en el servidor
                    URL url = new URL("http://34.175.200.65/add_user.php");

                    // Establecer la conexión
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Construir los parámetros que se enviarán al servidor
                    String parameters = "username=" + URLEncoder.encode(username, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");

                    // Escribir los parámetros en la conexión
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(parameters);
                    writer.flush();

                    // Leer la respuesta del servidor
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Cerrar los flujos y la conexión
                    writer.close();
                    reader.close();
                    connection.disconnect();

                    // Mostrar la respuesta del servidor (puedes ajustar esto según tu necesidad)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StartActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Manejar cualquier error
                }
            }
        }).start();
    }

}
