package com.example.smkiosk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smkiosk.R;
import com.example.smkiosk.Util.ApiClient;
import com.example.smkiosk.Util.CopyUtils;
import com.example.smkiosk.api.ApiService;
import com.example.smkiosk.model.SettlementResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryActivity extends AppCompatActivity {

    private TextView tvSummary, tvMenuCount;
    private Button btnBack, btnOrderDetail, btnDonationDetail;
    private ApiService apiService;
    private SettlementResponse lastSummary;   // 상세 화면에서 다시 써도 되고, 이메일만 넘겨서 다시 호출해도 됨

    private String kioskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvSummary = findViewById(R.id.tvSummary);
        tvMenuCount = findViewById(R.id.tvMenuCount);
        btnBack = findViewById(R.id.btnBack);
        btnOrderDetail = findViewById(R.id.btnOrderDetail);
        btnDonationDetail = findViewById(R.id.btnDonationDetail);

        btnBack.setOnClickListener(v -> finish());
        CopyUtils.setupCopyButton(this, R.id.btnCopy, "summary", R.id.tvSummary, R.id.tvMenuCount);
        kioskId = getIntent().getStringExtra("KIOSK_ID");


        apiService = ApiClient.getApi();

        loadSummary();

        btnOrderDetail.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, OrderDetailActivity.class);
            intent.putExtra("KIOSK_ID", kioskId);   // 처음 받은 값
            startActivity(intent);
        });

        btnDonationDetail.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, DonationDetailActivity.class);
            intent.putExtra("KIOSK_ID", kioskId);   // 처음 받은 값
            startActivity(intent);
        });
    }

    private void loadSummary() {
        apiService.getSummary(kioskId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<SettlementResponse> call,
                                           Response<SettlementResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            tvSummary.setText("실패: " + response.code());
                            return;
                        }
                        lastSummary = response.body();
                        bindSummary(lastSummary);
                    }

                    @Override
                    public void onFailure(Call<SettlementResponse> call, Throwable t) {
                        tvSummary.setText("오류: " + t.getMessage());
                    }
                });
    }

    private void bindSummary(SettlementResponse r) {
        // 1) 요약 숫자들
        StringBuilder sb = new StringBuilder();
        sb.append("총 거래건수: ").append(String.format("%,d", r.totalCount)).append("건\n");
        sb.append("총 결제금액: ").append(String.format("%,d", r.totalAmount)).append("원\n\n");

        sb.append("찬조 건수  : ").append(String.format("%,d", r.totalDonationCount)).append("건\n");
        sb.append("찬조 금액  : ").append(String.format("%,d", r.totalDonationAmount)).append("원\n\n");

        sb.append("주문 건수  : ").append(String.format("%,d", r.totalOrderCount)).append("건\n");
        sb.append("주문 금액  : ").append(String.format("%,d", r.totalOrderPrice)).append("원\n");

        sb.append("주문 결제수단별\n");

        Map<String, Long> cntMap = r.paymentCount;
        Map<String, Long> amtMap = r.paymentAmount;

        // 표시명 매핑
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("CASH", "현금");
        labelMap.put("KKP", "카카오페이");
        labelMap.put("ETC", "기타");

        if (cntMap != null && !cntMap.isEmpty()) {
            String[] order = {"CASH", "KKP", "ETC"};

            // 지정 순서대로
            for (String p : order) {
                long cnt = cntMap.getOrDefault(p, 0L);
                if (cnt == 0) continue;

                long amt = (amtMap != null) ? amtMap.getOrDefault(p, 0L) : 0L;
                String label = labelMap.getOrDefault(p, p);

                sb.append(" - ")
                        .append(label)
                        .append(" : ")
                        .append(String.format("%,d", cnt)).append("건 : ")
                        .append(String.format("%,d", amt)).append("원\n");
            }

            // order에 없는 결제수단도 출력
            for (String p : cntMap.keySet()) {
                boolean known = false;
                for (String k : order)
                    if (k.equals(p)) {
                        known = true;
                        break;
                    }
                if (known) continue;

                long cnt = cntMap.getOrDefault(p, 0L);
                if (cnt == 0) continue;

                long amt = (amtMap != null) ? amtMap.getOrDefault(p, 0L) : 0L;
                String label = labelMap.getOrDefault(p, p);

                sb.append(" - ")
                        .append(label)
                        .append(" : ")
                        .append(String.format("%,d", cnt)).append("건 : ")
                        .append(String.format("%,d", amt)).append("원\n");
            }
        } else {
            sb.append(" - 없음\n");
        }


        tvSummary.setText(sb.toString());

        // 2) 메뉴별 판매 수
        StringBuilder menuSb = new StringBuilder();
        menuSb.append("메뉴별 판매수\n");
        if (r.menuCountMap != null) {
            for (Map.Entry<String, Long> e : r.menuCountMap.entrySet()) {
                menuSb.append(" - ")
                        .append(e.getKey())
                        .append(" : ")
                        .append(e.getValue())
                        .append("개\n");
            }
        }
        tvMenuCount.setText(menuSb.toString());
    }
}
