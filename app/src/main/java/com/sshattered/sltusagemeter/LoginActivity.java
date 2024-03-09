package com.sshattered.sltusagemeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editEmail, editPass;
    private Dialog alertDialog;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Glide.with(LoginActivity.this).asGif().load(R.raw.rain).into((ImageView) findViewById(R.id.imgAnimation));

        editEmail = findViewById(R.id.editEmail);
        editPass = findViewById(R.id.editPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        String[] st = SLTHandler.Instance(LoginActivity.this).GetDetails();
        editEmail.setText(st[0]);
        editPass.setText(st[1]);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnLogin.getId()) {
            if (editEmail.getText().length() != 0 &&
                    editPass.getText().length() != 0) {
                ShowLoadingDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SLTHandler.Instance(LoginActivity.this).SetLogin(editEmail.getText().toString(), editPass.getText().toString());
                        boolean state = SLTHandler.Instance(LoginActivity.this).GetLoginTelephone();
                        if (state) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowUsageDialog();
                                }
                            });
                        }
                        alertDialog.dismiss();
                    }
                }).start();
            }
        }
    }

    private void ShowUsageDialog(){
        PackUsage packUsage = SLTHandler.Instance(LoginActivity.this)._packUsage;
        View view = getLayoutInflater().inflate(R.layout.usage_layout, null, false);
        CustomBar customBar = view.findViewById(R.id.customBar);
        customBar.setPackage(packUsage.pack);
        customBar.setVolumes(packUsage.total,
                packUsage.day,
                packUsage.night);
        Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(view);
        dialog.create();

        alertDialog = dialog;
        alertDialog.show();
        alertDialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.90),
                (int)(getResources().getDisplayMetrics().heightPixels * 0.90)
        );
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        view.invalidate();
    }

    private void ShowLoadingDialog(){
        View view = getLayoutInflater().inflate(R.layout.loading_layout, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setView(view)
                .setCancelable(true);
        alertDialog = builder.create();
        alertDialog.show();
    }
}