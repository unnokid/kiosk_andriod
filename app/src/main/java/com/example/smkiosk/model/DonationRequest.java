package com.example.smkiosk.model;

public class DonationRequest {
    private String email;
    private String name;
    private long amount;

    public DonationRequest(String email, String name, long amount) {
        this.email = email;
        this.name = name;
        this.amount = amount;
    }

}