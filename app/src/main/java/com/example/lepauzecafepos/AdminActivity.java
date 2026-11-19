package com.example.lepauzecafepos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private static final long SIDEBAR_ANIM_DURATION_MS = 220L;
    private static final int DEFAULT_SIDEBAR_WIDTH_DP = 250;

    Button btnDashboard, btnMenu, btnSales, btnEmployee, btnCustomer, btnLogout;
    ImageButton btnToggleSidebar;
    LinearLayout sidebarContainer;
    View sidebarDimOverlay;
    LinearLayout layoutDashboard, layoutMenu, layoutSales, layoutEmployee, layoutCustomer;
    TextView tvHeaderTitle;

    MenuManager menuManager;
    CustomerManager customerManager;

    private boolean isSidebarAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Bind Sidebar & Header Controls
        sidebarContainer = findViewById(R.id.sidebarContainer);
        sidebarDimOverlay = findViewById(R.id.sidebarDimOverlay);
        btnToggleSidebar = findViewById(R.id.btnToggleSidebar);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        // Bind Sidebar Navigation Buttons
        btnDashboard = findViewById(R.id.btnDashboard);
        btnMenu = findViewById(R.id.btnMenu);
        btnSales = findViewById(R.id.btnSales);
        btnEmployee = findViewById(R.id.btnEmployee);
        btnCustomer = findViewById(R.id.btnCustomer);
        btnLogout = findViewById(R.id.btnLogout);

        // Bind Workspace Layouts
        layoutDashboard = findViewById(R.id.layoutDashboardContent);
        layoutMenu = findViewById(R.id.layoutMenuContent);
        layoutSales = findViewById(R.id.layoutSalesContent);
        layoutEmployee = findViewById(R.id.layoutEmployeeContent);
        layoutCustomer = findViewById(R.id.layoutCustomerContent);

        if (sidebarContainer != null) {
            sidebarContainer.setVisibility(View.GONE);
        }

        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.setAlpha(0f);
            sidebarDimOverlay.setVisibility(View.GONE);
            sidebarDimOverlay.setOnClickListener(v -> closeSidebar(true));
        }

        if (btnToggleSidebar != null) {
            btnToggleSidebar.setOnClickListener(v -> toggleSidebar());
        }

        // Initialize Managers
        View rootView = findViewById(android.R.id.content);
        menuManager = new MenuManager(this, rootView);
        customerManager = new CustomerManager(this, rootView);

        // Navigation Listeners for Tabs
        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> switchTab(layoutDashboard, btnDashboard, "Dashboard Overview"));
        }
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> switchTab(layoutMenu, btnMenu, "Menu Management"));
        }
        if (btnSales != null) {
            btnSales.setOnClickListener(v -> switchTab(layoutSales, btnSales, "Sales Records"));
        }
        if (btnEmployee != null) {
            btnEmployee.setOnClickListener(v -> switchTab(layoutEmployee, btnEmployee, "Employee Directory"));
        }
        if (btnCustomer != null) {
            btnCustomer.setOnClickListener(v -> switchTab(layoutCustomer, btnCustomer, "Customer Kiosk"));
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeSidebar(false);
                finish();
            });
        }

        // Default Startup Tab (without animating sidebar open)
        switchTab(layoutDashboard, btnDashboard, "Dashboard Overview", false);
    }

    private int getSidebarWidthPx() {
        if (sidebarContainer != null && sidebarContainer.getWidth() > 0) {
            return sidebarContainer.getWidth();
        }
        return (int) (DEFAULT_SIDEBAR_WIDTH_DP * getResources().getDisplayMetrics().density);
    }

    private void toggleSidebar() {
        if (sidebarContainer == null || isSidebarAnimating) return;

        if (sidebarContainer.getVisibility() == View.VISIBLE) {
            closeSidebar(true);
        } else {
            openSidebar();
        }
    }

    private void openSidebar() {
        if (sidebarContainer == null) return;

        isSidebarAnimating = true;
        sidebarContainer.animate().cancel();
        sidebarContainer.clearAnimation();

        int width = getSidebarWidthPx();
        sidebarContainer.setTranslationX(-width);
        sidebarContainer.setVisibility(View.VISIBLE);
        sidebarContainer.bringToFront();

        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.animate().cancel();
            sidebarDimOverlay.clearAnimation();
            sidebarDimOverlay.setAlpha(0f);
            sidebarDimOverlay.setVisibility(View.VISIBLE);
            sidebarDimOverlay.animate()
                    .alpha(1f)
                    .setDuration(SIDEBAR_ANIM_DURATION_MS)
                    .setListener(null)
                    .start();
        }

        sidebarContainer.animate()
                .translationX(0f)
                .setDuration(SIDEBAR_ANIM_DURATION_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        sidebarContainer.setTranslationX(0f);
                        isSidebarAnimating = false;
                    }
                })
                .start();
    }

    private void closeSidebar(boolean animate) {
        if (sidebarContainer == null) return;

        if (!animate || sidebarContainer.getVisibility() != View.VISIBLE) {
            sidebarContainer.animate().cancel();
            sidebarContainer.clearAnimation();
            sidebarContainer.setTranslationX(0f);
            sidebarContainer.setVisibility(View.GONE);

            if (sidebarDimOverlay != null) {
                sidebarDimOverlay.animate().cancel();
                sidebarDimOverlay.clearAnimation();
                sidebarDimOverlay.setAlpha(0f);
                sidebarDimOverlay.setVisibility(View.GONE);
            }

            isSidebarAnimating = false;
            return;
        }

        isSidebarAnimating = true;
        int width = getSidebarWidthPx();

        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.animate().cancel();
            sidebarDimOverlay.clearAnimation();
            sidebarDimOverlay.setAlpha(1f);
            sidebarDimOverlay.setVisibility(View.VISIBLE);
            sidebarDimOverlay.animate()
                    .alpha(0f)
                    .setDuration(SIDEBAR_ANIM_DURATION_MS)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            sidebarDimOverlay.setAlpha(0f);
                            sidebarDimOverlay.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }

        sidebarContainer.animate()
                .translationX(-width)
                .setDuration(SIDEBAR_ANIM_DURATION_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        sidebarContainer.setVisibility(View.GONE);
                        sidebarContainer.setTranslationX(0f);
                        isSidebarAnimating = false;
                    }
                })
                .start();
    }

    private void switchTab(View activeLayout, Button activeButton, String title) {
        switchTab(activeLayout, activeButton, title, true);
    }

    private void switchTab(View activeLayout, Button activeButton, String title, boolean animateClose) {
        if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
        if (layoutMenu != null) layoutMenu.setVisibility(View.GONE);
        if (layoutSales != null) layoutSales.setVisibility(View.GONE);
        if (layoutEmployee != null) layoutEmployee.setVisibility(View.GONE);
        if (layoutCustomer != null) layoutCustomer.setVisibility(View.GONE);

        if (activeLayout != null) {
            activeLayout.setVisibility(View.VISIBLE);
        }
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("LePauze Coffee - " + title);
        }

        int inactiveColor = Color.parseColor("#2A3126");
        if (btnDashboard != null) btnDashboard.setBackgroundColor(inactiveColor);
        if (btnMenu != null) btnMenu.setBackgroundColor(inactiveColor);
        if (btnSales != null) btnSales.setBackgroundColor(inactiveColor);
        if (btnEmployee != null) btnEmployee.setBackgroundColor(inactiveColor);
        if (btnCustomer != null) btnCustomer.setBackgroundColor(inactiveColor);

        if (activeButton != null) {
            activeButton.setBackgroundColor(Color.parseColor("#16A34A"));
        }

        closeSidebar(animateClose);
    }
}