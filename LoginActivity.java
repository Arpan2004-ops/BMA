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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * SCREEN 4 — Login Screen (shared by Teacher and Student)
 */
public class LoginActivity extends AppCompatActivity {

    // ── UI ────────────────────────────────────────────────────────
    private EditText etEmail, etOtp;
    private Button btnGetOtp, btnLogin;
    private ProgressBar progressBar;
    private TextView tvStatus, tvRoleLabel, tvRegisterLink;

    // ── State ─────────────────────────────────────────────────────
    private String role; // "teacher" or "student", received from Intent
    private DatabaseReference dbRef;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ── Read the role passed from the previous screen ─────────
        role = getIntent().getStringExtra(MainActivity.EXTRA_ROLE);
        if (role == null) role = MainActivity.ROLE_STUDENT; // safe default

        // ── Wire up UI ────────────────────────────────────────────
        etEmail        = findViewById(R.id.etEmail);
        etOtp          = findViewById(R.id.etOtp);
        btnGetOtp      = findViewById(R.id.btnGetOtp);
        btnLogin       = findViewById(R.id.btnLogin);
        progressBar    = findViewById(R.id.progressBar);
        tvStatus       = findViewById(R.id.tvStatus);
        tvRoleLabel    = findViewById(R.id.tvRoleLabel);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // ── Show role in header ───────────────────────────────────
        String roleDisplay = role.equals(MainActivity.ROLE_TEACHER) ? "Teacher" : "Student";
        tvRoleLabel.setText("Logging in as " + roleDisplay);

        // ── Firebase & Session ────────────────────────────────────
        dbRef   = FirebaseDatabase.getInstance().getReference("BMA");
        session = new SessionManager(this);

        // ── Buttons ───────────────────────────────────────────────
        btnGetOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!OtpHelper.isValidEmail(email)) {
                showStatus("Enter a valid email first", false);
                return;
            }
            OtpHelper.sendSimulatedOtp(LoginActivity.this);
            showStatus("OTP sent! Check the Toast notification.", true);
        });

        btnLogin.setOnClickListener(v -> handleLogin());

        // ── "Register" link ───────────────────────────────────────
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent;
            if (role.equals(MainActivity.ROLE_TEACHER)) {
                intent = new Intent(LoginActivity.this, TeacherRegisterActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, StudentRegisterActivity.class);
            }
            intent.putExtra(MainActivity.EXTRA_ROLE, role);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String otp   = etOtp.getText().toString().trim();

        if (!OtpHelper.areFieldsFilled(email, otp)) {
            showStatus("Please fill in all fields", false);
            return;
        }

        if (!OtpHelper.isValidEmail(email)) {
            showStatus("Enter a valid email address", false);
            return;
        }

        if (!OtpHelper.verifySimulatedOtp(otp)) {
            showStatus("Invalid OTP. Please try again.", false);
            return;
        }

        showLoading(true);

        String emailKey  = TeacherRegisterActivity.sanitizeEmail(email);
        String userGroup = role.equals(MainActivity.ROLE_TEACHER) ? "teachers" : "students";

        dbRef.child("users")
             .child(userGroup)
             .child(emailKey)
             .addListenerForSingleValueEvent(new ValueEventListener() {

                 @Override
                 public void onDataChange(DataSnapshot snapshot) {
                     showLoading(false);

                     if (snapshot.exists()) {
                         session.saveSession(email, role);
                         showStatus("Login successful! Redirecting...", true);

                         etEmail.postDelayed(() -> {
                             Intent intent;
                             if (role.equals(MainActivity.ROLE_TEACHER)) {
                                 intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                             } else {
                                 intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                             }
                             startActivity(intent);
                             finish();
                         }, 1000);

                     } else {
                         showStatus("Email not registered. Please sign up first.", false);
                     }
                 }

                 @Override
                 public void onCancelled(DatabaseError error) {
                     showLoading(false);
                     showStatus("Error: " + error.getMessage(), false);
                 }
             });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess
                ? getResources().getColor(R.color.success)
                : getResources().getColor(R.color.error));
    }
}
