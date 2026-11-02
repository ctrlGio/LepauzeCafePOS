package com.example.lepauzecafepos;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteCashier;
    private StringBuilder pinBuilder;
    private TextView[] pinBoxes;
    private static final int MAX_PIN_LENGTH = 6;

    private List<User> userList;
    private User selectedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autoCompleteCashier = findViewById(R.id.autoCompleteCashier);
        pinBuilder = new StringBuilder();

        // Initialize the 6 individual PIN TextView boxes
        pinBoxes = new TextView[]{
                findViewById(R.id.pinBox1),
                findViewById(R.id.pinBox2),
                findViewById(R.id.pinBox3),
                findViewById(R.id.pinBox4),
                findViewById(R.id.pinBox5),
                findViewById(R.id.pinBox6)
        };

        loadSampleUsers();

        // Clear ("CLR") button logic
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            pinBuilder.setLength(0);
            updatePinDisplay();
            Toast.makeText(LoginActivity.this, "PIN Cleared", Toast.LENGTH_SHORT).show();
        });

        // Delete ("⌫") button logic
        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                updatePinDisplay();
            }
        });

        // Forgot PIN Click Listener
        TextView tvForgotPin = findViewById(R.id.tvForgotPin);
        tvForgotPin.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Please contact Admin to reset PIN.", Toast.LENGTH_LONG).show();
            // TODO: Open Admin override dialog or recovery flow here
        });

        // Login Button Action
        com.google.android.material.button.MaterialButton btnStartShift = findViewById(R.id.btnStartShift);
        btnStartShift.setOnClickListener(v -> attemptLogin());
    }

    /**
     * XML Click Handler for numeric keypad buttons (0-9)
     */
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

    /**
     * Updates the individual boxes with typed digits or blanks
     */
    private void updatePinDisplay() {
        String currentPin = pinBuilder.toString();
        for (int i = 0; i < pinBoxes.length; i++) {
            if (i < currentPin.length()) {
                // Display the digit (or use "•" if you want it masked as bullets)
                pinBoxes[i].setText(String.valueOf(currentPin.charAt(i)));
            } else {
                pinBoxes[i].setText("");
            }
        }
    }

    private void loadSampleUsers() {
        userList = new ArrayList<>();
        userList.add(new User(1, "Gio (Admin)", "123456"));
        userList.add(new User(2, "Harvey (Cashier)", "654321"));

        List<String> userDisplayNames = new ArrayList<>();
        for (User u : userList) {
            userDisplayNames.add(u.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                userDisplayNames
        );
        autoCompleteCashier.setAdapter(adapter);

        autoCompleteCashier.setOnItemClickListener((parent, view, position, id) -> {
            selectedUser = userList.get(position);
        });
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

        if (selectedUser.getPin().equals(enteredPin)) {
            Toast.makeText(this, "Login Successful! Welcome, " + selectedUser.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Launch next activity
        } else {
            Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
            pinBuilder.setLength(0);
            updatePinDisplay();
        }
    }

    private static class User {
        private int id;
        private String name;
        private String pin;

        public User(int id, String name, String pin) {
            this.id = id;
            this.name = name;
            this.pin = pin;
        }

        public String getName() { return name; }
        public String getPin() { return pin; }
    }
}