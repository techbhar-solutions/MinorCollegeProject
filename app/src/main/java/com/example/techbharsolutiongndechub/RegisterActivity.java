package com.example.techbharsolutiongndechub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ImageView btnBack;
    TextInputEditText etName, etPhone, etEmail, etPassword;
    MaterialButton btnRegisterSubmit;
    AutoCompleteTextView autoCompleteCourse;
    ProgressBar loginLoading;
    TextView tvLoginLink;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private boolean isLoading = false;
    private Handler handler = new Handler();
    private Runnable emailCheckRunnable;

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

        setContentView(R.layout.activity_register);

        initViews();
        initFirebase();
        setupDropdown();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        autoCompleteCourse = findViewById(R.id.autoCompleteCourse);
        loginLoading = findViewById(R.id.loginLoading);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupDropdown() {
        String[] courses = {"B.Tech","M.Tech","Ph.D","M.Sc","MBA","BBA","B.Com","B.Voc","BCA","MCA"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                courses
        );

        autoCompleteCourse.setAdapter(adapter);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> moveToLogin());
        tvLoginLink.setOnClickListener(v -> moveToLogin());

        btnRegisterSubmit.setOnClickListener(v -> {
            if (!isLoading) registerUser();
        });

        // Phone validation
        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(String s) {
                if (s.length() < 10) {
                    etPhone.setError("Enter 10 digit number");
                }
            }
        });

        // Email validation + debounce check
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(String email) {

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Invalid Email");
                    return;
                }

                // debounce (delay API call)
                if (emailCheckRunnable != null) {
                    handler.removeCallbacks(emailCheckRunnable);
                }

                emailCheckRunnable = () -> checkEmailExists(email);
                handler.postDelayed(emailCheckRunnable, 800);
            }
        });
    }

    //  Lifecycle handling
    @Override
    protected void onResume() {
        super.onResume();
        hideLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideLoading();
        handler.removeCallbacksAndMessages(null); // prevent memory leak
    }

    private void moveToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void checkEmailExists(String email) {

        FirebaseAuth.getInstance()
                .fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {

                        boolean isNewUser =
                                task.getResult().getSignInMethods().isEmpty();

                        if (!isNewUser) {
                            etEmail.setError("Email already registered");
                        }
                    }
                });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String course = autoCompleteCourse.getText().toString().trim();

        if (!validateInput(name, phone, email, password, course)) return;

        showLoading();

        auth.createUserWithEmailAndPassword(email, password)

                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("fullname", name);
                    user.put("phone", phone);
                    user.put("email", email);
                    user.put("course", course);
                    user.put("role", "student");

                    db.collection("users")
                            .document(uid)
                            .set(user)

                            .addOnSuccessListener(unused -> {

                                hideLoading();
                                showToast("Registration Successful");

                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })

                            .addOnFailureListener(e -> {
                                hideLoading();
                                showToast(e.getMessage());
                            });
                })

                .addOnFailureListener(e -> {
                    hideLoading();
                    showToast(e.getMessage());
                });
    }

    //  Validation
    private boolean validateInput(String name, String phone, String email, String password, String course) {

        if (name.isEmpty()) {
            etName.setError("Enter Name");
            return false;
        }

        if (phone.length() < 10) {
            etPhone.setError("Invalid Phone");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid Email");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return false;
        }

        if (course.isEmpty()) {
            autoCompleteCourse.setError("Select Course");
            return false;
        }

        return true;
    }

    //  Loading UI
    private void showLoading() {
        isLoading = true;
        loginLoading.setVisibility(View.VISIBLE);
        btnRegisterSubmit.setEnabled(false);
    }

    private void hideLoading() {
        isLoading = false;
        loginLoading.setVisibility(View.GONE);
        btnRegisterSubmit.setEnabled(true);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //  Custom TextWatcher (clean code)
    abstract class SimpleTextWatcher implements TextWatcher {
        public abstract void onTextChanged(String s);

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged(s.toString());
        }
    }
}