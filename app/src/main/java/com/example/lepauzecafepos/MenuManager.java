package com.example.lepauzecafepos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class MenuManager {

    private final Context context;
    private final LinearLayout menuItemsContainer;
    private final Button btnAddItem, btnArchiveSelected, btnViewArchive;
    private final CheckBox headerCheckbox;

    private final List<CheckBox> allRowCheckboxes = new ArrayList<>();
    private final List<ArchivedItem> archivedItemsList = new ArrayList<>();

    private static class ArchivedItem {
        String name, category, price, cost;

        ArchivedItem(String name, String category, String price, String cost) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.cost = cost;
        }
    }

    public MenuManager(Context context, View rootView) {
        this.context = context;

        // Bind Menu Controls from rootView
        menuItemsContainer = rootView.findViewById(R.id.menuItemsContainer);
        btnAddItem = rootView.findViewById(R.id.btnAddItem);
        btnArchiveSelected = rootView.findViewById(R.id.btnArchiveSelected);
        btnViewArchive = rootView.findViewById(R.id.btnViewArchive);
        headerCheckbox = rootView.findViewById(R.id.headerCheckbox);

        if (btnArchiveSelected != null) {
            btnArchiveSelected.setEnabled(false);
            btnArchiveSelected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
        }

        if (headerCheckbox != null) {
            headerCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CheckBox cb : allRowCheckboxes) {
                    if (cb != null) {
                        cb.setChecked(isChecked);
                    }
                }
                updateArchiveButtonState();
            });
        }

        if (btnAddItem != null) {
            btnAddItem.setOnClickListener(v -> showAddItemDialog());
        }
        if (btnArchiveSelected != null) {
            btnArchiveSelected.setOnClickListener(v -> bulkArchiveSelectedItems());
        }
        if (btnViewArchive != null) {
            btnViewArchive.setOnClickListener(v -> showArchiveDialog());
        }
    }

    private void updateArchiveButtonState() {
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
                    anyChecked ? Color.parseColor("#8B0000") : Color.parseColor("#555555")
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
            return String.format("%.1f%%", margin);
        } catch (Exception e) {
            return "0.0%";
        }
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

        builder.setView(layout);

        builder.setPositiveButton("Add Item", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            String rawPrice = inputPrice.getText().toString().trim();
            String rawCost = inputCost.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || rawPrice.isEmpty() || rawCost.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPrice = formatCurrencyInput(rawPrice);
            String formattedCost = formatCurrencyInput(rawCost);

            addNewRowToTable(name, category, formattedPrice, formattedCost);
            Toast.makeText(context, name + " added to menu!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addNewRowToTable(String name, String category, String price, String cost) {
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int cbWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics());
        int cbMarginEndPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());

        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightPx));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(paddingPx, 0, paddingPx, 0);

        int childCount = menuItemsContainer.getChildCount();
        row.setBackgroundColor(childCount % 2 == 1 ? Color.parseColor("#F9F9F9") : Color.WHITE);

        CheckBox checkBox = new CheckBox(context);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
                cbWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT);
        cbParams.setMarginEnd(cbMarginEndPx);
        checkBox.setLayoutParams(cbParams);
        checkBox.setChecked(headerCheckbox != null && headerCheckbox.isChecked());
        checkBox.setTag(row);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateArchiveButtonState());

        allRowCheckboxes.add(checkBox);
        row.addView(checkBox);

        TextView tvName = createCellTextView(name, 1.6f, "#333333");
        row.addView(tvName);

        TextView tvCategory = createCellTextView(category, 1.1f, "#666666");
        row.addView(tvCategory);

        TextView tvPrice = createCellTextView(price, 1.0f, "#333333");
        row.addView(tvPrice);

        TextView tvCost = createCellTextView(cost, 1.0f, "#333333");
        row.addView(tvCost);

        String marginText = calculateMargin(price, cost);
        TextView tvMargin = createCellTextView(marginText, 0.9f, "#16A34A");
        row.addView(tvMargin);

        row.setTag(new TextView[]{tvName, tvCategory, tvPrice, tvCost, tvMargin});

        // Action Buttons Container
        LinearLayout actionLayout = new LinearLayout(context);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actionLayout.setLayoutParams(new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics()),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        int iconSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics());

        // Edit Icon Button
        ImageButton btnEdit = new ImageButton(context);
        btnEdit.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
        btnEdit.setImageResource(R.drawable.ic_edit);
        btnEdit.setBackgroundColor(Color.TRANSPARENT);
        btnEdit.setColorFilter(Color.parseColor("#817060"));
        btnEdit.setOnClickListener(v -> showEditDialog(tvName, tvCategory, tvPrice, tvCost, tvMargin));
        actionLayout.addView(btnEdit);

        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(16, LinearLayout.LayoutParams.MATCH_PARENT));
        actionLayout.addView(spacer);

        // Archive Icon Button
        ImageButton btnArchive = new ImageButton(context);
        btnArchive.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
        btnArchive.setImageResource(R.drawable.ic_archive);
        btnArchive.setBackgroundColor(Color.TRANSPARENT);
        btnArchive.setColorFilter(Color.parseColor("#4A4A4A"));
        btnArchive.setOnClickListener(v -> {
            String currentName = tvName.getText().toString();
            new AlertDialog.Builder(context)
                    .setTitle("Archive Item")
                    .setMessage("Are you sure you want to archive " + currentName + "?")
                    .setPositiveButton("Archive", (dialog, which) -> {
                        archivedItemsList.add(new ArchivedItem(
                                currentName,
                                tvCategory.getText().toString(),
                                tvPrice.getText().toString(),
                                tvCost.getText().toString()
                        ));
                        menuItemsContainer.removeView(row);
                        allRowCheckboxes.remove(checkBox);
                        recolorTableRows();
                        updateArchiveButtonState();
                        Toast.makeText(context, currentName + " moved to archive.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        actionLayout.addView(btnArchive);

        row.addView(actionLayout);
        menuItemsContainer.addView(row);
        updateArchiveButtonState();
    }

    private void recolorTableRows() {
        for (int i = 0; i < menuItemsContainer.getChildCount(); i++) {
            View child = menuItemsContainer.getChildAt(i);
            child.setBackgroundColor(i % 2 == 1 ? Color.parseColor("#F9F9F9") : Color.WHITE);
        }
    }

    private void bulkArchiveSelectedItems() {
        List<View> rowsToRemove = new ArrayList<>();
        List<CheckBox> checkboxesToRemove = new ArrayList<>();

        for (CheckBox cb : allRowCheckboxes) {
            if (cb != null && cb.isChecked()) {
                View row = (View) cb.getTag();
                if (row != null) {
                    rowsToRemove.add(row);
                    checkboxesToRemove.add(cb);
                }
            }
        }

        if (rowsToRemove.isEmpty()) {
            Toast.makeText(context, "No items selected to archive.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedCount = rowsToRemove.size();
        new AlertDialog.Builder(context)
                .setTitle("Archive Selected Items")
                .setMessage("Are you sure you want to archive " + selectedCount + " selected item(s)?")
                .setPositiveButton("Archive", (dialog, which) -> {
                    for (View row : rowsToRemove) {
                        TextView[] cells = (TextView[]) row.getTag();
                        if (cells != null && cells.length >= 4) {
                            archivedItemsList.add(new ArchivedItem(
                                    cells[0].getText().toString(),
                                    cells[1].getText().toString(),
                                    cells[2].getText().toString(),
                                    cells[3].getText().toString()
                            ));
                        }
                        menuItemsContainer.removeView(row);
                    }
                    allRowCheckboxes.removeAll(checkboxesToRemove);
                    recolorTableRows();

                    if (headerCheckbox != null) {
                        headerCheckbox.setChecked(false);
                    }
                    updateArchiveButtonState();
                    Toast.makeText(context, selectedCount + " item(s) moved to archive.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showArchiveDialog() {
        if (archivedItemsList.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle("Archived Items")
                    .setMessage("There are no archived items.")
                    .setPositiveButton("Close", null)
                    .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Archived Items (" + archivedItemsList.size() + ")");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 24, 40, 24);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);

        builder.setPositiveButton("Close", null);
        AlertDialog dialogInstance = builder.create();

        for (int i = 0; i < archivedItemsList.size(); i++) {
            ArchivedItem item = archivedItemsList.get(i);

            LinearLayout itemRow = new LinearLayout(context);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);
            itemRow.setPadding(0, 12, 0, 12);

            TextView tvInfo = new TextView(context);
            tvInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            String itemMargin = calculateMargin(item.price, item.cost);
            tvInfo.setText(item.name + " (" + item.category + ") - Price: " + item.price + " | Margin: " + itemMargin);
            tvInfo.setTextColor(Color.parseColor("#333333"));
            tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            itemRow.addView(tvInfo);

            Button btnRestore = new Button(context);
            btnRestore.setText("Restore");
            btnRestore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            btnRestore.setAllCaps(false);
            btnRestore.setTextColor(Color.WHITE);
            btnRestore.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));

            final int index = i;
            btnRestore.setOnClickListener(v -> {
                addNewRowToTable(item.name, item.category, item.price, item.cost);
                archivedItemsList.remove(index);
                Toast.makeText(context, item.name + " restored to menu!", Toast.LENGTH_SHORT).show();
                dialogInstance.dismiss();
                showArchiveDialog();
            });

            itemRow.addView(btnRestore);
            scrollContent.addView(itemRow);
        }

        scrollView.addView(scrollContent);
        layout.addView(scrollView);
        dialogInstance.setView(layout);
        dialogInstance.show();
    }

    private TextView createCellTextView(String text, float weight, String hexColor) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        return tv;
    }

    private void showEditDialog(TextView nameView, TextView categoryView, TextView priceView, TextView costView, TextView marginView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Menu Item");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        layout.addView(createFormLabel("Item Name", 0));
        final EditText inputName = new EditText(context);
        inputName.setText(nameView.getText().toString());
        layout.addView(inputName);

        layout.addView(createFormLabel("Category", 12));
        final EditText inputCategory = new EditText(context);
        inputCategory.setText(categoryView.getText().toString());
        layout.addView(inputCategory);

        layout.addView(createFormLabel("Price", 12));
        final EditText inputPrice = new EditText(context);
        inputPrice.setText(priceView.getText().toString().replace("₱", "").trim());
        layout.addView(createCurrencyInputRow(inputPrice));

        layout.addView(createFormLabel("Cost", 12));
        final EditText inputCost = new EditText(context);
        inputCost.setText(costView.getText().toString().replace("₱", "").trim());
        layout.addView(createCurrencyInputRow(inputCost));

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String rawPrice = inputPrice.getText().toString().trim();
            String rawCost = inputCost.getText().toString().trim();

            String formattedPrice = formatCurrencyInput(rawPrice);
            String formattedCost = formatCurrencyInput(rawCost);

            nameView.setText(inputName.getText().toString().trim());
            categoryView.setText(inputCategory.getText().toString().trim());
            priceView.setText(formattedPrice);
            costView.setText(formattedCost);

            marginView.setText(calculateMargin(formattedPrice, formattedCost));

            Toast.makeText(context, "Item updated successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}