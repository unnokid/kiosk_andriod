package com.example.smkiosk.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smkiosk.R;

public class AdminActivity extends AppCompatActivity {

    private String kioskId;

    private final ActivityResultLauncher<Intent> categoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) setResult(RESULT_OK);
            });

    private final ActivityResultLauncher<Intent> menuLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) setResult(RESULT_OK);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        kioskId = getIntent().getStringExtra("KIOSK_ID");

        findViewById(R.id.btnAdminBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCategory).setOnClickListener(v -> {
            Intent i = new Intent(this, CategoryAdminActivity.class);
            i.putExtra("KIOSK_ID", kioskId);
            categoryLauncher.launch(i);
        });

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent i = new Intent(this, MenuAdminActivity.class);
            i.putExtra("KIOSK_ID", kioskId);
            menuLauncher.launch(i);
        });
    }
}
