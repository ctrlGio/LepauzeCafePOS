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

import java.util.ArrayList;
import java.util.List;

public class MenuManager {

    private static final int ERROR_RED = Color.parseColor("#EA2A2A");

    private final Context context;
    private final LinearLayout menuItemsContainer;
    private final Button btnAddItem, btnArchiveSelected, btnViewArchive;
    private final CheckBox headerCheckbox;

    private final List<CheckBox> allRowCheckboxes = new ArrayList<>();
    private final List<MenuItem> activeItems = new ArrayList<>();
    private final List<MenuItem> archivedItems = new ArrayList<>();

    private boolean isViewingArchive = false;

    private static class MenuItem {
        String name, category, price, cost;

        MenuItem(String name, String category, String price, String cost) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.cost = cost;
        }
    }

    public MenuManager(Context context, View rootView) {
        this.context = context;

        menuItemsContainer = rootView.findViewById(R.id.menuItemsContainer);
        btnAddItem = rootView.findViewById(R.id.btnAddItem);
        btnArchiveSelected = rootView.findViewById(R.id.btnArchiveSelected);
        btnViewArchive = rootView.findViewById(R.id.btnViewArchive);
        headerCheckbox = rootView.findViewById(R.id.headerCheckbox);

        if (btnArchiveSelected != null) {
            btnArchiveSelected.setText("Archive Selected");
            btnArchiveSelected.setEnabled(false);
            btnArchiveSelected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
        }

        if (btnViewArchive != null) {
            btnViewArchive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#817060")));
            btnViewArchive.setOnClickListener(v -> toggleArchiveView());
        }

        if (headerCheckbox != null) {
            headerCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CheckBox cb : allRowCheckboxes) {
                    if (cb != null) {
                        cb.setChecked(isChecked);
                    }
                }
                updateActionButtonState();
            });
        }

        if (btnAddItem != null) {
            btnAddItem.setOnClickListener(v -> showAddItemDialog());
        }
        if (btnArchiveSelected != null) {
            btnArchiveSelected.setOnClickListener(v -> {
                if (isViewingArchive) {
                    confirmRestoreSelectedItems();
                } else {
                    confirmArchiveSelectedItems();
                }
            });
        }

        seedInitialItems(); // To be deleted pag di na need ng sample items sa menu
        refreshTableUI();
    }

    // eto den
    private void seedInitialItems() {
        activeItems.add(new MenuItem("Iced Spanish Latte", "Coffee", "₱145.00", "₱42.00"));
        activeItems.add(new MenuItem("Caramel Macchiato", "Coffee", "₱155.00", "₱48.00"));
        activeItems.add(new MenuItem("Iced Americano", "Coffee", "₱120.00", "₱28.00"));
        activeItems.add(new MenuItem("Iced Matcha Latte", "Non-Coffee", "₱160.00", "₱55.00"));
        activeItems.add(new MenuItem("Butter Croissant", "Pastries", "₱95.00", "₱35.00"));
    }
    // hanggang dito sa line na to delete

    private void toggleArchiveView() {
        isViewingArchive = !isViewingArchive;
        if (isViewingArchive) {
            if (btnViewArchive != null) btnViewArchive.setText("Go Back");
            if (btnAddItem != null) btnAddItem.setVisibility(View.GONE);
            if (btnArchiveSelected != null) btnArchiveSelected.setText("Restore Selected");
        } else {
            if (btnViewArchive != null) btnViewArchive.setText("View Archive");
            if (btnAddItem != null) btnAddItem.setVisibility(View.VISIBLE);
            if (btnArchiveSelected != null) btnArchiveSelected.setText("Archive Selected");
        }
        if (headerCheckbox != null) headerCheckbox.setChecked(false);
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
        if (btnArchiveSelected != null) {
            btnArchiveSelected.setEnabled(anyChecked);
            btnArchiveSelected.setBackgroundTintList(ColorStateList.valueOf(
                    anyChecked ? Color.parseColor(isViewingArchive ? "#16A34A" : "#8B0000") : Color.parseColor("#555555")
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

    private LinearLayout createCurrencyInputRow(EditText editText) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView pesoPrefix = new TextView(context);
        pesoPrefix.setText("₱");
        pesoPrefix.setTextColor(Color.parseColor("#1A1F17"));
        pesoPrefix.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        pesoPrefix.setTypeface(null, Typeface.BOLD);
        pesoPrefix.setPadding(0, 0, 12, 0);

        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        row.addView(pesoPrefix);
        row.addView(editText);
        return row;
    }

    private String formatCurrencyInput(String rawInput) {
        if (rawInput.isEmpty()) return "₱0.00";
        String cleanValue = rawInput.replace("₱", "").trim();

        if (!cleanValue.contains(".")) {
            cleanValue = cleanValue + ".00";
        } else {
            String[] parts = cleanValue.split("\\.");
            if (parts.length > 1 && parts[1].length() == 1) {
                cleanValue = cleanValue + "0";
            }
        }
        return "₱" + cleanValue;
    }

    private String calculateMargin(String priceStr, String costStr) {
        try {
            String cleanPrice = priceStr.replace("₱", "").trim();
            String cleanCost = costStr.replace("₱", "").trim();
            double price = Double.parseDouble(cleanPrice);
            double cost = Double.parseDouble(cleanCost);

            if (price <= 0) return "0.0%";
            double margin = ((price - cost) / price) * 100.0;
            return String.format(java.util.Locale.US, "%.1f%%", margin);
        } catch (Exception e) {
            return "0.0%";
        }
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

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Menu Item");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        layout.addView(createFormLabel("Item Name", 0));
        final EditText inputName = new EditText(context);
        inputName.setHint("e.g., Iced Americano");
        layout.addView(inputName);

        layout.addView(createFormLabel("Category", 12));
        final EditText inputCategory = new EditText(context);
        inputCategory.setHint("e.g., Coffee, Pastries");
        layout.addView(inputCategory);

        layout.addView(createFormLabel("Price", 12));
        final EditText inputPrice = new EditText(context);
        inputPrice.setHint("150");
        layout.addView(createCurrencyInputRow(inputPrice));

        layout.addView(createFormLabel("Cost", 12));
        final EditText inputCost = new EditText(context);
        inputCost.setHint("45");
        layout.addView(createCurrencyInputRow(inputCost));

        ColorStateList nameDefaultTint = inputName.getBackgroundTintList();
        ColorStateList nameDefaultText = inputName.getTextColors();
        ColorStateList categoryDefaultTint = inputCategory.getBackgroundTintList();
        ColorStateList categoryDefaultText = inputCategory.getTextColors();
        ColorStateList priceDefaultTint = inputPrice.getBackgroundTintList();
        ColorStateList priceDefaultText = inputPrice.getTextColors();
        ColorStateList costDefaultTint = inputCost.getBackgroundTintList();
        ColorStateList costDefaultText = inputCost.getTextColors();

        attachClearErrorOnType(inputName, nameDefaultTint, nameDefaultText);
        attachClearErrorOnType(inputCategory, categoryDefaultTint, categoryDefaultText);
        attachClearErrorOnType(inputPrice, priceDefaultTint, priceDefaultText);
        attachClearErrorOnType(inputCost, costDefaultTint, costDefaultText);

        builder.setView(layout);
        builder.setPositiveButton("Add Item", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addButton.setOnClickListener(v -> {
            clearFieldError(inputName, nameDefaultTint, nameDefaultText);
            clearFieldError(inputCategory, categoryDefaultTint, categoryDefaultText);
            clearFieldError(inputPrice, priceDefaultTint, priceDefaultText);
            clearFieldError(inputCost, costDefaultTint, costDefaultText);

            String name = inputName.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            String rawPrice = inputPrice.getText().toString().trim();
            String rawCost = inputCost.getText().toString().trim();

            boolean hasError = false;
            EditText firstErrorField = null;

            if (name.isEmpty()) {
                applyFieldError(inputName, nameDefaultTint, "Item name cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputName;
            }
            if (category.isEmpty()) {
                applyFieldError(inputCategory, categoryDefaultTint, "Category cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputCategory;
            }
            if (rawPrice.isEmpty()) {
                applyFieldError(inputPrice, priceDefaultTint, "Price cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputPrice;
            }
            if (rawCost.isEmpty()) {
                applyFieldError(inputCost, costDefaultTint, "Cost cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputCost;
            }

            if (hasError) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                if (firstErrorField != null) {
                    firstErrorField.requestFocus();
                }
                return;
            }

            String formattedPrice = formatCurrencyInput(rawPrice);
            String formattedCost = formatCurrencyInput(rawCost);

            activeItems.add(new MenuItem(name, category, formattedPrice, formattedCost));
            refreshTableUI();
            dialog.dismiss();
            Toast.makeText(context, name + " added to menu!", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshTableUI() {
        menuItemsContainer.removeAllViews();
        allRowCheckboxes.clear();

        List<MenuItem> targetList = isViewingArchive ? archivedItems : activeItems;

        int index = 0;
        for (MenuItem item : targetList) {
            addMenuTableRow(item, index++);
        }
        updateActionButtonState();
    }

    private void addMenuTableRow(MenuItem item, int position) {
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int cbWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics());
        int cbMarginEndPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());

        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightPx));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(paddingPx, 0, paddingPx, 0);
        row.setBackgroundColor(position % 2 == 1 ? Color.parseColor("#F9F9F9") : Color.WHITE);

        CheckBox checkBox = new CheckBox(context);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
                cbWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT);
        cbParams.setMarginEnd(cbMarginEndPx);
        checkBox.setLayoutParams(cbParams);
        checkBox.setChecked(headerCheckbox != null && headerCheckbox.isChecked());
        checkBox.setTag(item);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateActionButtonState());

        allRowCheckboxes.add(checkBox);
        row.addView(checkBox);

        TextView tvName = createCellTextView(item.name, 1.6f, "#333333");
        row.addView(tvName);

        TextView tvCategory = createCellTextView(item.category, 1.1f, "#666666");
        row.addView(tvCategory);

        TextView tvPrice = createCellTextView(item.price, 1.0f, "#333333");
        row.addView(tvPrice);

        TextView tvCost = createCellTextView(item.cost, 1.0f, "#333333");
        row.addView(tvCost);

        String marginText = calculateMargin(item.price, item.cost);
        TextView tvMargin = createCellTextView(marginText, 0.9f, "#16A34A");
        row.addView(tvMargin);

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
            btnEdit.setOnClickListener(v -> showEditDialog(item));
            actionLayout.addView(btnEdit);

            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(16, LinearLayout.LayoutParams.MATCH_PARENT));
            actionLayout.addView(spacer);
        }

        ImageButton btnArchiveToggle = new ImageButton(context);
        btnArchiveToggle.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
        btnArchiveToggle.setImageResource(isViewingArchive ? R.drawable.ic_restore : R.drawable.ic_archive);
        btnArchiveToggle.setBackgroundColor(Color.TRANSPARENT);
        btnArchiveToggle.setColorFilter(Color.parseColor(isViewingArchive ? "#16A34A" : "#4A4A4A"));
        btnArchiveToggle.setOnClickListener(v -> {
            String title = isViewingArchive ? "Restore Item" : "Archive Item";
            String message = isViewingArchive
                    ? "Are you sure you want to restore " + item.name + "?"
                    : "Are you sure you want to archive " + item.name + "?";
            String positiveBtn = isViewingArchive ? "Restore" : "Archive";

            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveBtn, (dialog, which) -> {
                        if (isViewingArchive) {
                            archivedItems.remove(item);
                            activeItems.add(item);
                            Toast.makeText(context, item.name + " restored to menu!", Toast.LENGTH_SHORT).show();
                        } else {
                            activeItems.remove(item);
                            archivedItems.add(item);
                            Toast.makeText(context, item.name + " moved to archive.", Toast.LENGTH_SHORT).show();
                        }
                        refreshTableUI();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        actionLayout.addView(btnArchiveToggle);

        row.addView(actionLayout);
        menuItemsContainer.addView(row);
    }

    private void confirmArchiveSelectedItems() {
        List<MenuItem> toArchive = new ArrayList<>();
        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                MenuItem item = (MenuItem) cb.getTag();
                if (item != null) toArchive.add(item);
            }
        }

        if (toArchive.isEmpty()) {
            Toast.makeText(context, "No items selected to archive.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Archive Selected Items")
                .setMessage("Are you sure you want to archive " + toArchive.size() + " selected item(s)?")
                .setPositiveButton("Archive", (dialog, which) -> {
                    activeItems.removeAll(toArchive);
                    archivedItems.addAll(toArchive);
                    if (headerCheckbox != null) headerCheckbox.setChecked(false);
                    refreshTableUI();
                    Toast.makeText(context, toArchive.size() + " item(s) moved to archive.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRestoreSelectedItems() {
        List<MenuItem> toRestore = new ArrayList<>();
        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                MenuItem item = (MenuItem) cb.getTag();
                if (item != null) toRestore.add(item);
            }
        }

        if (toRestore.isEmpty()) {
            Toast.makeText(context, "No items selected to restore.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Restore Selected Items")
                .setMessage("Are you sure you want to restore " + toRestore.size() + " selected item(s)?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    archivedItems.removeAll(toRestore);
                    activeItems.addAll(toRestore);
                    if (headerCheckbox != null) headerCheckbox.setChecked(false);
                    refreshTableUI();
                    Toast.makeText(context, toRestore.size() + " item(s) restored to menu!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private TextView createCellTextView(String text, float weight, String hexColor) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        return tv;
    }

    private void showEditDialog(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Menu Item");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        layout.addView(createFormLabel("Item Name", 0));
        final EditText inputName = new EditText(context);
        inputName.setText(item.name);
        layout.addView(inputName);

        layout.addView(createFormLabel("Category", 12));
        final EditText inputCategory = new EditText(context);
        inputCategory.setText(item.category);
        layout.addView(inputCategory);

        layout.addView(createFormLabel("Price", 12));
        final EditText inputPrice = new EditText(context);
        inputPrice.setText(item.price.replace("₱", "").trim());
        layout.addView(createCurrencyInputRow(inputPrice));

        layout.addView(createFormLabel("Cost", 12));
        final EditText inputCost = new EditText(context);
        inputCost.setText(item.cost.replace("₱", "").trim());
        layout.addView(createCurrencyInputRow(inputCost));

        ColorStateList nameDefaultTint = inputName.getBackgroundTintList();
        ColorStateList nameDefaultText = inputName.getTextColors();
        ColorStateList categoryDefaultTint = inputCategory.getBackgroundTintList();
        ColorStateList categoryDefaultText = inputCategory.getTextColors();
        ColorStateList priceDefaultTint = inputPrice.getBackgroundTintList();
        ColorStateList priceDefaultText = inputPrice.getTextColors();
        ColorStateList costDefaultTint = inputCost.getBackgroundTintList();
        ColorStateList costDefaultText = inputCost.getTextColors();

        attachClearErrorOnType(inputName, nameDefaultTint, nameDefaultText);
        attachClearErrorOnType(inputCategory, categoryDefaultTint, categoryDefaultText);
        attachClearErrorOnType(inputPrice, priceDefaultTint, priceDefaultText);
        attachClearErrorOnType(inputCost, costDefaultTint, costDefaultText);

        builder.setView(layout);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            clearFieldError(inputName, nameDefaultTint, nameDefaultText);
            clearFieldError(inputCategory, categoryDefaultTint, categoryDefaultText);
            clearFieldError(inputPrice, priceDefaultTint, priceDefaultText);
            clearFieldError(inputCost, costDefaultTint, costDefaultText);

            String name = inputName.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            String rawPrice = inputPrice.getText().toString().trim();
            String rawCost = inputCost.getText().toString().trim();

            boolean hasError = false;
            EditText firstErrorField = null;

            if (name.isEmpty()) {
                applyFieldError(inputName, nameDefaultTint, "Item name cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputName;
            }
            if (category.isEmpty()) {
                applyFieldError(inputCategory, categoryDefaultTint, "Category cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputCategory;
            }
            if (rawPrice.isEmpty()) {
                applyFieldError(inputPrice, priceDefaultTint, "Price cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputPrice;
            }
            if (rawCost.isEmpty()) {
                applyFieldError(inputCost, costDefaultTint, "Cost cannot be blank");
                hasError = true;
                if (firstErrorField == null) firstErrorField = inputCost;
            }

            if (hasError) {
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                if (firstErrorField != null) {
                    firstErrorField.requestFocus();
                }
                return;
            }

            item.name = name;
            item.category = category;
            item.price = formatCurrencyInput(rawPrice);
            item.cost = formatCurrencyInput(rawCost);

            refreshTableUI();
            dialog.dismiss();
            Toast.makeText(context, "Item updated successfully!", Toast.LENGTH_SHORT).show();
        });
    }
}