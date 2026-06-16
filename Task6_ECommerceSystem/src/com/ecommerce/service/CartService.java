package com.ecommerce.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.ecommerce.exception.CartException;
import com.ecommerce.exception.CustomerException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Product;

public class CartService {

    /** ONE active cart per customer (Rule Cart 1). */
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final CustomerService customerService;
    private final ProductService productService;

    public CartService(CustomerService customerService, ProductService productService) {
        this.customerService = customerService;
        this.productService = productService;
    }

    public Cart getCart(String customerId) {
        return carts.get(customerId);
    }

    public void addToCart(String customerId, String productId, int qty) {
        Customer c = customerService.get(customerId);
        if (!c.canOrder()) {
            throw new CustomerException("Customer " + customerId + " cannot order (status=" + c.getStatus() + " or no address)");
        }
        Product p = productService.get(productId);
        if (!p.isPurchasable()) {
            throw new ProductException("Product " + productId + " not purchasable (status=" + p.getStatus() + ")");
        }
        if (p.getAvailable() < qty) {
            throw new InsufficientStockException("Product " + productId + " only has "
                    + p.getAvailable() + " available.");
        }
        if (qty <= 0) throw new CartException("Quantity must be positive.");

        ReentrantLock lock = locks.computeIfAbsent(customerId, k -> new ReentrantLock());
        lock.lock();
        try {
            Cart cart = carts.computeIfAbsent(customerId, Cart::new);
            int existing = cart.getItems().getOrDefault(productId, 0);
            if (existing + qty > p.getAvailable()) {
                throw new InsufficientStockException("Adding " + qty + " would exceed available stock ("
                        + p.getAvailable() + ").");
            }
            cart.addOrIncrease(productId, qty, p.getPrice());
        } finally { lock.unlock(); }
    }

    public void clearCart(String customerId) {
        Cart c = carts.get(customerId);
        if (c != null) c.clear();
    }

    public void removeCart(String customerId) { carts.remove(customerId); }
}
