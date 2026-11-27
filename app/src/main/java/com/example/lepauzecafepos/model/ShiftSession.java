package com.example.lepauzecafepos.model;

public class ShiftSession {
    public String cashierName;
    public String cashierRole;
    public boolean isActive;
    public long startedAtMillis;
    public double openingCash;
    public double currentSales;
    public int transactionCount;
    public double cashSales;
    public double cashIn;
    public double cashOut;
    public double expenses;

    public ShiftSession(String cashierName, String cashierRole, boolean isActive, long startedAtMillis, double openingCash) {
        this.cashierName = cashierName;
        this.cashierRole = cashierRole;
        this.isActive = isActive;
        this.startedAtMillis = startedAtMillis;
        this.openingCash = openingCash;
    }

    public double getExpectedCashInDrawer() {
        return openingCash + cashSales + cashIn - cashOut - expenses;
    }
}
