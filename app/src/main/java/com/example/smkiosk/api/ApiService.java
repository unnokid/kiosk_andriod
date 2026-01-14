package com.example.smkiosk.api;

import com.example.smkiosk.model.Category;
import com.example.smkiosk.model.CategoryCreateRequest;
import com.example.smkiosk.model.CategoryDeleteRequest;
import com.example.smkiosk.model.DonationRequest;
import com.example.smkiosk.model.MenuCreateRequest;
import com.example.smkiosk.model.MenuDeleteRequest;
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

    @GET("category/all")
    Call<List<Category>> getCategoryList(@Query("email") String email);

    @POST("category/plus")
    Call<Void> addCategory(@Body CategoryCreateRequest request);

    @POST("category/minus")
    Call<Void> deleteCategory(@Body CategoryDeleteRequest request);

    @POST("menu/plus")
    Call<Void> addMenu(@Body MenuCreateRequest req);

    @POST("menu/minus")
    Call<Void> deleteMenu(@Body MenuDeleteRequest req);
}
