package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * SCREEN 6 — Student Dashboard
 */
public class StudentDashboardActivity extends AppCompatActivity {

    private EditText etCourseName;
    private Button btnFetchCode, btnLogout, btnCopyCode;
    private ProgressBar progressBar;
    private TextView tvStudentEmail, tvCode, tvCourseLabel, tvNoCode;
    private LinearLayout layoutCodeResult;

    private DatabaseReference dbRef;
    private SessionManager session;
    private String currentCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        etCourseName    = findViewById(R.id.etCourseName);
        btnFetchCode    = findViewById(R.id.btnFetchCode);
        btnLogout       = findViewById(R.id.btnLogout);
        btnCopyCode     = findViewById(R.id.btnCopyCode);
        progressBar     = findViewById(R.id.progressBar);
        tvStudentEmail  = findViewById(R.id.tvStudentEmail);
        tvCode          = findViewById(R.id.tvCode);
        tvCourseLabel   = findViewById(R.id.tvCourseLabel);
        tvNoCode        = findViewById(R.id.tvNoCode);
        layoutCodeResult = findViewById(R.id.layoutCodeResult);

        session = new SessionManager(this);
        dbRef   = FirebaseDatabase.getInstance().getReference("BMA");

        tvStudentEmail.setText(session.getEmail());

        btnFetchCode.setOnClickListener(v -> handleFetchCode());
        btnCopyCode.setOnClickListener(v -> copyCodeToClipboard());
        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent intent = new Intent(StudentDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void handleFetchCode() {
        String courseName = etCourseName.getText().toString().trim();
        if (courseName.isEmpty()) {
            Toast.makeText(this, "Please enter the course name", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnFetchCode.setEnabled(false);
        layoutCodeResult.setVisibility(View.GONE);

        String courseKey = sanitizeKey(courseName);
        String emailKey  = TeacherRegisterActivity.sanitizeEmail(session.getEmail());

        dbRef.child(courseKey)
             .child(emailKey)
             .addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot snapshot) {
                     progressBar.setVisibility(View.GONE);
                     btnFetchCode.setEnabled(true);
                     layoutCodeResult.setVisibility(View.VISIBLE);

                     if (snapshot.exists() && snapshot.getValue() != null) {
                         currentCode = snapshot.getValue(String.class);
                         tvCode.setText(currentCode);
                         tvCode.setVisibility(View.VISIBLE);
                         tvCourseLabel.setText("Course: " + courseName);
                         tvCourseLabel.setVisibility(View.VISIBLE);
                         btnCopyCode.setVisibility(View.VISIBLE);
                         tvNoCode.setVisibility(View.GONE);
                     } else {
                         currentCode = "";
                         tvCode.setVisibility(View.GONE);
                         tvCourseLabel.setVisibility(View.GONE);
                         btnCopyCode.setVisibility(View.GONE);
                         tvNoCode.setVisibility(View.VISIBLE);
                     }
                 }

                 @Override
                 public void onCancelled(DatabaseError error) {
                     progressBar.setVisibility(View.GONE);
                     btnFetchCode.setEnabled(true);
                     Toast.makeText(StudentDashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                 }
             });
    }

    private void copyCodeToClipboard() {
        if (currentCode.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("BMA Code", currentCode);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code copied to clipboard! \ud83d\udccb", Toast.LENGTH_SHORT).show();
    }

    private String sanitizeKey(String input) {
        return input.replaceAll("[.#$\\[\\]/]", "_");
    }
}
