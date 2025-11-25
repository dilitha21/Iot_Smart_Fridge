package com.example.smartfridge;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to test Firebase connection and debug issues
 * Use this to verify your Firebase setup is working correctly
 */
public class FirebaseTestUtility {

    private static final String TAG = "FirebaseTest";
    private Context context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public FirebaseTestUtility(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Test authentication status
     */
    public void testAuthentication() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String uid = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "✓ Authenticated as: " + email);
            Log.d(TAG, "  User ID: " + uid);
            showToast("✓ Authenticated as: " + email);
        } else {
            Log.e(TAG, "✗ Not authenticated");
            showToast("✗ Not authenticated");
        }
    }

    /**
     * Test database connection by writing a test value
     */
    public void testDatabaseWrite() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot test database - not authenticated");
            showToast("Please login first");
            return;
        }

        // Create test data
        Map<String, Object> testData = new HashMap<>();
        testData.put("timestamp", System.currentTimeMillis());
        testData.put("testMessage", "Android app connected successfully");
        testData.put("userId", mAuth.getCurrentUser().getUid());

        // Write to test location
        mDatabase.child("test").child("connection").setValue(testData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Database write successful");
                    showToast("✓ Database write successful");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Database write failed: " + e.getMessage());
                    showToast("✗ Database write failed: " + e.getMessage());
                });
    }

    /**
     * Test reading sensor data
     */
    public void testSensorDataRead() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot test sensor read - not authenticated");
            showToast("Please login first");
            return;
        }

        mDatabase.child("sensors").child("currentReadings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "✓ Sensor data found:");

                            // Read temperature
                            Double temp = dataSnapshot.child("dht11/temperature").getValue(Double.class);
                            if (temp != null) {
                                Log.d(TAG, "  Temperature: " + temp + "°C");
                            }

                            // Read humidity
                            Double humidity = dataSnapshot.child("dht11/humidity").getValue(Double.class);
                            if (humidity != null) {
                                Log.d(TAG, "  Humidity: " + humidity + "%");
                            }

                            // Read alcohol
                            Double alcohol = dataSnapshot.child("mq3/alcoholRaw").getValue(Double.class);
                            if (alcohol != null) {
                                Log.d(TAG, "  Alcohol: " + alcohol);
                            }

                            // Read ammonia
                            Double ammonia = dataSnapshot.child("mq135/ammoniaRaw").getValue(Double.class);
                            if (ammonia != null) {
                                Log.d(TAG, "  Ammonia: " + ammonia);
                            }

                            // Read weight
                            Double weight = dataSnapshot.child("hx711/currentWeightGrams").getValue(Double.class);
                            if (weight != null) {
                                Log.d(TAG, "  Weight: " + weight + "g");
                            }

                            // Reading count
                            Long count = dataSnapshot.child("system/readingCount").getValue(Long.class);
                            if (count != null) {
                                Log.d(TAG, "  Reading Count: " + count);
                            }

                            showToast("✓ Successfully read sensor data");
                        } else {
                            Log.w(TAG, "⚠ No sensor data available yet");
                            showToast("⚠ No sensor data available. Is ESP32 running?");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "✗ Failed to read sensor data: " + databaseError.getMessage());
                        showToast("✗ Read failed: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Test database rules by attempting to read without authentication
     */
    public void testDatabaseRules() {
        // Sign out temporarily
        FirebaseAuth tempAuth = FirebaseAuth.getInstance();
        tempAuth.signOut();

        // Try to read data while signed out
        mDatabase.child("sensors").child("currentReadings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.w(TAG, "⚠ WARNING: Database rules allow unauthenticated read!");
                        showToast("⚠ Database allows public read - consider securing");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "✓ Database rules working - blocked unauthenticated access");
                        Log.d(TAG, "  Error: " + databaseError.getMessage());
                        showToast("✓ Database rules secure");
                    }
                });
    }

    /**
     * Run all tests
     */
    public void runAllTests() {
        Log.d(TAG, "=== Starting Firebase Tests ===");

        // Test 1: Authentication
        testAuthentication();

        // Test 2: Database Write (after 1 second)
        new android.os.Handler().postDelayed(() -> testDatabaseWrite(), 1000);

        // Test 3: Sensor Data Read (after 2 seconds)
        new android.os.Handler().postDelayed(() -> testSensorDataRead(), 2000);

        // Test 4: Database Rules (after 3 seconds)
        new android.os.Handler().postDelayed(() -> testDatabaseRules(), 3000);

        Log.d(TAG, "=== Tests Scheduled - Check Logcat ===");
    }

    private void showToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get Firebase configuration info
     */
    public void logFirebaseConfig() {
        Log.d(TAG, "=== Firebase Configuration ===");
        Log.d(TAG, "Database URL: " + FirebaseDatabase.getInstance().getReference().toString());

        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "Current User: " + mAuth.getCurrentUser().getEmail());
            Log.d(TAG, "User UID: " + mAuth.getCurrentUser().getUid());
        } else {
            Log.d(TAG, "No user signed in");
        }

        Log.d(TAG, "===========================");
    }
}
