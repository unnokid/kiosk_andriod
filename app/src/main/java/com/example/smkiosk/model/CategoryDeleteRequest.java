package com.example.smkiosk.model;

public class CategoryDeleteRequest {
    private String email;
    private Long categoryId;

    public CategoryDeleteRequest(String email, Long categoryId) {
        this.email = email;
        this.categoryId = categoryId;
    }
}
