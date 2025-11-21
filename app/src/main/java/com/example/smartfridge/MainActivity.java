package com.example.smartfridge;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private TextView tempTextView, humidityTextView, alcoholTextView, ammoniaTextView;
    private TextView weightTextView, weightLossTextView, readingCountTextView;
    private TextView statusTextView, lastUpdateTextView;
    private CardView alcoholCard, ammoniaCard, weightCard, tempHumidityCard;
    private ProgressBar progressBar;
    private Button logFirebaseButton; // NEW: button to trigger Firebase logging

    // Firebase Realtime Database (no auth required in-app)
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;

    // Alert thresholds (matching your ESP32 code)
    private static final int ALCOHOL_THRESHOLD = 2200;
    private static final int AMMONIA_THRESHOLD = 2000;
    private static final double WEIGHT_LOSS_THRESHOLD = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        initializeViews();

        // Start listening to Firebase data
        startDataListener();
    }

    private void initializeViews() {
        // Temperature and Humidity
        tempHumidityCard = findViewById(R.id.tempHumidityCard);
        tempTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        // Alcohol (MQ-3)
        alcoholCard = findViewById(R.id.alcoholCard);
        alcoholTextView = findViewById(R.id.alcoholTextView);

        // Ammonia (MQ-135)
        ammoniaCard = findViewById(R.id.ammoniaCard);
        ammoniaTextView = findViewById(R.id.ammoniaTextView);

        // Weight (HX711)
        weightCard = findViewById(R.id.weightCard);
        weightTextView = findViewById(R.id.weightTextView);
        weightLossTextView = findViewById(R.id.weightLossTextView);

        // System
        readingCountTextView = findViewById(R.id.readingCountTextView);
        statusTextView = findViewById(R.id.statusTextView);
        lastUpdateTextView = findViewById(R.id.lastUpdateTextView);
        progressBar = findViewById(R.id.progressBar);

        // NEW: log Firebase button wiring
        logFirebaseButton = findViewById(R.id.logFirebaseButton);
        logFirebaseButton.setOnClickListener(v -> {
            FirebaseTestUtility tester = new FirebaseTestUtility(MainActivity.this);
            tester.logFirebaseConfig();
            tester.runAllTests();
            Toast.makeText(MainActivity.this, "Firebase tests started (check Logcat)", Toast.LENGTH_SHORT).show();
        });

        // Set action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Fridge IoT Monitor");
        }
    }

    private void startDataListener() {
        progressBar.setVisibility(View.VISIBLE);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    updateUI(dataSnapshot);
                } else {
                    statusTextView.setText("No data available");
                    Toast.makeText(MainActivity.this, "No sensor data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                statusTextView.setText("Error: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };

        // Listen to the sensor data path
        mDatabase.child("sensors").child("currentReadings").addValueEventListener(valueEventListener);
    }

    private void updateUI(DataSnapshot dataSnapshot) {
        try {
            // Temperature and Humidity
            Double temperature = dataSnapshot.child("dht11/temperature").getValue(Double.class);
            Double humidity = dataSnapshot.child("dht11/humidity").getValue(Double.class);

            if (temperature != null && humidity != null) {
                tempTextView.setText(String.format(Locale.US, "%.1f°C", temperature));
                humidityTextView.setText(String.format(Locale.US, "%.0f%%", humidity));

                // Check temperature range
                if (temperature > 10 || temperature < 2) {
                    tempHumidityCard.setCardBackgroundColor(Color.parseColor("#FFEB3B")); // Yellow warning
                } else {
                    tempHumidityCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // Alcohol (MQ-3)
            Double alcoholRaw = dataSnapshot.child("mq3/alcoholRaw").getValue(Double.class);
            Boolean alcoholAlarm = dataSnapshot.child("mq3/alcoholAlarm").getValue(Boolean.class);

            if (alcoholRaw != null) {
                alcoholTextView.setText(String.format(Locale.US, "Raw: %.0f", alcoholRaw));

                if (alcoholAlarm != null && alcoholAlarm) {
                    alcoholCard.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red alert
                    alcoholTextView.setText(String.format(Locale.US, "⚠ SPOILAGE! Raw: %.0f", alcoholRaw));
                } else if (alcoholRaw > ALCOHOL_THRESHOLD * 0.8) {
                    alcoholCard.setCardBackgroundColor(Color.parseColor("#FFEB3B")); // Yellow warning
                } else {
                    alcoholCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // Ammonia (MQ-135)
            Double ammoniaRaw = dataSnapshot.child("mq135/ammoniaRaw").getValue(Double.class);
            Boolean ammoniaAlarm = dataSnapshot.child("mq135/ammoniaAlarm").getValue(Boolean.class);

            if (ammoniaRaw != null) {
                ammoniaTextView.setText(String.format(Locale.US, "Raw: %.0f", ammoniaRaw));

                if (ammoniaAlarm != null && ammoniaAlarm) {
                    ammoniaCard.setCardBackgroundColor(Color.parseColor("#FFC107")); // Amber warning
                    ammoniaTextView.setText(String.format(Locale.US, "⚠ High! Raw: %.0f", ammoniaRaw));
                } else {
                    ammoniaCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // Weight (HX711)
            Double currentWeight = dataSnapshot.child("hx711/currentWeightGrams").getValue(Double.class);
            Double weightLossPercent = dataSnapshot.child("hx711/weightLossPercent").getValue(Double.class);
            Boolean isSpoiledByWeight = dataSnapshot.child("hx711/isSpoiledByWeight").getValue(Boolean.class);

            if (currentWeight != null) {
                weightTextView.setText(String.format(Locale.US, "%.1f g", currentWeight));
            }

            if (weightLossPercent != null) {
                weightLossTextView.setText(String.format(Locale.US, "Loss: %.1f%%", weightLossPercent));

                if (isSpoiledByWeight != null && isSpoiledByWeight) {
                    weightCard.setCardBackgroundColor(Color.parseColor("#FF9800")); // Orange warning
                    weightLossTextView.setText(String.format(Locale.US, "⚠ DEHYDRATED: %.1f%%", weightLossPercent));
                } else {
                    weightCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // System
            Long readingCount = dataSnapshot.child("system/readingCount").getValue(Long.class);
            if (readingCount != null) {
                readingCountTextView.setText("Reading #" + readingCount);
            }

            // Update status
            updateStatus(alcoholAlarm, ammoniaAlarm, isSpoiledByWeight);

            // Update timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US);
            lastUpdateTextView.setText("Last update: " + sdf.format(new Date()));

        } catch (Exception e) {
            statusTextView.setText("Error parsing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatus(Boolean alcoholAlarm, Boolean ammoniaAlarm, Boolean spoiledByWeight) {
        if (alcoholAlarm != null && alcoholAlarm) {
            statusTextView.setText("⚠️ ALERT: Food Spoilage Detected (Alcohol)!");
            statusTextView.setTextColor(Color.RED);
        } else if (spoiledByWeight != null && spoiledByWeight) {
            statusTextView.setText("⚠️ WARNING: Food Dehydration Detected!");
            statusTextView.setTextColor(Color.parseColor("#FF9800"));
        } else if (ammoniaAlarm != null && ammoniaAlarm) {
            statusTextView.setText("⚠️ WARNING: High Ammonia Levels!");
            statusTextView.setTextColor(Color.parseColor("#FFC107"));
        } else {
            statusTextView.setText("✓ All systems normal");
            statusTextView.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            mDatabase.child("sensors").child("currentReadings").removeEventListener(valueEventListener);
        }
    }
}
