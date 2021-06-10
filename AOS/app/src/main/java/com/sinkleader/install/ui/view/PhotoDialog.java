package com.sinkleader.install.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sinkleader.install.R;

/**
 * Created by Snow on 12/2/2017.
 */

public class PhotoDialog extends BaseDialog {
    RelativeLayout bg;

    LinearLayout v_confirm1;
    LinearLayout v_confirm2;

    private ConfirmDialogListener m_listener;

    public PhotoDialog(Context context, ConfirmDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_photo);

        m_listener = listener;

        bg = findViewById(R.id.rly_bg);
        bg.setOnClickListener(v -> {
            onClose();
        });


        v_confirm1 = findViewById(R.id.btn_camera);
        v_confirm1.setOnClickListener(v -> {
            onCamera();
        });

        v_confirm2 = findViewById(R.id.btn_photo);
        v_confirm2.setOnClickListener(v -> {
            onPhoto();
        });
    }

    @Override
    public void onBackPressed() {
        onClose();
    }

    void onPhoto() {
        dismiss();
        if (m_listener != null) {
            m_listener.onPhoto();
        }
    }

    void onCamera() {
        dismiss();
        if (m_listener != null) {
            m_listener.onCamera();
        }
    }

    void onClose() {
        dismiss();
        if (m_listener != null) {
            m_listener.onClose();
        }
    }

    public interface ConfirmDialogListener {
        void onPhoto();
        void onCamera();
        void onClose();
    }
}
