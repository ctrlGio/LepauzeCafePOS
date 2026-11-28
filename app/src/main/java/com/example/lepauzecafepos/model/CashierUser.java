package com.example.lepauzecafepos.model;

public class CashierUser {
    public final String id;
    public final String name;
    public final String role;
    public final String pin;

    public CashierUser(String id, String name, String role, String pin) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.pin = pin;
    }
}