package com.example.lepauzecafepos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lepauzecafepos.model.CashierUser;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_CASHIER_ID = "extra_cashier_id";
    public static final String EXTRA_CASHIER_NAME = "extra_cashier_name";
    public static final String EXTRA_CASHIER_ROLE = "extra_cashier_role";

    private AutoCompleteTextView autoCompleteCashier;
    private StringBuilder pinBuilder;
    private TextView[] pinBoxes;
    private static final int MAX_PIN_LENGTH = 6;

    private List<CashierUser> userList;
    private CashierUser selectedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autoCompleteCashier = findViewById(R.id.auto_complete_cashier);
        pinBuilder = new StringBuilder();

        pinBoxes = new TextView[]{
                findViewById(R.id.tv_pin_box_1),
                findViewById(R.id.tv_pin_box_2),
                findViewById(R.id.tv_pin_box_3),
                findViewById(R.id.tv_pin_box_4),
                findViewById(R.id.tv_pin_box_5),
                findViewById(R.id.tv_pin_box_6)
        };

        loadSampleUsers();

        Button btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> {
            pinBuilder.setLength(0);
            updatePinDisplay();
            Toast.makeText(LoginActivity.this, "PIN Cleared", Toast.LENGTH_SHORT).show();
        });

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                updatePinDisplay();
            }
        });

        TextView tvForgotPin = findViewById(R.id.tv_forgot_pin);
        tvForgotPin.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Please contact Admin to reset PIN.", Toast.LENGTH_LONG).show()
        );

        MaterialButton btnStartShift = findViewById(R.id.btn_start_shift);
        btnStartShift.setOnClickListener(v -> attemptLogin());
    }

    public void onPinDigitClick(View view) {
        if (view instanceof Button) {
            Button button = (Button) view;
            String digit = button.getText().toString();

            if (pinBuilder.length() < MAX_PIN_LENGTH) {
                pinBuilder.append(digit);
                updatePinDisplay();
            }
        }
    }

    private void updatePinDisplay() {
        String currentPin = pinBuilder.toString();
        for (int i = 0; i < pinBoxes.length; i++) {
            if (i < currentPin.length()) {
                pinBoxes[i].setText(String.valueOf(currentPin.charAt(i)));
            } else {
                pinBoxes[i].setText("");
            }
        }
    }

    private void loadSampleUsers() {
        userList = new ArrayList<>();
        userList.add(new CashierUser("1", "Gio", "Admin", "123456"));
        userList.add(new CashierUser("2", "Harvey", "Barista", "654321"));

        List<String> userDisplayNames = new ArrayList<>();
        for (CashierUser u : userList) {
            userDisplayNames.add(u.name + " (" + u.role + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                userDisplayNames
        );
        autoCompleteCashier.setAdapter(adapter);

        autoCompleteCashier.setOnItemClickListener((parent, view, position, id) ->
                selectedUser = userList.get(position)
        );
    }

    private void attemptLogin() {
        if (selectedUser == null) {
            Toast.makeText(this, "Please select a user profile first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredPin = pinBuilder.toString();
        if (enteredPin.length() < 4) {
            Toast.makeText(this, "Please enter your complete PIN.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Objects.equals(selectedUser.pin, enteredPin)) {
            Toast.makeText(this, "Welcome, " + selectedUser.name + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_CASHIER_ID, selectedUser.id);
            intent.putExtra(EXTRA_CASHIER_NAME, selectedUser.name);
            intent.putExtra(EXTRA_CASHIER_ROLE, selectedUser.role);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
            pinBuilder.setLength(0);
            updatePinDisplay();
        }
    }
}