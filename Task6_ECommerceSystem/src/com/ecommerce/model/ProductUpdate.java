package com.ecommerce.model;

import java.time.LocalDateTime;

/** History entry for a product change (Rule Product 10). */
public class ProductUpdate {

    private final String field;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime timestamp;

    public ProductUpdate(String field, String oldValue, String newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }

    public String getField()    { return field; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return timestamp + " | " + field + " : " + oldValue + " -> " + newValue;
    }
}
