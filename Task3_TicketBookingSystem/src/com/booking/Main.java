package com.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.booking.exception.BookingException;
import com.booking.model.Booking;
import com.booking.model.Coupon;
import com.booking.model.Customer;
import com.booking.model.Movie;
import com.booking.model.Screen;
import com.booking.model.Seat;
import com.booking.model.Show;
import com.booking.model.Theater;
import com.booking.enums.SeatCategory;
import com.booking.enums.ShowStatus;
import com.booking.service.BookingService;
import com.booking.service.CouponService;
import com.booking.service.CustomerService;
import com.booking.service.MovieService;
import com.booking.service.NotificationService;
import com.booking.service.PaymentService;
import com.booking.service.PricingService;
import com.booking.service.ReviewService;
import com.booking.service.SeatLockService;
import com.booking.service.ShowService;
import com.booking.service.TheaterService;

/**
 * Ticket Booking System main entry.
 * PHASE 1 - bootstrap demo data.
 * PHASE 2 - automatic happy-path booking demo.
 * PHASE 3 - interactive menu.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final TheaterService theaterService = new TheaterService();
    private static final MovieService movieService = new MovieService();
    private static final ShowService showService = new ShowService(movieService);
    private static final SeatLockService seatLockService = new SeatLockService();
    private static final CustomerService customerService = new CustomerService();
    private static final PricingService pricingService = new PricingService();
    private static final CouponService couponService = new CouponService();
    private static final PaymentService paymentService = new PaymentService();
    private static final NotificationService notificationService = new NotificationService();
    private static final ReviewService reviewService = new ReviewService();
    private static final BookingService bookingService = new BookingService(
            theaterService, showService, seatLockService, customerService,
            pricingService, couponService, paymentService, notificationService);

    public static void main(String[] args) {
        printBanner();
        bootstrapDemoData();
        runHappyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int choice = readInt("Choose option: ");
            System.out.println();
            try {
                switch (choice) {
                    case 1: listTheaters();      break;
                    case 2: listMovies();        break;
                    case 3: listShows();         break;
                    case 4: showSeats();         break;
                    case 5: bookSeats();         break;
                    case 6: cancelBooking();     break;
                    case 7: listBookings();      break;
                    case 8: showReports();       break;
                    case 9: stressTest();        break;
                    case 10: submitReview();     break;
                    case 11: listCustomers();    break;
                    case 12: run = false;        break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (BookingException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED: " + ex.getMessage());
            }
            System.out.println();
        }
        seatLockService.shutdown();
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void bootstrapDemoData() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Demo data bootstrap");
        System.out.println("=========================================================");

        // theaters
        Theater t1 = theaterService.addTheater(new Theater("T1", "PVR Forum", "Bangalore"));
        Theater t2 = theaterService.addTheater(new Theater("T2", "INOX City Center", "Hyderabad"));

        // screens with seats (small for demo)
        Screen s1 = new Screen("S1", "T1", "Audi 1");
        Screen s2 = new Screen("S2", "T1", "Audi 2 IMAX");
        Screen s3 = new Screen("S3", "T2", "Screen A");

        theaterService.addScreen(s1);
        theaterService.addScreen(s2);
        theaterService.addScreen(s3);

        addSeats(s1, "A", 1, 5, SeatCategory.REGULAR);
        addSeats(s1, "B", 1, 5, SeatCategory.PREMIUM);
        addSeats(s2, "A", 1, 4, SeatCategory.RECLINER);
        addSeats(s2, "B", 1, 3, SeatCategory.VIP);
        addSeats(s3, "A", 1, 6, SeatCategory.REGULAR);

        // movies (and approve)
        Movie m1 = movieService.add(new Movie("M1", "RRR", 187, "Telugu", "Action", "U/A"));
        Movie m2 = movieService.add(new Movie("M2", "Inception", 148, "English", "Sci-Fi", "U/A"));
        Movie m3 = movieService.add(new Movie("M3", "PendingFlick", 120, "Hindi", "Drama", "U"));
        movieService.approve(m1.getId());
        movieService.approve(m2.getId());
        // m3 left PENDING_APPROVAL so we can show rejection of schedule

        // shows (today + a few days; future times)
        LocalDateTime now = LocalDateTime.now();
        showService.schedule(new Show("SH1", "M1", "S1",
                now.plusHours(2), now.plusHours(5), 200));
        showService.schedule(new Show("SH2", "M1", "S2",
                now.plusHours(3), now.plusHours(6), 250));
        showService.schedule(new Show("SH3", "M2", "S3",
                now.plusHours(4), now.plusHours(7), 220));

        // customers
        customerService.add(new Customer("C1", "Alice",   "alice@test.com",   "9000000001", true, true));
        customerService.add(new Customer("C2", "Bob",     "bob@test.com",     "9000000002", true, true));
        customerService.add(new Customer("C3", "Charlie", "charlie@test.com", "9000000003", false, true));   // email NOT verified

        // coupons
        couponService.add(new Coupon("FIRST20", 20, 300, LocalDate.now().plusDays(30)));
        couponService.add(new Coupon("FLAT50",  50, 500, LocalDate.now().plusDays(7)));
        couponService.add(new Coupon("EXPIRED", 25, 200, LocalDate.now().minusDays(1)));

        System.out.println("  2 theaters, 3 screens, 23 seats, 3 movies (2 approved), 3 shows, 3 customers, 3 coupons.");
        System.out.println();
    }

    private static void addSeats(Screen screen, String row, int from, int to, SeatCategory cat) {
        for (int i = from; i <= to; i++) {
            String seatNum = row + i;
            String id = screen.getId() + "-" + seatNum;
            theaterService.addSeat(new Seat(id, screen.getId(), seatNum, cat));
        }
    }

    private static void runHappyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path auto booking for Alice (C1)");
        System.out.println("=========================================================");
        try {
            List<String> seats = Arrays.asList("S1-A1", "S1-A2", "S1-B1");   // 2 regular + 1 premium
            Booking b = bookingService.book("C1", "SH1", seats, "FIRST20");
            System.out.println("Booking placed: " + b);
        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    // ====================== Menu ======================
    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#                JHires Ticket Booking System            #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. List theaters and screens");
        System.out.println(" 2. List movies");
        System.out.println(" 3. List shows");
        System.out.println(" 4. Show available seats for a show");
        System.out.println(" 5. Book seats (seat lock + payment)");
        System.out.println(" 6. Cancel booking");
        System.out.println(" 7. List all bookings");
        System.out.println(" 8. Show reports (revenue, taxes, refunds)");
        System.out.println(" 9. Stress test (concurrent booking same seats)");
        System.out.println("10. Submit movie review");
        System.out.println("11. List customers");
        System.out.println("12. Exit");
        System.out.println("==============================================");
    }

    private static void listTheaters() {
        for (Theater t : theaterService.getAllTheaters()) {
            System.out.println(t);
            for (Screen s : t.getScreens()) {
                System.out.println("    " + s);
            }
        }
    }

    private static void listMovies() {
        for (Movie m : movieService.getAll()) {
            System.out.println(m + "  avg-rating=" + reviewService.averageRating(m.getId()));
        }
    }

    private static void listShows() {
        for (Show s : showService.getAll()) {
            System.out.println(s);
        }
    }

    private static void showSeats() {
        String showId = readString("Show ID    : ");
        Show show = showService.get(showId);
        if (show == null) { System.out.println("Show not found."); return; }
        Screen screen = theaterService.getScreen(show.getScreenId());
        System.out.println("Available seats in show " + showId + " (basePrice Rs." + show.getBasePrice() + "):");
        for (Seat seat : screen.getSeats()) {
            boolean lockedByOther = seatLockService.isLockedByOther(showId, seat.getId(), "X");
            String state;
            if (show.getBookedSeats().containsKey(seat.getId())) state = "BOOKED";
            else if (lockedByOther) state = "LOCKED-OTHER";
            else if (!seat.isBookable()) state = seat.getStatus().toString();
            else state = "AVAILABLE";
            double price = pricingService.seatPrice(show, seat.getCategory());
            System.out.println("  " + seat + " | Rs." + price + " | " + state);
        }
    }

    private static void bookSeats() {
        String customerId = readString("Customer ID    : ");
        String showId = readString("Show ID        : ");
        String seatsCSV = readString("Seat IDs (CSV) : ");
        String coupon  = readString("Coupon (blank=none): ");
        List<String> seats = Arrays.asList(seatsCSV.split(","));
        seats.replaceAll(String::trim);
        Booking b = bookingService.book(customerId, showId, seats,
                coupon.isBlank() ? null : coupon);
        System.out.println("Booking placed: " + b);
    }

    private static void cancelBooking() {
        String bookingId = readString("Booking ID : ");
        Booking b = bookingService.cancel(bookingId);
        System.out.println("Booking cancelled: " + b);
    }

    private static void listBookings() {
        for (Booking b : bookingService.getAll()) {
            System.out.println("  " + b);
        }
    }

    private static void showReports() {
        System.out.println("--- Daily revenue ---");
        for (Map.Entry<String, Double> e : bookingService.getDailyRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Theater revenue ---");
        for (Map.Entry<String, Double> e : bookingService.getTheaterRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Movie revenue ---");
        for (Map.Entry<String, Double> e : bookingService.getMovieRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Tax collected ---");
        for (Map.Entry<String, Double> e : bookingService.getTaxCollected().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Refunds total ---");
        System.out.println("  Rs." + bookingService.getRefundsTotal());
    }

    private static void stressTest() {
        System.out.println("--- Concurrent booking same seats stress test ---");
        // 5 threads all try to book the SAME seat - only 1 should succeed.
        String showId = "SH2";
        Show show = showService.get(showId);
        if (show == null) { System.out.println("SH2 not found."); return; }
        Screen screen = theaterService.getScreen(show.getScreenId());
        Seat targetSeat = null;
        for (Seat seat : screen.getSeats()) {
            if (!show.getBookedSeats().containsKey(seat.getId())) {
                targetSeat = seat; break;
            }
        }
        if (targetSeat == null) { System.out.println("No available seat in SH2 to test."); return; }
        String seatId = targetSeat.getId();
        System.out.println("Target seat: " + seatId + " - 5 threads racing...");

        int N = 5;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            final int idx = i;
            String customerId = (i % 2 == 0) ? "C1" : "C2";
            Thread t = new Thread(() -> {
                try {
                    Booking b = bookingService.book(customerId, showId,
                            Arrays.asList(seatId), null);
                    System.out.println("    [thread " + idx + "] " + customerId + " WON: " + b.getId());
                } catch (BookingException ex) {
                    System.out.println("    [thread " + idx + "] " + customerId + " LOST: " + ex.getMessage());
                }
            }, "BookWorker-" + i);
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        System.out.println("Seat " + seatId + " final booked-by: " + show.getBookedSeats().get(seatId));
    }

    private static void submitReview() {
        String movieId = readString("Movie ID : ");
        String customerId = readString("Customer ID : ");
        int rating = readInt("Rating 1-5 : ");
        String comment = readString("Comment    : ");
        reviewService.addReview(movieId, customerId, rating, comment);
        System.out.println("OK. Review submitted.");
    }

    private static void listCustomers() {
        for (Customer c : customerService.getAll()) System.out.println(c);
    }

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
