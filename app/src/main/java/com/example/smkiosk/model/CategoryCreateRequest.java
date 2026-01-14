package com.example.smkiosk.model;

public class CategoryCreateRequest {
    private String email;
    private String name;

    public CategoryCreateRequest(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
