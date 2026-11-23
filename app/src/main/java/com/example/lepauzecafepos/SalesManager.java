package com.example.lepauzecafepos;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesManager {

    public enum FilterRange {
        TODAY,
        YESTERDAY,
        THIS_WEEK,
        THIS_MONTH,
        LAST_MONTH,
        CUSTOM
    }

    public enum DisplayMode {
        CHART,
        TABLE
    }

    public static class RecordEntry {
        public final long timestampMillis;
        public final double amount;
        public final String label;
        public final String employeeName;

        public RecordEntry(long timestampMillis, double amount, String label) {
            this(timestampMillis, amount, label, "Unassigned");
        }

        public RecordEntry(long timestampMillis, double amount, String label, String employeeName) {
            this.timestampMillis = timestampMillis;
            this.amount = amount;
            this.label = label;
            this.employeeName = (employeeName == null || employeeName.trim().isEmpty())
                    ? "Unassigned"
                    : employeeName.trim();
        }
    }

    private static class EmployeeSummary {
        final String name;
        int orderCount;
        double totalSales;

        EmployeeSummary(String name) {
            this.name = name;
        }
    }

    private final Context context;
    private final View rootView;
    private TextView tvSalesDateRange;
    private TextView tvSalesFilterToday;
    private TextView tvSalesFilterYesterday;
    private TextView tvSalesFilterWeek;
    private TextView tvSalesFilterMonth;
    private TextView tvSalesFilterLastMonth;
    private TextView tvSalesFilterCustom;
    private AppCompatButton btnExportSalesReport;

    // Custom Date
    private LinearLayout layoutCustomDateRange;
    private TextView tvCustomFromDate;
    private TextView tvCustomToDate;

    // Metric Card
    private TextView tvSalesTotalValue;
    private TextView tvSalesDeltaText;
    private TextView tvExpensesTotalValue;
    private TextView tvExpensesDeltaText;
    private TextView tvNetTotalValue;

    // Mode Views
    private LinearLayout salesModeContainer;
    private TextView tvModeChart;
    private TextView tvModeTable;

    // Chart
    private FrameLayout salesChartContainer;
    private LinearLayout salesTableContainer;
    private LinearLayout salesTableHeaderRow;
    private LinearLayout salesTableRowsContainer;

    // Donut
    private FrameLayout salesDonutContainer;
    private TextView tvPesoLeftValue;
    private TextView tvPesoSpentValue;

    // Employee Sales Views
    private TextView tvEmployeeSalesCount;
    private LinearLayout employeeSalesRowsContainer;

    // Data
    private final List<RecordEntry> salesEntries = new ArrayList<RecordEntry>();
    private final List<RecordEntry> expenseEntries = new ArrayList<RecordEntry>();

    private FilterRange currentFilter = FilterRange.TODAY;
    private DisplayMode currentMode = DisplayMode.CHART;

    private long customFromMillis;
    private long customToMillis;

    private final Locale phLocale = Locale.forLanguageTag("en-PH");
    private final NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(phLocale);
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    public SalesManager(Context context, View rootView) {
        this.context = context;
        this.rootView = rootView;

        initCustomDefaultDates();
        bindViews();
        seedSampleData();
        setupListeners();
        refreshAll();
    }

    private void initCustomDefaultDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        customToMillis = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, -3);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        customFromMillis = cal.getTimeInMillis();
    }

    private void bindViews() {
        tvSalesDateRange = rootView.findViewById(R.id.tvSalesDateRange);
        tvSalesFilterToday = rootView.findViewById(R.id.tvSalesFilterToday);
        tvSalesFilterYesterday = rootView.findViewById(R.id.tvSalesFilterYesterday);
        tvSalesFilterWeek = rootView.findViewById(R.id.tvSalesFilterWeek);
        tvSalesFilterMonth = rootView.findViewById(R.id.tvSalesFilterMonth);
        tvSalesFilterLastMonth = rootView.findViewById(R.id.tvSalesFilterLastMonth);
        tvSalesFilterCustom = rootView.findViewById(R.id.tvSalesFilterCustom);
        btnExportSalesReport = rootView.findViewById(R.id.btnExportSalesReport);

        layoutCustomDateRange = rootView.findViewById(R.id.layoutCustomDateRange);
        tvCustomFromDate = rootView.findViewById(R.id.tvCustomFromDate);
        tvCustomToDate = rootView.findViewById(R.id.tvCustomToDate);

        tvSalesTotalValue = rootView.findViewById(R.id.tvSalesTotalValue);
        tvSalesDeltaText = rootView.findViewById(R.id.tvSalesDeltaText);
        tvExpensesTotalValue = rootView.findViewById(R.id.tvExpensesTotalValue);
        tvExpensesDeltaText = rootView.findViewById(R.id.tvExpensesDeltaText);
        tvNetTotalValue = rootView.findViewById(R.id.tvNetTotalValue);

        salesModeContainer = rootView.findViewById(R.id.salesModeContainer);
        tvModeChart = rootView.findViewById(R.id.tvModeChart);
        tvModeTable = rootView.findViewById(R.id.tvModeTable);

        salesChartContainer = rootView.findViewById(R.id.salesChartContainer);
        salesTableContainer = rootView.findViewById(R.id.salesTableContainer);
        salesTableHeaderRow = rootView.findViewById(R.id.salesTableHeaderRow);
        salesTableRowsContainer = rootView.findViewById(R.id.salesTableRowsContainer);

        salesDonutContainer = rootView.findViewById(R.id.salesDonutContainer);
        tvPesoLeftValue = rootView.findViewById(R.id.tvPesoLeftValue);
        tvPesoSpentValue = rootView.findViewById(R.id.tvPesoSpentValue);

        tvEmployeeSalesCount = rootView.findViewById(R.id.tvEmployeeSalesCount);
        employeeSalesRowsContainer = rootView.findViewById(R.id.employeeSalesRowsContainer);

        if (btnExportSalesReport != null) {
            styleRoundedView(btnExportSalesReport, "#DDD5C7", "#C9C1B1", 10f);
            btnExportSalesReport.setTextColor(Color.parseColor("#1A1F17"));
        }

        if (salesModeContainer != null) {
            styleRoundedView(salesModeContainer, "#EAE4D9", "transparent", 10f);
        }

        styleRoundedView(tvCustomFromDate, "#FFFFFF", "#C9C1B1", 8f);
        styleRoundedView(tvCustomToDate, "#FFFFFF", "#C9C1B1", 8f);
        updateCustomDateLabels();
    }

    public void setSalesEntries(List<RecordEntry> entries) {
        salesEntries.clear();
        if (entries != null) {
            salesEntries.addAll(entries);
        }
        refreshAll();
    }

    public void setExpenseEntries(List<RecordEntry> entries) {
        expenseEntries.clear();
        if (entries != null) {
            expenseEntries.addAll(entries);
        }
        refreshAll();
    }

    public void addSale(long timestampMillis, double amount, String label) {
        salesEntries.add(new RecordEntry(timestampMillis, amount, label));
        refreshAll();
    }

    public void addSale(long timestampMillis, double amount, String label, String employeeName) {
        salesEntries.add(new RecordEntry(timestampMillis, amount, label, employeeName));
        refreshAll();
    }

    public void addExpense(long timestampMillis, double amount, String label) {
        expenseEntries.add(new RecordEntry(timestampMillis, amount, label));
        refreshAll();
    }

    private void seedSampleData() {
        salesEntries.clear();
        expenseEntries.clear();

        Calendar cal = Calendar.getInstance();
        for (int dayOffset = 45; dayOffset >= 0; dayOffset--) {
            Calendar baseDay = (Calendar) cal.clone();
            baseDay.add(Calendar.DAY_OF_YEAR, -dayOffset);

            Calendar tA1 = (Calendar) baseDay.clone();
            tA1.set(Calendar.HOUR_OF_DAY, 9);
            tA1.set(Calendar.MINUTE, 15);
            salesEntries.add(new RecordEntry(tA1.getTimeInMillis(), 2450.0 + (dayOffset % 3) * 350.0, "Morning Shift", "Barista Aron"));

            Calendar tA2 = (Calendar) baseDay.clone();
            tA2.set(Calendar.HOUR_OF_DAY, 12);
            tA2.set(Calendar.MINUTE, 30);
            salesEntries.add(new RecordEntry(tA2.getTimeInMillis(), 1680.0, "Lunch Rush", "Barista Aron"));

            Calendar tB = (Calendar) baseDay.clone();
            tB.set(Calendar.HOUR_OF_DAY, 15);
            tB.set(Calendar.MINUTE, 45);
            salesEntries.add(new RecordEntry(tB.getTimeInMillis(), 3120.0 + (dayOffset % 4) * 250.0, "Afternoon Shift", "Barista Gian"));

            Calendar tC = (Calendar) baseDay.clone();
            tC.set(Calendar.HOUR_OF_DAY, 19);
            tC.set(Calendar.MINUTE, 10);
            salesEntries.add(new RecordEntry(tC.getTimeInMillis(), 1890.0 + (dayOffset % 2) * 500.0, "Evening Shift", "Barista Ryan"));

            // Daily Expenses
            Calendar exp1 = (Calendar) baseDay.clone();
            exp1.set(Calendar.HOUR_OF_DAY, 10);
            exp1.set(Calendar.MINUTE, 0);
            expenseEntries.add(new RecordEntry(exp1.getTimeInMillis(), 1200.0 + (dayOffset % 3) * 300.0, "Dairy & Beans"));

            Calendar exp2 = (Calendar) baseDay.clone();
            exp2.set(Calendar.HOUR_OF_DAY, 16);
            exp2.set(Calendar.MINUTE, 0);
            expenseEntries.add(new RecordEntry(exp2.getTimeInMillis(), 950.0 + (dayOffset % 4) * 250.0, "Cups & Pastries"));
        }
    }

    private void setupListeners() {
        if (tvSalesFilterToday != null) tvSalesFilterToday.setOnClickListener(v -> selectFilter(FilterRange.TODAY));
        if (tvSalesFilterYesterday != null) tvSalesFilterYesterday.setOnClickListener(v -> selectFilter(FilterRange.YESTERDAY));
        if (tvSalesFilterWeek != null) tvSalesFilterWeek.setOnClickListener(v -> selectFilter(FilterRange.THIS_WEEK));
        if (tvSalesFilterMonth != null) tvSalesFilterMonth.setOnClickListener(v -> selectFilter(FilterRange.THIS_MONTH));
        if (tvSalesFilterLastMonth != null) tvSalesFilterLastMonth.setOnClickListener(v -> selectFilter(FilterRange.LAST_MONTH));
        if (tvSalesFilterCustom != null) tvSalesFilterCustom.setOnClickListener(v -> selectFilter(FilterRange.CUSTOM));

        if (tvCustomFromDate != null) tvCustomFromDate.setOnClickListener(v -> showDatePicker(true));
        if (tvCustomToDate != null) tvCustomToDate.setOnClickListener(v -> showDatePicker(false));

        if (tvModeChart != null) {
            tvModeChart.setOnClickListener(v -> {
                currentMode = DisplayMode.CHART;
                refreshAll();
            });
        }
        if (tvModeTable != null) {
            tvModeTable.setOnClickListener(v -> {
                currentMode = DisplayMode.TABLE;
                refreshAll();
            });
        }

        if (btnExportSalesReport != null) {
            btnExportSalesReport.setOnClickListener(v ->
                    Toast.makeText(context, "Exporting sales report (" + currentFilter.name() + ")...", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void selectFilter(FilterRange range) {
        currentFilter = range;
        if (layoutCustomDateRange != null) {
            layoutCustomDateRange.setVisibility(range == FilterRange.CUSTOM ? View.VISIBLE : View.GONE);
        }
        refreshAll();
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(isFrom ? customFromMillis : customToMillis);

        new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isFrom) {
                        picked.set(Calendar.HOUR_OF_DAY, 0);
                        picked.set(Calendar.MINUTE, 0);
                        picked.set(Calendar.SECOND, 0);
                        picked.set(Calendar.MILLISECOND, 0);
                        customFromMillis = picked.getTimeInMillis();
                        if (customFromMillis > customToMillis) {
                            customToMillis = endOfDay(customFromMillis);
                        }
                    } else {
                        customToMillis = endOfDay(picked.getTimeInMillis());
                        if (customToMillis < customFromMillis) {
                            customFromMillis = startOfDay(customToMillis);
                        }
                    }

                    updateCustomDateLabels();
                    refreshAll();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateCustomDateLabels() {
        if (tvCustomFromDate != null) {
            tvCustomFromDate.setText(displayDateFormat.format(new Date(customFromMillis)) + "   📅");
        }
        if (tvCustomToDate != null) {
            tvCustomToDate.setText(displayDateFormat.format(new Date(customToMillis)) + "   📅");
        }
    }

    public void refreshAll() {
        updateFilterPillStyles();
        updateSwitcherStyles();

        long[] currentWindow = getWindowForFilter(currentFilter);
        long startMillis = currentWindow[0];
        long endMillis = currentWindow[1];
        String comparisonLabel = getComparisonLabel(currentFilter, startMillis, endMillis);

        long durationMillis = Math.max(86_400_000L, endMillis - startMillis + 1L);
        long prevEndMillis = startMillis - 1L;
        long prevStartMillis = prevEndMillis - durationMillis + 1L;

        if (tvSalesDateRange != null) {
            String startStr = shortDateFormat.format(new Date(startMillis));
            String endStr = shortDateFormat.format(new Date(endMillis));
            int year = Calendar.getInstance().get(Calendar.YEAR);
            tvSalesDateRange.setText(startStr.equals(endStr)
                    ? startStr + ", " + year
                    : startStr + " – " + endStr + ", " + year);
        }

        double currentSales = sumInRange(salesEntries, startMillis, endMillis);
        double previousSales = sumInRange(salesEntries, prevStartMillis, prevEndMillis);

        double currentExpenses = sumInRange(expenseEntries, startMillis, endMillis);
        double previousExpenses = sumInRange(expenseEntries, prevStartMillis, prevEndMillis);

        double netLeft = currentSales - currentExpenses;

        if (tvSalesTotalValue != null) tvSalesTotalValue.setText(pesoFormat.format(currentSales));
        if (tvExpensesTotalValue != null) tvExpensesTotalValue.setText(pesoFormat.format(currentExpenses));
        if (tvNetTotalValue != null) tvNetTotalValue.setText(pesoFormat.format(netLeft));

        updateDeltaLabel(tvSalesDeltaText, currentSales, previousSales, comparisonLabel, false);
        updateDeltaLabel(tvExpensesDeltaText, currentExpenses, previousExpenses, comparisonLabel, true);

        if (tvPesoLeftValue != null) tvPesoLeftValue.setText(pesoFormat.format(Math.max(0.0, netLeft)));
        if (tvPesoSpentValue != null) tvPesoSpentValue.setText(pesoFormat.format(Math.max(0.0, currentExpenses)));

        renderDonutChart(Math.max(0.0, netLeft), Math.max(0.0, currentExpenses));
        renderEmployeeSales(startMillis, endMillis);

        Map<String, double[]> bucketed = buildBucketedSeries(startMillis, endMillis);
        if (currentMode == DisplayMode.CHART) {
            if (salesChartContainer != null) salesChartContainer.setVisibility(View.VISIBLE);
            if (salesTableContainer != null) salesTableContainer.setVisibility(View.GONE);
            renderLineAndExpenseChart(bucketed);
        } else {
            if (salesChartContainer != null) salesChartContainer.setVisibility(View.GONE);
            if (salesTableContainer != null) salesTableContainer.setVisibility(View.VISIBLE);
            renderTableRows(bucketed);
        }
    }

    private void renderEmployeeSales(long startMillis, long endMillis) {
        if (employeeSalesRowsContainer == null) {
            return;
        }
        employeeSalesRowsContainer.removeAllViews();

        Map<String, EmployeeSummary> summaryMap = new LinkedHashMap<String, EmployeeSummary>();
        for (int i = 0; i < salesEntries.size(); i++) {
            RecordEntry entry = salesEntries.get(i);
            if (entry.timestampMillis >= startMillis && entry.timestampMillis <= endMillis) {
                EmployeeSummary summary = summaryMap.get(entry.employeeName);
                if (summary == null) {
                    summary = new EmployeeSummary(entry.employeeName);
                    summaryMap.put(entry.employeeName, summary);
                }
                summary.orderCount += 1;
                summary.totalSales += entry.amount;
            }
        }

        List<EmployeeSummary> rankedList = new ArrayList<EmployeeSummary>(summaryMap.values());
        for (int outer = 0; outer < rankedList.size(); outer++) {
            for (int inner = outer + 1; inner < rankedList.size(); inner++) {
                if (rankedList.get(inner).totalSales > rankedList.get(outer).totalSales) {
                    EmployeeSummary swap = rankedList.get(outer);
                    rankedList.set(outer, rankedList.get(inner));
                    rankedList.set(inner, swap);
                }
            }
        }

        if (tvEmployeeSalesCount != null) {
            int staffCount = rankedList.size();
            tvEmployeeSalesCount.setText(staffCount + " staff");
        }

        if (rankedList.isEmpty()) {
            TextView emptyTv = new TextView(context);
            emptyTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            emptyTv.setPadding(36, 36, 36, 36);
            emptyTv.setText("No employee sales recorded for this period.");
            emptyTv.setTextColor(Color.parseColor("#817060"));
            emptyTv.setTextSize(12f);
            employeeSalesRowsContainer.addView(emptyTv);
            return;
        }

        for (int idx = 0; idx < rankedList.size(); idx++) {
            EmployeeSummary item = rankedList.get(idx);

            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(36, 28, 36, 28);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView cName = makeRowCell(item.name, 1.5f, "#1A1F17", Gravity.START);
            TextView cOrders = makeRowCell(String.valueOf(item.orderCount), 0.8f, "#817060", Gravity.END);
            TextView cSales = makeRowCell(pesoFormat.format(item.totalSales), 1.1f, "#16A34A", Gravity.END);

            row.addView(cName);
            row.addView(cOrders);
            row.addView(cSales);

            View divider = new View(context);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#EFECE6"));

            employeeSalesRowsContainer.addView(row);
            employeeSalesRowsContainer.addView(divider);
        }
    }

    private void updateDeltaLabel(TextView tvDelta, double currentVal, double prevVal, String comparisonLabel, boolean lowerIsBetter) {
        if (tvDelta == null) return;

        if (prevVal <= 0.0) {
            tvDelta.setText("—  " + comparisonLabel);
            tvDelta.setTextColor(Color.parseColor("#817060"));
            return;
        }

        double diffPct = ((currentVal - prevVal) / prevVal) * 100.0;
        String arrow = diffPct >= 0 ? "▲ " : "▼ ";
        String text = String.format(Locale.getDefault(), "%s%.1f%%  %s", arrow, Math.abs(diffPct), comparisonLabel);
        tvDelta.setText(text);

        boolean isPositive = lowerIsBetter ? (diffPct <= 0.0) : (diffPct >= 0.0);
        tvDelta.setTextColor(Color.parseColor(isPositive ? "#16A34A" : "#EA2A2A"));
    }

    private String getComparisonLabel(FilterRange filter, long startMillis, long endMillis) {
        switch (filter) {
            case TODAY:
                return "vs yesterday";
            case YESTERDAY:
                return "vs day before";
            case THIS_WEEK:
                return "vs last week";
            case THIS_MONTH:
                return "vs last month";
            case LAST_MONTH:
                return "vs prior month";
            case CUSTOM:
            default:
                long days = Math.max(1L, ((endMillis - startMillis) / 86_400_000L) + 1L);
                return "vs previous " + days + (days == 1L ? " day" : " days");
        }
    }

    private long[] getWindowForFilter(FilterRange range) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();

        switch (range) {
            case TODAY:
                return new long[]{startOfDay(now), endOfDay(now)};
            case YESTERDAY: {
                Calendar y = (Calendar) cal.clone();
                y.add(Calendar.DAY_OF_YEAR, -1);
                return new long[]{startOfDay(y.getTimeInMillis()), endOfDay(y.getTimeInMillis())};
            }
            case THIS_WEEK: {
                Calendar w = (Calendar) cal.clone();
                w.set(Calendar.DAY_OF_WEEK, w.getFirstDayOfWeek());
                return new long[]{startOfDay(w.getTimeInMillis()), endOfDay(now)};
            }
            case THIS_MONTH: {
                Calendar m = (Calendar) cal.clone();
                m.set(Calendar.DAY_OF_MONTH, 1);
                return new long[]{startOfDay(m.getTimeInMillis()), endOfDay(now)};
            }
            case LAST_MONTH: {
                Calendar lmStart = (Calendar) cal.clone();
                lmStart.add(Calendar.MONTH, -1);
                lmStart.set(Calendar.DAY_OF_MONTH, 1);

                Calendar lmEnd = (Calendar) lmStart.clone();
                lmEnd.set(Calendar.DAY_OF_MONTH, lmEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
                return new long[]{startOfDay(lmStart.getTimeInMillis()), endOfDay(lmEnd.getTimeInMillis())};
            }
            case CUSTOM:
            default:
                return new long[]{startOfDay(customFromMillis), endOfDay(customToMillis)};
        }
    }

    private double sumInRange(List<RecordEntry> list, long startMillis, long endMillis) {
        double total = 0.0;
        for (int i = 0; i < list.size(); i++) {
            RecordEntry entry = list.get(i);
            if (entry.timestampMillis >= startMillis && entry.timestampMillis <= endMillis) {
                total += entry.amount;
            }
        }
        return total;
    }

    private Map<String, double[]> buildBucketedSeries(long startMillis, long endMillis) {
        Map<String, double[]> map = new LinkedHashMap<String, double[]>();
        long totalSpanMillis = endMillis - startMillis;

        if (totalSpanMillis <= 86_400_000L) {
            SimpleDateFormat hourFmt = new SimpleDateFormat("h a", Locale.getDefault());
            Calendar cursor = Calendar.getInstance();
            cursor.setTimeInMillis(startOfDay(startMillis));

            for (int block = 0; block < 8; block++) {
                long bStart = cursor.getTimeInMillis();
                cursor.add(Calendar.HOUR_OF_DAY, 3);
                long bEnd = cursor.getTimeInMillis() - 1L;

                String key = hourFmt.format(new Date(bStart));
                double s = sumInRange(salesEntries, bStart, bEnd);
                double e = sumInRange(expenseEntries, bStart, bEnd);
                map.put(key, new double[]{s, e});
            }
            return map;
        }

        SimpleDateFormat bucketFmt = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        Calendar cursor = Calendar.getInstance();
        cursor.setTimeInMillis(startOfDay(startMillis));

        while (cursor.getTimeInMillis() <= endMillis) {
            long bStart = startOfDay(cursor.getTimeInMillis());
            long bEnd = endOfDay(bStart);
            cursor.add(Calendar.DAY_OF_YEAR, 1);

            String key = bucketFmt.format(new Date(bStart));
            double s = sumInRange(salesEntries, bStart, bEnd);
            double e = sumInRange(expenseEntries, bStart, bEnd);
            map.put(key, new double[]{s, e});
        }

        if (map.isEmpty()) {
            map.put(shortDateFormat.format(new Date(startMillis)), new double[]{0.0, 0.0});
        }
        return map;
    }

    private void renderLineAndExpenseChart(Map<String, double[]> series) {
        if (salesChartContainer == null) return;
        salesChartContainer.removeAllViews();

        final List<String> labels = new ArrayList<String>(series.keySet());
        final List<double[]> values = new ArrayList<double[]>(series.values());

        salesChartContainer.addView(new View(context) {
            private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint xLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint expenseBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                gridPaint.setColor(Color.parseColor("#ECE7DC"));
                gridPaint.setStrokeWidth(1.5f);

                axisLabelPaint.setColor(Color.parseColor("#817060"));
                axisLabelPaint.setTextSize(24f);
                axisLabelPaint.setTextAlign(Paint.Align.LEFT);

                xLabelPaint.setColor(Color.parseColor("#817060"));
                xLabelPaint.setTextSize(25f);
                xLabelPaint.setTextAlign(Paint.Align.CENTER);

                expenseBarPaint.setColor(Color.parseColor("#C2410C"));
                areaPaint.setColor(Color.parseColor("#2416A34A"));
                areaPaint.setStyle(Paint.Style.FILL);

                linePaint.setColor(Color.parseColor("#16A34A"));
                linePaint.setStrokeWidth(4f);
                linePaint.setStyle(Paint.Style.STROKE);
                linePaint.setStrokeCap(Paint.Cap.ROUND);
                linePaint.setStrokeJoin(Paint.Join.ROUND);

                pointPaint.setColor(Color.parseColor("#16A34A"));
                pointPaint.setStyle(Paint.Style.FILL);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0 || values.isEmpty()) return;

                float leftAxisWidth = 76f;
                float rightPad = 20f;
                float topPad = 14f;
                float bottomPad = 42f;

                float plotLeft = leftAxisWidth;
                float plotRight = Math.max(plotLeft + 40f, w - rightPad);
                float plotBottom = Math.max(topPad + 40f, h - bottomPad);
                float plotHeight = plotBottom - topPad;
                float plotWidth = plotRight - plotLeft;

                double rawMax = 1000.0;
                for (int i = 0; i < values.size(); i++) {
                    double[] pair = values.get(i);
                    rawMax = Math.max(rawMax, Math.max(pair[0], pair[1]));
                }
                double maxAxisVal = Math.ceil(rawMax / 5000.0) * 5000.0;
                if (maxAxisVal <= 0) maxAxisVal = 5000.0;

                int gridSteps = 3;
                for (int i = 0; i <= gridSteps; i++) {
                    float frac = (float) i / gridSteps;
                    float y = plotBottom - frac * plotHeight;
                    canvas.drawLine(plotLeft, y, plotRight, y, gridPaint);

                    double tickVal = maxAxisVal * frac;
                    String tickText = (tickVal == 0)
                            ? "₱0"
                            : String.format(Locale.getDefault(), "₱%.0fk", tickVal / 1000.0);
                    canvas.drawText(tickText, 4f, y + 8f, axisLabelPaint);
                }

                int count = values.size();
                float stepX = (count == 1) ? 0f : plotWidth / (count - 1);
                float[] ptsX = new float[count];
                float[] ptsY = new float[count];

                float barHalfWidth = Math.min(14f, Math.max(6f, (plotWidth / Math.max(1, count)) * 0.14f));

                for (int i = 0; i < count; i++) {
                    float x = (count == 1) ? (plotLeft + plotWidth * 0.5f) : (plotLeft + i * stepX);
                    double saleVal = values.get(i)[0];
                    double expVal = values.get(i)[1];

                    float expH = (float) ((expVal / maxAxisVal) * plotHeight);
                    canvas.drawRect(
                            x - barHalfWidth,
                            plotBottom - expH,
                            x + barHalfWidth,
                            plotBottom,
                            expenseBarPaint
                    );

                    float y = plotBottom - (float) ((saleVal / maxAxisVal) * plotHeight);
                    ptsX[i] = x;
                    ptsY[i] = y;

                    if (count <= 8 || i % Math.max(1, count / 5) == 0 || i == count - 1) {
                        canvas.drawText(labels.get(i), x, h - 10f, xLabelPaint);
                    }
                }

                if (count >= 2) {
                    Path areaPath = new Path();
                    Path linePath = new Path();
                    areaPath.moveTo(ptsX[0], plotBottom);
                    areaPath.lineTo(ptsX[0], ptsY[0]);
                    linePath.moveTo(ptsX[0], ptsY[0]);

                    for (int i = 1; i < count; i++) {
                        areaPath.lineTo(ptsX[i], ptsY[i]);
                        linePath.lineTo(ptsX[i], ptsY[i]);
                    }
                    areaPath.lineTo(ptsX[count - 1], plotBottom);
                    areaPath.close();

                    canvas.drawPath(areaPath, areaPaint);
                    canvas.drawPath(linePath, linePaint);
                }

                for (int i = 0; i < count; i++) {
                    canvas.drawCircle(ptsX[i], ptsY[i], 6.5f, pointPaint);
                }
            }
        });
    }

    private void renderTableRows(Map<String, double[]> series) {
        if (salesTableHeaderRow == null || salesTableRowsContainer == null) return;

        salesTableHeaderRow.removeAllViews();
        salesTableRowsContainer.removeAllViews();

        String[] headers = {"Period", "Sales", "Expenses", "Net"};
        float[] weights = {1.3f, 1.0f, 1.0f, 1.0f};
        for (int i = 0; i < headers.length; i++) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[i]));
            tv.setText(headers[i]);
            tv.setTextColor(Color.parseColor("#1A1F17"));
            tv.setTextSize(12f);
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            if (i > 0) tv.setGravity(Gravity.END);
            salesTableHeaderRow.addView(tv);
        }

        for (Map.Entry<String, double[]> entry : series.entrySet()) {
            double s = entry.getValue()[0];
            double e = entry.getValue()[1];
            double net = s - e;

            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(36, 28, 36, 28);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView cPeriod = makeRowCell(entry.getKey(), weights[0], "#1A1F17", Gravity.START);
            TextView cSales = makeRowCell(pesoFormat.format(s), weights[1], "#16A34A", Gravity.END);
            TextView cExp = makeRowCell(pesoFormat.format(e), weights[2], "#C2410C", Gravity.END);
            TextView cNet = makeRowCell(pesoFormat.format(net), weights[3], "#1A1F17", Gravity.END);

            row.addView(cPeriod);
            row.addView(cSales);
            row.addView(cExp);
            row.addView(cNet);

            View divider = new View(context);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#EFECE6"));

            salesTableRowsContainer.addView(row);
            salesTableRowsContainer.addView(divider);
        }
    }

    private TextView makeRowCell(String text, float weight, String colorHex, int gravity) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(12f);
        tv.setGravity(gravity);
        return tv;
    }

    private void renderDonutChart(double leftVal, double spentVal) {
        if (salesDonutContainer == null) return;
        salesDonutContainer.removeAllViews();

        salesDonutContainer.addView(new View(context) {
            private final Paint leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint spentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF arcRect = new RectF();

            {
                leftPaint.setStyle(Paint.Style.STROKE);
                leftPaint.setStrokeWidth(26f);
                leftPaint.setColor(Color.parseColor("#16A34A"));

                spentPaint.setStyle(Paint.Style.STROKE);
                spentPaint.setStrokeWidth(26f);
                spentPaint.setColor(Color.parseColor("#C2410C"));

                emptyPaint.setStyle(Paint.Style.STROKE);
                emptyPaint.setStrokeWidth(26f);
                emptyPaint.setColor(Color.parseColor("#E5E0D5"));
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float pad = 16f;
                arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

                double total = leftVal + spentVal;
                if (total <= 0.0) {
                    canvas.drawArc(arcRect, 0f, 360f, false, emptyPaint);
                    return;
                }

                float leftSweep = (float) ((leftVal / total) * 360.0);
                float spentSweep = 360f - leftSweep;

                canvas.drawArc(arcRect, -90f, leftSweep, false, leftPaint);
                canvas.drawArc(arcRect, -90f + leftSweep, spentSweep, false, spentPaint);
            }
        });
    }

    private void updateFilterPillStyles() {
        Map<FilterRange, TextView> pills = new LinkedHashMap<FilterRange, TextView>();
        pills.put(FilterRange.TODAY, tvSalesFilterToday);
        pills.put(FilterRange.YESTERDAY, tvSalesFilterYesterday);
        pills.put(FilterRange.THIS_WEEK, tvSalesFilterWeek);
        pills.put(FilterRange.THIS_MONTH, tvSalesFilterMonth);
        pills.put(FilterRange.LAST_MONTH, tvSalesFilterLastMonth);
        pills.put(FilterRange.CUSTOM, tvSalesFilterCustom);

        for (Map.Entry<FilterRange, TextView> entry : pills.entrySet()) {
            TextView pill = entry.getValue();
            if (pill == null) continue;
            boolean active = entry.getKey() == currentFilter;
            styleRoundedView(pill, active ? "#16A34A" : "#FFFFFF", active ? "#16A34A" : "#D6CFC2", 999f);
            pill.setTextColor(Color.parseColor(active ? "#FFFFFF" : "#1A1F17"));
        }
    }

    private void updateSwitcherStyles() {
        boolean isChart = currentMode == DisplayMode.CHART;
        if (tvModeChart != null) {
            styleRoundedView(tvModeChart, isChart ? "#1A1F17" : "transparent", "transparent", 8f);
            tvModeChart.setTextColor(Color.parseColor(isChart ? "#EEE9DF" : "#1A1F17"));
        }
        if (tvModeTable != null) {
            styleRoundedView(tvModeTable, !isChart ? "#1A1F17" : "transparent", "transparent", 8f);
            tvModeTable.setTextColor(Color.parseColor(!isChart ? "#EEE9DF" : "#1A1F17"));
        }
    }

    private void styleRoundedView(View view, String bgHex, String strokeHex, float radiusPx) {
        if (view == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(radiusPx);
        gd.setColor("transparent".equalsIgnoreCase(bgHex) ? Color.TRANSPARENT : Color.parseColor(bgHex));
        if (!"transparent".equalsIgnoreCase(strokeHex)) {
            gd.setStroke(2, Color.parseColor(strokeHex));
        }
        view.setBackground(gd);
    }

    private long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long endOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }
}