package com.ecommerce;

import java.util.Map;
import java.util.Scanner;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.ECommerceException;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.ProductUpdate;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final ProductService productService = new ProductService();
    private static final CustomerService customerService = new CustomerService();
    private static final CartService cartService = new CartService(customerService, productService);
    private static final InventoryService inventoryService = new InventoryService(productService);
    private static final OrderService orderService = new OrderService(customerService, cartService, inventoryService);

    public static void main(String[] args) {
        printBanner();
        bootstrap();
        runHappyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int c = readInt("Choose: ");
            System.out.println();
            try {
                switch (c) {
                    case 1: listProducts();        break;
                    case 2: viewProductHistory();  break;
                    case 3: addToCart();           break;
                    case 4: viewCart();            break;
                    case 5: checkout();            break;
                    case 6: advanceOrder();        break;
                    case 7: listOrders();          break;
                    case 8: updateProductPrice();  break;
                    case 9: discontinueProduct();  break;
                    case 10: blockCustomer();      break;
                    case 11: run = false;          break;
                    default: System.out.println("Invalid.");
                }
            } catch (ECommerceException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void bootstrap() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Bootstrap demo data");
        System.out.println("=========================================================");

        productService.add("SKU-001", "iPhone 15",         85000, 20, "Electronics");
        productService.add("SKU-002", "Samsung TV 55",     65000, 15, "Electronics");
        productService.add("SKU-003", "Nike Shoes",         5500, 50, "Fashion");
        productService.add("SKU-004", "Old Stock Item",     1500,  5, "Misc");
        productService.add("SKU-005", "Discontinued Toy",    500,  3, "Toys");
        productService.setStatus("P1005", ProductStatus.DISCONTINUED);

        Customer alice = customerService.register("Alice",   "alice@test.com",   "9000000001");
        Customer bob   = customerService.register("Bob",     "bob@test.com",     "9000000002");
        Customer carol = customerService.register("Carol",   "carol@test.com",   "9000000003");

        customerService.verify("C1001");
        customerService.verify("C1002");
        // Carol UNVERIFIED (for demo)

        customerService.addAddress("C1001", "Flat 12, MG Road",     "Bangalore",  "560001", true);
        customerService.addAddress("C1001", "Office Park, ITPL",    "Bangalore",  "560066", false);
        customerService.addAddress("C1002", "House 5, Park Street", "Hyderabad",  "500001", true);

        System.out.println("  5 products (1 discontinued), 3 customers (1 unverified), addresses set");
        System.out.println();
    }

    private static void runHappyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path checkout for Alice");
        System.out.println("=========================================================");
        try {
            cartService.addToCart("C1001", "P1001", 1);     // iPhone
            cartService.addToCart("C1001", "P1003", 2);     // Nike shoes x 2
            System.out.println("  Cart: " + cartService.getCart("C1001"));
            try {
                Order o = orderService.checkout("C1001", "demo-key-1");
                System.out.println("  Order placed: " + o);
                orderService.advance(o.getId(), OrderStatus.SHIPPED);
                orderService.advance(o.getId(), OrderStatus.DELIVERED);
                System.out.println("  Order delivered: " + o);
            } catch (ECommerceException ex) {
                System.out.println("  Payment failed (random): " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#                JHires E-Commerce System                #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. List products (only displayable)");
        System.out.println(" 2. View product update history");
        System.out.println(" 3. Add to cart");
        System.out.println(" 4. View cart");
        System.out.println(" 5. Checkout (reserves inventory, simulates payment)");
        System.out.println(" 6. Advance order status");
        System.out.println(" 7. List orders");
        System.out.println(" 8. Update product price");
        System.out.println(" 9. Discontinue product");
        System.out.println("10. Block customer");
        System.out.println("11. Exit");
        System.out.println("==============================================");
    }

    private static void listProducts() {
        for (Product p : productService.getDisplayable()) System.out.println(p);
    }

    private static void viewProductHistory() {
        String pid = readString("Product ID : ");
        Product p = productService.get(pid);
        System.out.println("History of " + p.getName() + ":");
        for (ProductUpdate u : p.getHistory()) System.out.println("  " + u);
    }

    private static void addToCart() {
        String cust = readString("Customer ID : ");
        String pid  = readString("Product ID  : ");
        int qty = readInt("Quantity    : ");
        cartService.addToCart(cust, pid, qty);
        System.out.println("Added.");
    }

    private static void viewCart() {
        String cust = readString("Customer ID : ");
        var cart = cartService.getCart(cust);
        if (cart == null || cart.isEmpty()) { System.out.println("Cart empty."); return; }
        System.out.println(cart);
        for (Map.Entry<String, Integer> e : cart.getItems().entrySet()) {
            System.out.println("  " + e.getKey() + " x " + e.getValue());
        }
    }

    private static void checkout() {
        String cust = readString("Customer ID : ");
        Order o = orderService.checkout(cust, "manual-" + System.nanoTime());
        System.out.println("Order placed: " + o);
    }

    private static void advanceOrder() {
        String oid = readString("Order ID : ");
        System.out.println("Next: 1.CONFIRMED 2.SHIPPED 3.DELIVERED 4.CANCELLED");
        int n = readInt("Choose : ");
        OrderStatus next = n == 1 ? OrderStatus.CONFIRMED
                : n == 2 ? OrderStatus.SHIPPED
                : n == 3 ? OrderStatus.DELIVERED
                : OrderStatus.CANCELLED;
        orderService.advance(oid, next);
        System.out.println("Status updated.");
    }

    private static void listOrders() {
        for (Order o : orderService.getAll()) System.out.println(o);
    }

    private static void updateProductPrice() {
        String pid = readString("Product ID : ");
        double price = Double.parseDouble(readString("New price : "));
        productService.updatePrice(pid, price);
        System.out.println("Updated.");
    }

    private static void discontinueProduct() {
        String pid = readString("Product ID : ");
        productService.setStatus(pid, ProductStatus.DISCONTINUED);
        System.out.println("Discontinued.");
    }

    private static void blockCustomer() {
        String cust = readString("Customer ID : ");
        customerService.block(cust);
        System.out.println("Blocked.");
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Invalid number."); }
        }
    }
}
