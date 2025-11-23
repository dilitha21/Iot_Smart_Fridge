package com.example.smartfridge;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    private TextView weightTextView, weightLossTextView;
    private CardView alcoholCard, ammoniaCard, weightCard, tempHumidityCard;
    private ProgressBar progressBar;

    // Firebase (database only)
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;

    // Alert thresholds (matching your ESP32 code)
    private static final int ALCOHOL_THRESHOLD = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase database reference
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
        progressBar = findViewById(R.id.progressBar);

        // Hide the action bar entirely so the top black bar is removed
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
                    setStatus("No data available");
                    Toast.makeText(MainActivity.this, "No sensor data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                setStatus("Error: " + databaseError.getMessage());
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
                // Show readable label rather than technical name
                alcoholTextView.setText(String.format(Locale.US, "%.0f", alcoholRaw));

                if (alcoholAlarm != null && alcoholAlarm) {
                    alcoholCard.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red alert
                    setStatus("ALERT: Alcohol/Spoilage detected");
                } else if (alcoholRaw > ALCOHOL_THRESHOLD * 0.8) {
                    alcoholCard.setCardBackgroundColor(Color.parseColor("#FFEB3B")); // Yellow warning
                    setStatus("Warning: Elevated alcohol level");
                } else {
                    alcoholCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // Ammonia (MQ-135)
            Double ammoniaRaw = dataSnapshot.child("mq135/ammoniaRaw").getValue(Double.class);
            Boolean ammoniaAlarm = dataSnapshot.child("mq135/ammoniaAlarm").getValue(Boolean.class);

            if (ammoniaRaw != null) {
                ammoniaTextView.setText(String.format(Locale.US, "%.0f", ammoniaRaw));

                if (ammoniaAlarm != null && ammoniaAlarm) {
                    ammoniaCard.setCardBackgroundColor(Color.parseColor("#FFC107")); // Amber warning
                    setStatus("Warning: High ammonia levels");
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
                    setStatus("Warning: Significant weight loss (dehydration)");
                } else {
                    weightCard.setCardBackgroundColor(Color.WHITE);
                }
            }

            // System reading count (optional) - removed action bar subtitle usage
            Long readingCount = dataSnapshot.child("system/readingCount").getValue(Long.class);
            if (readingCount != null) {
                // previously updated action bar subtitle; left intentionally empty
            }

            // Update timestamp - removed action bar subtitle usage

            // If no current status was set by alarms above, set a healthy message
            setStatus("All systems normal");

        } catch (Exception e) {
            setStatus("Error parsing data");
            Log.e("MainActivity", "Error parsing data", e);
        }
    }

    private void setStatus(String message) {
        // Do not display status in action bar since action bar is hidden
        // also show a brief Toast to make important alerts more visible
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // remove or hide log action if present
        MenuItem logItem = menu.findItem(R.id.action_log_firebase);
        if (logItem != null) {
            logItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            startDataListener();
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            mDatabase.child("sensors").child("currentReadings").removeEventListener(valueEventListener);
        }
    }
}
