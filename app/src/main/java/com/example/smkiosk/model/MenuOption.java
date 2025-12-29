package com.example.smkiosk.model;

public class MenuOption {
    public String id;
    public String menuId;
    public String optionName;
    public int price;

    public MenuOption() {
    }

    public MenuOption(String id, String menuId, String optionName, int price) {
        this.id = id;
        this.menuId = menuId;
        this.optionName = optionName;
        this.price = price;
    }
}
