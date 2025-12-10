package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class DanhMuc {
    @SerializedName("_id")
    private String id;
    @SerializedName("category_id")
    private String categoryId;
    private String name;
    private String description;

    public DanhMuc() {
    }

    public DanhMuc(String id, String categoryId, String name, String description) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId != null ? categoryId : id;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}

