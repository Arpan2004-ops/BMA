package com.example.myapplication;

import android.app.Activity;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Random;

/**
 * OtpHelper — Central utility class for all OTP-related logic.
 */
public class OtpHelper {

    private static final String TAG = "OtpHelper";

    // Holds the most recently generated OTP (simple in-memory storage)
    private static String lastGeneratedOtp = "";

    /**
     * Generates a random 6-digit OTP and shows it in a Toast.
     */
    public static void sendSimulatedOtp(Activity activity) {
        // Generate a 6-digit random number
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        lastGeneratedOtp = String.valueOf(otp);

        // Show OTP in Toast (remove this in production; send via email/SMS)
        Toast.makeText(
            activity,
            "Your OTP: " + lastGeneratedOtp + "\n(Demo mode — shown here for testing)",
            Toast.LENGTH_LONG
        ).show();

        Log.d(TAG, "Simulated OTP generated: " + lastGeneratedOtp);
    }

    /**
     * Verifies that the user-entered OTP matches the last generated one.
     */
    public static boolean verifySimulatedOtp(String userInput) {
        if (lastGeneratedOtp == null || lastGeneratedOtp.isEmpty()) {
            return false;
        }
        return lastGeneratedOtp.equals(userInput.trim());
    }

    /**
     * Returns true if the given string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        return email != null
                && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Returns true if all provided strings are non-null and non-empty.
     */
    public static boolean areFieldsFilled(String... fields) {
        if (fields == null) return false;
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
