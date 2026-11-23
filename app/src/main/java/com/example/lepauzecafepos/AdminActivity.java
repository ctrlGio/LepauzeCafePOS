package com.example.lepauzecafepos;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private static final float SIDEBAR_WIDTH_DP = 250f;
    private static final int COLOR_EERIE_BLACK = Color.parseColor("#1A1F17");
    private static final int COLOR_MOCHA_LATTE = Color.parseColor("#817060");
    private static final int COLOR_OATMEAL = Color.parseColor("#C9C1B1");
    private static final int COLOR_PALLADIAN = Color.parseColor("#EEE9DF");

    private View sidebarDimOverlay;
    private LinearLayout sidebarContainer;
    private ImageButton btnToggleSidebar;
    private TextView tvHeaderTitle;

    private Button btnDashboard, btnMenu, btnSales, btnEmployee, btnCustomer, btnLogout;
    private View layoutDashboardContent, layoutMenuContent, layoutSalesContent, layoutEmployeeContent, layoutCustomerContent;

    private MenuManager menuManager;
    private CustomerManager customerManager;
    private EmployeeManager employeeManager;
    private SalesManager salesManager;
    private DashboardManager dashboardManager;

    private boolean isSidebarOpen = false;
    private float sidebarWidthPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sidebarWidthPx = SIDEBAR_WIDTH_DP * getResources().getDisplayMetrics().density;

        View rootView = findViewById(android.R.id.content);
        sidebarDimOverlay = findViewById(R.id.sidebarDimOverlay);
        sidebarContainer = findViewById(R.id.adminSidebar);
        btnToggleSidebar = findViewById(R.id.btnToggleSidebar);
        tvHeaderTitle = findViewById(R.id.tvAdminHeaderTitle);

        btnDashboard = findViewById(R.id.btnNavDashboard);
        btnMenu = findViewById(R.id.btnNavMenu);
        btnSales = findViewById(R.id.btnNavSales);
        btnEmployee = findViewById(R.id.btnNavEmployees);
        btnCustomer = findViewById(R.id.btnNavCustomers);
        btnLogout = findViewById(R.id.btnAdminLogout);

        layoutDashboardContent = findViewById(R.id.layoutDashboardContent);
        layoutMenuContent = findViewById(R.id.layoutMenuContent);
        layoutSalesContent = findViewById(R.id.layoutSalesContent);
        layoutEmployeeContent = findViewById(R.id.layoutEmployeeContent);
        layoutCustomerContent = findViewById(R.id.layoutCustomerContent);

        if (sidebarContainer != null) {
            sidebarContainer.setTranslationX(-sidebarWidthPx);
            sidebarContainer.setVisibility(View.GONE);
        }
        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.setVisibility(View.GONE);
            sidebarDimOverlay.setOnClickListener(v -> closeSidebar());
        }
        if (btnToggleSidebar != null) {
            btnToggleSidebar.setOnClickListener(v -> {
                if (isSidebarOpen) {
                    closeSidebar();
                } else {
                    openSidebar();
                }
            });
        }

        menuManager = new MenuManager(this, rootView);
        customerManager = new CustomerManager(this, rootView);
        employeeManager = new EmployeeManager(this, rootView);
        salesManager = new SalesManager(this, rootView);
        dashboardManager = new DashboardManager(this, rootView);

        setupNavigationListeners();
        showSection("dashboard");
    }

    private void setupNavigationListeners() {
        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                showSection("dashboard");
                closeSidebar();
            });
        }
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                showSection("menu");
                closeSidebar();
            });
        }
        if (btnCustomer != null) {
            btnCustomer.setOnClickListener(v -> {
                showSection("customer");
                closeSidebar();
            });
        }
        if (btnEmployee != null) {
            btnEmployee.setOnClickListener(v -> {
                showSection("employee");
                closeSidebar();
            });
        }
        if (btnSales != null) {
            btnSales.setOnClickListener(v -> {
                showSection("sales");
                closeSidebar();
            });
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeSidebar();
                Toast.makeText(this, "Logged out of admin workspace.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void showSection(String section) {
        if (layoutDashboardContent != null) {
            layoutDashboardContent.setVisibility("dashboard".equals(section) ? View.VISIBLE : View.GONE);
        }
        if (layoutMenuContent != null) {
            layoutMenuContent.setVisibility("menu".equals(section) ? View.VISIBLE : View.GONE);
        }
        if (layoutCustomerContent != null) {
            layoutCustomerContent.setVisibility("customer".equals(section) ? View.VISIBLE : View.GONE);
        }
        if (layoutEmployeeContent != null) {
            layoutEmployeeContent.setVisibility("employee".equals(section) ? View.VISIBLE : View.GONE);
        }
        if (layoutSalesContent != null) {
            layoutSalesContent.setVisibility("sales".equals(section) ? View.VISIBLE : View.GONE);
        }

        if ("sales".equals(section) && salesManager != null) {
            salesManager.refreshAll();
        }

        if (tvHeaderTitle != null) {
            switch (section) {
                case "menu":
                    tvHeaderTitle.setText("LePauze Coffee - Menu Management");
                    break;
                case "customer":
                    tvHeaderTitle.setText("LePauze Coffee - Customer Records");
                    break;
                case "employee":
                    tvHeaderTitle.setText("LePauze Coffee - Employee Directory");
                    break;
                case "sales":
                    tvHeaderTitle.setText("LePauze Coffee - Sales Records");
                    break;
                case "dashboard":
                default:
                    tvHeaderTitle.setText("LePauze Coffee - Dashboard");
                    break;
            }
        }

        updateNavButtonStyles(section);
    }

    private void updateNavButtonStyles(String activeSection) {
        styleNavButton(btnDashboard, "dashboard".equals(activeSection));
        styleNavButton(btnMenu, "menu".equals(activeSection));
        styleNavButton(btnCustomer, "customer".equals(activeSection));
        styleNavButton(btnEmployee, "employee".equals(activeSection));
        styleNavButton(btnSales, "sales".equals(activeSection));
    }

    private void styleNavButton(Button button, boolean active) {
        if (button == null) return;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                getResources().getDisplayMetrics()
        ));
        bg.setColor(active ? COLOR_MOCHA_LATTE : COLOR_EERIE_BLACK);

        button.setBackground(bg);
        button.setBackgroundTintList(null);
        button.setTextColor(active ? COLOR_PALLADIAN : COLOR_OATMEAL);
    }

    private void openSidebar() {
        if (sidebarContainer == null) return;
        isSidebarOpen = true;
        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.setVisibility(View.VISIBLE);
            sidebarDimOverlay.setAlpha(0f);
            sidebarDimOverlay.animate().alpha(1f).setDuration(200).start();
        }
        sidebarContainer.setVisibility(View.VISIBLE);
        sidebarContainer.animate().translationX(0f).setDuration(220).start();
    }

    private void closeSidebar() {
        if (sidebarContainer == null) return;
        isSidebarOpen = false;
        if (sidebarDimOverlay != null) {
            sidebarDimOverlay.animate()
                    .alpha(0f)
                    .setDuration(180)
                    .withEndAction(() -> sidebarDimOverlay.setVisibility(View.GONE))
                    .start();
        }
        sidebarContainer.animate()
                .translationX(-sidebarWidthPx)
                .setDuration(200)
                .withEndAction(() -> sidebarContainer.setVisibility(View.GONE))
                .start();
    }
}