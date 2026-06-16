package com.ecommerce.service;

import java.util.HashMap;
import java.util.Map;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;

/**
 * Two-phase inventory management (Inventory rules 2, 3, 4, 5).
 *  Phase 1: RESERVE during checkout. Atomic across multiple items.
 *  Phase 2: COMMIT on order confirmation OR RELEASE on payment failure / abandon.
 */
public class InventoryService {

    private final ProductService productService;

    public InventoryService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Try to reserve all items. If any fails, rolls back already-reserved items.
     * Returns the items that were successfully reserved (same as the request on success).
     */
    public Map<String, Integer> reserveAll(Map<String, Integer> items) {
        Map<String, Integer> reserved = new HashMap<>();
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            Product p = productService.get(e.getKey());
            if (!p.reserve(e.getValue())) {
                // rollback
                for (Map.Entry<String, Integer> done : reserved.entrySet()) {
                    productService.get(done.getKey()).release(done.getValue());
                }
                throw new InsufficientStockException("Cannot reserve " + e.getValue()
                        + " of " + p.getName() + " (available=" + p.getAvailable() + ")");
            }
            reserved.put(e.getKey(), e.getValue());
        }
        return reserved;
    }

    public void commitAll(Map<String, Integer> items) {
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            Product p = productService.get(e.getKey());
            p.commitReserved(e.getValue());
            if (p.isLowStock()) {
                System.out.println("  [inventory alert] LOW STOCK for "
                        + p.getName() + " (available=" + p.getAvailable() + ")");
            }
        }
    }

    public void releaseAll(Map<String, Integer> items) {
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            productService.get(e.getKey()).release(e.getValue());
        }
    }
}
