package com.example.smkiosk.model;

public class OrderSaveResponse {
    private Long orderNo;

    private String createdt;

    public OrderSaveResponse(Long orderNo, String createdt) {
        this.orderNo = orderNo;
        this.createdt = createdt;
    }

    public String getCreatedt() {
        return createdt;
    }

    public Long getOrderNo() {
        return orderNo;
    }
}

