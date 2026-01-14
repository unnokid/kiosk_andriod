package com.example.smkiosk.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smkiosk.BuildConfig;
import com.example.smkiosk.R;
import com.example.smkiosk.Util.ApiClient;
import com.example.smkiosk.adapter.CartAdapter;
import com.example.smkiosk.adapter.CategoryAdapter;
import com.example.smkiosk.adapter.MenuAdapter;
import com.example.smkiosk.api.ApiService;
import com.example.smkiosk.model.DonationRequest;
import com.example.smkiosk.model.MenuItem;
import com.example.smkiosk.model.MenuOption;
import com.example.smkiosk.model.MenuOptionResponse;
import com.example.smkiosk.model.MenuResponse;
import com.example.smkiosk.model.OrderRequest;
import com.example.smkiosk.model.OrderSaveResponse;
import com.example.smkiosk.model.SelectedItem;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMenu, rvCart, rvCategory;
    private TextView tvTotal;
    private Button btnOrder, btnTotalPage, btnDonation;

    private String kioskId;

    private static final int REQ_BT = 1001;
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String ENC = "MS949";

    private BluetoothDevice selectedPrinter = null;

    private final List<MenuItem> allMenus = new ArrayList<>();
    private final List<MenuOption> allOptions = new ArrayList<>();
    private final List<SelectedItem> cart = new ArrayList<>();

    private MenuAdapter menuAdapter;
    private CartAdapter cartAdapter;

    private CategoryAdapter categoryAdapter;

    // Retrofit
    private ApiService apiService;

    private String selectedCategory = null;

    private final ActivityResultLauncher<Intent> summaryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMenusFromServer(); // 메뉴판 재조회
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //앱 전체 라이트 모드 고정
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View 연결
        rvMenu = findViewById(R.id.rvMenu);
        rvCart = findViewById(R.id.rvCart);
        rvCategory = findViewById(R.id.rvCategory);

        tvTotal = findViewById(R.id.tvTotal);
        btnOrder = findViewById(R.id.btnOrder);
        btnTotalPage = findViewById(R.id.btnTotalPage);
        btnDonation = findViewById(R.id.btnDonation);

        btnTotalPage.setOnClickListener(v -> {
            //Toast.makeText(this, "★★ 정산 버튼 클릭 ★★", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            intent.putExtra("KIOSK_ID", kioskId);   // 처음 받은 값
            summaryLauncher.launch(intent);
        });

        btnDonation.setOnClickListener(v -> {
            //Toast.makeText(this, "★★ 찬조 버튼 클릭 ★★", Toast.LENGTH_SHORT).show();
            showDonationDialog();
        });

        // 레이아웃 매니저
        rvMenu.setLayoutManager(new GridLayoutManager(this, 3));
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        // 상단 카테고리: 가로 스크롤
        rvCategory.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        // 장바구니 어댑터 (+ / - 버튼)
        cartAdapter = new CartAdapter(cart, new CartAdapter.CartListener() {
            @Override
            public void onPlus(int position) {
                if (position < 0 || position >= cart.size()) return;
                SelectedItem s = cart.get(position);
                s.count += 1;
                cartAdapter.notifyItemChanged(position);
                refreshTotal();
            }

            @Override
            public void onMinus(int position) {
                if (position < 0 || position >= cart.size()) return;
                SelectedItem s = cart.get(position);
                if (s.count > 1) {
                    s.count -= 1;
                    cartAdapter.notifyItemChanged(position);
                } else {
                    cart.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                }
                refreshTotal();
            }
        });
        rvCart.setAdapter(cartAdapter);

        // Retrofit 생성
        apiService = ApiClient.getApi();

        // 메뉴 불러오기
        showIdDialog();

        btnOrder.setOnClickListener(v -> sendOrder());

        refreshTotal();
    }

    private void loadMenusFromServer() {
        //Toast.makeText(this, "★★ 메뉴 갱신 여부 확인 ★★", Toast.LENGTH_SHORT).show();

        apiService.getMenuList(kioskId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<MenuResponse>> call, Response<List<MenuResponse>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                List<MenuResponse> resList = response.body();

                allMenus.clear();
                allOptions.clear();

                for (MenuResponse mr : resList) {
                    // MenuItem 변환 (앱 내부용)
                    MenuItem menu = new MenuItem();
                    menu.id = String.valueOf(mr.id);
                    menu.name = mr.name;
                    menu.category = mr.category;
                    menu.price = mr.price;
                    allMenus.add(menu);

                    // 옵션 변환
                    if (mr.options != null) {
                        for (MenuOptionResponse or : mr.options) {
                            MenuOption opt = new MenuOption();
                            opt.id = String.valueOf(or.id);
                            opt.menuId = menu.id;
                            opt.optionName = or.optionName;
                            opt.price = or.price;
                            allOptions.add(opt);
                        }
                    }
                }

                // 카테고리 목록 추출
                List<String> categories = new ArrayList<>();
                for (MenuItem m : allMenus) {
                    if (m.category != null && !categories.contains(m.category)) {
                        categories.add(m.category);
                    }
                }

                if (categories.isEmpty()) {
                    // 화면 비우고 싶으면 여기서 어댑터 처리
                    if (menuAdapter != null) menuAdapter.setItems(new ArrayList<>());
                    return;
                }

                // 선택 카테고리 유지 (없거나 사라졌으면 0번)
                String categoryToShow;
                if (selectedCategory != null && categories.contains(selectedCategory)) {
                    categoryToShow = selectedCategory;
                } else {
                    categoryToShow = categories.get(0);
                    selectedCategory = categoryToShow;
                }

                // 카테고리 어댑터 세팅
                if (categoryAdapter == null) {
                    categoryAdapter = new CategoryAdapter(categories, category -> {
                        selectedCategory = category; // ✅ 선택 저장

                        if (menuAdapter != null) {
                            menuAdapter.setItems(filterMenuByCategory(category));
                        }
                        categoryAdapter.setSelectedCategory(category);
                    });
                    rvCategory.setAdapter(categoryAdapter);
                } else {
                    categoryAdapter.setItems(categories);
                }

                // 선택 표시도 현재 선택으로 맞춤
                categoryAdapter.setSelectedCategory(categoryToShow);

                // 메뉴 어댑터 세팅 (현재 선택 카테고리 기준으로 갱신)
                if (menuAdapter == null) {
                    menuAdapter = new MenuAdapter(
                            filterMenuByCategory(categoryToShow),
                            allOptions,
                            MainActivity.this::onMenuClicked
                    );
                    rvMenu.setAdapter(menuAdapter);
                } else {
                    menuAdapter.setItems(filterMenuByCategory(categoryToShow));
                }
            }

            @Override
            public void onFailure(Call<List<MenuResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // 카테고리별 메뉴 필터
    private List<MenuItem> filterMenuByCategory(String category) {
        List<MenuItem> list = new ArrayList<>();
        for (MenuItem m : allMenus) {
            if (category.equals(m.category)) {
                list.add(m);
            }
        }

        // 옵션 개수 기준 오름차순 정렬
        Collections.sort(list, (m1, m2) -> {
            int c1 = getOptionCountForMenu(m1.id);
            int c2 = getOptionCountForMenu(m2.id);

            int cmp = Integer.compare(c1, c2); // 1차: 옵션 개수
            if (cmp != 0) return cmp;

            // 2차: 메뉴 이름
            return m1.name.compareToIgnoreCase(m2.name);
        });
        return list;
    }

    private int getOptionCountForMenu(String menuId) {
        int cnt = 0;
        for (MenuOption o : allOptions) {
            if (menuId.equals(o.menuId)) {
                cnt++;
            }
        }
        return cnt;
    }

    // 메뉴 클릭 시 (옵션 있는 메뉴면 옵션 선택 다이얼로그)
    private void onMenuClicked(MenuItem item) {

        List<MenuOption> options = getOptionsForMenu(item.id);

        if (!options.isEmpty()) {

            List<String> labels = new ArrayList<>();
            for (MenuOption o : options) {
                int price = (item.price != null ? item.price : 0) + o.price;
                labels.add(o.optionName + " " + String.format("%,d원", price));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.item_option_dialog,   // 커스텀 리스트 아이템
                    R.id.tvOption,
                    labels
            );

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(item.name + " 종류 선택");
            builder.setAdapter(adapter, (dialog, which) -> {
                addToCart(item, options.get(which));
            });

            AlertDialog dialog = builder.show();

            // 다크모드 대비 제목 색상 강화
            dialog.getWindow().getDecorView().getRootView().setBackgroundColor(Color.WHITE);
        } else {
            addToCart(item, null);
        }
    }

    private List<MenuOption> getOptionsForMenu(String menuId) {
        List<MenuOption> list = new ArrayList<>();
        for (MenuOption o : allOptions) {
            if (menuId.equals(o.menuId)) {
                list.add(o);
            }
        }
        return list;
    }

    // 장바구니에 추가 (같은 메뉴 + 옵션이면 수량만 +1)
    private void addToCart(MenuItem item, MenuOption option) {
        for (SelectedItem s : cart) {
            boolean sameMenu = s.menu.id.equals(item.id);
            boolean sameOpt =
                    (s.option == null && option == null) ||
                            (s.option != null && option != null &&
                                    s.option.id.equals(option.id));

            if (sameMenu && sameOpt) {
                s.count += 1;
                cartAdapter.notifyDataSetChanged();
                refreshTotal();
                return;
            }
        }

        SelectedItem newItem = new SelectedItem(item, option, 1);
        cart.add(newItem);
        cartAdapter.notifyDataSetChanged();
        refreshTotal();
    }

    // 총금액 계산
    private void refreshTotal() {
        int sum = 0;
        for (SelectedItem s : cart) {
            sum += s.getLinePrice();
        }
        tvTotal.setText("총합: " + String.format("%,d", sum) + "원");
    }

    // 주문하기
    private void sendOrder() {
        if (cart.isEmpty()) return;

        // 1. 장바구니 총 금액
        int total = cart.stream().mapToInt(SelectedItem::getLinePrice).sum();

        // 2. 커스텀 뷰 inflate
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_input, null);
        TextView tvTotalMsg = dialogView.findViewById(R.id.tvTotalMsg);
        EditText etPaid = dialogView.findViewById(R.id.etPaid);
        RadioGroup rgPayment = dialogView.findViewById(R.id.rgPayment);

        tvTotalMsg.setText("메뉴 총 금액: " + String.format("%,d원", total));
        etPaid.setText(String.valueOf(total));   // 기본값 = 총 금액

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("받은금액 입력")
                .setView(dialogView)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", null)
                .create();

        alertDialog.setOnShowListener(d -> {
            // 버튼 색
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.DKGRAY);

            // 확인 버튼 클릭 로직 override
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String text = etPaid.getText().toString().trim();
                        if (text.isEmpty()) {
                            Toast.makeText(this, "금액을 입력하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int paidAmount;
                        try {
                            paidAmount = Integer.parseInt(text);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "숫자만 입력하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int checkedId = rgPayment.getCheckedRadioButtonId();
                        if (checkedId == -1) {
                            Toast.makeText(this, "결제 방식을 선택하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String paymentType;
                        if (checkedId == R.id.rbCash) paymentType = "CASH";
                        else if (checkedId == R.id.rbKakao) paymentType = "KKP";
                        else paymentType = "ETC";

                        // 실제 주문 전송
                        sendOrderInternal(paidAmount, paymentType);

                        alertDialog.dismiss();
                    });
        });

        alertDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.sb_bg))
        );

        alertDialog.show();
    }


    private void sendOrderInternal(int paidAmount, String paymentType) {
        // 장바구니 총액 다시 계산 (검증용)
        int total = 0;
        for (SelectedItem s : cart) {
            total += s.getLinePrice();
        }

        OrderRequest req = new OrderRequest();
        req.email = kioskId;
        req.paidAmount = paidAmount;
        req.payment = paymentType;
        req.carts = new ArrayList<>();

        for (SelectedItem s : cart) {
            OrderRequest.CartRequest cr = new OrderRequest.CartRequest();

            try {
                cr.menuId = Long.parseLong(s.menu.id);
            } catch (NumberFormatException e) {
                cr.menuId = 0L;
            }

            cr.quantity = s.count;

            List<OrderRequest.CartOptionRequest> optList = new ArrayList<>();
            if (s.option != null) {
                OrderRequest.CartOptionRequest cor = new OrderRequest.CartOptionRequest();
                cor.optionName = s.option.optionName;
                optList.add(cor);
            }
            cr.cartOptions = optList;

            req.carts.add(cr);
        }

        // 프린터용으로 cart 복사 (clear 하기 전에)
        final List<SelectedItem> printTarget = new ArrayList<>(cart);

        String json = new Gson().toJson(req);
        Log.d("SMKIOSK", json);   // 실제 전송되는 JSON 확인

        apiService.sendOrder(req).enqueue(new Callback<>() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
            @Override
            public void onResponse(Call<OrderSaveResponse> call,
                                   Response<OrderSaveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderSaveResponse res = response.body();
                    String receipt = buildReceiptString(res, printTarget);
                    Log.d("SMKIOSK", receipt);

                    //TODO : 실제 배포시에는 써야함
                    printReceipt(receipt, response.body().getOrderNo());

                    cart.clear();
                    cartAdapter.notifyDataSetChanged();
                    refreshTotal();
                    Toast.makeText(MainActivity.this,
                            response.body().getOrderNo() + "번 주문이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("ORDER_ERR", "code=" + response.code() + ", body=" + err);

                    Toast.makeText(MainActivity.this,
                            "주문 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderSaveResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "주문 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    // 영수증 문자열 생성
    private String buildReceiptString(OrderSaveResponse response, List<SelectedItem> items) {
        StringBuilder sb = new StringBuilder();
        String line = "------------------------\n";

        sb.append(line);
        sb.append("●주문번호: ").append(response.getOrderNo()).append("번\n");
        sb.append("●시간: ").append(response.getCreatedt()).append("\n");
        sb.append(line);

        for (SelectedItem s : items) {
            String menuName = s.menu.name;
            String optionName = (s.option != null) ? s.option.optionName : "";

            sb.append("* ")
                    .append(menuName);

            if (!optionName.isEmpty()) {
                sb.append("(").append(optionName).append(")");
            }

            sb.append(" : ")
                    .append(s.count)
                    .append("개\n");
        }

        sb.append(line).append("\n\n");
        return sb.toString();
    }

    // 블루투스 프린터로 출력
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void printReceipt(String text, Long orderNo) {
        if (!ensureBtPermissionOrRequest()) return;

        if (selectedPrinter == null) {
            showBondedPrinterPicker(() -> printReceipt(text, orderNo)); // 선택 후 재시도
            return;
        }

        BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
        if (ad == null || !ad.isEnabled()) {
            Toast.makeText(this, "블루투스를 켜주세요", Toast.LENGTH_LONG).show();
            return;
        }

        final BluetoothDevice device = selectedPrinter;

        new Thread(() -> {
            BluetoothSocket socket = null;
            OutputStream os = null;

            try {
                // 연결 안정화
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        ad.cancelDiscovery();
                    } else {
                        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                            ad.cancelDiscovery();
                        }
                    }
                } catch (Exception ignored) {
                }
                SystemClock.sleep(200);

                // insecure 먼저가 더 잘 되는 프린터가 많음
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();
                } catch (Exception ignored) {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();
                }

                SystemClock.sleep(200);

                os = socket.getOutputStream();
                os.write(new byte[]{0x1B, 0x40}); // init

                os.write(new byte[]{0x1B, 0x61, 0x01}); // center
                os.write(new byte[]{0x1B, 0x45, 0x01}); // bold on
                os.write(new byte[]{0x1D, 0x42, 0x01}); // invert on (선택)
                os.write(new byte[]{0x1D, 0x21, 0x22}); // 3x3

                os.write(("  " + orderNo + "  \n").getBytes(ENC)); // 숫자만 + 좌우 공백

                // 원복(역상/크기/굵기만 원복, 정렬은 아래에서 처리)
                os.write(new byte[]{0x1D, 0x42, 0x00}); // invert off
                os.write(new byte[]{0x1B, 0x45, 0x00}); // bold off
                os.write(new byte[]{0x1D, 0x21, 0x11}); // 2x2
                os.write(new byte[]{0x1B, 0x61, 0x00}); // left
                os.write(text.getBytes(ENC));

                // 여백 5줄 + 커트
                os.write(new byte[]{0x0A, 0x0A, 0x0A, 0x0A, 0x0A});
                os.write(new byte[]{0x1D, 0x56, 0x01}); // cut
                os.flush();

                runOnUiThread(() -> Toast.makeText(this, "출력 성공", Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                final String msg = "출력 실패: " + e.getClass().getSimpleName() + " / " + e.getMessage();
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("BT 오류")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                        .show());
            } finally {
                try {
                    if (os != null) os.close();
                } catch (Exception ignored) {
                }
                try {
                    if (socket != null) socket.close();
                } catch (Exception ignored) {
                }
            }
        }).start();
    }


    private void showDonationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_donation, null);
        EditText etName = dialogView.findViewById(R.id.etDonationName);
        EditText etAmount = dialogView.findViewById(R.id.etDonationAmount);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("찬조 입력")
                .setView(dialogView)
                .setPositiveButton("확인", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();

                    if (name.isEmpty() || amountStr.isEmpty()) return;

                    long amount = Long.parseLong(amountStr);

                    DonationRequest req =
                            new DonationRequest(kioskId, name, amount);

                    apiService.addDonation(req).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            Toast.makeText(MainActivity.this,
                                    "찬조 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MainActivity.this,
                                    "찬조 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            t.printStackTrace();
                        }
                    });
                })
                .setNegativeButton("취소", null)
                .create();

        alertDialog.setOnShowListener(d -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.DKGRAY);
        });

        alertDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.sb_bg))
        );
        alertDialog.show();
    }

    private void showIdDialog() {
        EditText input = new EditText(this);
        input.setHint("아이디 입력");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("키오스크 ID 입력")
                .setView(input)
                .setCancelable(false)   // ID 입력 전엔 못 나가게
                .setPositiveButton("확인", (d, w) -> {
                    String id = input.getText().toString().trim();
                    if (id.isEmpty()) {
                        Toast.makeText(this, "ID를 입력하세요.", Toast.LENGTH_SHORT).show();
                        showIdDialog();  // 다시 요청
                        return;
                    }
                    kioskId = id;

                    // 한 번 저장해두고 다음 실행부터 재사용
                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("KIOSK_ID", kioskId)
                            .apply();

                    loadMenusFromServer();
                })
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK);
        });

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.sb_bg))
        );

        dialog.show();
    }

    private boolean ensureBtPermissionOrRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;

        List<String> req = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            req.add(Manifest.permission.BLUETOOTH_CONNECT);
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            req.add(Manifest.permission.BLUETOOTH_SCAN);

        if (!req.isEmpty()) {
            requestPermissions(req.toArray(new String[0]), REQ_BT);
            Toast.makeText(this, "블루투스 권한 허용 후 다시 시도", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showBondedPrinterPicker(Runnable afterPick) {
        if (!ensureBtPermissionOrRequest()) return;

        BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
        if (ad == null || !ad.isEnabled()) {
            Toast.makeText(this, "블루투스를 켜주세요", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "CONNECT 권한 없음", Toast.LENGTH_LONG).show();
            return;
        }

        Set<BluetoothDevice> bonded = ad.getBondedDevices();
        if (bonded == null || bonded.isEmpty()) {
            Toast.makeText(this, "페어링된 기기 없음", Toast.LENGTH_LONG).show();
            return;
        }

        List<BluetoothDevice> list = new ArrayList<>(bonded);
        String[] labels = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            BluetoothDevice d = list.get(i);
            String name = (d.getName() == null ? "(no-name)" : d.getName());
            labels[i] = name + " / " + d.getAddress();
        }

        new AlertDialog.Builder(this)
                .setTitle("프린터 선택(페어링 목록)")
                .setItems(labels, (dlg, which) -> {
                    selectedPrinter = list.get(which);
                    Toast.makeText(this, "선택됨: " + labels[which], Toast.LENGTH_LONG).show();
                    if (afterPick != null) afterPick.run();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
