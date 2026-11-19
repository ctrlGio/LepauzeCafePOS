package com.example.lepauzecafepos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerManager {

    private static final int ERROR_RED = Color.parseColor("#EA2A2A");

    private final Context context;
    private final LinearLayout customerItemsContainer;
    private final Button btnAddCustomer, btnArchiveCustomerSelected, btnViewCustomerArchive;
    private final CheckBox custHeaderCheckbox;

    private final List<CheckBox> allRowCheckboxes = new ArrayList<>();
    private final List<CustomerRecord> activeRecords = new ArrayList<>();
    private final List<CustomerRecord> archivedRecords = new ArrayList<>();

    private boolean isViewingArchive = false;

    public static class OrderItem {
        String productName;
        int quantity;

        public OrderItem(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }
    }

    public static class CustomerRecord {
        String name, servedBy, date, totalCost;
        List<OrderItem> orders;

        public CustomerRecord(String name, String servedBy, String date, List<OrderItem> orders, String totalCost) {
            this.name = name;
            this.servedBy = servedBy;
            this.date = date;
            this.orders = orders;
            this.totalCost = totalCost;
        }
    }

    public CustomerManager(Context context, View rootView) {
        this.context = context;

        customerItemsContainer = rootView.findViewById(R.id.customerItemsContainer);
        btnAddCustomer = rootView.findViewById(R.id.btnAddCustomer);
        btnArchiveCustomerSelected = rootView.findViewById(R.id.btnDeleteCustomerSelected);
        btnViewCustomerArchive = rootView.findViewById(R.id.btnViewCustomerArchive);
        custHeaderCheckbox = rootView.findViewById(R.id.custHeaderCheckbox);

        if (btnArchiveCustomerSelected != null) {
            btnArchiveCustomerSelected.setText("Archive Selected");
            btnArchiveCustomerSelected.setEnabled(false);
            btnArchiveCustomerSelected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
        }

        if (btnViewCustomerArchive != null) {
            btnViewCustomerArchive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#817060")));
            btnViewCustomerArchive.setOnClickListener(v -> toggleArchiveView());
        }

        if (custHeaderCheckbox != null) {
            custHeaderCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CheckBox cb : allRowCheckboxes) {
                    if (cb != null) {
                        cb.setChecked(isChecked);
                    }
                }
                updateActionButtonState();
            });
        }

        if (btnAddCustomer != null) {
            btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
        }
        if (btnArchiveCustomerSelected != null) {
            btnArchiveCustomerSelected.setOnClickListener(v -> {
                if (isViewingArchive) {
                    confirmRestoreSelectedRecords();
                } else {
                    confirmArchiveSelectedRecords();
                }
            });
        }

        seedInitialRecords(); // delete pag di na need ng sample item
        refreshTableUI();
    }

    // mula dito
    private void seedInitialRecords() {
        List<OrderItem> order1 = new ArrayList<>();
        order1.add(new OrderItem("Iced Americano", 2));
        order1.add(new OrderItem("Butter Croissant", 1));
        activeRecords.add(new CustomerRecord("Albert James Mactal", "Barista Ryan", "Nov 17, 2026", order1, "₱335.00"));

        List<OrderItem> order2 = new ArrayList<>();
        order2.add(new OrderItem("Caramel Macchiato", 1));
        activeRecords.add(new CustomerRecord("Aron John Returan", "Barista Gian", "Nov 16, 2026", order2, "₱155.00"));
    }
    // hanggang dito sa line na to para mawala sample items

    private void toggleArchiveView() {
        isViewingArchive = !isViewingArchive;
        if (isViewingArchive) {
            if (btnViewCustomerArchive != null) btnViewCustomerArchive.setText("Go Back");
            if (btnAddCustomer != null) btnAddCustomer.setVisibility(View.GONE);
            if (btnArchiveCustomerSelected != null) btnArchiveCustomerSelected.setText("Restore Selected");
        } else {
            if (btnViewCustomerArchive != null) btnViewCustomerArchive.setText("View Archive");
            if (btnAddCustomer != null) btnAddCustomer.setVisibility(View.VISIBLE);
            if (btnArchiveCustomerSelected != null) btnArchiveCustomerSelected.setText("Archive Selected");
        }
        if (custHeaderCheckbox != null) custHeaderCheckbox.setChecked(false);
        refreshTableUI();
    }

    private void updateActionButtonState() {
        boolean anyChecked = false;
        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                anyChecked = true;
                break;
            }
        }
        if (btnArchiveCustomerSelected != null) {
            btnArchiveCustomerSelected.setEnabled(anyChecked);
            btnArchiveCustomerSelected.setBackgroundTintList(ColorStateList.valueOf(
                    anyChecked ? Color.parseColor("#16A34A") : Color.parseColor("#555555")
            ));
        }
    }

    private TextView createFormLabel(String labelText, int topMarginDp) {
        TextView label = new TextView(context);
        label.setText(labelText);
        label.setTextColor(Color.parseColor("#1A1F17"));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        label.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int topPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMarginDp, context.getResources().getDisplayMetrics());
        params.setMargins(0, topPx, 0, 4);
        label.setLayoutParams(params);
        return label;
    }

    private void applyFieldError(EditText editText, ColorStateList defaultTint, String message) {
        editText.setError(message);
        editText.setBackgroundTintList(ColorStateList.valueOf(ERROR_RED));
        editText.setTextColor(ERROR_RED);
    }

    private void clearFieldError(EditText editText, ColorStateList defaultTint, ColorStateList defaultTextColor) {
        editText.setError(null);
        if (defaultTint != null) {
            editText.setBackgroundTintList(defaultTint);
        }
        if (defaultTextColor != null) {
            editText.setTextColor(defaultTextColor);
        }
    }

    private void attachClearErrorOnType(EditText editText, ColorStateList defaultTint, ColorStateList defaultTextColor) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getError() != null || (defaultTint != null && editText.getBackgroundTintList() != defaultTint)) {
                    clearFieldError(editText, defaultTint, defaultTextColor);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showAddCustomerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Customer Purchase Record");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        layout.addView(createFormLabel("Customer Name", 0));
        final EditText inputName = new EditText(context);
        inputName.setHint("e.g., John Doe");
        layout.addView(inputName);

        layout.addView(createFormLabel("Served By (Employee)", 12));
        final EditText inputServedBy = new EditText(context);
        inputServedBy.setHint("e.g., Albert James Mactal");
        layout.addView(inputServedBy);

        layout.addView(createFormLabel("Product & Quantity (e.g. Spanish Latte:2, Croissant:1)", 12));
        final EditText inputOrders = new EditText(context);
        inputOrders.setHint("Spanish Latte:2, Croissant:1");
        layout.addView(inputOrders);

        layout.addView(createFormLabel("Total Cost", 12));
        final EditText inputTotal = new EditText(context);
        inputTotal.setHint("290.00");
        inputTotal.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputTotal);

        ColorStateList nameDefaultTint = inputName.getBackgroundTintList();
        ColorStateList nameDefaultText = inputName.getTextColors();
        ColorStateList servedDefaultTint = inputServedBy.getBackgroundTintList();
        ColorStateList servedDefaultText = inputServedBy.getTextColors();
        ColorStateList ordersDefaultTint = inputOrders.getBackgroundTintList();
        ColorStateList ordersDefaultText = inputOrders.getTextColors();
        ColorStateList totalDefaultTint = inputTotal.getBackgroundTintList();
        ColorStateList totalDefaultText = inputTotal.getTextColors();

        attachClearErrorOnType(inputName, nameDefaultTint, nameDefaultText);
        attachClearErrorOnType(inputServedBy, servedDefaultTint, servedDefaultText);
        attachClearErrorOnType(inputOrders, ordersDefaultTint, ordersDefaultText);
        attachClearErrorOnType(inputTotal, totalDefaultTint, totalDefaultText);

        builder.setView(layout);
        builder.setPositiveButton("Add Record", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        // change the whole addButton on this if the client wants it to automatically change the served by into "Barista (name)"
        // private String formatServedByName(String rawServedBy) {
        //    String trimmed = rawServedBy == null ? "" : rawServedBy.trim();
        //    if (trimmed.isEmpty()) {
        //        return "";
        //    }
        //    if (trimmed.toLowerCase(Locale.US).startsWith("barista ")) {
        //        return trimmed;
        //    }
        //    return "Barista " + trimmed;
        //}
        addButton.setOnClickListener(v -> {
            clearFieldError(inputName, nameDefaultTint, nameDefaultText);
            clearFieldError(inputServedBy, servedDefaultTint, servedDefaultText);
            clearFieldError(inputOrders, ordersDefaultTint, ordersDefaultText);
            clearFieldError(inputTotal, totalDefaultTint, totalDefaultText);

            String name = inputName.getText().toString().trim();
            String servedBy = inputServedBy.getText().toString().trim(); // change this to this String servedBy = formatServedByName(inputServedBy.getText().toString());
            String rawOrders = inputOrders.getText().toString().trim();
            String total = inputTotal.getText().toString().trim();

            boolean hasError = false;
            EditText firstErrorField = null;

            if (name.isEmpty()) {
                applyFieldError(inputName, nameDefaultTint, "Customer name cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputName;
            }
            if (servedBy.isEmpty()) {
                applyFieldError(inputServedBy, servedDefaultTint, "Served by cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputServedBy;
            }
            if (rawOrders.isEmpty()) {
                applyFieldError(inputOrders, ordersDefaultTint, "Orders cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputOrders;
            }
            if (total.isEmpty()) {
                applyFieldError(inputTotal, totalDefaultTint, "Total cost cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputTotal;
            }

            if (hasError) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                if (firstErrorField != null) {
                    firstErrorField.requestFocus();
                }
                return;
            }

            List<OrderItem> parsedOrders = new ArrayList<>();
            try {
                String[] parts = rawOrders.split(",");
                for (String p : parts) {
                    String[] sub = p.split(":");
                    String prod = sub[0].trim();
                    int qty = sub.length > 1 ? Integer.parseInt(sub[1].trim()) : 1;
                    parsedOrders.add(new OrderItem(prod, qty));
                }
            } catch (Exception e) {
                parsedOrders.clear();
                parsedOrders.add(new OrderItem(rawOrders, 1));
            }

            String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
            String formattedTotal = total.startsWith("₱") ? total : "₱" + total;

            activeRecords.add(new CustomerRecord(name, servedBy, currentDate, parsedOrders, formattedTotal));
            refreshTableUI();
            dialog.dismiss();
            Toast.makeText(context, "Purchase record added successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshTableUI() {
        customerItemsContainer.removeAllViews();
        allRowCheckboxes.clear();

        List<CustomerRecord> targetList = isViewingArchive ? archivedRecords : activeRecords;

        int index = 0;
        for (CustomerRecord record : targetList) {
            addMenuTableRow(record, index++);
        }
        updateActionButtonState();
    }

    private void addMenuTableRow(CustomerRecord record, int position) {
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int cbWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics());
        int cbMarginEndPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());

        LinearLayout masterRowWrapper = new LinearLayout(context);
        masterRowWrapper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        masterRowWrapper.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics())));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(paddingPx, 0, paddingPx, 0);
        row.setBackgroundColor(position % 2 == 1 ? Color.parseColor("#F9F9F9") : Color.WHITE);

        CheckBox checkBox = new CheckBox(context);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(cbWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT);
        cbParams.setMarginEnd(cbMarginEndPx);
        checkBox.setLayoutParams(cbParams);
        checkBox.setChecked(custHeaderCheckbox != null && custHeaderCheckbox.isChecked());
        checkBox.setTag(record);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateActionButtonState());
        allRowCheckboxes.add(checkBox);
        row.addView(checkBox);

        TextView tvName = createCellTextView(record.name, 1.6f, "#333333");
        row.addView(tvName);

        TextView tvServed = createCellTextView(record.servedBy, 1.4f, "#666666");
        row.addView(tvServed);

        TextView tvDate = createCellTextView(record.date, 1.3f, "#666666");
        row.addView(tvDate);

        String summaryText = record.orders.isEmpty() ? "No orders" : record.orders.get(0).productName + " (" + record.orders.get(0).quantity + ")";
        if (record.orders.size() > 1) {
            summaryText += " (+" + (record.orders.size() - 1) + " more)";
        }
        TextView tvOrdersSummary = createCellTextView(summaryText, 1.6f, "#1A1F17");
        tvOrdersSummary.setTypeface(null, Typeface.BOLD);
        row.addView(tvOrdersSummary);

        TextView tvTotal = createCellTextView(record.totalCost, 1.0f, "#16A34A");
        row.addView(tvTotal);

        // Action Buttons Container
        LinearLayout actionLayout = new LinearLayout(context);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actionLayout.setLayoutParams(new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics()),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        int iconSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics());

        if (!isViewingArchive) {
            ImageButton btnEdit = new ImageButton(context);
            btnEdit.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
            btnEdit.setImageResource(R.drawable.ic_edit);
            btnEdit.setBackgroundColor(Color.TRANSPARENT);
            btnEdit.setColorFilter(Color.parseColor("#817060"));
            btnEdit.setOnClickListener(v -> showEditDialog(record));
            actionLayout.addView(btnEdit);

            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(16, LinearLayout.LayoutParams.MATCH_PARENT));
            actionLayout.addView(spacer);
        }

        ImageButton btnArchiveToggle = new ImageButton(context);
        btnArchiveToggle.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
        btnArchiveToggle.setImageResource(isViewingArchive ? R.drawable.ic_restore : R.drawable.ic_archive);
        btnArchiveToggle.setBackgroundColor(Color.TRANSPARENT);
        btnArchiveToggle.setColorFilter(Color.parseColor(isViewingArchive ? "#16A34A" : "#EA2A2A"));
        btnArchiveToggle.setOnClickListener(v -> {
            String title = isViewingArchive ? "Restore Record" : "Archive Record";
            String message = isViewingArchive ? "Are you sure you want to restore " + record.name + "'s record?" : "Are you sure you want to archive " + record.name + "'s record?";
            String positiveBtn = isViewingArchive ? "Restore" : "Archive";

            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveBtn, (dialog, which) -> {
                        if (isViewingArchive) {
                            archivedRecords.remove(record);
                            activeRecords.add(record);
                            Toast.makeText(context, record.name + " restored from archive.", Toast.LENGTH_SHORT).show();
                        } else {
                            activeRecords.remove(record);
                            archivedRecords.add(record);
                            Toast.makeText(context, record.name + " moved to archive.", Toast.LENGTH_SHORT).show();
                        }
                        refreshTableUI();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        actionLayout.addView(btnArchiveToggle);
        row.addView(actionLayout);

        masterRowWrapper.addView(row);

        LinearLayout detailsContainer = new LinearLayout(context);
        detailsContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        detailsContainer.setOrientation(LinearLayout.VERTICAL);
        detailsContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
        detailsContainer.setPadding(paddingPx + cbWidthPx + cbMarginEndPx, dpToPx(8), paddingPx, dpToPx(8));
        detailsContainer.setVisibility(View.GONE);

        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setPadding(0, 0, 0, dpToPx(4));

        TextView lblProduct = new TextView(context);
        lblProduct.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
        lblProduct.setText("Product:");
        lblProduct.setTypeface(null, Typeface.BOLD);
        lblProduct.setTextColor(Color.parseColor("#1A1F17"));
        headerRow.addView(lblProduct);

        TextView lblQty = new TextView(context);
        lblQty.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        lblQty.setText("Quantity:");
        lblQty.setTypeface(null, Typeface.BOLD);
        lblQty.setTextColor(Color.parseColor("#1A1F17"));
        headerRow.addView(lblQty);
        detailsContainer.addView(headerRow);

        for (OrderItem item : record.orders) {
            LinearLayout itemRow = new LinearLayout(context);
            itemRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setPadding(0, dpToPx(2), 0, dpToPx(2));

            TextView tvProdName = new TextView(context);
            tvProdName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
            tvProdName.setText(item.productName);
            tvProdName.setTextColor(Color.parseColor("#333333"));
            tvProdName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            itemRow.addView(tvProdName);

            TextView tvProdQty = new TextView(context);
            tvProdQty.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvProdQty.setText(String.valueOf(item.quantity));
            tvProdQty.setTextColor(Color.parseColor("#333333"));
            tvProdQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            itemRow.addView(tvProdQty);

            detailsContainer.addView(itemRow);
        }

        masterRowWrapper.addView(detailsContainer);

        row.setOnClickListener(v -> {
            if (detailsContainer.getVisibility() == View.VISIBLE) {
                detailsContainer.setVisibility(View.GONE);
            } else {
                detailsContainer.setVisibility(View.VISIBLE);
            }
        });

        customerItemsContainer.addView(masterRowWrapper);
    }

    private void confirmArchiveSelectedRecords() {
        List<CustomerRecord> toArchive = new ArrayList<>();
        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                CustomerRecord record = (CustomerRecord) cb.getTag();
                if (record != null) toArchive.add(record);
            }
        }

        if (toArchive.isEmpty()) {
            Toast.makeText(context, "No records selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Archive Selected Records")
                .setMessage("Are you sure you want to archive " + toArchive.size() + " selected record(s)?")
                .setPositiveButton("Archive", (dialog, which) -> {
                    activeRecords.removeAll(toArchive);
                    archivedRecords.addAll(toArchive);
                    if (custHeaderCheckbox != null) custHeaderCheckbox.setChecked(false);
                    refreshTableUI();
                    Toast.makeText(context, toArchive.size() + " record(s) archived.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRestoreSelectedRecords() {
        List<CustomerRecord> toRestore = new ArrayList<>();
        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                CustomerRecord record = (CustomerRecord) cb.getTag();
                if (record != null) toRestore.add(record);
            }
        }

        if (toRestore.isEmpty()) {
            Toast.makeText(context, "No records selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Restore Selected Records")
                .setMessage("Are you sure you want to restore " + toRestore.size() + " selected record(s)?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    archivedRecords.removeAll(toRestore);
                    activeRecords.addAll(toRestore);
                    if (custHeaderCheckbox != null) custHeaderCheckbox.setChecked(false);
                    refreshTableUI();
                    Toast.makeText(context, toRestore.size() + " record(s) restored.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private TextView createCellTextView(String text, float weight, String hexColor) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        return tv;
    }

    private void showEditDialog(CustomerRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Purchase Record");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        layout.addView(createFormLabel("Customer Name", 0));
        final EditText inputName = new EditText(context);
        inputName.setText(record.name);
        layout.addView(inputName);

        layout.addView(createFormLabel("Served By (Employee)", 12));
        final EditText inputServed = new EditText(context);
        inputServed.setText(record.servedBy);
        layout.addView(inputServed);

        StringBuilder orderStr = new StringBuilder();
        for (int i = 0; i < record.orders.size(); i++) {
            OrderItem o = record.orders.get(i);
            orderStr.append(o.productName).append(":").append(o.quantity);
            if (i < record.orders.size() - 1) orderStr.append(", ");
        }

        layout.addView(createFormLabel("Orders & Quantity", 12));
        final EditText inputOrders = new EditText(context);
        inputOrders.setText(orderStr.toString());
        layout.addView(inputOrders);

        layout.addView(createFormLabel("Total Cost", 12));
        final EditText inputTotal = new EditText(context);
        inputTotal.setText(record.totalCost.replace("₱", "").trim());
        inputTotal.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputTotal);

        ColorStateList nameDefaultTint = inputName.getBackgroundTintList();
        ColorStateList nameDefaultText = inputName.getTextColors();
        ColorStateList servedDefaultTint = inputServed.getBackgroundTintList();
        ColorStateList servedDefaultText = inputServed.getTextColors();
        ColorStateList ordersDefaultTint = inputOrders.getBackgroundTintList();
        ColorStateList ordersDefaultText = inputOrders.getTextColors();
        ColorStateList totalDefaultTint = inputTotal.getBackgroundTintList();
        ColorStateList totalDefaultText = inputTotal.getTextColors();

        attachClearErrorOnType(inputName, nameDefaultTint, nameDefaultText);
        attachClearErrorOnType(inputServed, servedDefaultTint, servedDefaultText);
        attachClearErrorOnType(inputOrders, ordersDefaultTint, ordersDefaultText);
        attachClearErrorOnType(inputTotal, totalDefaultTint, totalDefaultText);

        builder.setView(layout);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            clearFieldError(inputName, nameDefaultTint, nameDefaultText);
            clearFieldError(inputServed, servedDefaultTint, servedDefaultText);
            clearFieldError(inputOrders, ordersDefaultTint, ordersDefaultText);
            clearFieldError(inputTotal, totalDefaultTint, totalDefaultText);

            String name = inputName.getText().toString().trim();
            String servedBy = inputServed.getText().toString().trim(); // this too into this String servedBy = formatServedByName(inputServed.getText().toString());
            String rawOrders = inputOrders.getText().toString().trim();
            String totalText = inputTotal.getText().toString().trim();

            boolean hasError = false;
            EditText firstErrorField = null;

            if (name.isEmpty()) {
                applyFieldError(inputName, nameDefaultTint, "Customer name cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputName;
            }
            if (servedBy.isEmpty()) {
                applyFieldError(inputServed, servedDefaultTint, "Served by cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputServed;
            }
            if (rawOrders.isEmpty()) {
                applyFieldError(inputOrders, ordersDefaultTint, "Orders cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputOrders;
            }
            if (totalText.isEmpty()) {
                applyFieldError(inputTotal, totalDefaultTint, "Total cost cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputTotal;
            }

            if (hasError) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                if (firstErrorField != null) {
                    firstErrorField.requestFocus();
                }
                return;
            }

            record.name = name;
            record.servedBy = servedBy;

            List<OrderItem> parsedOrders = new ArrayList<>();
            try {
                String[] parts = rawOrders.split(",");
                for (String p : parts) {
                    String[] sub = p.split(":");
                    String prod = sub[0].trim();
                    int qty = sub.length > 1 ? Integer.parseInt(sub[1].trim()) : 1;
                    parsedOrders.add(new OrderItem(prod, qty));
                }
            } catch (Exception e) {
                parsedOrders.clear();
                parsedOrders.add(new OrderItem(rawOrders, 1));
            }
            record.orders = parsedOrders;
            record.totalCost = totalText.startsWith("₱") ? totalText : "₱" + totalText;

            refreshTableUI();
            dialog.dismiss();
            Toast.makeText(context, "Record updated successfully!", Toast.LENGTH_SHORT).show();
        });
    }
}