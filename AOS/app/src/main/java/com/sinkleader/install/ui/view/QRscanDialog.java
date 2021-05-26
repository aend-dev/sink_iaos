package com.sinkleader.install.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sinkleader.install.R;

import droidninja.filepicker.views.SmoothCheckBox;

/**
 * Created by Snow on 12/2/2017.
 */

public class QRscanDialog extends BaseDialog {
    LinearLayout backgrond;
    TextView tv_content;
    EditText edit_sirlal;
    CheckBox checkBox;
    Button preButton;
    Button enterButton;

    private ConfirmDialogListener m_listener;

    public QRscanDialog(Context context, CharSequence content, ConfirmDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_qrscan);

        backgrond = findViewById(R.id.back_scanpop);
        backgrond.setOnClickListener(v->{
            dismiss();
        });

        tv_content = findViewById(R.id.txt_popup_qrscan);
        edit_sirlal = findViewById(R.id.edit_qrscan);
        checkBox = findViewById(R.id.check_qrscan);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)->{
            Log.d("checkBox", "isChecked : " + isChecked);

            if (isChecked){
                edit_sirlal.setText("확인 불가");
                edit_sirlal.setEnabled(false);
            }else{
                edit_sirlal.setText("");
                edit_sirlal.setEnabled(true);
            }
        });

        preButton = findViewById(R.id.back_btn_scan);
        preButton.setOnClickListener(view -> {
            dismiss();
        });
        enterButton = findViewById(R.id.open_btn_scan);
        enterButton.setOnClickListener(view -> {
            onConfirm(edit_sirlal.getText().toString());
        });

        ImageView img_close = findViewById(R.id.close_qrscan_dialog);
        img_close.setOnClickListener(v -> {
            dismiss();
        });

        m_listener = listener;
    }

    void onConfirm(String data) {
        dismiss();
        if (m_listener != null) {
            m_listener.onConfirm(data);
        }
    }

    public interface ConfirmDialogListener {
        void onConfirm(String sirial);
    }
}
