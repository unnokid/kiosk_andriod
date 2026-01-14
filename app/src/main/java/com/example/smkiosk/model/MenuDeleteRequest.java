package com.example.smkiosk.model;

public class MenuDeleteRequest {
    private String email;
    private long menuId;

    public MenuDeleteRequest(String email, Long menuId) {
        this.email = email;
        this.menuId = menuId;
    }
}
