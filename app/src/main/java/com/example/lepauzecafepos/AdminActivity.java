package com.example.lepauzecafepos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    Button btnDashboard, btnMenu, btnSales, btnEmployee;
    LinearLayout layoutDashboard, layoutMenu, layoutSales, layoutEmployee;
    TextView tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Bind navigation buttons
        btnDashboard = findViewById(R.id.btnDashboard);
        btnMenu = findViewById(R.id.btnMenu);
        btnSales = findViewById(R.id.btnSales);
        btnEmployee = findViewById(R.id.btnEmployee);

        // Bind content layouts
        layoutDashboard = findViewById(R.id.layoutDashboardContent);
        layoutMenu = findViewById(R.id.layoutMenuContent);
        layoutSales = findViewById(R.id.layoutSalesContent);
        layoutEmployee = findViewById(R.id.layoutEmployeeContent);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        // Click Listeners for navigation tabs
        btnDashboard.setOnClickListener(v -> switchTab(layoutDashboard, btnDashboard, "Dashboard Overview"));
        btnMenu.setOnClickListener(v -> switchTab(layoutMenu, btnMenu, "Menu Management"));
        btnSales.setOnClickListener(v -> switchTab(layoutSales, btnSales, "Sales Records"));
        btnEmployee.setOnClickListener(v -> switchTab(layoutEmployee, btnEmployee, "Employee Directory"));
    }

    private void switchTab(View activeLayout, Button activeButton, String title) {
        // Hide all layouts
        layoutDashboard.setVisibility(View.GONE);
        layoutMenu.setVisibility(View.GONE);
        layoutSales.setVisibility(View.GONE);
        layoutEmployee.setVisibility(View.GONE);

        // Show chosen layout
        activeLayout.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("LePauze Coffee - " + title);

        // Reset all button backgrounds to inactive dark tone
        int inactiveColor = android.graphics.Color.parseColor("#2A3126");
        btnDashboard.setBackgroundColor(inactiveColor);
        btnMenu.setBackgroundColor(inactiveColor);
        btnSales.setBackgroundColor(inactiveColor);
        btnEmployee.setBackgroundColor(inactiveColor);

        // Highlight active button with Emerald Green
        activeButton.setBackgroundColor(android.graphics.Color.parseColor("#16A34A"));
    }
}