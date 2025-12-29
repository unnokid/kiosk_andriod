package com.example.smkiosk.model;

public class MenuItem {
    public String id;          // menu_id
    public String name;        // "아메리카노", "케이크"
    public String category;    // "DRINK", "SNACK" ...
    public Integer price;  // 옵션 없는 기본 가격 (없으면 0)

    public MenuItem() {
    }

    public MenuItem(String id, String name, String category, Integer price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }
}

