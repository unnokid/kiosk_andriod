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

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderDetail;
    private Button btnBack;
    private ApiService apiService;

    private String kioskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        //kioskId 갱신
        kioskId = getIntent().getStringExtra("KIOSK_ID");
        tvOrderDetail = findViewById(R.id.tvOrderDetail);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        CopyUtils.setupCopyButton(this, R.id.btnCopy, "order_detail", R.id.tvOrderDetail);

        apiService = ApiClient.getApi();

        loadOrderDetail();
    }

    private void loadOrderDetail() {
        apiService.getSummary(kioskId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<SettlementResponse> call,
                                           Response<SettlementResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            tvOrderDetail.setText("실패: " + response.code());
                            return;
                        }
                        SettlementResponse r = response.body();
                        StringBuilder sb = new StringBuilder();
                        sb.append("총 주문 건수: ").append(String.format("%,d", r.totalOrderCount)).append("건\n");
                        sb.append("총 주문 금액: ").append(String.format("%,d", r.totalOrderPrice)).append("원\n\n");
                        if (r.orderList != null) {
                            for (SettlementResponse.OrderDto o : r.orderList) {

                                sb.append("[ 주문 ").append(o.orderNo).append("번 ]\n");
                                sb.append("시간 : ").append(formatTime(o.createDate)).append("\n");
                                sb.append("상태 : ").append(o.status).append("\n");
                                sb.append("결제수단 : ").append(paymentLabel(o.payment)).append("\n");
                                sb.append("총액 : ").append(String.format("%,d", o.total_amount))
                                        .append("원 / 받은금액 ")
                                        .append(String.format("%,d", o.paidAmount)).append("원\n");

                                sb.append("메뉴리스트 : {\n");
                                if (o.menuList != null) {
                                    for (SettlementResponse.OrderMenuDto m : o.menuList) {
                                        String name = m.menuName;
                                        if (m.optionName != null && !m.optionName.isEmpty()) {
                                            name += "(" + m.optionName + ")";
                                        }
                                        sb.append("    ")
                                                .append(name)
                                                .append(" : ")
                                                .append(m.quantity)
                                                .append("개,\n");
                                    }
                                }
                                sb.append("}\n\n");
                            }
                        }
                        tvOrderDetail.setText(sb.toString());
                    }

                    @Override
                    public void onFailure(Call<SettlementResponse> call, Throwable t) {
                        tvOrderDetail.setText("오류: " + t.getMessage());
                    }
                });
    }

    private String paymentLabel(String code) {
        if (code == null) return "기타";
        switch (code) {
            case "CASH":
                return "현금";
            case "KKP":
                return "카카오페이";
            case "ETC":
                return "기타";
            default:
                return code; // 알 수 없는 코드면 그대로
        }
    }
}
