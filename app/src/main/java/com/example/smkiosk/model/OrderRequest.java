package com.example.smkiosk.model;

import java.util.List;

public class OrderRequest {
    public String email;          // 계정 ID
    public int paidAmount;         // 총 금액
    public List<CartRequest> carts;  // 주문 상세 리스트

    public String payment; // 결제 방식

    public static class CartRequest {
        public long menuId;
        public int quantity;
        public List<CartOptionRequest> cartOptions;
    }

    public static class CartOptionRequest {
        public String optionName;
    }
}