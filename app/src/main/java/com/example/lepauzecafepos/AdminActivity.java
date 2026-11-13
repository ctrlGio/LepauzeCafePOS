package com.example.lepauzecafepos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    Button btnDashboard, btnMenu, btnSales, btnEmployee;
    LinearLayout layoutDashboard, layoutMenu, layoutSales, layoutEmployee;
    TextView tvHeaderTitle;
    CheckBox headerCheckbox, itemCheckbox1, itemCheckbox2, itemCheckbox3;

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

        // Bind Checkboxes for Select-All functionality
        headerCheckbox = findViewById(R.id.headerCheckbox);
        itemCheckbox1 = findViewById(R.id.itemCheckbox1);
        itemCheckbox2 = findViewById(R.id.itemCheckbox2);
        itemCheckbox3 = findViewById(R.id.itemCheckbox3);

        // Click Listeners for navigation tabs
        btnDashboard.setOnClickListener(v -> switchTab(layoutDashboard, btnDashboard, "Dashboard Overview"));
        btnMenu.setOnClickListener(v -> switchTab(layoutMenu, btnMenu, "Menu Management"));
        btnSales.setOnClickListener(v -> switchTab(layoutSales, btnSales, "Sales Records"));
        btnEmployee.setOnClickListener(v -> switchTab(layoutEmployee, btnEmployee, "Employee Directory"));

        // Header Checkbox "Select All" Listener
        if (headerCheckbox != null) {
            headerCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (itemCheckbox1 != null) itemCheckbox1.setChecked(isChecked);
                if (itemCheckbox2 != null) itemCheckbox2.setChecked(isChecked);
                if (itemCheckbox3 != null) itemCheckbox3.setChecked(isChecked);
            });
        }
    }

    private void switchTab(View activeLayout, Button activeButton, String title) {
        // Hide all layouts
        layoutDashboard.setVisibility(View.GONE);
        layoutMenu.setVisibility(View.GONE);
        layoutSales.setVisibility(View.GONE);
        layoutEmployee.setVisibility(View.GONE);

        activeLayout.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("LePauze Coffee - " + title);

        int inactiveColor = android.graphics.Color.parseColor("#2A3126");
        btnDashboard.setBackgroundColor(inactiveColor);
        btnMenu.setBackgroundColor(inactiveColor);
        btnSales.setBackgroundColor(inactiveColor);
        btnEmployee.setBackgroundColor(inactiveColor);

        // Highlight active button with Emerald Green
        activeButton.setBackgroundColor(android.graphics.Color.parseColor("#16A34A"));
    }
}