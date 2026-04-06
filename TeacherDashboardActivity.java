package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * SCREEN 5 — Teacher Dashboard
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private EditText etCourseName, etStudentEmails;
    private Button btnGenerateCodes, btnLogout;
    private ProgressBar progressBar;
    private TextView tvStatus, tvTeacherEmail;
    private LinearLayout layoutResults, layoutCodesList;

    private DatabaseReference dbRef;
    private SessionManager session;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH   = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        etCourseName    = findViewById(R.id.etCourseName);
        etStudentEmails = findViewById(R.id.etStudentEmails);
        btnGenerateCodes = findViewById(R.id.btnGenerateCodes);
        btnLogout       = findViewById(R.id.btnLogout);
        progressBar     = findViewById(R.id.progressBar);
        tvStatus        = findViewById(R.id.tvStatus);
        tvTeacherEmail  = findViewById(R.id.tvTeacherEmail);
        layoutResults   = findViewById(R.id.layoutResults);
        layoutCodesList = findViewById(R.id.layoutCodesList);

        session = new SessionManager(this);
        dbRef   = FirebaseDatabase.getInstance().getReference("BMA");

        tvTeacherEmail.setText(session.getEmail());

        btnGenerateCodes.setOnClickListener(v -> handleGenerateCodes());

        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent intent = new Intent(TeacherDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void handleGenerateCodes() {
        String courseName    = etCourseName.getText().toString().trim();
        String emailsRaw     = etStudentEmails.getText().toString().trim();

        if (courseName.isEmpty()) {
            showStatus("Please enter a course name", false);
            return;
        }
        if (emailsRaw.isEmpty()) {
            showStatus("Please enter at least one student email", false);
            return;
        }

        String[] rawEmails = emailsRaw.split(",");
        List<String> validEmails = new ArrayList<>();

        for (String rawEmail : rawEmails) {
            String trimmed = rawEmail.trim();
            if (OtpHelper.isValidEmail(trimmed)) {
                validEmails.add(trimmed);
            }
        }

        if (validEmails.isEmpty()) {
            showStatus("No valid emails found. Check format.", false);
            return;
        }

        showLoading(true);
        layoutResults.setVisibility(View.GONE);
        layoutCodesList.removeAllViews();

        String courseKey = sanitizeKey(courseName);
        Map<String, String> codeMap = new HashMap<>();

        for (String email : validEmails) {
            String generatedCode = generateCode();
            codeMap.put(email, generatedCode);
        }

        Map<String, Object> firebaseUpdates = new HashMap<>();
        for (Map.Entry<String, String> entry : codeMap.entrySet()) {
            String emailKey = TeacherRegisterActivity.sanitizeEmail(entry.getKey());
            firebaseUpdates.put(courseKey + "/" + emailKey, entry.getValue());
        }

        dbRef.updateChildren(firebaseUpdates)
             .addOnSuccessListener(unused -> {
                 showLoading(false);
                 showStatus("✅ " + validEmails.size() + " code(s) generated & saved!", true);
                 displayGeneratedCodes(codeMap);
             })
             .addOnFailureListener(e -> {
                 showLoading(false);
                 showStatus("Failed to save codes: " + e.getMessage(), false);
             });
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("BMA");
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CODE_CHARS.length());
            sb.append(CODE_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private void displayGeneratedCodes(Map<String, String> codeMap) {
        layoutCodesList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Map.Entry<String, String> entry : codeMap.entrySet()) {
            // Re-using the R.layout if it exists, otherwise this will error at build time
            // Assuming layout and view IDs are correct based on previous code snippets
            try {
                int layoutId = getResources().getIdentifier("item_code_result", "layout", getPackageName());
                if (layoutId == 0) return;
                
                View itemView = inflater.inflate(layoutId, layoutCodesList, false);
                TextView tvEmail = itemView.findViewById(getResources().getIdentifier("tvEmail", "id", getPackageName()));
                TextView tvCode  = itemView.findViewById(getResources().getIdentifier("tvGeneratedCode", "id", getPackageName()));

                tvEmail.setText(entry.getKey());
                tvCode.setText(entry.getValue());

                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(getResources().getColor(getResources().getIdentifier("divider", "color", getPackageName())));

                layoutCodesList.addView(itemView);
                layoutCodesList.addView(divider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        layoutResults.setVisibility(View.VISIBLE);
    }

    private String sanitizeKey(String input) {
        return input.replaceAll("[.#$\\[\\]/]", "_");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerateCodes.setEnabled(!show);
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess
                ? getResources().getColor(getResources().getIdentifier("success", "color", getPackageName()))
                : getResources().getColor(getResources().getIdentifier("error", "color", getPackageName())));
    }
}
