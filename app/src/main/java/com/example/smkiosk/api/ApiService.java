package com.example.smkiosk.api;

import com.example.smkiosk.model.DonationRequest;
import com.example.smkiosk.model.MenuResponse;
import com.example.smkiosk.model.OrderRequest;
import com.example.smkiosk.model.OrderSaveResponse;
import com.example.smkiosk.model.SettlementResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("menu/all")
    Call<List<MenuResponse>> getMenuList(
            @Query("email") String email
    );

    @GET("admin/summary")
    Call<SettlementResponse> getSummary(
            @Query("email") String email
    );

    @POST("order/plus")
    Call<OrderSaveResponse> sendOrder(@Body OrderRequest request);

    @POST("donation/plus")
    Call<Void> addDonation(@Body DonationRequest request);

}
