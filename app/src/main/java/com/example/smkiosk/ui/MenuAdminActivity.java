package com.example.smkiosk.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smkiosk.R;
import com.example.smkiosk.Util.ApiClient;
import com.example.smkiosk.api.ApiService;
import com.example.smkiosk.model.Category;
import com.example.smkiosk.model.MenuCreateRequest;
import com.example.smkiosk.model.MenuDeleteRequest;
import com.example.smkiosk.model.MenuOptionReq;
import com.example.smkiosk.model.MenuResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAdminActivity extends AppCompatActivity {

    private ApiService apiService;
    private String kioskId;
    private boolean changed = false;
    private Button btnAddMenu, btnBack;
    private ListView listMenus;
    private ArrayAdapter<String> adapter;
    private final List<MenuResponse> menus = new ArrayList<>();
    private final List<String> displayNames = new ArrayList<>();

    // 메뉴 추가 시 카테고리 선택용
    private final List<Category> categories = new ArrayList<>();
    private final List<String> categoryNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        kioskId = getIntent().getStringExtra("KIOSK_ID");

        apiService = ApiClient.getApi();

        btnAddMenu = findViewById(R.id.btnAddMenu);
        btnBack = findViewById(R.id.btnBack);
        listMenus = findViewById(R.id.listMenus);

        btnBack.setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        listMenus.setAdapter(adapter);

        btnAddMenu.setOnClickListener(v -> showAddMenuDialog());

        // 롱클릭 삭제
        listMenus.setOnItemLongClickListener((parent, view, position, id) -> {
            MenuResponse target = menus.get(position);
            confirmDelete(target);
            return true;
        });

        fetchCategories();
        fetchMenus();
    }

    private void fetchMenus() {
        apiService.getMenuList(kioskId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<MenuResponse>> call, Response<List<MenuResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MenuAdminActivity.this, "조회 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                menus.clear();
                displayNames.clear();

                menus.addAll(response.body());
                for (MenuResponse m : menus) {
                    String cat = (m.category == null ? "" : m.category);
                    displayNames.add(m.name + " (" + cat + ") - " + String.format("%,d원", m.price));

                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<MenuResponse>> call, Throwable t) {
                Toast.makeText(MenuAdminActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCategories() {
        apiService.getCategoryList(kioskId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                categories.clear();
                categoryNames.clear();

                categories.addAll(response.body());
                for (Category c : categories) categoryNames.add(c.getName());
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
            }
        });
    }

    private void showAddMenuDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "카테고리를 먼저 생성하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        View v = getLayoutInflater().inflate(R.layout.dialog_add_menu, null);

        EditText etName = v.findViewById(R.id.etMenuName);
        EditText etPrice = v.findViewById(R.id.etMenuPrice);
        Spinner spCategory = v.findViewById(R.id.spCategory);

        Button btnAddOptionRow = v.findViewById(R.id.btnAddOptionRow);
        LinearLayout optionContainer = v.findViewById(R.id.optionContainer);

        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER);

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spCategory.setAdapter(spAdapter);

        // 옵션 기본 1줄
        addOptionRow(optionContainer);
        btnAddOptionRow.setOnClickListener(x -> addOptionRow(optionContainer));

        new AlertDialog.Builder(this)
                .setTitle("메뉴 추가")
                .setView(v)
                .setPositiveButton("저장", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "메뉴명/가격을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int price;
                    try {
                        price = Integer.parseInt(priceStr);
                    } catch (Exception e) {
                        Toast.makeText(this, "가격은 숫자만 입력", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long categoryId = categories.get(spCategory.getSelectedItemPosition()).getId();

                    List<MenuOptionReq> options = collectOptions(optionContainer);
                    if (options.isEmpty()) {
                        Toast.makeText(this, "옵션을 1개 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MenuCreateRequest req = new MenuCreateRequest(kioskId, name, price, null, categoryId, options);

                    createMenu(req);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void addOptionRow(LinearLayout optionContainer) {
        View row = getLayoutInflater().inflate(R.layout.option_row, optionContainer, false);

        Button btnRemove = row.findViewById(R.id.btnRemoveRow);
        btnRemove.setOnClickListener(v -> optionContainer.removeView(row));

        optionContainer.addView(row);
    }

    private List<MenuOptionReq> collectOptions(LinearLayout optionContainer) {
        List<MenuOptionReq> list = new ArrayList<>();

        for (int i = 0; i < optionContainer.getChildCount(); i++) {
            View row = optionContainer.getChildAt(i);
            EditText etOptName = row.findViewById(R.id.etOptName);
            EditText etOptPrice = row.findViewById(R.id.etOptPrice);

            String optName = etOptName.getText().toString().trim();
            String optPriceStr = etOptPrice.getText().toString().trim();

            if (optName.isEmpty()) continue;

            int optPrice = 0;
            if (!optPriceStr.isEmpty()) {
                try {
                    optPrice = Integer.parseInt(optPriceStr);
                } catch (Exception ignored) {
                    optPrice = 0;
                }
            }

            list.add(new MenuOptionReq(optName, optPrice));
        }
        return list;
    }

    private void createMenu(MenuCreateRequest req) {
        apiService.addMenu(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MenuAdminActivity.this, "생성 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                changed = true;
                fetchMenus();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MenuAdminActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(MenuResponse target) {
        new AlertDialog.Builder(this)
                .setMessage("삭제할까요?\n" + target.name)
                .setPositiveButton("삭제", (d, w) -> deleteMenu(target.id))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteMenu(long menuId) {
        MenuDeleteRequest req = new MenuDeleteRequest(kioskId, menuId);

        apiService.deleteMenu(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MenuAdminActivity.this, "삭제 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                changed = true;
                fetchMenus();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MenuAdminActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        if (changed) setResult(RESULT_OK);
        super.finish();
    }
}
