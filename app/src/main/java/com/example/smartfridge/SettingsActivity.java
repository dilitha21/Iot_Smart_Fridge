package com.example.smartfridge;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v ->
                Toast.makeText(this, "Logout not implemented yet", Toast.LENGTH_SHORT).show());

        // Later you can make separate screens for zones, temperature, etc.
    }
}
