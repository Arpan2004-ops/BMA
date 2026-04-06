package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * SCREEN 3 — Student Registration
 */
public class StudentRegisterActivity extends AppCompatActivity {

    private EditText etName, etRegNumber, etEmail, etOtp;
    private Button btnGetOtp, btnRegister;
    private ProgressBar progressBar;
    private TextView tvStatus, tvLoginLink;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        etName      = findViewById(R.id.etName);
        etRegNumber = findViewById(R.id.etRegNumber);
        etEmail     = findViewById(R.id.etEmail);
        etOtp       = findViewById(R.id.etOtp);
        btnGetOtp   = findViewById(R.id.btnGetOtp);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvStatus    = findViewById(R.id.tvStatus);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        dbRef = FirebaseDatabase.getInstance().getReference("BMA");

        btnGetOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!OtpHelper.isValidEmail(email)) {
                showStatus("Enter a valid email before requesting OTP", false);
                return;
            }
            OtpHelper.sendSimulatedOtp(StudentRegisterActivity.this);
            showStatus("OTP sent! Check the Toast notification.", true);
        });

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(StudentRegisterActivity.this, LoginActivity.class);
            intent.putExtra(MainActivity.EXTRA_ROLE, MainActivity.ROLE_STUDENT);
            startActivity(intent);
        });
    }

    private void handleRegister() {
        String name      = etName.getText().toString().trim();
        String regNumber = etRegNumber.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String otp       = etOtp.getText().toString().trim();

        if (!OtpHelper.areFieldsFilled(name, regNumber, email, otp)) {
            showStatus("Please fill in all fields", false);
            return;
        }

        if (!OtpHelper.isValidEmail(email)) {
            showStatus("Please enter a valid email address", false);
            return;
        }

        if (!OtpHelper.verifySimulatedOtp(otp)) {
            showStatus("Invalid OTP. Request a new one.", false);
            return;
        }

        showLoading(true);

        String emailKey = TeacherRegisterActivity.sanitizeEmail(email);
        StudentData studentData = new StudentData(name, regNumber, email, MainActivity.ROLE_STUDENT);

        dbRef.child("users")
             .child("students")
             .child(emailKey)
             .setValue(studentData)
             .addOnSuccessListener(unused -> {
                 showLoading(false);
                 showStatus("Registration successful! Please login.", true);

                 etName.postDelayed(() -> {
                     Intent intent = new Intent(StudentRegisterActivity.this, LoginActivity.class);
                     intent.putExtra(MainActivity.EXTRA_ROLE, MainActivity.ROLE_STUDENT);
                     startActivity(intent);
                     finish();
                 }, 1500);
             })
             .addOnFailureListener(e -> {
                 showLoading(false);
                 showStatus("Registration failed: " + e.getMessage(), false);
             });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess
                ? getResources().getColor(R.color.success)
                : getResources().getColor(R.color.error));
    }

    public static class StudentData {
        public String name;
        public String regNumber;
        public String email;
        public String role;
        public StudentData() {}
        public StudentData(String name, String regNumber, String email, String role) {
            this.name      = name;
            this.regNumber = regNumber;
            this.email     = email;
            this.role      = role;
        }
    }
}
