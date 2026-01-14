package com.example.smkiosk.ui;

import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smkiosk.R;
import com.example.smkiosk.Util.ApiClient;
import com.example.smkiosk.api.ApiService;
import com.example.smkiosk.model.Category;
import com.example.smkiosk.model.CategoryCreateRequest;
import com.example.smkiosk.model.CategoryDeleteRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAdminActivity extends AppCompatActivity {

    private ApiService apiService;

    private String kioskId;
    private boolean changed = false;

    private Button btnAddCategory, btnBack;
    private ListView listCategories;

    private ArrayAdapter<String> adapter;
    private final List<Category> categories = new ArrayList<>();
    private final List<String> displayNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_admin);
        kioskId = getIntent().getStringExtra("KIOSK_ID");

        apiService = ApiClient.getApi();
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnBack = findViewById(R.id.btnBack);
        listCategories = findViewById(R.id.listCategories);


        btnBack.setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        listCategories.setAdapter(adapter);

        btnAddCategory.setOnClickListener(v -> showAddDialog());

        // 롱클릭 삭제
        listCategories.setOnItemLongClickListener((parent, view, position, id) -> {
            Category target = categories.get(position);
            confirmDelete(target);
            return true;
        });

        fetchCategories();

    }

    private void fetchCategories() {
        apiService.getCategoryList(kioskId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CategoryAdminActivity.this,
                            "조회 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                categories.clear();
                displayNames.clear();

                categories.addAll(response.body());
                for (Category c : categories) displayNames.add(c.getName());

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(CategoryAdminActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        EditText et = new EditText(this);
        et.setHint("카테고리명");
        et.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("카테고리 추가")
                .setView(et)
                .setPositiveButton("저장", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "카테고리명을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createCategory(name);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void createCategory(String name) {
        CategoryCreateRequest req = new CategoryCreateRequest(kioskId, name);

        apiService.addCategory(req).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CategoryAdminActivity.this,
                            "생성 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                changed = true;
                fetchCategories();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CategoryAdminActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Category target) {
        new AlertDialog.Builder(this)
                .setMessage("삭제할까요?\n" + target.getName())
                .setPositiveButton("삭제", (d, w) -> deleteCategory(target.getId()))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteCategory(long categoryId) {
        CategoryDeleteRequest req = new CategoryDeleteRequest(kioskId, categoryId);

        apiService.deleteCategory(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CategoryAdminActivity.this,
                            "삭제 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                changed = true;
                fetchCategories();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CategoryAdminActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        if (changed) setResult(RESULT_OK);
        super.finish();
    }

}
