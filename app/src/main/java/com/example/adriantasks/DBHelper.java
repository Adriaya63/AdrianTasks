package com.example.adriantasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "tasks";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COMPLETED = "completed";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Metodo que crea la tabla la primera vez que se ejecuta
        String createTableQuery = "CREATE T" +
                "ABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_COMPLETED + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Metodo que elimina la tabla y la vuelve a crear cuando se cambia la version de la base de datos
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addTask(String title, String desc) {
        //Metodo que ejecuta una query de tipo INSERT
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, desc);
        values.put(COLUMN_COMPLETED,0);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public int updateTask(Task task) {
        //Metodo que ejecuta una query de tipo UPDATE
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);
        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});

        db.close();
        return rowsAffected;
    }

    public void deleteTask(long taskId) {
        //Metodo que ejecuta una query de tipo DELETE
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{String.valueOf(taskId)});
        db.close();

    }

    public List<Task> getAllTasks() {
        //Metodo que se encarga de obtener la informacion de las distintas tareas,
        //crear objetos de tipo Task con esa informacion, añadirlas a un Array y pasarselo a la aplicaccion
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndexId = cursor.getColumnIndex(COLUMN_ID);
                    int columnIndexTitle = cursor.getColumnIndex(COLUMN_TITLE);
                    int columnIndexDescription = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                    int columnIndexCompleted = cursor.getColumnIndex(COLUMN_COMPLETED);

                    do {
                        long id = cursor.getInt(columnIndexId);
                        String title = cursor.getString(columnIndexTitle);
                        String description = cursor.getString(columnIndexDescription);
                        boolean completed = cursor.getInt(columnIndexCompleted) == 1;

                        Task task = new Task(id,title, description, completed);
                        taskList.add(task);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        db.close();
        return taskList;
    }

}
