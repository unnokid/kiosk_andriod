package com.example.smkiosk.model;

public class OrderSaveResponse {
    private Long orderNo;

    private String createdt;

    public OrderSaveResponse(Long orderNo, String createdt) {
        this.orderNo = orderNo;
        this.createdt = createdt;
    }

    public String getCreatedt() {
        String s = createdt;
        s = s.replace('T', ' ');
        return (s.length() >= 16) ? s.substring(0, 16) : s;
    }

    public Long getOrderNo() {
        return orderNo;
    }
}

