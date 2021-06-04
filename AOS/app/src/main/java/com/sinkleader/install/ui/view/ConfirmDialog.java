package com.sinkleader.install.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.sinkleader.install.R;

/**
 * Created by Snow on 12/2/2017.
 */

public class ConfirmDialog extends BaseDialog {
    TextView tv_content;
    TextView tv_confirm1;

    private ConfirmDialogListener m_listener;

    public ConfirmDialog(Context context, CharSequence content,String confirm1, ConfirmDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_confirm);

        tv_content = findViewById(R.id.tv_content);
        tv_confirm1 = findViewById(R.id.tv_confirm1);
        tv_confirm1.setOnClickListener(view -> {
            onConfirm1();
        });

        setProperty(content, confirm1);
        m_listener = listener;
    }

    void setProperty(CharSequence content, String confirm1) {

        tv_content.setVisibility(View.VISIBLE);
        tv_content.setText(content);

        if (confirm1.isEmpty() || confirm1 == null) {
            tv_confirm1.setVisibility(View.GONE);
        } else {
            tv_confirm1.setVisibility(View.VISIBLE);
            tv_confirm1.setText(confirm1);
        }

    }

    void onConfirm1() {
        dismiss();
        if (m_listener != null) {
            m_listener.onConfirm1();
        }
    }


    public interface ConfirmDialogListener {
        void onConfirm1();
    }
}
