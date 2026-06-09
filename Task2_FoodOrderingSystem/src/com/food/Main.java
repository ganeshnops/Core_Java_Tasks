package com.food;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.food.enums.MenuItemStatus;
import com.food.enums.OrderStatus;
import com.food.enums.PaymentMode;
import com.food.exception.FoodOrderException;
import com.food.model.Coupon;
import com.food.model.Customer;
import com.food.model.DeliveryPartner;
import com.food.model.MenuItem;
import com.food.model.Order;
import com.food.model.Restaurant;
import com.food.service.CartService;
import com.food.service.CouponService;
import com.food.service.CustomerService;
import com.food.service.DeliveryService;
import com.food.service.NotificationService;
import com.food.service.OrderService;
import com.food.service.PaymentService;
import com.food.service.RestaurantService;
import com.food.service.ReviewService;

/**
 * Food Ordering System - Main entry point.
 *
 * PHASE 1 - bootstrap demo data + automatic happy-path order.
 * PHASE 2 - interactive console menu so the user can play with the system.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    // services
    private static final RestaurantService    rs = new RestaurantService();
    private static final CustomerService      cs = new CustomerService();
    private static final CartService          cts = new CartService(cs, rs);
    private static final CouponService        cps = new CouponService();
    private static final PaymentService       ps  = new PaymentService();
    private static final DeliveryService      ds  = new DeliveryService();
    private static final NotificationService  ns  = new NotificationService();
    private static final OrderService         os  = new OrderService(rs, cs, cts, cps, ps, ds, ns);
    private static final ReviewService        rvs = new ReviewService(rs, ds);

    public static void main(String[] args) {
        printBanner();
        bootstrapDemoData();
        runHappyPathDemo();

        boolean run = true;
        while (run) {
            printMenu();
            int choice = readInt("Choose option: ");
            System.out.println();
            try {
                switch (choice) {
                    case 1: listRestaurants();        break;
                    case 2: addToCart();              break;
                    case 3: viewCart();               break;
                    case 4: placeOrder();             break;
                    case 5: advanceOrder();           break;
                    case 6: cancelOrder();            break;
                    case 7: listOrders();             break;
                    case 8: showReports();            break;
                    case 9: stressTest();             break;
                    case 10: submitReview();          break;
                    case 11: listCustomers();         break;
                    case 12: run = false;             break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (FoodOrderException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED ERROR: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    // ====================== Bootstrap demo data ======================
    private static void bootstrapDemoData() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Demo data bootstrap");
        System.out.println("=========================================================");

        // 2 restaurants
        Restaurant biryaniHouse = rs.addRestaurant(new Restaurant("R1", "Biryani House", 5));
        Restaurant pizzaCorner  = rs.addRestaurant(new Restaurant("R2", "Pizza Corner", 5));

        // menu for R1
        rs.addMenuItem("R1", new MenuItem("M11", "Chicken Biryani", 250, "R1", 50, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R1", new MenuItem("M12", "Mutton Biryani",  350, "R1", 30, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R1", new MenuItem("M13", "Veg Biryani",     180, "R1", 40, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R1", new MenuItem("M14", "Old Soda",         50, "R1",  0, MenuItemStatus.OUT_OF_STOCK));
        rs.addMenuItem("R1", new MenuItem("M15", "Fried Rice",      150, "R1", 25, MenuItemStatus.DISCONTINUED));

        // menu for R2
        rs.addMenuItem("R2", new MenuItem("M21", "Margherita Pizza", 200, "R2", 40, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R2", new MenuItem("M22", "Veggie Supreme",   320, "R2", 25, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R2", new MenuItem("M23", "Pasta",            180, "R2", 30, MenuItemStatus.AVAILABLE));
        rs.addMenuItem("R2", new MenuItem("M24", "Garlic Bread",      90, "R2", 60, MenuItemStatus.AVAILABLE));

        // 3 customers
        cs.add(new Customer("C1", "Alice",   "9000000001", true,  "Flat 12, MG Road",   3000));
        cs.add(new Customer("C2", "Bob",     "9000000002", true,  "House 5, Park St",   1500));
        cs.add(new Customer("C3", "Charlie", "9000000003", false, "Apt 8, Lake View",   2000));   // mobile NOT verified

        // 3 delivery partners (different distances)
        ds.add(new DeliveryPartner("DP1", "Ravi",    1.5, 3));
        ds.add(new DeliveryPartner("DP2", "Suresh",  3.0, 3));
        ds.add(new DeliveryPartner("DP3", "Manish",  5.0, 3));

        // coupons
        cps.add(new Coupon("WELCOME10", 10, 200, LocalDate.now().plusDays(30)));
        cps.add(new Coupon("FIFTY",     50, 500, LocalDate.now().plusDays(7)));
        cps.add(new Coupon("EXPIRED",   20, 100, LocalDate.now().minusDays(1)));   // expired

        System.out.println("  2 restaurants, 9 menu items, 3 customers, 3 delivery partners, 3 coupons created.");
        System.out.println();
    }

    // ====================== Happy-path automatic demo ======================
    private static void runHappyPathDemo() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path automatic order for Alice (C1)");
        System.out.println("=========================================================");
        // Alice orders 2 Chicken Biryani + 1 Veg Biryani from R1 with WELCOME10 coupon
        cts.addToCart("C1", "R1", "M11", 2);
        cts.addToCart("C1", "R1", "M13", 1);
        Order o = os.placeOrder("C1", PaymentMode.PREPAID, "WELCOME10", "demo-key-001");
        System.out.println("Order placed: " + o);

        try {
            os.advanceStatus(o.getId(), OrderStatus.PREPARING);
            os.advanceStatus(o.getId(), OrderStatus.READY);            // assigns rider
            os.advanceStatus(o.getId(), OrderStatus.OUT_FOR_DELIVERY);
            os.advanceStatus(o.getId(), OrderStatus.DELIVERED);
            System.out.println("Order flow completed: " + o);
            rvs.addReview(o, 5, 4, "Tasty biryani, quick delivery");
            System.out.println("Review submitted for R1.");
        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    // ====================== Menu ======================
    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#               JHires Food Ordering System              #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. List restaurants and menus");
        System.out.println(" 2. Add item to cart");
        System.out.println(" 3. View cart");
        System.out.println(" 4. Place order");
        System.out.println(" 5. Advance order status (PREPARING -> READY -> ...)");
        System.out.println(" 6. Cancel order");
        System.out.println(" 7. List all orders");
        System.out.println(" 8. Show reports (revenue, top items, commission)");
        System.out.println(" 9. Stress test (concurrent orders)");
        System.out.println("10. Submit review for a delivered order");
        System.out.println("11. List customers");
        System.out.println("12. Exit");
        System.out.println("==============================================");
    }

    // ====================== Menu actions ======================
    private static void listRestaurants() {
        for (Restaurant r : rs.getAll()) {
            System.out.println(r);
            for (MenuItem m : r.getMenu().values()) {
                System.out.println("    " + m);
            }
        }
    }

    private static void addToCart() {
        String customerId = readString("Customer ID    : ");
        String restaurantId = readString("Restaurant ID  : ");
        String menuItemId = readString("Menu Item ID   : ");
        int qty = readInt("Quantity       : ");
        cts.addToCart(customerId, restaurantId, menuItemId, qty);
        System.out.println("OK. Added to cart.");
    }

    private static void viewCart() {
        String customerId = readString("Customer ID : ");
        com.food.model.Cart cart = cts.getCart(customerId);
        if (cart == null || cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        System.out.println(cart);
        for (Map.Entry<String, Integer> e : cart.getItems().entrySet()) {
            System.out.println("  " + e.getKey() + " x " + e.getValue());
        }
    }

    private static void placeOrder() {
        String customerId = readString("Customer ID            : ");
        System.out.println("Payment mode: 1.PREPAID  2.COD");
        int pm = readInt("Choose                : ");
        PaymentMode mode = (pm == 2) ? PaymentMode.COD : PaymentMode.PREPAID;
        String coupon = readString("Coupon code (blank=none): ");
        String idem   = readString("Idempotency key (blank=auto): ");
        if (idem.isBlank()) idem = "manual-" + System.nanoTime();
        Order o = os.placeOrder(customerId, mode, coupon.isBlank() ? null : coupon, idem);
        System.out.println("Order placed: " + o);
    }

    private static void advanceOrder() {
        String orderId = readString("Order ID    : ");
        System.out.println("Next status: 1.PREPARING  2.READY  3.OUT_FOR_DELIVERY  4.DELIVERED");
        int s = readInt("Choose      : ");
        OrderStatus next;
        switch (s) {
            case 1: next = OrderStatus.PREPARING;        break;
            case 2: next = OrderStatus.READY;            break;
            case 3: next = OrderStatus.OUT_FOR_DELIVERY; break;
            case 4: next = OrderStatus.DELIVERED;        break;
            default: System.out.println("Invalid."); return;
        }
        os.advanceStatus(orderId, next);
        System.out.println("OK. Status updated.");
    }

    private static void cancelOrder() {
        String orderId = readString("Order ID : ");
        os.cancelOrder(orderId);
        System.out.println("OK. Order cancelled.");
    }

    private static void listOrders() {
        for (Order o : os.getAllOrders()) {
            System.out.println("  " + o);
        }
    }

    private static void showReports() {
        System.out.println("--- Daily Revenue ---");
        for (Map.Entry<String, Double> e : os.getDailyRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Top selling items (top 5) ---");
        for (Map.Entry<String, Integer> e : os.topSellingItems(5)) {
            System.out.println("  " + e.getKey() + " -> " + e.getValue() + " units");
        }
        System.out.println("--- Platform commission total ---");
        System.out.println("  Rs." + os.getPlatformCommissionTotal());
        if (!os.getAuditLog().isEmpty()) {
            System.out.println("--- Recent audit entries (last 5) ---");
            List<String> log = os.getAuditLog();
            int from = Math.max(0, log.size() - 5);
            for (int i = from; i < log.size(); i++) System.out.println("  " + log.get(i));
        }
    }

    private static void submitReview() {
        String orderId = readString("Order ID            : ");
        int rRating = readInt("Restaurant rating 1-5 : ");
        int dRating = readInt("Delivery rating 1-5   : ");
        String comment = readString("Comment             : ");
        Order o = os.getOrder(orderId);
        if (o == null) {
            System.out.println("Order not found.");
            return;
        }
        rvs.addReview(o, rRating, dRating, comment);
        System.out.println("OK. Review submitted.");
    }

    private static void listCustomers() {
        for (Customer c : cs.getAll()) System.out.println("  " + c);
    }

    private static void stressTest() {
        System.out.println("--- Concurrent ordering stress test ---");
        int threadCount = 20;
        List<Thread> threads = new ArrayList<>();
        String[] customers = { "C1", "C2" };       // C3 is mobile-unverified
        String[] restaurants = { "R1", "R2" };

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                String customerId = customers[rnd.nextInt(customers.length)];
                String restaurantId = restaurants[rnd.nextInt(restaurants.length)];
                // pick a known available item per restaurant
                String itemId = restaurantId.equals("R1") ? "M11" : "M21";
                try {
                    cts.addToCart(customerId, restaurantId, itemId, 1);
                    // top up wallet first if customer is low
                    Customer c = cs.get(customerId);
                    if (c.getWalletBalance() < 1000) c.credit(2000, "stress-test top-up");
                    Order o = os.placeOrder(customerId, PaymentMode.PREPAID, null,
                            "stress-" + id + "-" + System.nanoTime());
                    System.out.println("    [thread " + id + "] placed " + o.getId() + " for " + customerId);
                } catch (FoodOrderException ex) {
                    System.out.println("    [thread " + id + "] " + ex.getMessage());
                }
            }, "OrderWorker-" + i);
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        System.out.println("All " + threadCount + " threads finished. Total orders: " + os.getAllOrders().size());
    }

    // ====================== Input helpers ======================
    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("  Invalid number, try again."); }
        }
    }
}
