package com.example.techbharsolutiongndechub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText etEmailPhone, etPassword;
    private TextView btnRegister, tvForgot;
    private MaterialButton btnLogin;
    private ProgressBar loginLoading;

    private SharedPreferences prefs;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        EdgeToEdge.enable(this,
                SystemBarStyle.dark(ContextCompat.getColor(this, R.color.statusbarcolor)),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_login);

        initViews();
        initFirebase();
        setListeners();
    }

    private void initViews() {
        etEmailPhone = findViewById(R.id.etEmailPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgot = findViewById(R.id.tvForgot);
        loginLoading = findViewById(R.id.loginLoading);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("appSession", MODE_PRIVATE);
    }

    private void setListeners() {

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgetpasswordActivity.class)));

        btnLogin.setOnClickListener(v -> {
            if (!isLoading) {
                loginUser();
            }
        });
    }

    //  Lifecycle Aware Session Check
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String role = prefs.getString("role", null);

            if (role != null) {
                openDashboard(role);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // UI reset (important for lifecycle)
        hideLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Prevent memory leaks / UI issues
        hideLoading();
    }

    //  Login Logic
    private void loginUser() {

        String email = etEmailPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) return;

        showLoading();

        auth.signInWithEmailAndPassword(email, password)

                .addOnSuccessListener(result -> {

                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        hideLoading();
                        showToast("User not found");
                        return;
                    }

                    fetchUserRole(user.getUid());
                })

                .addOnFailureListener(e -> {
                    hideLoading();
                    showToast("Login Failed: " + e.getMessage());
                });
    }

    //  Fetch Role from Firestore
    private void fetchUserRole(String uid) {

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    hideLoading();

                    if (!doc.exists()) {
                        showToast("User data not found");
                        return;
                    }

                    String role = doc.getString("role");

                    if (role == null) {
                        showToast("Invalid user role");
                        return;
                    }

                    // Save session
                    prefs.edit().putString("role", role).apply();

                    openDashboard(role);
                })

                .addOnFailureListener(e -> {
                    hideLoading();
                    showToast(e.getMessage());
                });
    }

    //  Navigation
    private void openDashboard(String role) {

        Intent intent;

        if ("admin".equals(role)) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }

    //  Validation
    private boolean validateInput(String email, String password) {

        if (email.isEmpty()) {
            etEmailPhone.setError("Enter Email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailPhone.setError("Invalid Email");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter Password");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return false;
        }

        return true;
    }

    //  Loading UI Control
    private void showLoading() {
        isLoading = true;
        loginLoading.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
    }

    private void hideLoading() {
        isLoading = false;
        loginLoading.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
    }

    //  Toast Helper
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}