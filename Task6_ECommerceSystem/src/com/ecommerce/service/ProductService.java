package com.ecommerce.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.ProductException;
import com.ecommerce.model.Product;

public class ProductService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Set<String> usedSkus = ConcurrentHashMap.newKeySet();

    public Product add(String sku, String name, double price, int stock, String category) {
        if (!usedSkus.add(sku)) {
            throw new ProductException("SKU already exists: " + sku);
        }
        String id = "P" + seq.incrementAndGet();
        Product p = new Product(id, sku, name, price, stock, category);
        products.put(id, p);
        return p;
    }

    public Product get(String id) {
        Product p = products.get(id);
        if (p == null) throw new ProductException("Product not found: " + id);
        return p;
    }

    public Collection<Product> getAll() { return Collections.unmodifiableCollection(products.values()); }

    /** Only displayable products (excludes DISCONTINUED) - Rule P7. */
    public List<Product> getDisplayable() {
        List<Product> out = new ArrayList<>();
        for (Product p : products.values()) if (p.isDisplayable()) out.add(p);
        return out;
    }

    public void setStatus(String id, ProductStatus s) { get(id).updateStatus(s); }
    public void updatePrice(String id, double price)  { get(id).updatePrice(price); }
    public void updateName(String id, String name)    { get(id).updateName(name); }
}
