package com.example.smkiosk.model;

import java.util.List;
import java.util.Map;

public class SettlementResponse {
    public int totalAmount;
    public int totalCount;
    public int totalDonationCount;
    public int totalDonationAmount;
    public int totalOrderCount;
    public int totalOrderPrice;

    public Map<String,Long> paymentCount;
    public Map<String,Long> paymentAmount;
    public Map<String, Long> menuCountMap;
    public List<DonationDto> donationList;
    public List<OrderDto> orderList;

    public static class DonationDto {
        public long id;

        public long donationNo;
        public String createDate;


        public String name;
        public int amount;
    }

    // ★ 주문 단위
    public static class OrderDto {
        public long orderNo;              // 주문번호
        public String createDate;    // 주문시간
        public String status;        // 상태

        public String payment; // 결제수단
        public int total_amount;     // 총금액
        public int paidAmount;       // 받은금액
        public List<OrderMenuDto> menuList;  // 메뉴들
    }

    // ★ 주문 안의 메뉴 한 줄
    public static class OrderMenuDto {
        public String menuName;
        public String optionName;
        public int quantity;
    }
}

