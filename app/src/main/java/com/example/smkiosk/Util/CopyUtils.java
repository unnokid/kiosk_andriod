package com.example.smkiosk.Util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CopyUtils {

    public static void setupCopyButton(Activity activity,
                                       int copyBtnId,
                                       String label,
                                       int... textViewIds) {

        Button btnCopy = activity.findViewById(copyBtnId);
        if (btnCopy == null) return;

        btnCopy.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();

            for (int id : textViewIds) {
                TextView tv = activity.findViewById(id);
                if (tv == null) continue;

                if (sb.length() > 0) sb.append("\n");  // 구분 줄바꿈
                sb.append(tv.getText().toString());
            }

            String text = sb.toString();
            if (text.isEmpty()) return;

            ClipboardManager clipboard =
                    (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, "내용을 복사했습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}

