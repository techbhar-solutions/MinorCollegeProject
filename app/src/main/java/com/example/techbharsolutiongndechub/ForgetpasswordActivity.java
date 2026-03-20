package com.example.techbharsolutiongndechub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetpasswordActivity extends AppCompatActivity {
      CardView btnBack;
     TextInputEditText etemail;
    Button btnresetpass;
    private FirebaseAuth mAuth;
    private String strEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this, SystemBarStyle.dark(ContextCompat.getColor(this, R.color.statusbarcolor)),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_forgetpassword);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etemail = findViewById(R.id.etEmail);
        btnresetpass = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance(); // Firebase Auth
        btnBack.setOnClickListener(v -> MoveToLogin());

        btnresetpass.setOnClickListener(v ->{
            strEmail = etemail.getText() != null
                    ? etemail.getText().toString().trim()
                    : "";


            if(!TextUtils.isEmpty(strEmail)){
                ResetPassword(strEmail);
            }
            else{
                etemail.setError("Email field can't be empty");
            }

        });

    }


    private void ResetPassword(String email)
    {
     try {
         btnresetpass.setVisibility(View.INVISIBLE);
         mAuth.sendPasswordResetEmail(email)
                 .addOnSuccessListener(unused ->  {
                         Toast.makeText(ForgetpasswordActivity.this, "Reset link sent to your Email", Toast.LENGTH_SHORT).show();
                         Intent intent = new Intent(ForgetpasswordActivity.this, LoginActivity.class);
                         startActivity(intent);
                         finish();
                 })
                 .addOnFailureListener(unused -> {
                         btnresetpass.setVisibility(View.VISIBLE);
                         Toast.makeText(ForgetpasswordActivity.this, "Error: " + unused.getMessage(), Toast.LENGTH_SHORT).show();
                         btnresetpass.setVisibility(View.VISIBLE);


                 });
     }
     catch (Exception ex){

         Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
     }
    }

    private void MoveToLogin(){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}