package com.example.smartfridge;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfridge.models.FoodItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddFoodActivity extends AppCompatActivity {

    private EditText edtName, edtCategory, edtShelf, edtProducing, edtExpiry, edtWeight, edtRemark;
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtCategory = findViewById(R.id.edtCategory);
        edtShelf = findViewById(R.id.edtShelf);
        edtProducing = findViewById(R.id.edtProducing);
        edtExpiry = findViewById(R.id.edtExpiry);
        edtWeight = findViewById(R.id.edtWeight);
        edtRemark = findViewById(R.id.edtRemark);

        Button btnSave = findViewById(R.id.btnSaveFood);
        btnSave.setOnClickListener(v -> saveFood());
    }

    private void saveFood() {
        String name = edtName.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String shelf = edtShelf.getText().toString().trim();
        String prodStr = edtProducing.getText().toString().trim();
        String expStr = edtExpiry.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || shelf.isEmpty()
                || prodStr.isEmpty() || expStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long prodMillis = 0;
        long expMillis = 0;

        try {
            prodMillis = sdf.parse(prodStr).getTime();
            expMillis = sdf.parse(expStr).getTime();
        } catch (ParseException e) {
            Toast.makeText(this, "Date format must be yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);

        FoodItem item = new FoodItem(
                null,
                name,
                category,
                shelf,
                prodMillis,
                expMillis,
                weight,
                null
        );

        db.collection("foods")
                .add(item)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Food saved", Toast.LENGTH_SHORT).show();
                    finish(); // go back to dashboard
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
