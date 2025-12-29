package com.example.smkiosk.ui;

import static com.example.smkiosk.Util.TimeUtils.formatTime;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smkiosk.R;
import com.example.smkiosk.Util.ApiClient;
import com.example.smkiosk.Util.CopyUtils;
import com.example.smkiosk.api.ApiService;
import com.example.smkiosk.model.SettlementResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonationDetailActivity extends AppCompatActivity {

    private TextView tvDonationDetail;
    private Button btnBack;
    private ApiService apiService;

    private String kioskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);

        //kioskId 갱신
        kioskId = getIntent().getStringExtra("KIOSK_ID");
        tvDonationDetail = findViewById(R.id.tvDonationDetail);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        CopyUtils.setupCopyButton(this, R.id.btnCopy, "order_detail", R.id.tvDonationDetail);
        apiService = ApiClient.getApi();

        loadDonationDetail();
    }

    private void loadDonationDetail() {


        apiService.getSummary(kioskId)
                .enqueue(new Callback<SettlementResponse>() {
                    @Override
                    public void onResponse(Call<SettlementResponse> call,
                                           Response<SettlementResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            tvDonationDetail.setText("실패: " + response.code());
                            return;
                        }
                        SettlementResponse r = response.body();
                        StringBuilder sb = new StringBuilder();
                        sb.append("총 찬조 건수: ").append(String.format("%,d", r.totalDonationCount)).append("건\n");
                        sb.append("총 찬조 금액: ").append(String.format("%,d", r.totalDonationAmount)).append("원\n\n");
                        if (r.donationList != null) {
                            for (SettlementResponse.DonationDto d : r.donationList) {
                                sb.append("[ 찬조 ").append(d.donationNo).append("번 ]\n");
                                sb.append("시간 : ").append(formatTime(d.createDate)).append("\n");
                                sb.append("이름 : ").append(d.name).append("\n");
                                sb.append("금액 : ").append(String.format("%,d", d.amount)).append("원\n\n");
                            }
                        }
                        tvDonationDetail.setText(sb.toString());
                    }

                    @Override
                    public void onFailure(Call<SettlementResponse> call, Throwable t) {
                        tvDonationDetail.setText("오류: " + t.getMessage());
                    }
                });
    }
}

