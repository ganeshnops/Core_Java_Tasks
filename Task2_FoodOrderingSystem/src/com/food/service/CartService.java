package com.food.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.food.enums.MenuItemStatus;
import com.food.exception.CartException;
import com.food.exception.InvalidCustomerException;
import com.food.exception.MenuItemUnavailableException;
import com.food.model.Cart;
import com.food.model.Customer;
import com.food.model.MenuItem;
import com.food.model.Restaurant;

/**
 * Cart logic.
 *  - One active cart per customer (Rule 11).
 *  - All cart items must belong to the same restaurant (Rule 12).
 *  - Out-of-stock / discontinued items rejected (Rules 8, 9).
 *  - Customer must not be blocked (Rule 54).
 *
 * The cart map is concurrent (different customers go in different shards) and
 * each cart mutation is guarded by a per-customer lock so two threads can't
 * race on the same cart.
 */
public class CartService {

    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final CustomerService customerService;
    private final RestaurantService restaurantService;

    public CartService(CustomerService customerService, RestaurantService restaurantService) {
        this.customerService = customerService;
        this.restaurantService = restaurantService;
    }

    public Cart getCart(String customerId) {
        return carts.get(customerId);
    }

    /** Add item to the customer's active cart, creating a cart if needed. */
    public void addToCart(String customerId, String restaurantId, String menuItemId, int qty) {
        Customer customer = customerService.get(customerId);
        if (customer == null) {
            throw new InvalidCustomerException("Customer not found: " + customerId);
        }
        if (customer.isBlocked()) {
            throw new InvalidCustomerException("Customer " + customerId + " is blocked.");
        }
        Restaurant r = restaurantService.get(restaurantId);
        if (r == null) {
            throw new CartException("Restaurant not found: " + restaurantId);
        }
        MenuItem item = r.getMenuItem(menuItemId);
        if (item == null) {
            throw new MenuItemUnavailableException("Menu item not found: " + menuItemId);
        }
        if (item.getStatus() == MenuItemStatus.DISCONTINUED) {
            throw new MenuItemUnavailableException("Item discontinued: " + menuItemId);
        }
        if (item.getStatus() == MenuItemStatus.OUT_OF_STOCK || item.getInventory() < qty) {
            throw new MenuItemUnavailableException("Item out of stock: " + menuItemId);
        }
        if (qty <= 0) {
            throw new CartException("Quantity must be positive.");
        }

        ReentrantLock lock = locks.computeIfAbsent(customerId, k -> new ReentrantLock());
        lock.lock();
        try {
            Cart cart = carts.computeIfAbsent(customerId, Cart::new);
            // Rule 12: items must be from same restaurant.
            if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(restaurantId)) {
                throw new CartException("Cart already has items from restaurant " + cart.getRestaurantId()
                        + ". Clear cart first to order from a different restaurant.");
            }
            if (cart.getRestaurantId() == null) {
                cart.setRestaurantId(restaurantId);
            }
            cart.addItem(menuItemId, qty, item.getPrice());
        } finally {
            lock.unlock();
        }
    }

    public void clearCart(String customerId) {
        Cart c = carts.get(customerId);
        if (c != null) c.clear();
    }

    public void removeCart(String customerId) {
        carts.remove(customerId);
    }
}
