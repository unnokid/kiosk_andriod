package com.example.smkiosk.model;

import java.util.List;

public class MenuResponse {
    public long id;
    public String name;
    public String category;
    public int price;
    public List<MenuOptionResponse> options;
}
