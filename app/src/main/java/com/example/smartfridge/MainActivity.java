package com.example.smartfridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfridge.adapters.FoodAdapter;
import com.example.smartfridge.models.FoodItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference foodsRef;

    private FoodAdapter adapter;
    private TextView txtAlerts;
    private Button btnCatAll, btnCatVeg, btnCatMeat, btnCatFruits, btnCatDrinks, btnCatSeason;

    private ArrayList<FoodItem> allFoods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        foodsRef = db.collection("foods");

        txtAlerts = findViewById(R.id.txtAlertsList);
        RecyclerView recycler = findViewById(R.id.recyclerFoods);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FoodAdapter();
        recycler.setAdapter(adapter);

        // category buttons
        btnCatAll = findViewById(R.id.btnCatAll);
        btnCatVeg = findViewById(R.id.btnCatVeg);
        btnCatMeat = findViewById(R.id.btnCatMeat);
        btnCatFruits = findViewById(R.id.btnCatFruits);
        btnCatDrinks = findViewById(R.id.btnCatDrinks);
        btnCatSeason = findViewById(R.id.btnCatSeason);

        // settings button
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        // search button – we will add SearchActivity later
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        // add food
        Button btnAdd = findViewById(R.id.btnAddFood);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddFoodActivity.class)));

        setupCategoryFilters();

        listenForFoodChanges();
    }

    private void setupCategoryFilters() {
        btnCatAll.setOnClickListener(v -> showCategory("All"));
        btnCatVeg.setOnClickListener(v -> showCategory("Veg"));
        btnCatMeat.setOnClickListener(v -> showCategory("Meat"));
        btnCatFruits.setOnClickListener(v -> showCategory("Fruits"));
        btnCatDrinks.setOnClickListener(v -> showCategory("Drinks"));
        btnCatSeason.setOnClickListener(v -> showCategory("Seasonings"));
    }

    private void showCategory(String category) {
        if (category.equals("All")) {
            adapter.setItems(new ArrayList<>(allFoods));
            return;
        }

        ArrayList<FoodItem> filtered = new ArrayList<>();
        for (FoodItem f : allFoods) {
            if (f.getCategory() != null && f.getCategory().equalsIgnoreCase(category)) {
                filtered.add(f);
            }
        }
        adapter.setItems(filtered);
    }

    private void listenForFoodChanges() {
        foodsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {

                if (value == null) return;

                allFoods.clear();
                for (QueryDocumentSnapshot doc : value) {
                    FoodItem item = doc.toObject(FoodItem.class);
                    item.setId(doc.getId());
                    allFoods.add(item);
                }
                adapter.setItems(new ArrayList<>(allFoods));
                updateAlerts();
            }
        });
    }

    private void updateAlerts() {
        StringBuilder sb = new StringBuilder();
        long now = System.currentTimeMillis();

        for (FoodItem f : allFoods) {
            long diff = f.getExpiryDate() - now;
            long days = diff / (1000 * 60 * 60 * 24);

            if (days < 0) {
                sb.append("• ").append(f.getName())
                        .append(" is expired!\n");
            } else if (days <= 2) {
                sb.append("• ").append(f.getName())
                        .append(" will expire in ")
                        .append(days).append(" day(s)\n");
            }

            if (f.getWeight() <= 50) { // you can tune threshold
                sb.append("• ").append(f.getName())
                        .append(" is running low.\n");
            }
        }

        if (sb.length() == 0) {
            txtAlerts.setText("No alerts.");
        } else {
            txtAlerts.setText(sb.toString());
        }
    }
}
