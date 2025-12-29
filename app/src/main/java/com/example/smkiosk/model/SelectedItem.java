package com.example.smkiosk.model;

public class SelectedItem {
    public MenuItem menu;
    public MenuOption option;
    public int count;


    public SelectedItem(MenuItem menu, MenuOption option, int count) {
        this.menu = menu;
        this.option = option;
        this.count = count;
    }

    public int getUnitPrice() {
        int base  = (menu.price != null) ? menu.price : 0;
        int extra = (option != null) ? option.price : 0;
        return base + extra;   // ★ 기본가 + 옵션추가금
    }

    public int getLinePrice() {
        return getUnitPrice() * count;
    }
}
