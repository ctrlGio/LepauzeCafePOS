package com.example.lepauzecafepos;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lepauzecafepos.model.ShiftSession;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ShiftSession currentShift;
    private PopupWindow shiftPopupWindow;

    private String activeCashierName = "Demo Barista";
    private String activeCashierRole = "Barista";

    private View pillShiftStatus;
    private ImageView ivShiftIndicator;
    private TextView tvShiftStatus;
    private TextView tvShiftDetails;

    public interface OnAmountConfirmed {
        void onConfirmed(double amount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Read cashier info passed from LoginActivity (with safe defaults)
        String extraName = getIntent().getStringExtra(LoginActivity.EXTRA_CASHIER_NAME);
        String extraRole = getIntent().getStringExtra(LoginActivity.EXTRA_CASHIER_ROLE);
        if (extraName != null && !extraName.trim().isEmpty()) {
            activeCashierName = extraName.trim();
        }
        if (extraRole != null && !extraRole.trim().isEmpty()) {
            activeCashierRole = extraRole.trim();
        }

        // 2. Bind toolbar shift views (included from layout_toolbar.xml)
        pillShiftStatus = findViewById(R.id.pill_shift_status);
        ivShiftIndicator = findViewById(R.id.iv_shift_indicator);
        tvShiftStatus = findViewById(R.id.tv_shift_status);
        tvShiftDetails = findViewById(R.id.tv_shift_details);

        // 3. Initialize sample shift for the logged-in user
        currentShift = createDemoShiftForUser(activeCashierName, activeCashierRole);
        refreshShiftPill();

        // 4. Open shift popover when tapping the shift pill
        if (pillShiftStatus != null) {
            pillShiftStatus.setOnClickListener(this::toggleShiftPopover);
        }
    }

    private ShiftSession createDemoShiftForUser(String name, String role) {
        long now = System.currentTimeMillis();
        long startedAt = now - (2 * 60 * 60 * 1000L); // Started 2 hours ago for testing
        ShiftSession session = new ShiftSession(name, role, true, startedAt, 0.00);
        session.currentSales = 7376.50;
        session.transactionCount = 23;
        session.cashSales = 4253.00;
        return session;
    }

    private void refreshShiftPill() {
        if (tvShiftStatus == null || tvShiftDetails == null) return;

        if (currentShift != null && currentShift.isActive) {
            tvShiftStatus.setText("Shift active");
            tvShiftStatus.setTextColor(Color.parseColor("#15803D"));
            if (ivShiftIndicator != null) {
                ivShiftIndicator.setImageResource(R.drawable.ic_shift_active_dot);
            }
            String details = "Started " + formatStartTime(currentShift.startedAtMillis)
                    + " · Opening cash " + formatPeso(currentShift.openingCash);
            tvShiftDetails.setText(details);
        } else {
            tvShiftStatus.setText("No active shift");
            tvShiftStatus.setTextColor(Color.parseColor("#6B7280"));
            tvShiftDetails.setText("Tap to start a shift");
        }
    }

    private void toggleShiftPopover(View anchorPill) {
        if (currentShift == null || !currentShift.isActive) {
            showStartShiftDialog();
            return;
        }

        if (shiftPopupWindow != null && shiftPopupWindow.isShowing()) {
            shiftPopupWindow.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_shift_details, null);
        bindShiftPopup(popupView, currentShift);

        int widthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 310, getResources().getDisplayMetrics());

        shiftPopupWindow = new PopupWindow(
                popupView,
                widthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        shiftPopupWindow.setElevation(16f);

        ImageView chevron = anchorPill.findViewById(R.id.iv_shift_chevron);
        if (chevron != null) {
            chevron.animate().rotation(90f).setDuration(150).start();
        }

        shiftPopupWindow.setOnDismissListener(() -> {
            if (chevron != null) {
                chevron.animate().rotation(0f).setDuration(150).start();
            }
        });

        popupView.findViewById(R.id.btn_popup_cash_in).setOnClickListener(v -> {
            shiftPopupWindow.dismiss();
            showDrawerAmountDialog("Cash in", amount -> {
                currentShift.cashIn += amount;
                Toast.makeText(this, "Added " + formatPeso(amount) + " Cash In", Toast.LENGTH_SHORT).show();
            });
        });

        popupView.findViewById(R.id.btn_popup_cash_out).setOnClickListener(v -> {
            shiftPopupWindow.dismiss();
            showDrawerAmountDialog("Cash out", amount -> {
                currentShift.cashOut += amount;
                Toast.makeText(this, "Recorded " + formatPeso(amount) + " Cash Out", Toast.LENGTH_SHORT).show();
            });
        });

        popupView.findViewById(R.id.btn_popup_add_expense).setOnClickListener(v -> {
            shiftPopupWindow.dismiss();
            showDrawerAmountDialog("Add expense", amount -> {
                currentShift.expenses += amount;
                Toast.makeText(this, "Added " + formatPeso(amount) + " Expense", Toast.LENGTH_SHORT).show();
            });
        });

        popupView.findViewById(R.id.btn_popup_view_shift).setOnClickListener(v -> {
            shiftPopupWindow.dismiss();
            Toast.makeText(this, "Full shift details view coming next", Toast.LENGTH_SHORT).show();
        });

        popupView.findViewById(R.id.btn_popup_end_shift).setOnClickListener(v -> {
            shiftPopupWindow.dismiss();
            confirmEndShift();
        });

        int yOffsetPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        shiftPopupWindow.showAsDropDown(anchorPill, 0, yOffsetPx);
    }

    private void bindShiftPopup(View popupView, ShiftSession shift) {
        ((TextView) popupView.findViewById(R.id.tv_popup_cashier_name)).setText(shift.cashierName);
        ((TextView) popupView.findViewById(R.id.tv_popup_cashier_role)).setText(shift.cashierRole);

        TextView tvStatus = popupView.findViewById(R.id.tv_popup_status);
        tvStatus.setText(shift.isActive ? "Active" : "Closed");
        tvStatus.setTextColor(Color.parseColor(shift.isActive ? "#15803D" : "#6B7280"));

        ((TextView) popupView.findViewById(R.id.tv_popup_started))
                .setText(formatStartTime(shift.startedAtMillis));
        ((TextView) popupView.findViewById(R.id.tv_popup_duration))
                .setText(formatDuration(shift.startedAtMillis));
        ((TextView) popupView.findViewById(R.id.tv_popup_opening_cash))
                .setText(formatPeso(shift.openingCash));
        ((TextView) popupView.findViewById(R.id.tv_popup_current_sales))
                .setText(formatPeso(shift.currentSales));
        ((TextView) popupView.findViewById(R.id.tv_popup_transactions))
                .setText(String.valueOf(shift.transactionCount));
        ((TextView) popupView.findViewById(R.id.tv_popup_cash_sales))
                .setText(formatPeso(shift.cashSales));
        ((TextView) popupView.findViewById(R.id.tv_popup_cash_in))
                .setText(formatPeso(shift.cashIn));
        ((TextView) popupView.findViewById(R.id.tv_popup_cash_out))
                .setText(formatPeso(shift.cashOut));
        ((TextView) popupView.findViewById(R.id.tv_popup_expenses))
                .setText(formatPeso(shift.expenses));
        ((TextView) popupView.findViewById(R.id.tv_popup_expected_cash))
                .setText(formatPeso(shift.getExpectedCashInDrawer()));
    }

    private void showDrawerAmountDialog(String title, OnAmountConfirmed callback) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("0.00");

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Enter amount (₱)")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String raw = input.getText().toString().trim();
                    if (!raw.isEmpty()) {
                        try {
                            double value = Double.parseDouble(raw);
                            if (value > 0) {
                                callback.onConfirmed(value);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmEndShift() {
        new AlertDialog.Builder(this)
                .setTitle("End Shift")
                .setMessage("Expected cash in drawer: " + formatPeso(currentShift.getExpectedCashInDrawer())
                        + "\n\nAre you sure you want to end this shift?")
                .setPositiveButton("End Shift", (dialog, which) -> {
                    currentShift.isActive = false;
                    refreshShiftPill();
                    Toast.makeText(this, "Shift ended", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showStartShiftDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("0.00");

        new AlertDialog.Builder(this)
                .setTitle("Start New Shift")
                .setMessage("Enter opening cash amount (₱)")
                .setView(input)
                .setPositiveButton("Start Shift", (dialog, which) -> {
                    double opening = 0.0;
                    String raw = input.getText().toString().trim();
                    if (!raw.isEmpty()) {
                        try {
                            opening = Double.parseDouble(raw);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    currentShift = new ShiftSession(
                            activeCashierName,
                            activeCashierRole,
                            true,
                            System.currentTimeMillis(),
                            opening
                    );
                    refreshShiftPill();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatPeso(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "₱" + formatter.format(amount);
    }

    private String formatStartTime(long startMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(startMillis));
    }

    private String formatDuration(long startMillis) {
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - startMillis);
        long totalMinutes = elapsedMillis / (60 * 1000);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }
}