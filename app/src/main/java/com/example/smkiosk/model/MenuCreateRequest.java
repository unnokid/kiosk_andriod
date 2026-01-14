package com.example.smkiosk.model;

import java.util.List;

public class MenuCreateRequest {
    private String email;

    private String name;

    private int price;

    public Integer count;

    private long categoryId;

    public List<MenuOptionReq> options;

    public MenuCreateRequest(String email, String name, int price, Integer count, long categoryId, List<MenuOptionReq> options) {
        this.email = email;
        this.name = name;
        this.price = price;
        this.count = count;
        this.categoryId = categoryId;
        this.options = options;
    }


}
