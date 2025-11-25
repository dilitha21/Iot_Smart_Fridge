package com.example.smartfridge.models;

public class FoodItem {

    private String id;          // Firestore document id
    private String name;
    private String category;    // "Veg", "Meat", "Fruits", etc.
    private String storageShelf; // "Upper", "Middle", "Bottom"
    private long producingDate; // store as timestamp (milliseconds)
    private long expiryDate;    // timestamp
    private double weight;      // grams
    private String imageUrl;    // later if you add Firebase Storage

    public FoodItem() {
        // empty constructor needed for Firestore
    }

    public FoodItem(String id, String name, String category,
                    String storageShelf, long producingDate,
                    long expiryDate, double weight, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.storageShelf = storageShelf;
        this.producingDate = producingDate;
        this.expiryDate = expiryDate;
        this.weight = weight;
        this.imageUrl = imageUrl;
    }

    // ------- getters and setters ---------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStorageShelf() { return storageShelf; }
    public void setStorageShelf(String storageShelf) { this.storageShelf = storageShelf; }

    public long getProducingDate() { return producingDate; }
    public void setProducingDate(long producingDate) { this.producingDate = producingDate; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
