package com.ecommerce.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.ProductException;

/**
 * Product entity.
 *  - Rule P1: unique product ID
 *  - Rule P2: name not empty
 *  - Rule P3: price > 0
 *  - Rule P4: stock >= 0 (AtomicInteger)
 *  - Rule P5: category mandatory
 *  - Rule P6: only ACTIVE can be purchased
 *  - Rule P7: DISCONTINUED hidden from sale
 *  - Rule P8: unique SKU
 *  - Rule P9: inventory updated after every order
 *  - Rule P10: update history maintained
 *
 *  stock = available (uncommitted) inventory.
 *  reserved = stock reserved during pending checkouts but not committed.
 *  available = stock - reserved.
 *
 *  Two-phase inventory pattern (Inventory rules 2, 3, 4).
 */
public class Product {

    public static final int LOW_STOCK_THRESHOLD = 5;

    private final String id;
    private final String sku;
    private volatile String name;
    private volatile double price;
    private final AtomicInteger stock;
    private final AtomicInteger reserved = new AtomicInteger(0);
    private volatile String category;
    private volatile ProductStatus status;
    private final List<ProductUpdate> history = new CopyOnWriteArrayList<>();

    public Product(String id, String sku, String name, double price, int stock, String category) {
        if (name == null || name.isBlank()) throw new ProductException("Product name cannot be empty.");
        if (price <= 0)                     throw new ProductException("Price must be > 0.");
        if (stock < 0)                      throw new ProductException("Stock cannot be negative.");
        if (category == null || category.isBlank()) throw new ProductException("Category mandatory.");
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stock = new AtomicInteger(stock);
        this.category = category;
        this.status = ProductStatus.ACTIVE;
    }

    public String getId()             { return id; }
    public String getSku()            { return sku; }
    public String getName()           { return name; }
    public double getPrice()          { return price; }
    public int getStock()             { return stock.get(); }
    public int getReserved()          { return reserved.get(); }
    public int getAvailable()         { return stock.get() - reserved.get(); }
    public String getCategory()       { return category; }
    public ProductStatus getStatus()  { return status; }
    public List<ProductUpdate> getHistory() { return Collections.unmodifiableList(history); }

    public boolean isPurchasable()    { return status == ProductStatus.ACTIVE; }
    public boolean isDisplayable()    { return status != ProductStatus.DISCONTINUED; }
    public boolean isLowStock()       { return getAvailable() <= LOW_STOCK_THRESHOLD; }

    public void updateName(String n) {
        history.add(new ProductUpdate("name", this.name, n));
        this.name = n;
    }
    public void updatePrice(double p) {
        if (p <= 0) throw new ProductException("Price must be > 0.");
        history.add(new ProductUpdate("price", String.valueOf(this.price), String.valueOf(p)));
        this.price = p;
    }
    public void updateStatus(ProductStatus s) {
        history.add(new ProductUpdate("status", this.status.toString(), s.toString()));
        this.status = s;
    }

    /** Reserve qty stock (CAS loop). Returns true on success. */
    public boolean reserve(int qty) {
        if (qty <= 0) return false;
        while (true) {
            int currentReserved = reserved.get();
            int available = stock.get() - currentReserved;
            if (available < qty) return false;
            if (reserved.compareAndSet(currentReserved, currentReserved + qty)) return true;
        }
    }

    /** Release previously reserved stock (called when payment fails / cart cleared). */
    public void release(int qty) {
        if (qty <= 0) return;
        reserved.addAndGet(-qty);
        if (reserved.get() < 0) reserved.set(0);
    }

    /** Commit reserved stock - reduces actual stock + reserved. (Called on order confirm.) */
    public void commitReserved(int qty) {
        if (qty <= 0) return;
        stock.addAndGet(-qty);
        reserved.addAndGet(-qty);
    }

    public void restock(int qty) {
        if (qty > 0) stock.addAndGet(qty);
    }

    @Override
    public String toString() {
        return String.format("%s | SKU=%s | %s | Rs.%.2f | stock=%d (reserved=%d, available=%d) | %s | %s",
                id, sku, name, price, stock.get(), reserved.get(), getAvailable(), category, status);
    }
}
