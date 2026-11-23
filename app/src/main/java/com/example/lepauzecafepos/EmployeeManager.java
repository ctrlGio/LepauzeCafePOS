package com.example.lepauzecafepos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeManager {

    private static final int COLOR_EERIE_BLACK = Color.parseColor("#1A1F17");
    private static final int COLOR_MOCHA_LATTE = Color.parseColor("#817060");
    private static final int COLOR_OATMEAL = Color.parseColor("#C9C1B1");
    private static final int COLOR_PALLADIAN = Color.parseColor("#EEE9DF");
    private static final int COLOR_EMERALD = Color.parseColor("#16A34A");
    private static final int COLOR_SOFT_GREEN = Color.parseColor("#DCFCE7");
    private static final int COLOR_TEXT_DARK = Color.parseColor("#333333");
    private static final int COLOR_ROW_ALT = Color.parseColor("#F9F7F2");

    private final Context context;
    private final LinearLayout employeeItemsContainer;
    private final LinearLayout employeeHeaderRow;
    private final Button btnInviteEmployee;
    private final TextView tvEmpTabTeam, tvEmpTabHours;
    private final FrameLayout iconEmpOnDuty, iconEmpHoursToday, iconEmpEndedShifts;
    private final TextView tvEmpOnDutyCount, tvEmpHoursToday, tvEmpEndedCount;
    private final TextView tvEmpSectionTitle, tvEmpSectionCount;

    private final List<EmployeeShiftRecord> employees = new ArrayList<>();
    private boolean isHoursTabSelected = false;

    public static class EmployeeShiftRecord {
        String name;
        String role;
        String startTime;
        String endTime;
        String hoursToday;
        String hoursThisWeek;

        public EmployeeShiftRecord(
                String name,
                String role,
                String startTime,
                String endTime,
                String hoursToday,
                String hoursThisWeek
        ) {
            this.name = name;
            this.role = role;
            this.startTime = startTime;
            this.endTime = endTime;
            this.hoursToday = hoursToday;
            this.hoursThisWeek = hoursThisWeek;
        }

        public boolean isOnDuty() {
            return endTime == null || endTime.trim().isEmpty();
        }
    }

    public EmployeeManager(Context context, View rootView) {
        this.context = context;

        employeeItemsContainer = rootView.findViewById(R.id.employeeItemsContainer);
        employeeHeaderRow = rootView.findViewById(R.id.employeeHeaderRow);
        btnInviteEmployee = rootView.findViewById(R.id.btnInviteEmployee);
        tvEmpTabTeam = rootView.findViewById(R.id.tvEmpTabTeam);
        tvEmpTabHours = rootView.findViewById(R.id.tvEmpTabHours);

        iconEmpOnDuty = rootView.findViewById(R.id.iconEmpOnDuty);
        iconEmpHoursToday = rootView.findViewById(R.id.iconEmpHoursToday);
        iconEmpEndedShifts = rootView.findViewById(R.id.iconEmpEndedShifts);

        tvEmpOnDutyCount = rootView.findViewById(R.id.tvEmpOnDutyCount);
        tvEmpHoursToday = rootView.findViewById(R.id.tvEmpHoursToday);
        tvEmpEndedCount = rootView.findViewById(R.id.tvEmpEndedCount);
        tvEmpSectionTitle = rootView.findViewById(R.id.tvEmpSectionTitle);
        tvEmpSectionCount = rootView.findViewById(R.id.tvEmpSectionCount);

        if (btnInviteEmployee != null) {
            btnInviteEmployee.setOnClickListener(null);
        }

        if (tvEmpTabTeam != null) {
            tvEmpTabTeam.setOnClickListener(v -> selectTab(false));
        }

        if (tvEmpTabHours != null) {
            tvEmpTabHours.setOnClickListener(v -> selectTab(true));
        }

        setupSummaryCardIcons();
        seedInitialEmployees();
        selectTab(false);
    }

    private void setupSummaryCardIcons() {
        styleBadgeIcon(iconEmpOnDuty, COLOR_SOFT_GREEN, "●", COLOR_EMERALD);
        styleBadgeIcon(iconEmpHoursToday, COLOR_PALLADIAN, "⏱", COLOR_EERIE_BLACK);
        styleBadgeIcon(iconEmpEndedShifts, COLOR_PALLADIAN, "✓", COLOR_EERIE_BLACK);
    }

    private void styleBadgeIcon(FrameLayout container, int bgColor, String glyph, int glyphColor) {
        if (container == null) return;
        container.removeAllViews();

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(bgColor);
        container.setBackground(bg);

        TextView label = new TextView(context);
        label.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        label.setGravity(Gravity.CENTER);
        label.setText(glyph);
        label.setTextColor(glyphColor);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setTypeface(null, Typeface.BOLD);
        container.addView(label);
    }

    private void seedInitialEmployees() {
        employees.add(new EmployeeShiftRecord(
                "Ryan Regalario",
                "Barista",
                "6:00 AM",
                null,
                "9h 44m",
                "42h 15m"
        ));
        employees.add(new EmployeeShiftRecord(
                "Gian Tuzon",
                "Barista",
                "8:00 AM",
                "2:00 PM",
                "6h 00m",
                "31h 30m"
        ));
        employees.add(new EmployeeShiftRecord(
                "Aron John",
                "Barista",
                "1:00 PM",
                "7:00 PM",
                "6h 00m",
                "24h 10m"
        ));
    }

    private void selectTab(boolean hoursTab) {
        isHoursTabSelected = hoursTab;
        updateTabStyles();
        updateSummaryCards();
        refreshTable();
    }

    private void updateTabStyles() {
        if (tvEmpTabTeam != null) {
            GradientDrawable activeBg = new GradientDrawable();
            activeBg.setShape(GradientDrawable.RECTANGLE);
            activeBg.setCornerRadius(dpToPx(6));
            activeBg.setColor(!isHoursTabSelected ? COLOR_EERIE_BLACK : Color.TRANSPARENT);
            tvEmpTabTeam.setBackground(activeBg);
            tvEmpTabTeam.setTextColor(!isHoursTabSelected ? COLOR_PALLADIAN : COLOR_EERIE_BLACK);
        }

        if (tvEmpTabHours != null) {
            GradientDrawable hoursBg = new GradientDrawable();
            hoursBg.setShape(GradientDrawable.RECTANGLE);
            hoursBg.setCornerRadius(dpToPx(6));
            hoursBg.setColor(isHoursTabSelected ? COLOR_EERIE_BLACK : Color.TRANSPARENT);
            tvEmpTabHours.setBackground(hoursBg);
            tvEmpTabHours.setTextColor(isHoursTabSelected ? COLOR_PALLADIAN : COLOR_EERIE_BLACK);
        }

        if (tvEmpSectionTitle != null) {
            tvEmpSectionTitle.setText(isHoursTabSelected ? "Shift Hours & Logs" : "Team");
        }
        if (tvEmpSectionCount != null) {
            int count = employees.size();
            tvEmpSectionCount.setText(String.format(Locale.US, "%d %s", count, count == 1 ? "person" : "people"));
        }
    }

    private void updateSummaryCards() {
        int onDutyCount = 0;
        int endedCount = 0;
        int totalMinutesToday = 0;

        for (EmployeeShiftRecord emp : employees) {
            if (emp.isOnDuty()) {
                onDutyCount++;
            } else {
                endedCount++;
            }
            totalMinutesToday += parseHoursToMinutes(emp.hoursToday);
        }

        if (tvEmpOnDutyCount != null) {
            tvEmpOnDutyCount.setText(String.valueOf(onDutyCount));
        }
        if (tvEmpEndedCount != null) {
            tvEmpEndedCount.setText(String.valueOf(endedCount));
        }
        if (tvEmpHoursToday != null) {
            int hrs = totalMinutesToday / 60;
            int mins = totalMinutesToday % 60;
            tvEmpHoursToday.setText(String.format(Locale.US, "%dh %dm", hrs, mins));
        }
    }

    private int parseHoursToMinutes(String duration) {
        if (duration == null) return 0;
        int hours = 0;
        int minutes = 0;
        String[] parts = duration.trim().split("\\s+");
        for (String part : parts) {
            try {
                if (part.endsWith("h")) {
                    hours = Integer.parseInt(part.replace("h", ""));
                } else if (part.endsWith("m")) {
                    minutes = Integer.parseInt(part.replace("m", ""));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return (hours * 60) + minutes;
    }

    private void refreshTable() {
        if (employeeHeaderRow == null || employeeItemsContainer == null) return;

        employeeHeaderRow.removeAllViews();
        employeeItemsContainer.removeAllViews();

        if (isHoursTabSelected) {
            employeeHeaderRow.addView(createHeaderCell("Employee", 1.5f));
            employeeHeaderRow.addView(createHeaderCell("Start Time", 1.1f));
            employeeHeaderRow.addView(createHeaderCell("End Time", 1.2f));
            employeeHeaderRow.addView(createHeaderCell("Today", 1.0f));
            employeeHeaderRow.addView(createHeaderCell("This Week", 1.0f));
        } else {
            employeeHeaderRow.addView(createHeaderCell("Employee", 1.6f));
            employeeHeaderRow.addView(createHeaderCell("Role", 1.2f));
            employeeHeaderRow.addView(createHeaderCell("Started", 1.1f));
            employeeHeaderRow.addView(createHeaderCell("Ended / Status", 1.4f));
            employeeHeaderRow.addView(createHeaderCell("Today", 1.0f));
        }

        int index = 0;
        for (EmployeeShiftRecord emp : employees) {
            employeeItemsContainer.addView(createEmployeeRow(emp, index++));
        }
    }

    private LinearLayout createEmployeeRow(EmployeeShiftRecord emp, int position) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56)
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        row.setBackgroundColor(position % 2 == 1 ? COLOR_ROW_ALT : Color.WHITE);

        if (isHoursTabSelected) {
            row.addView(createEmployeeIdentityCell(emp.name, emp.role, 1.5f));
            row.addView(createBodyCell(emp.startTime, 1.1f, COLOR_TEXT_DARK, false));
            row.addView(createEndOrOnDutyCell(emp, 1.2f));
            row.addView(createBodyCell(emp.hoursToday, 1.0f, COLOR_EERIE_BLACK, true));
            row.addView(createBodyCell(emp.hoursThisWeek, 1.0f, COLOR_MOCHA_LATTE, false));
        } else {
            row.addView(createEmployeeIdentityCell(emp.name, null, 1.6f));
            row.addView(createRoleBadgeCell(emp.role, 1.2f));
            row.addView(createBodyCell(emp.startTime, 1.1f, COLOR_TEXT_DARK, false));
            row.addView(createEndOrOnDutyCell(emp, 1.4f));
            row.addView(createBodyCell(emp.hoursToday, 1.0f, COLOR_EERIE_BLACK, true));
        }

        return row;
    }

    private LinearLayout createEmployeeIdentityCell(String name, String subtitle, float weight) {
        LinearLayout cell = new LinearLayout(context);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textStack = new LinearLayout(context);
        textStack.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(context);
        tvName.setText(name);
        tvName.setTextColor(COLOR_EERIE_BLACK);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvName.setTypeface(null, Typeface.BOLD);
        textStack.addView(tvName);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(context);
            tvSub.setText(subtitle);
            tvSub.setTextColor(COLOR_MOCHA_LATTE);
            tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            textStack.addView(tvSub);
        }

        cell.addView(textStack);
        return cell;
    }

    private LinearLayout createRoleBadgeCell(String role, float weight) {
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        wrapper.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(context);
        badge.setText(role);
        badge.setTextColor(COLOR_EERIE_BLACK);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setCornerRadius(dpToPx(12));
        pill.setColor(COLOR_PALLADIAN);
        badge.setBackground(pill);

        wrapper.addView(badge);
        return wrapper;
    }

    private LinearLayout createEndOrOnDutyCell(EmployeeShiftRecord emp, float weight) {
        LinearLayout cell = new LinearLayout(context);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        if (emp.isOnDuty()) {
            View dot = new View(context);
            int dotSize = dpToPx(8);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotParams.setMarginEnd(dpToPx(6));
            dot.setLayoutParams(dotParams);

            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);
            dotDrawable.setColor(COLOR_EMERALD);
            dot.setBackground(dotDrawable);

            TextView tvOnDuty = new TextView(context);
            tvOnDuty.setText("On Duty");
            tvOnDuty.setTextColor(COLOR_EMERALD);
            tvOnDuty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvOnDuty.setTypeface(null, Typeface.BOLD);

            cell.addView(dot);
            cell.addView(tvOnDuty);
        } else {
            TextView tvEnded = new TextView(context);
            tvEnded.setText(emp.endTime);
            tvEnded.setTextColor(COLOR_MOCHA_LATTE);
            tvEnded.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            cell.addView(tvEnded);
        }

        return cell;
    }

    private TextView createHeaderCell(String title, float weight) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(title);
        tv.setTextColor(COLOR_EERIE_BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private TextView createBodyCell(String text, float weight, int color, boolean bold) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }
        return tv;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}