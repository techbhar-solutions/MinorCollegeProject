package com.example.techbharsolutiongndechub;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    ViewPager2 viewPager;
    // Firebase
    FirebaseAuth auth;
    FirebaseFirestore db;

    // Header Views
    TextView userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screenshot disable
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        EdgeToEdge.enable(this,
                SystemBarStyle.dark(ContextCompat.getColor(this, R.color.statusbarcolor)),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_home);

        // 1. Initialize Firebase first
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.main_frame);
        toolbar = findViewById(R.id.toolbar);

        // 3. Setup Navigation Header (after navigationView initialization)
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.userName);
        userEmail = headerView.findViewById(R.id.userEmail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ViewPager Adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 4. Load user data (after db initialization)
        loadUserData();

        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Bottom Navigation → ViewPager control
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_home) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.bottom_live) {
                viewPager.setCurrentItem(1);
            } else if (id == R.id.bottom_whatsapp) {
                viewPager.setCurrentItem(2);
            } else if (id == R.id.bottom_notifications) {
                viewPager.setCurrentItem(3);
            }
            return true;
        });

        // Swipe → BottomNavigation update
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_live);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_whatsapp);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_notifications);
                        break;
                }
            }
        });

        // Drawer Navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_academic) {
                openWebPage("https://academics.gndec.ac.in/");
            } else if (id == R.id.nav_website) {
                openWebPage("https://gndec.ac.in/");
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Click Profile", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Using fullname as used in RegisterActivity
                            String name = document.getString("fullname");
                            String email = document.getString("email");
                            
                            if (userName != null && name != null) userName.setText(name);
                            if (userEmail != null && email != null) userEmail.setText(email);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void logoutUser() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
