package com.example.smartfridge;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfridge.adapters.FoodAdapter;
import com.example.smartfridge.models.FoodItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView recyclerView;
    private FoodAdapter adapter;

    private FirebaseFirestore db;
    private ArrayList<FoodItem> allFoods = new ArrayList<>(); // store all
    private ArrayList<FoodItem> filteredFoods = new ArrayList<>(); // filtered results

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerSearchFoods);

        adapter = new FoodAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAllFoods(); // load data first

        // live typing search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoods(s.toString());    // filter when typing
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // load all foods from firebase only one time
    private void loadAllFoods() {
        db.collection("foods").get()
                .addOnSuccessListener(snap -> {
                    allFoods.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        item.setId(doc.getId());
                        allFoods.add(item);
                    }
                    adapter.setItems(new ArrayList<>(allFoods)); // show all initially
                });
    }

    private void filterFoods(String text) {
        filteredFoods.clear();

        for (FoodItem f : allFoods) {
            if (f.getName() != null && f.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredFoods.add(f);
            }
        }

        adapter.setItems(filteredFoods);
    }
}
