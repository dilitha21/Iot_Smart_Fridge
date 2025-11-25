package com.example.smartfridge.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfridge.R;
import com.example.smartfridge.models.FoodItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private ArrayList<FoodItem> items = new ArrayList<>();

    public void setItems(ArrayList<FoodItem> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    public ArrayList<FoodItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = items.get(position);

        holder.txtName.setText(item.getName());
        holder.txtShelf.setText(item.getStorageShelf());
        holder.txtWeight.setText(item.getWeight() + " g");

        long now = System.currentTimeMillis();
        long diff = item.getExpiryDate() - now;
        long days = diff / (1000 * 60 * 60 * 24);

        if (days < 0) {
            holder.txtDaysLeft.setText("0");
            holder.txtDaysLabel.setText("Expired");
        } else {
            holder.txtDaysLeft.setText(String.valueOf(days));
            holder.txtDaysLabel.setText("Days");
        }

        // For now we just use launcher icon.
        holder.imgFood.setImageResource(R.mipmap.ic_launcher_round);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        ImageView imgFood;
        TextView txtName, txtShelf, txtDaysLeft, txtDaysLabel, txtWeight;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            txtName = itemView.findViewById(R.id.txtFoodName);
            txtShelf = itemView.findViewById(R.id.txtShelf);
            txtDaysLeft = itemView.findViewById(R.id.txtDaysLeft);
            txtDaysLabel = itemView.findViewById(R.id.txtDaysLabel);
            txtWeight = itemView.findViewById(R.id.txtWeight);
        }
    }
}
