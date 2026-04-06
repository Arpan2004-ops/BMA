package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.app.R;

/**
 * SCREEN 1 — Main Screen
 */
public class MainActivity extends AppCompatActivity {

    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";
    public static final String EXTRA_ROLE   = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTeacher = findViewById(R.id.btnTeacher);
        Button btnStudent = findViewById(R.id.btnStudent);

        btnTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeacherRegisterActivity.class);
            intent.putExtra(EXTRA_ROLE, ROLE_TEACHER);
            startActivity(intent);
        });

        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentRegisterActivity.class);
            intent.putExtra(EXTRA_ROLE, ROLE_STUDENT);
            startActivity(intent);
        });
    }
}
