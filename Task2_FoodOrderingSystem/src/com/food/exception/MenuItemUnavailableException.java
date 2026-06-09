package com.food.exception;

/** Rules 7, 8, 9, 10 - menu item not available / out-of-stock / discontinued / inventory negative. */
public class MenuItemUnavailableException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public MenuItemUnavailableException(String message) { super(message); }
}
