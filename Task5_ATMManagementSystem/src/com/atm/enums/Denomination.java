package com.atm.enums;

/** Indian rupee ATM denominations. */
public enum Denomination {
    RS_2000(2000),
    RS_500(500),
    RS_200(200),
    RS_100(100);

    private final int value;
    Denomination(int v) { this.value = v; }
    public int getValue() { return value; }
}
